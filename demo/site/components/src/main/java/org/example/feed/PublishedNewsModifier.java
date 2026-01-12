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
 * <p>
 * Example concrete implementation of property filtering.
 * This modifier filters news feed items to only include published documents
 * by checking the 'status' property.
 * </p>
 *
 * <p>
 * This serves as a reference implementation showing how to extend PropertyFilterModifier
 * to create domain-specific filters.
 * </p>
 *
 * <h2>Spring Configuration Example:</h2>
 *
 * <pre>
 * &lt;bean id="publishedNewsModifier" class="org.example.feed.PublishedNewsModifier" scope="singleton"/&gt;
 *
 * &lt;bean id="jaxrsRSSResource" class="org.bloomreach.forge.feed.resource.RssSyndicationResource"&gt;
 *   &lt;property name="modifier" ref="publishedNewsModifier"/&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * @see PropertyFilterModifier
 */
public class PublishedNewsModifier extends PropertyFilterModifier {

    /**
     * Initialize the filter to only show published news items.
     * The 'status' property is a common pattern in Bloomreach content models.
     */
    public PublishedNewsModifier() {
        super();
        // Only include documents with status = 'published'
        addFilter("status", "published");
    }

    /**
     * Alternative constructor if you want to use a different status value.
     *
     * @param statusValue the status value to filter by (e.g., "published", "live", "active")
     */
    public PublishedNewsModifier(String statusValue) {
        super();
        addFilter("status", statusValue);
    }
}
