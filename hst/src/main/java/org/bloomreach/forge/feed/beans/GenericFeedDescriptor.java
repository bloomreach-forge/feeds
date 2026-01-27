/*
 * Copyright 2013-2020 Hippo B.V. (http://www.onehippo.com)
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
package org.bloomreach.forge.feed.beans;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.bloomreach.forge.feed.api.FeedDescriptor;
import org.bloomreach.forge.feed.api.FeedType;
import org.bloomreach.forge.feed.api.annot.SyndicationElement;
import org.bloomreach.forge.feed.api.transform.CalendarToDateConverter;
import org.bloomreach.forge.feed.api.transform.DocumentLinkResolver;
import org.bloomreach.forge.feed.api.transform.gen.AuthorListToSyndPersonListConverter;
import org.bloomreach.forge.feed.api.transform.gen.HippoGalleryImageSetToSyndImageTransformer;
import org.bloomreach.forge.feed.api.transform.gen.ListToSyndCategoryListConverter;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

@Node(jcrType = "feed:genericdescriptor")
public class GenericFeedDescriptor extends HippoDocument implements FeedDescriptor<SyndFeed, SyndEntry> {

    private static final Logger log = LoggerFactory.getLogger(GenericFeedDescriptor.class);

    public String getType() {
        return getSingleProperty("feed:type");
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "title")
    public String getTitle() {
        return getSingleProperty("feed:title");
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "publishedDate", converter = CalendarToDateConverter.class)
    public Calendar getPublicationDate() {
        return getSingleProperty("hippostdpubwf:publicationDate");
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "authors", converter = AuthorListToSyndPersonListConverter.class)
    public List<String> getAuthor() {
        String[] authors = getMultipleProperty("feed:author");
        if (authors != null) {
            return Arrays.asList(authors);
        } else {
            return Collections.emptyList();
        }
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "description")
    public String getDescription() {
        return getSingleProperty("feed:description");
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "language")
    public String getLanguage() {
        return getSingleProperty("feed:language");
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "contributor", converter = AuthorListToSyndPersonListConverter.class)
    public List<String> getContributors() {
        String[] contributors = getMultipleProperty("feed:contributor");
        if (contributors != null) {
            return Arrays.asList(contributors);
        } else {
            return Collections.emptyList();
        }
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "copyright")
    public String getCopyright() {
        return getSingleProperty("feed:copyright");
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "image", transformer = HippoGalleryImageSetToSyndImageTransformer.class)
    public HippoGalleryImageSet getImage() {
        return getLinkedBean("feed:image", HippoGalleryImageSet.class);
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "categories", converter = ListToSyndCategoryListConverter.class)
    public List<String> getCategories() {
        String[] categories = getMultipleProperty("feed:categories");
        if (categories != null) {
            return Arrays.asList(categories);
        } else {
            return Collections.emptyList();
        }
    }

    @SyndicationElement(type = FeedType.GENERIC, name = "link", transformer = DocumentLinkResolver.class)
    public HippoDocument getLink() {
        return this;
    }

    @Override
    public SyndFeed createSyndication() {
        final SyndFeedImpl syndFeed = new SyndFeedImpl();
        syndFeed.setFeedType(getType());
        return syndFeed;
    }

    @Override
    public SyndEntry createEntry() {
        return new SyndEntryImpl();
    }

    @Override
    public void set(final SyndFeed syndication, final List<SyndEntry> entries) {
        syndication.setEntries(entries);
    }

    @Override
    public String process(final SyndFeed syndication) {
        String feed = null;
        try (final Writer writer = new StringWriter()) {
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(syndication, writer);
            feed = writer.toString();
        } catch (FeedException | IOException e) {
            log.error("Error processing SyndFeed", e);
        }
        return feed;
    }

    @Override
    public FeedType type() {
        return FeedType.GENERIC;
    }

    /**
     * descriptor
     */

    public String getScope() {
        return getSingleProperty("feed:scope");
    }

    public String getDocumentType() {
        return getSingleProperty("feed:documentType");
    }

    public String getExclude() {
        return getSingleProperty("feed:excludeItems");
    }

    public Long getItemCount() {
        return getSingleProperty("feed:itemCount");
    }

    public String getSortByField() {
        return getSingleProperty("feed:sortByField");
    }

    public String getFilterByProperty() {
        return getSingleProperty("feed:filterByProperty");
    }

    public String getFilterByValue() {
        return getSingleProperty("feed:filterByValue");
    }

}
