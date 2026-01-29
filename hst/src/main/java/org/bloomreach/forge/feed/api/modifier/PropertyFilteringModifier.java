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

package org.bloomreach.forge.feed.api.modifier;

import java.util.HashSet;
import java.util.Set;

import org.bloomreach.forge.feed.api.FeedDescriptor;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Base class for property-based feed filtering. Provides core filtering logic that can be
 * extended for different feed types (RSS, Atom, Generic).
 * </p>
 *
 * <p>
 * <strong>How it works:</strong> This modifier evaluates each feed entry (document) during
 * feed generation against a single property-value filter configured on the feed descriptor.
 * Entries that don't match are excluded from the final feed.
 * </p>
 *
 * <p>
 * <strong>Configuration:</strong> Users configure two fields on the feed descriptor:
 * <ul>
 *   <li><code>filterByProperty</code> - The bean property name to evaluate (e.g., "status")</li>
 *   <li><code>filterByValue</code> - The expected value (e.g., "published")</li>
 * </ul>
 * If either field is null/empty, filtering is skipped.
 * </p>
 *
 * @param <K> The feed type (Channel, Feed, SyndFeed)
 * @param <V> The entry type (Item, Entry, SyndEntry)
 * @param <E> The descriptor type (RSS20FeedDescriptor, Atom10FeedDescriptor, etc.)
 */
public abstract class PropertyFilteringModifier<K, V, E extends FeedDescriptor>
        implements Modifier<K, V, E> {

    private static final Logger log = LoggerFactory.getLogger(PropertyFilteringModifier.class);

    /**
     * Thread-local storage for the descriptor.
     * Stored in modifyHstQuery and used in modifyEntry/modifyFeed.
     */
    private static final ThreadLocal<FeedDescriptor> CURRENT_DESCRIPTOR = ThreadLocal.withInitial(() -> null);

    /**
     * Thread-local storage for entries to be excluded during this request.
     * Avoids concurrency issues by scoping per-thread.
     */
    private static final ThreadLocal<Set<?>> FILTERED_ENTRIES = ThreadLocal.withInitial(HashSet::new);

    @Override
    public void modifyHstQuery(final HstRequestContext context, final HstQuery query, final E descriptor) {
        // Store descriptor in thread-local for use in modifyEntry and modifyFeed
        CURRENT_DESCRIPTOR.set(descriptor);
    }

    @Override
    public void modifyEntry(final HstRequestContext context, final V entry, final HippoBean bean) {
        FeedDescriptor descriptor = CURRENT_DESCRIPTOR.get();
        if (descriptor == null) {
            return;
        }

        String filterProperty = descriptor.getFilterByProperty();
        String filterValue = descriptor.getFilterByValue();

        // Skip filtering if not configured
        if (filterProperty == null || filterValue == null ||
            filterProperty.trim().isEmpty() || filterValue.trim().isEmpty()) {
            return;
        }

        // Check if bean matches the filter
        if (!matchesFilter(bean, filterProperty, filterValue)) {
            log.debug("Entry {} excluded: property '{}' != '{}'",
                    bean.getName(), filterProperty, filterValue);
            getFilteredEntries().add(entry);
        }
    }

    @Override
    public void modifyFeed(final HstRequestContext context, final K feed, final E descriptor) {
        try {
            Set<?> excludedEntries = getFilteredEntries();
            if (!excludedEntries.isEmpty()) {
                removeFilteredEntries(feed, excludedEntries);
                log.debug("Removed {} excluded entries from feed", excludedEntries.size());
            }
        } finally {
            // Always clean up thread-locals to avoid memory leaks
            FILTERED_ENTRIES.remove();
            CURRENT_DESCRIPTOR.remove();
        }
    }

    /**
     * Check if a bean property matches the filter value.
     * Supports case-insensitive matching and wildcard patterns (* for zero or more characters).
     *
     * @param bean the HippoBean to evaluate
     * @param property the property name (getter method name without "get" prefix)
     * @param filterPattern the expected property value or wildcard pattern (e.g., "pub*", "*draft*", "PUBLISHED")
     * @return true if the property matches the filter pattern
     */
    protected boolean matchesFilter(HippoBean bean, String property, String filterPattern) {
        try {
            // Build getter method name: "status" -> "getStatus"
            String methodName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
            Object methodResult = bean.getClass().getMethod(methodName).invoke(bean);

            String propertyValue = methodResult != null ? methodResult.toString() : null;
            if (propertyValue == null) {
                log.debug("Property '{}' not found on bean {}", property, bean.getName());
                return false;
            }

            boolean matches = matchesPattern(propertyValue, filterPattern);
            log.debug("Bean {}: property '{}' = '{}' (pattern '{}', matches={})",
                    bean.getName(), property, propertyValue, filterPattern, matches);
            return matches;
        } catch (Exception e) {
            log.debug("Error evaluating property '{}' on bean {}: {}",
                    property, bean.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Check if a string value matches a filter pattern.
     * Supports:
     * - Exact match (case-insensitive): "published" matches "PUBLISHED"
     * - Wildcard patterns: "pub*" matches "published", "publishing", etc.
     * - Start pattern: "pub*" matches strings starting with "pub"
     * - End pattern: "*lished" matches strings ending with "lished"
     * - Middle pattern: "*ublish*" matches strings containing "ublish"
     *
     * @param value the string value to test
     * @param pattern the filter pattern (may contain * wildcards)
     * @return true if value matches the pattern
     */
    private boolean matchesPattern(String value, String pattern) {
        if (value == null || pattern == null) {
            return false;
        }

        // Perform case-insensitive comparison
        String lowerValue = value.toLowerCase();
        String lowerPattern = pattern.toLowerCase();

        // If no wildcards, do exact case-insensitive match
        if (!lowerPattern.contains("*")) {
            return lowerValue.equals(lowerPattern);
        }

        // Convert wildcard pattern to regex
        // Escape regex special characters except *
        String regex = lowerPattern
                .replaceAll("([.+?^${}()|\\[\\]\\\\])", "\\\\$1")  // Escape regex special chars
                .replaceAll("\\*", ".*");                             // Convert * to .*

        // Match entire string
        return lowerValue.matches(regex);
    }

    /**
     * Remove excluded entries from the feed.
     * Subclasses must implement this to handle feed-type-specific removal.
     *
     * @param feed the feed object
     * @param entriesToRemove the set of entries to remove
     */
    protected abstract void removeFilteredEntries(K feed, Set<?> entriesToRemove);

    /**
     * Get thread-local set of filtered entries.
     *
     * @return the set of entries to be removed
     */
    @SuppressWarnings("unchecked")
    private Set<V> getFilteredEntries() {
        return (Set<V>) FILTERED_ENTRIES.get();
    }
}
