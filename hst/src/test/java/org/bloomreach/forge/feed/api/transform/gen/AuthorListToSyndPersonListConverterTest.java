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
package org.bloomreach.forge.feed.api.transform.gen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rometools.rome.feed.synd.SyndPerson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorListToSyndPersonListConverterTest {

    private final AuthorListToSyndPersonListConverter converter = new AuthorListToSyndPersonListConverter();

    @Test
    void convert_withAuthors_returnsMappedPersonList() {
        List<SyndPerson> result = converter.convert(Arrays.asList("Dave", "Eve"));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Dave", result.get(0).getName());
        assertEquals("Eve", result.get(1).getName());
    }

    @Test
    void convert_withNull_returnsEmptyList() {
        List<SyndPerson> result = converter.convert(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void convert_withEmptyList_returnsEmptyList() {
        List<SyndPerson> result = converter.convert(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void convert_withSingleAuthor_returnsOnePersonWithName() {
        List<SyndPerson> result = converter.convert(Collections.singletonList("Frank"));
        assertEquals(1, result.size());
        assertEquals("Frank", result.get(0).getName());
    }
}
