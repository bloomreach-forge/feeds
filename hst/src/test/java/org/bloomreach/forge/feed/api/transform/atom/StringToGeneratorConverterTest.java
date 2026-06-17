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

import com.rometools.rome.feed.atom.Generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringToGeneratorConverterTest {

    private final StringToGeneratorConverter converter = new StringToGeneratorConverter();

    @Test
    void convert_withValue_setsGeneratorValue() {
        Generator result = converter.convert("MyGenerator/1.0");
        assertNotNull(result);
        assertEquals("MyGenerator/1.0", result.getValue());
    }

    @Test
    void convert_withNull_returnsGeneratorWithNullValue() {
        Generator result = converter.convert(null);
        assertNotNull(result);
        assertNull(result.getValue());
    }
}
