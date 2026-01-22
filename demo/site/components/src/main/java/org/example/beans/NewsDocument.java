package org.example.beans;
/*
 * Copyright 2014-2020 Hippo B.V. (http://www.onehippo.com)
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
import java.util.Calendar;

import org.bloomreach.forge.feed.api.FeedType;
import org.bloomreach.forge.feed.api.annot.SyndicationElement;
import org.bloomreach.forge.feed.api.annot.SyndicationRefs;
import org.bloomreach.forge.feed.api.transform.CalendarToDateConverter;
import org.bloomreach.forge.feed.api.transform.rss.StringToDescriptionConverter;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

@HippoEssentialsGenerated(internalName = "feedsdemo:newsdocument")
@Node(jcrType="feedsdemo:newsdocument")
public class NewsDocument extends HippoDocument {

    /**
     * The document type of the news document.
     */
    public static final String DOCUMENT_TYPE = "feedsdemo:newsdocument";

    private static final String TITLE = "feedsdemo:title";
    private static final String DATE = "feedsdemo:date";
    private static final String INTRODUCTION = "feedsdemo:introduction";
    private static final String IMAGE = "feedsdemo:image";
    private static final String CONTENT = "feedsdemo:content";
    private static final String LOCATION = "feedsdemo:location";
    private static final String AUTHOR = "feedsdemo:author";
    private static final String SOURCE = "feedsdemo:source";

    /**
     * Get the title of the document.
     *
     * @return the title
     */
    @SyndicationRefs({
            @SyndicationElement(type = FeedType.RSS, name = "title"),
            @SyndicationElement(type = FeedType.ATOM, name = "title")
    })
    @HippoEssentialsGenerated(internalName = "feedsdemo:title")
    public String getTitle() {
        return getSingleProperty(TITLE);
    }

    /**
     * Get the date of the document.
     *
     * @return the date
     */
    @SyndicationElement(type = FeedType.RSS, name = "pubDate", converter = CalendarToDateConverter.class)
    @HippoEssentialsGenerated(internalName = "feedsdemo:date")
    public Calendar getDate() {
        return getSingleProperty(DATE);
    }

    /**
     * Get the introduction of the document.
     *
     * @return the introduction
     */
    @SyndicationElement(type = FeedType.RSS, name = "description", converter = StringToDescriptionConverter.class)
    @HippoEssentialsGenerated(internalName = "feedsdemo:introduction")
    public String getIntroduction() {
        return getSingleProperty(INTRODUCTION);
    }

    /**
     * Get the image of the document.
     *
     * @return the image
     */
    @HippoEssentialsGenerated(internalName = "feedsdemo:image")
    public HippoGalleryImageSet getImage() {
        return getLinkedBean(IMAGE, HippoGalleryImageSet.class);
    }

    /**
     * Get the main content of the document.
     *
     * @return the content
     */
    @HippoEssentialsGenerated(internalName = "feedsdemo:content")
    public HippoHtml getContent() {
        return getHippoHtml(CONTENT);
    }

    /**
     * Get the location of the document.
     *
     * @return the location
     */
    @HippoEssentialsGenerated(internalName = "feedsdemo:location")
    public String getLocation() {
        return getSingleProperty(LOCATION);
    }

    /**
     * Get the author of the document.
     *
     * @return the author
     */
    @HippoEssentialsGenerated(internalName = "feedsdemo:author")
    public String getAuthor() {
        return getSingleProperty(AUTHOR);
    }

    /**
     * Get the source of the document.
     *
     * @return the source
     */
    @HippoEssentialsGenerated(internalName = "feedsdemo:source")
    public String getSource() {
        return getSingleProperty(SOURCE);
    }

}

