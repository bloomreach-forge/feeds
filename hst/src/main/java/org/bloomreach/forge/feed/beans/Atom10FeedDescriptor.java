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
import org.bloomreach.forge.feed.api.transform.PathLinkResolver;
import org.bloomreach.forge.feed.api.transform.atom.AuthorListToPersonListConverter;
import org.bloomreach.forge.feed.api.transform.atom.DocumentAtomLinkResolver;
import org.bloomreach.forge.feed.api.transform.atom.ListToAtomCategoryListConverter;
import org.bloomreach.forge.feed.api.transform.atom.StringToContentConverter;
import org.bloomreach.forge.feed.api.transform.atom.StringToGeneratorConverter;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;

@Node(jcrType = "feed:atom10descriptor")
public class Atom10FeedDescriptor extends HippoDocument implements FeedDescriptor<Feed, Entry> {

    private static final Logger log = LoggerFactory.getLogger(Atom10FeedDescriptor.class);

    /**
     * required /recommended
     */

    @SyndicationElement(type = FeedType.ATOM, name = "title")
    public String getTitle() {
        return getSingleProperty("feed:title");
    }

    @SyndicationElement(type = FeedType.ATOM, name = "authors", converter = AuthorListToPersonListConverter.class)
    public List<String> getAuthor() {
        String[] authors = getMultipleProperty("feed:author");
        if (authors != null) {
            return Arrays.asList(authors);
        } else {
            return Collections.emptyList();
        }
    }

    @SyndicationElement(type = FeedType.ATOM, name = "subtitle", converter = StringToContentConverter.class)
    public String getSubtitle() {
        return getSingleProperty("feed:subtitle");
    }

    @SyndicationElement(type = FeedType.ATOM, name = "updated", converter = CalendarToDateConverter.class)
    public Calendar getPublicationDate() {
        return getSingleProperty("hippostdpubwf:publicationDate");
    }

    @SyndicationElement(type = FeedType.ATOM, name = "id", transformer = PathLinkResolver.class)
    public String getId() {
        return "/";
    }

    @SyndicationElement(type = FeedType.ATOM, name = "alternateLinks", transformer = DocumentAtomLinkResolver.class)
    public HippoDocument getLink() {
        return this;
    }

    /**
     * other
     */

    @SyndicationElement(type = FeedType.ATOM, name = "categories", converter = ListToAtomCategoryListConverter.class)
    public List<String> getCategory() {
        String[] categories = getMultipleProperty("feed:category");
        if (categories != null) {
            return Arrays.asList(categories);
        } else {
            return Collections.emptyList();
        }
    }

    @SyndicationElement(type = FeedType.ATOM, name = "contributors", converter = AuthorListToPersonListConverter.class)
    public List<String> getContributor() {
        String[] contributors = getMultipleProperty("feed:contributor");
        if (contributors != null) {
            return Arrays.asList(contributors);
        } else {
            return Collections.emptyList();
        }
    }

    @SyndicationElement(type = FeedType.ATOM, name = "generator", converter = StringToGeneratorConverter.class)
    public String getGenerator() {
        return getSingleProperty("feed:generator");
    }

    @SyndicationElement(type = FeedType.ATOM, name = "icon", transformer = DocumentLinkResolver.class)
    public HippoGalleryImageSet getIcon() {
        return getLinkedBean("feed:icon", HippoGalleryImageSet.class);
    }

    @SyndicationElement(type = FeedType.ATOM, name = "logo", transformer = DocumentLinkResolver.class)
    public HippoGalleryImageSet getLogo() {
        return getLinkedBean("feed:logo", HippoGalleryImageSet.class);
    }

    @SyndicationElement(type = FeedType.ATOM, name = "rights")
    public String getRights() {
        return getSingleProperty("feed:rights");
    }

    @Override
    public Feed createSyndication() {
        return new Feed("atom_1.0");
    }

    @Override
    public Entry createEntry() {
        return new Entry();
    }

    @Override
    public void set(final Feed syndication, final List entries) {
        syndication.setEntries(entries);
    }

    @Override
    public String process(final Feed syndication) {

        String feed = null;
        try (final Writer writer = new StringWriter()) {
            WireFeedOutput output = new WireFeedOutput();
            output.output(syndication, writer);
            feed = writer.toString();
        } catch (FeedException | IOException e) {
            log.error("Error processing feed", e);
        }
        return feed;
    }

    @Override
    public FeedType type() {
        return FeedType.ATOM;
    }

    /**
     * right side
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
