/*
 * Copyright 2013-2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.forge.feed.beans;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

import org.apache.commons.io.IOUtils;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.onehippo.forge.feed.api.FeedDescriptor;
import org.onehippo.forge.feed.api.FeedType;
import org.onehippo.forge.feed.api.annot.SyndicationElement;
import org.onehippo.forge.feed.api.transform.rss.ListToRssCategoryListConverter;
import org.onehippo.forge.feed.api.transform.CalendarToDateConverter;
import org.onehippo.forge.feed.api.transform.rss.HippoGalleryImageSetToImageTransformer;
import org.onehippo.forge.feed.api.transform.PathLinkResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Node(jcrType = "feed:rss20descriptor")
public class RSS20FeedDescriptor extends HippoDocument implements FeedDescriptor<Channel, Item> {

    private static final Logger log = LoggerFactory.getLogger(RSS20FeedDescriptor.class);

    /**
     * required
     */

    @SyndicationElement(type = FeedType.RSS, name = "title")
    public String getTitle() {
        return getSingleProperty("feed:title");
    }

    @SyndicationElement(type = FeedType.RSS, name = "description")
    public String getDescription() {
        return getSingleProperty("feed:description");
    }

    @SyndicationElement(type = FeedType.RSS, name = "pubdate", converter = CalendarToDateConverter.class)
    public Calendar getPublicationDate() {
        return getSingleProperty("hippostdpubwf:publicationDate");
    }

    @SyndicationElement(type = FeedType.RSS, name = "link", transformer = PathLinkResolver.class)
    public String getLink() {
        return "/";
    }

    /**
     * other
     */

    @SyndicationElement(type = FeedType.RSS, name = "language")
    public String getLanguage() {
        return getSingleProperty("feed:language");
    }

    @SyndicationElement(type = FeedType.RSS, name = "copyright")
    public String getCopyright() {
        return getSingleProperty("feed:copyright");
    }

    @SyndicationElement(type = FeedType.RSS, name = "managingEditor")
    public String getManagingEditor() {
        return getSingleProperty("feed:managingEditor");
    }

    @SyndicationElement(type = FeedType.RSS, name = "webMaster")
    public String getWebMaster() {
        return getSingleProperty("feed:webMaster");
    }

    @SyndicationElement(type = FeedType.RSS, name = "categories", converter = ListToRssCategoryListConverter.class)
    public List<String> getCategory() {
        String[] categories = getMultipleProperty("feed:category");
        if (categories != null) {
            return Arrays.asList(categories);
        } else {
            return Collections.emptyList();
        }
    }

    @SyndicationElement(type = FeedType.RSS, name = "generator")
    public String getGenerator() {
        return getSingleProperty("feed:generator");
    }

    @SyndicationElement(type = FeedType.RSS, name = "image", transformer = HippoGalleryImageSetToImageTransformer.class)
    public HippoGalleryImageSet getImage() {
        return getLinkedBean("feed:image", HippoGalleryImageSet.class);
    }

    @Override
    public Channel createSyndication() {
        return new Channel("rss_2.0");
    }

    @Override
    public Item createEntry() {
        return new Item();
    }

    @Override
    public void set(final Channel syndication, final List<Item> entries) {
        syndication.setItems(entries);
    }

    @Override
    public FeedType type() {
        return FeedType.RSS;
    }

    @Override
    public String process(final Channel syndication) {
        Writer writer = null;
        String feed = null;
        try {
            writer = new StringWriter();
            WireFeedOutput output = new WireFeedOutput();
            output.output(syndication, writer);
            feed = writer.toString();
        } catch (FeedException | IOException e) {
            log.error("", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return feed;
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

    @Override
    public String[] getPropertyFilters() {
        return getMultipleProperty("feed:propertyfilter");
    }
}
