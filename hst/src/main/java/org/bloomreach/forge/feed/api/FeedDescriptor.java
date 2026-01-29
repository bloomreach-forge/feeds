/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
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

package org.bloomreach.forge.feed.api;

import java.util.List;

/**
 * @version "$Id$"
 */
public interface FeedDescriptor<T, E> {

    /**
     * Create top level element
     * @return
     */
    public T createSyndication();

    /**
     * Create entry element to include in top level element
     * @return
     */
    public E createEntry();

    /**
     * Add entry list to the syndication
     * @param syndication
     * @param entries
     */
    public void set(T syndication, List<E> entries);

    /**
     * Process syndication to XML format
     * @param syndication
     * @return
     */
    public String process(T syndication);

    /***
     * FeedType
     * @return
     */
    public FeedType type();

    /**
     * Right side elements
     */

    public String getScope();

    public String getDocumentType();

    public Long getItemCount();

    public String getSortByField();

    public String getExclude();

    /**
     * Property-based filtering configuration
     */

    public String getFilterByProperty();

    public String getFilterByValue();

}
