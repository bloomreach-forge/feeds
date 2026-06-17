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

import com.rometools.rome.feed.rss.Description;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringToDescriptionConverterTest {

    private final StringToDescriptionConverter converter = new StringToDescriptionConverter();

    @Test
    void convert_withValue_setsDescriptionValue() {
        Description result = converter.convert("My Description");
        assertNotNull(result);
        assertEquals("My Description", result.getValue());
    }

    @Test
    void convert_withNull_returnsDescriptionWithNullValue() {
        Description result = converter.convert(null);
        assertNotNull(result);
        assertNull(result.getValue());
    }

    @Test
    void convert_withHtmlContent_preservesHtmlValue() {
        String html = "<p>Content &amp; more</p>";
        Description result = converter.convert(html);
        assertEquals(html, result.getValue());
    }
}
