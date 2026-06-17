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
package org.bloomreach.forge.feed.api.transform.rss;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rometools.rome.feed.rss.Category;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListToRssCategoryListConverterTest {

    private final ListToRssCategoryListConverter converter = new ListToRssCategoryListConverter();

    @Test
    void convert_withCategories_returnsMappedList() {
        List<Category> result = converter.convert(Arrays.asList("sports", "tech"));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("sports", result.get(0).getValue());
        assertEquals("tech", result.get(1).getValue());
    }

    @Test
    void convert_withNull_returnsEmptyList() {
        List<Category> result = converter.convert(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void convert_withEmptyList_returnsEmptyList() {
        List<Category> result = converter.convert(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
