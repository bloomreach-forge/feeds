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
import java.util.Map;

/**
 * Unit test for AtomPropertyFilterModifier to verify filter configuration.
 * Tests that AtomPropertyFilterModifier properly supports property-based filtering for Atom feeds.
 * Note: These are configuration tests that don't require external dependencies.
 */
public class AtomPropertyFilterModifierTest {

    public void testAddFilterToAtomModifier() {
        // Given: Modifier with no initial filters
        AtomPropertyFilterModifier modifier = new AtomPropertyFilterModifier();
        modifier.clearFilters();

        // When: Adding a single filter
        modifier.addFilter("status", "published");

        // Then: Filter is added
        if (!"published".equals(modifier.getFilters().get("status"))) {
            throw new AssertionError("Filter should be added to Atom modifier");
        }
    }

    public void testSetFiltersOnAtomModifier() {
        // Given: Atom modifier with empty filters
        AtomPropertyFilterModifier modifier = new AtomPropertyFilterModifier();

        // When: Setting filters
        Map<String, String> filters = new HashMap<>();
        filters.put("location", "Rotterdam");
        modifier.setFilters(filters);

        // Then: Filters are set
        if (!"Rotterdam".equals(modifier.getFilters().get("location"))) {
            throw new AssertionError("Should set location filter on Atom modifier");
        }
    }

    public void testClearFiltersOnAtomModifier() {
        // Given: Atom modifier with filters configured
        AtomPropertyFilterModifier modifier = new AtomPropertyFilterModifier();
        Map<String, String> filters = new HashMap<>();
        filters.put("status", "published");
        filters.put("location", "Rotterdam");
        modifier.setFilters(filters);

        if (modifier.getFilters().size() != 2) {
            throw new AssertionError("Should have 2 filters initially");
        }

        // When: Clearing filters
        modifier.clearFilters();

        // Then: All filters are removed
        if (modifier.getFilters().size() != 0) {
            throw new AssertionError("Filters should be cleared on Atom modifier");
        }
    }

    public void testMultipleFiltersOnAtomModifier() {
        // Given: Atom modifier with multiple filters
        AtomPropertyFilterModifier modifier = new AtomPropertyFilterModifier();
        Map<String, String> filters = new HashMap<>();
        filters.put("status", "published");
        filters.put("location", "Rotterdam");
        modifier.setFilters(filters);

        // Then: Multiple filters are configured
        if (modifier.getFilters().size() != 2) {
            throw new AssertionError("Atom modifier should have 2 filters configured");
        }
        if (!modifier.getFilters().containsKey("status")) {
            throw new AssertionError("Should have status filter");
        }
        if (!modifier.getFilters().containsKey("location")) {
            throw new AssertionError("Should have location filter");
        }
    }

    public void testGetFiltersFromAtomModifier() {
        // Given: Atom modifier with filters
        AtomPropertyFilterModifier modifier = new AtomPropertyFilterModifier();
        Map<String, String> filters = new HashMap<>();
        filters.put("location", "Rotterdam");
        modifier.setFilters(filters);

        // When: Getting filters
        Map<String, String> retrievedFilters = modifier.getFilters();

        // Then: Filters are retrieved correctly
        if (!"Rotterdam".equals(retrievedFilters.get("location"))) {
            throw new AssertionError("Should retrieve location filter from Atom modifier");
        }
    }

    public void testAtomAndRSSModifiersHaveSameInterface() {
        // Given: Both RSS and Atom modifiers
        PropertyFilterModifier rssModifier = new PropertyFilterModifier();
        AtomPropertyFilterModifier atomModifier = new AtomPropertyFilterModifier();

        // When: Setting same filter on both
        Map<String, String> filters = new HashMap<>();
        filters.put("location", "Rome");
        rssModifier.setFilters(new HashMap<>(filters));
        atomModifier.setFilters(new HashMap<>(filters));

        // Then: Both have the same filter configuration
        if (!rssModifier.getFilters().get("location").equals(atomModifier.getFilters().get("location"))) {
            throw new AssertionError("Both modifiers should have same filter value");
        }
    }
}
