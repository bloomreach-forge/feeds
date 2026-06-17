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
package org.bloomreach.forge.feed.util;

import java.util.Calendar;
import java.util.List;

import org.bloomreach.forge.feed.api.FeedType;
import org.bloomreach.forge.feed.api.annot.ContextTransformable;
import org.bloomreach.forge.feed.api.annot.SyndicationElement;
import org.bloomreach.forge.feed.api.annot.SyndicationRefs;
import org.bloomreach.forge.feed.api.transform.CalendarToDateConverter;
import org.bloomreach.forge.feed.api.transform.CalendarToDateTransformer;
import org.bloomreach.forge.feed.api.transform.atom.StringToContentConverter;
import org.junit.jupiter.api.Test;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.rss.Channel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConversionUtil annotation-driven property mapping.
 * These use plain bean stubs — no JCR, Spring, or Wicket required.
 */
class ConversionUtilTest {

    // --- minimal FeedDescriptor stub for RSS Channel ---

    static class RssChannelDescriptor implements org.bloomreach.forge.feed.api.FeedDescriptor<Channel, com.rometools.rome.feed.rss.Item> {
        @Override public Channel createSyndication() { return new Channel("rss_2.0"); }
        @Override public com.rometools.rome.feed.rss.Item createEntry() { return new com.rometools.rome.feed.rss.Item(); }
        @Override public void set(Channel s, List<com.rometools.rome.feed.rss.Item> e) { s.setItems(e); }
        @Override public String process(Channel s) { return null; }
        @Override public FeedType type() { return FeedType.RSS; }
        @Override public String getScope() { return null; }
        @Override public String getDocumentType() { return null; }
        @Override public Long getItemCount() { return null; }
        @Override public String getSortByField() { return null; }
        @Override public String getExclude() { return null; }
        @Override public String getFilterByProperty() { return null; }
        @Override public String getFilterByValue() { return null; }
    }

    // --- minimal FeedDescriptor stub for Atom Feed ---

    static class AtomFeedDescriptor implements org.bloomreach.forge.feed.api.FeedDescriptor<Feed, Entry> {
        @Override public Feed createSyndication() { Feed f = new Feed("atom_1.0"); return f; }
        @Override public Entry createEntry() { return new Entry(); }
        @Override public void set(Feed s, List<Entry> e) { s.setEntries(e); }
        @Override public String process(Feed s) { return null; }
        @Override public FeedType type() { return FeedType.ATOM; }
        @Override public String getScope() { return null; }
        @Override public String getDocumentType() { return null; }
        @Override public Long getItemCount() { return null; }
        @Override public String getSortByField() { return null; }
        @Override public String getExclude() { return null; }
        @Override public String getFilterByProperty() { return null; }
        @Override public String getFilterByValue() { return null; }
    }

    // --- source bean with @SyndicationElement for RSS ---

    public static class RssSourceBean {
        @SyndicationElement(type = FeedType.RSS, name = "title")
        public String getTitle() { return "Test Title"; }

        @SyndicationElement(type = FeedType.RSS, name = "description")
        public String getDescription() { return "Test Description"; }
    }

    // --- source bean with @SyndicationElement using a converter for ATOM ---

    public static class AtomSourceBean {
        @SyndicationElement(type = FeedType.ATOM, name = "title", converter = StringToContentConverter.class)
        public String getTitle() { return "Atom Title"; }
    }

    // --- source bean with @SyndicationRefs (multi-type) ---

    public static class MultiTypeSourceBean {
        @SyndicationRefs({
            @SyndicationElement(type = FeedType.RSS, name = "title"),
            @SyndicationElement(type = FeedType.ATOM, name = "title", converter = StringToContentConverter.class)
        })
        public String getTitle() { return "Multi Title"; }
    }

    // --- source bean with a transformer (CalendarToDateTransformer for RSS pubDate) ---

    public static class TransformerSourceBean {
        @SyndicationElement(type = FeedType.RSS, name = "pubDate", transformer = CalendarToDateTransformer.class)
        public Calendar getPublicationDate() { return Calendar.getInstance(); }
    }

    // ---- tests ----

    @Test
    void covertToAppropriateSyndicationFeed_rss_mapsTitle() {
        Channel channel = ConversionUtil.covertToAppropriateSyndicationFeed(
                new RssChannelDescriptor(), new RssSourceBean());
        assertEquals("Test Title", channel.getTitle());
    }

    @Test
    void covertToAppropriateSyndicationFeed_rss_mapsDescription() {
        Channel channel = ConversionUtil.covertToAppropriateSyndicationFeed(
                new RssChannelDescriptor(), new RssSourceBean());
        // RSS Channel.description maps to the channel description String field
        assertNotNull(channel.getDescription());
        assertEquals("Test Description", channel.getDescription());
    }

    @Test
    void covertToAppropriateSyndicationFeed_atom_useConverter() {
        Feed feed = ConversionUtil.covertToAppropriateSyndicationFeed(
                new AtomFeedDescriptor(), new AtomSourceBean());
        assertNotNull(feed);
        // BeanUtils maps Content via setTitle(String), so getTitle() returns Content.toString()
        // and the actual value is embedded in that string
        String title = feed.getTitle();
        assertNotNull(title);
        assertTrue(title.contains("Atom Title"), "title should contain 'Atom Title' but was: " + title);
    }

    @Test
    void covertToAppropriateSyndicationFeed_syndicationRefs_rss_mapsCorrectType() {
        Channel channel = ConversionUtil.covertToAppropriateSyndicationFeed(
                new RssChannelDescriptor(), new MultiTypeSourceBean());
        assertEquals("Multi Title", channel.getTitle());
    }

    @Test
    void covertToAppropriateSyndicationFeed_syndicationRefs_atom_usesConverter() {
        Feed feed = ConversionUtil.covertToAppropriateSyndicationFeed(
                new AtomFeedDescriptor(), new MultiTypeSourceBean());
        // BeanUtils maps Content via setTitle(String)
        String title = feed.getTitle();
        assertNotNull(title);
        assertTrue(title.contains("Multi Title"), "title should contain 'Multi Title' but was: " + title);
    }

    @Test
    void covertToAppropriateSyndicationFeed_rss_transformerSetsPubDate() {
        Channel channel = ConversionUtil.covertToAppropriateSyndicationFeed(
                new RssChannelDescriptor(), new TransformerSourceBean());
        assertNotNull(channel.getPubDate());
    }

    @Test
    void covertToAppropriateSyndicationEntry_rss_mapsTitle() {
        RssEntryDescriptor descriptor = new RssEntryDescriptor();
        com.rometools.rome.feed.rss.Item item = ConversionUtil.covertToAppropriateSyndicationEntry(
                descriptor, new RssItemSourceBean());
        assertEquals("Item Title", item.getTitle());
    }

    // ---- RSS entry helpers ----

    static class RssEntryDescriptor implements org.bloomreach.forge.feed.api.FeedDescriptor<Channel, com.rometools.rome.feed.rss.Item> {
        @Override public Channel createSyndication() { return new Channel("rss_2.0"); }
        @Override public com.rometools.rome.feed.rss.Item createEntry() { return new com.rometools.rome.feed.rss.Item(); }
        @Override public void set(Channel s, List<com.rometools.rome.feed.rss.Item> e) { s.setItems(e); }
        @Override public String process(Channel s) { return null; }
        @Override public FeedType type() { return FeedType.RSS; }
        @Override public String getScope() { return null; }
        @Override public String getDocumentType() { return null; }
        @Override public Long getItemCount() { return null; }
        @Override public String getSortByField() { return null; }
        @Override public String getExclude() { return null; }
        @Override public String getFilterByProperty() { return null; }
        @Override public String getFilterByValue() { return null; }
    }

    public static class RssItemSourceBean {
        @SyndicationElement(type = FeedType.RSS, name = "title")
        public String getTitle() { return "Item Title"; }
    }
}
