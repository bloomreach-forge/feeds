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
package org.bloomreach.forge.feed.api.transform.atom;

import com.rometools.rome.feed.atom.Content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringToContentConverterTest {

    private final StringToContentConverter converter = new StringToContentConverter();

    @Test
    void convert_withValue_setsContentValue() {
        Content result = converter.convert("hello");
        assertNotNull(result);
        assertEquals("hello", result.getValue());
    }

    @Test
    void convert_withNull_returnsContentWithNullValue() {
        Content result = converter.convert(null);
        assertNotNull(result);
        assertNull(result.getValue());
    }

    @Test
    void convert_withEmptyString_setsEmptyValue() {
        Content result = converter.convert("");
        assertNotNull(result);
        assertEquals("", result.getValue());
    }
}
