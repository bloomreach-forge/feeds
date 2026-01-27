/*
 * Copyright 2024 Bloomreach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example.feed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bloomreach.forge.feed.api.modifier.Atom10Modifier;
import org.bloomreach.forge.feed.beans.Atom10FeedDescriptor;
import org.example.beans.NewsDocument;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;

/**
 * <p>
 * Atom-compatible version of PropertyFilterModifier for filtering Atom feed entries
 * based on document properties. This mirrors the functionality of PropertyFilterModifier
 * but works with Atom feed types.
 * </p>
 *
 * <p>
 * <strong>How it works:</strong> This modifier evaluates each Atom feed entry (document) during
 * feed generation and filters out entries that don't match all specified property filters.
 * This happens after the HstQuery executes, providing a reliable way to filter regardless
 * of the Bloomreach/HST version.
 * </p>
 *
 * <h2>Configuration Example:</h2>
 *
 * <pre>
 * &lt;bean id="atomPropertyFilterModifier" class="org.example.feed.AtomPropertyFilterModifier"&gt;
 *   &lt;property name="filters"&gt;
 *     &lt;map&gt;
 *       &lt;entry key="location" value="Rotterdam" /&gt;
 *     &lt;/map&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * @see PropertyFilterModifier
 */
public class AtomPropertyFilterModifier extends Atom10Modifier {

    private static final Logger log = LoggerFactory.getLogger(AtomPropertyFilterModifier.class);

    /**
     * Simple property=value filters.
     */
    private Map<String, String> filters = new HashMap<>();

    /**
     * Track excluded entries during this request.
     */
    private static final ThreadLocal<Set<Entry>> EXCLUDED_ENTRIES = ThreadLocal.withInitial(HashSet::new);

    /**
     * Custom namespace for enriched feed content.
     */
    private static final Namespace FEEDSDEMO_NS = Namespace.getNamespace("feedsdemo", "http://feedsdemo.example.org/");

    @Override
    public void modifyEntry(final HstRequestContext context, final Entry entry, final HippoBean bean) {
        super.modifyEntry(context, entry, bean);

        if (filters == null || filters.isEmpty()) {
            // No filters, allow all entries
            return;
        }

        // Evaluate each filter against the bean
        for (Map.Entry<String, String> filterEntry : filters.entrySet()) {
            String propertyName = filterEntry.getKey();
            String expectedValue = filterEntry.getValue();

            if (!matchesFilter(bean, propertyName, expectedValue)) {
                // Entry doesn't match this filter, mark it for exclusion
                log.debug("Atom entry {} excluded: property '{}' != '{}'",
                        bean.getName(), propertyName, expectedValue);
                // Track this entry for removal in modifyFeed()
                EXCLUDED_ENTRIES.get().add(entry);
            }
        }

        // Add custom namespace fields to enrich the Atom entry
        if (bean instanceof NewsDocument) {
            addCustomNamespaceFields(context, entry, (NewsDocument) bean);
        }
    }

    /**
     * Add custom namespace-qualified fields to the Atom entry.
     * This extends standard Atom with document-specific metadata using a custom namespace.
     *
     * This approach allows Atom feeds to carry custom fields while remaining valid Atom documents,
     * as extension elements are explicitly allowed by the Atom specification (RFC 4287).
     */
    private void addCustomNamespaceFields(final HstRequestContext context, final Entry entry,
                                         final NewsDocument doc) {
        try {
            List<Element> foreignMarkup = entry.getForeignMarkup();

            // Add introduction/description field
            String introduction = doc.getIntroduction();
            if (introduction != null && !introduction.isEmpty()) {
                Element introElement = new Element("introduction", FEEDSDEMO_NS);
                introElement.setText(introduction);
                foreignMarkup.add(introElement);
                log.debug("Added introduction field to Atom entry: {}", introduction);
            }

            // Add location field
            String location = doc.getLocation();
            if (location != null && !location.isEmpty()) {
                Element locationElement = new Element("location", FEEDSDEMO_NS);
                locationElement.setText(location);
                foreignMarkup.add(locationElement);
                log.debug("Added location field to Atom entry: {}", location);
            }

            // Add author field
            String author = doc.getAuthor();
            if (author != null && !author.isEmpty()) {
                Element authorElement = new Element("author", FEEDSDEMO_NS);
                authorElement.setText(author);
                foreignMarkup.add(authorElement);
                log.debug("Added author field to Atom entry: {}", author);
            }

            // Add source field
            String source = doc.getSource();
            if (source != null && !source.isEmpty()) {
                Element sourceElement = new Element("source", FEEDSDEMO_NS);
                sourceElement.setText(source);
                foreignMarkup.add(sourceElement);
                log.debug("Added source field to Atom entry: {}", source);
            }
        } catch (Exception e) {
            log.debug("Error adding custom namespace fields to Atom entry: {}", e.getMessage());
        }
    }

    @Override
    public void modifyFeed(final HstRequestContext context, final Feed feed, final Atom10FeedDescriptor descriptor) {
        super.modifyFeed(context, feed, descriptor);

        try {
            // Remove excluded entries from the feed
            Set<Entry> excluded = EXCLUDED_ENTRIES.get();
            if (!excluded.isEmpty() && feed.getEntries() != null) {
                List<Entry> entries = feed.getEntries();
                for (Entry excludedEntry : excluded) {
                    if (entries.remove(excludedEntry)) {
                        log.debug("Removed excluded entry from Atom feed");
                    }
                }
            }
        } finally {
            // Clean up thread-local after use
            EXCLUDED_ENTRIES.remove();
        }
    }

    /**
     * Check if a bean matches a property filter.
     *
     * @param bean the HippoBean to evaluate
     * @param propertyName the property name to check
     * @param expectedValue the expected property value
     * @return true if the property matches the expected value, false otherwise
     */
    protected boolean matchesFilter(HippoBean bean, String propertyName, String expectedValue) {
        try {
            // Construct the getter method name from property name: "location" -> "getLocation"
            String methodName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            Object methodResult = bean.getClass().getMethod(methodName).invoke(bean);

            String propertyValue = methodResult != null ? methodResult.toString() : null;
            if (propertyValue == null) {
                log.debug("Property '{}' not found on bean {}", propertyName, bean.getName());
                return false;
            }
            log.debug("Bean {}: property '{}' = '{}' (expected '{}')",
                    bean.getName(), propertyName, propertyValue, expectedValue);
            return propertyValue.equals(expectedValue);
        } catch (Exception e) {
            log.debug("Error evaluating property '{}' on bean {}: {}",
                    propertyName, bean.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Set simple property=value filters.
     *
     * @param filters map of property names to property values
     */
    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    /**
     * Get the current filters.
     *
     * @return map of property names to property values
     */
    public Map<String, String> getFilters() {
        return filters;
    }

    /**
     * Add a single filter.
     *
     * @param propertyName the property name to filter on
     * @param propertyValue the property value to match
     */
    public void addFilter(String propertyName, String propertyValue) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        filters.put(propertyName, propertyValue);
    }

    /**
     * Clear all filters.
     */
    public void clearFilters() {
        if (filters != null) {
            filters.clear();
        }
    }
}
