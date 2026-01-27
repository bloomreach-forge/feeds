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

package org.bloomreach.forge.feed.api.modifier;

import java.util.List;
import java.util.Set;

import org.bloomreach.forge.feed.beans.Atom10FeedDescriptor;
import org.hippoecm.hst.core.request.HstRequestContext;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;

/**
 * Atom 1.0 implementation of property-based feed filtering.
 * Filters Atom feed entries based on a single property-value pair configured on the feed descriptor.
 */
public class Atom10PropertyFilteringModifier extends PropertyFilteringModifier<Feed, Entry, Atom10FeedDescriptor> {

    @Override
    protected void removeFilteredEntries(Feed feed, Set<?> entriesToRemove) {
        if (feed == null || feed.getEntries() == null) {
            return;
        }

        List<Entry> entries = feed.getEntries();
        for (Object entry : entriesToRemove) {
            if (entry instanceof Entry) {
                entries.remove(entry);
            }
        }
    }
}
