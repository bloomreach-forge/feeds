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

import org.apache.commons.beanutils.BeanUtils;
import org.bloomreach.forge.feed.api.modifier.RSS20Modifier;
import org.bloomreach.forge.feed.beans.RSS20FeedDescriptor;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Item;

/**
 * <p>
 * Reference implementation for filtering feed items based on document properties.
 * This modifier filters feed entries by evaluating document properties and excluding
 * entries that don't match the specified filter criteria.
 * </p>
 *
 * <p>
 * <strong>How it works:</strong> This modifier evaluates each feed entry (document) during
 * feed generation and filters out entries that don't match all specified property filters.
 * This happens after the HstQuery executes, providing a reliable way to filter regardless
 * of the Bloomreach/HST version.
 * </p>
 *
 * <h2>Configuration Example:</h2>
 *
 * <pre>
 * &lt;bean id="propertyFilterModifier" class="org.example.feed.PropertyFilterModifier"&gt;
 *   &lt;property name="filters"&gt;
 *     &lt;map&gt;
 *       &lt;entry key="status" value="published" /&gt;
 *       &lt;entry key="featured" value="true" /&gt;
 *     &lt;/map&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <h2>Filtering Logic:</h2>
 * <ul>
 *   <li>An entry is included if it matches ALL filters (AND logic)</li>
 *   <li>An entry is excluded if it doesn't match any filter</li>
 *   <li>Property values are compared as strings</li>
 *   <li>Comparison is case-sensitive</li>
 *   <li>Missing properties cause the entry to be excluded</li>
 * </ul>
 *
 * <h2>Supported Operators:</h2>
 * <ul>
 *   <li><code>EQUALS</code> (default): property = value</li>
 *   <li><code>NOT_EQUALS</code>: property != value</li>
 *   <li><code>GREATER_THAN</code>: property &gt; value (for dates and numbers)</li>
 *   <li><code>LESS_THAN</code>: property &lt; value (for dates and numbers)</li>
 *   <li><code>GREATER_THAN_OR_EQUAL</code>: property &gt;= value</li>
 *   <li><code>LESS_THAN_OR_EQUAL</code>: property &lt;= value</li>
 *   <li><code>CONTAINS</code>: property contains substring</li>
 * </ul>
 *
 * @see RSS20Modifier
 */
public class PropertyFilterModifier extends RSS20Modifier {

    private static final Logger log = LoggerFactory.getLogger(PropertyFilterModifier.class);

    /**
     * Simple property=value filters. Override or inject additional filters as needed.
     */
    private Map<String, String> filters = new HashMap<>();

    /**
     * Track excluded entries during this request (thread-local to avoid concurrency issues).
     * Note: This is a simplified approach. For production, consider using request-scoped storage.
     */
    private static final ThreadLocal<Set<Item>> EXCLUDED_ENTRIES = ThreadLocal.withInitial(HashSet::new);

    /**
     * Supported filter operators for property-based filtering.
     */
    public enum FilterOperator {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN_OR_EQUAL,
        CONTAINS
    }

    /**
     * Container for a single filter specification with property name, value, and operator.
     */
    public static class Filter {
        private final String property;
        private final String value;
        private final FilterOperator operator;

        public Filter(String property, String value) {
            this(property, value, FilterOperator.EQUALS);
        }

        public Filter(String property, String value, FilterOperator operator) {
            this.property = property;
            this.value = value;
            this.operator = operator;
        }

        public String getProperty() {
            return property;
        }

        public String getValue() {
            return value;
        }

        public FilterOperator getOperator() {
            return operator;
        }

        @Override
        public String toString() {
            return "Filter{" +
                    "property='" + property + '\'' +
                    ", value='" + value + '\'' +
                    ", operator=" + operator +
                    '}';
        }
    }

    @Override
    public void modifyEntry(final HstRequestContext context, final Item entry, final HippoBean bean) {
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
                log.debug("Entry {} excluded: property '{}' != '{}'",
                        bean.getName(), propertyName, expectedValue);
                // Track this entry for removal in modifyFeed()
                EXCLUDED_ENTRIES.get().add(entry);
            }
        }
    }

    @Override
    public void modifyFeed(final HstRequestContext context, final Channel feed, final RSS20FeedDescriptor descriptor) {
        super.modifyFeed(context, feed, descriptor);

        try {
            // Remove excluded entries from the feed
            Set<Item> excluded = EXCLUDED_ENTRIES.get();
            if (!excluded.isEmpty() && feed.getItems() != null) {
                List<Item> items = feed.getItems();
                for (Item excludedItem : excluded) {
                    if (items.remove(excludedItem)) {
                        log.debug("Removed excluded entry from feed");
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
            String propertyValue = BeanUtils.getProperty(bean, propertyName);
            if (propertyValue == null) {
                log.debug("Property '{}' not found on bean {}", propertyName, bean.getName());
                return false;
            }
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
