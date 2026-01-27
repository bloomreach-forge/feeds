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

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Item;

/**
 * Unit test for PropertyFilterModifier to verify filter configuration.
 * Note: These are basic configuration tests that don't require external dependencies.
 */
public class PropertyFilterModifierTest {

    public void testAddFilterMethod() {
        // Given: Modifier with no initial filters
        PropertyFilterModifier modifier = new PropertyFilterModifier();
        modifier.clearFilters();

        // When: Adding a single filter
        modifier.addFilter("status", "published");

        // Then: Filter is added
        if (!"published".equals(modifier.getFilters().get("status"))) {
            throw new AssertionError("Filter should be added");
        }
    }

    public void testSetFilters() {
        // Given: Modifier with empty filters
        PropertyFilterModifier modifier = new PropertyFilterModifier();

        // When: Setting filters
        Map<String, String> filters = new HashMap<>();
        filters.put("status", "published");
        filters.put("location", "Rome");
        modifier.setFilters(filters);

        // Then: Filters are set
        if (modifier.getFilters().size() != 2) {
            throw new AssertionError("Should have 2 filters configured");
        }
    }

    public void testClearFilters() {
        // Given: Modifier with filters configured
        PropertyFilterModifier modifier = new PropertyFilterModifier();
        Map<String, String> filters = new HashMap<>();
        filters.put("status", "published");
        filters.put("featured", "true");
        modifier.setFilters(filters);

        if (modifier.getFilters().size() != 2) {
            throw new AssertionError("Should have 2 filters initially");
        }

        // When: Clearing filters
        modifier.clearFilters();

        // Then: All filters are removed
        if (modifier.getFilters().size() != 0) {
            throw new AssertionError("Filters should be cleared");
        }
    }

    public void testGetFilters() {
        // Given: Modifier with multiple filters
        PropertyFilterModifier modifier = new PropertyFilterModifier();
        Map<String, String> filters = new HashMap<>();
        filters.put("location", "Rome");
        modifier.setFilters(filters);

        // When: Getting filters
        Map<String, String> retrievedFilters = modifier.getFilters();

        // Then: Filters are retrieved correctly
        if (!"Rome".equals(retrievedFilters.get("location"))) {
            throw new AssertionError("Should retrieve location filter");
        }
    }

    public void testMultipleFilters() {
        // Given: Modifier with multiple filters
        PropertyFilterModifier modifier = new PropertyFilterModifier();
        Map<String, String> filters = new HashMap<>();
        filters.put("status", "published");
        filters.put("location", "Rome");
        modifier.setFilters(filters);

        // Then: Multiple filters are configured
        if (modifier.getFilters().size() != 2) {
            throw new AssertionError("Should have 2 filters configured");
        }
    }
}
