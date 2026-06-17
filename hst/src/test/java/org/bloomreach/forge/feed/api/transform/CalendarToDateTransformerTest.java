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
package org.bloomreach.forge.feed.api.transform;

import java.util.Calendar;
import java.util.Date;

import org.bloomreach.forge.feed.api.annot.ContextTransformable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalendarToDateTransformerTest {

    @Test
    void convert_withValidCalendar_returnsEquivalentDate() {
        Calendar cal = Calendar.getInstance();
        Date result = CalendarToDateTransformer.convert(cal);
        assertNotNull(result);
        assertEquals(cal.getTime(), result);
    }

    @Test
    void convert_isAnnotatedWithContextTransformable() throws NoSuchMethodException {
        assertTrue(
            CalendarToDateTransformer.class
                .getMethod("convert", Calendar.class)
                .isAnnotationPresent(ContextTransformable.class)
        );
    }
}
