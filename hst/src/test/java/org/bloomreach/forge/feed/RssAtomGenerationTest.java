/*
 * Copyright 2020 Hippo B.V. (http://www.onehippo.com)
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

package org.bloomreach.forge.feed;import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.beanutils.BeanUtils;
import org.bloomreach.forge.feed.beans.Atom10FeedDescriptor;
import org.junit.Test;
import org.bloomreach.forge.feed.api.FeedType;
import org.bloomreach.forge.feed.api.annot.ContextTransformable;
import org.bloomreach.forge.feed.api.annot.SyndicationElement;
import org.bloomreach.forge.feed.api.annot.SyndicationRefs;
import org.bloomreach.forge.feed.api.annot.noop.NoopConverter;
import org.bloomreach.forge.feed.api.annot.noop.NoopTransformer;
import org.bloomreach.forge.feed.api.transform.CalendarToDateTransformer;
import org.bloomreach.forge.feed.api.transform.Converter;
import org.bloomreach.forge.feed.api.transform.atom.StringToContentConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.WireFeedOutput;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class RssAtomGenerationTest {

    private static final Logger log = LoggerFactory.getLogger(RssAtomGenerationTest.class);

    @Test
    public void testRssChannelGeneration() throws Exception {

        Channel channel = new Channel("rss_2.0");
        channel.setTitle("test");
        channel.setDescription("description");
        channel.setPubDate(Calendar.getInstance().getTime());
        channel.setLink("http://www.google.com");

        channel.setLanguage("nl");
        channel.setManagingEditor("managingeditor");
        channel.setWebMaster("webmaster");
        //channel.setCategories(Arrays.asList(new String[]{"test1, test2"}));
        channel.setGenerator("generator");
        channel.setCopyright("copyright");

        Writer writer = new StringWriter();
        WireFeedOutput output = new WireFeedOutput();
        output.output(channel, writer);
        log.info(writer.toString());

        final SyndFeedImpl syndFeed = new SyndFeedImpl(channel, true);

        Writer writer1 = new StringWriter();
        SyndFeedOutput syndFeedOutput = new SyndFeedOutput();
        syndFeedOutput.output(syndFeed, writer1);

        log.info(writer1.toString());

    }


    @Test
    public void testReflectionUtils() throws Exception {
        TestBean bean = new TestBean();
        final Channel channel = convertObject(new Channel(), bean, FeedType.RSS, 23324324);
        final Feed ourFeed = new Feed();
        final Feed feed = convertObject(ourFeed, bean, FeedType.ATOM, 23324324);
        feed.setFeedType("atom_1.0");
        log.info("feed {}", feed);
        final String title = channel.getTitle();
        final Writer writer = new StringWriter();
        final Atom10FeedDescriptor syndFeedOutput = new Atom10FeedDescriptor();
        log.info(syndFeedOutput.process(feed));
    }


    @Test
    public void testBlankStringPropertySkipped() throws Exception {
        Item item = convertObject(new Item(), new TestBeanWithBlankSource(), FeedType.RSS);
        // blank source must be silently skipped — Item.source is a complex type, not a String
        assertNull(item.getSource());
        // other fields must still be mapped correctly
        assertEquals("testtitle", item.getTitle());
    }

    public static class TestBeanWithBlankSource {

        @SyndicationElement(type = FeedType.RSS, name = "title")
        public String getTitle() {
            return "testtitle";
        }

        // mirrors NewsDocument.getSource() returning an empty JCR property
        @SyndicationElement(type = FeedType.RSS, name = "source")
        public String getSource() {
            return "";
        }
    }

    public static class TestBean {

        @SyndicationRefs({
                @SyndicationElement(type = FeedType.RSS, name = "title"),
                @SyndicationElement(type = FeedType.ATOM, name = "title", converter = StringToContentConverter.class)
        })
        public String getTitle() {
            return "testtitle";
        }


        @SyndicationElement(type = FeedType.RSS, name = "pubDate", transformer = CalendarToDateTransformer.class)
        public Calendar getPublicationDate() {
            return Calendar.getInstance();
        }

    }

    public static <T> T convertObject(final T destination, final Object source, FeedType type, Object... context) {
        Map<Class, Object> contentMap = new HashMap<>();
        for (Object o : context) {
            contentMap.put(o.getClass(), o);
        }
        final Method[] methods = source.getClass().getMethods();
        for (Method method : methods) {
            final boolean refPresent = method.isAnnotationPresent(SyndicationRefs.class);
            if (refPresent || (method.isAnnotationPresent(SyndicationElement.class) && method.getAnnotation(SyndicationElement.class).type().equals(type))) {
                SyndicationElement annotation = null;
                if (refPresent) {
                    final SyndicationElement[] value = method.getAnnotation(SyndicationRefs.class).value();
                    for (SyndicationElement element : value) {
                        if (element.type().equals(type)) {
                            annotation = element;
                            break;
                        }
                    }
                    if (annotation == null) {
                        continue;
                    }
                } else {
                    annotation = method.getAnnotation(SyndicationElement.class);
                }
                //final SyndicationElement annotation =
                final String name = annotation.name();
                try {
                    final Object initValue = method.invoke(source);
                    Object value = initValue;
                    if (!(annotation.converter().isAssignableFrom(NoopConverter.class))) {
                        final Converter converter = annotation.converter().newInstance();
                        value = converter.convert(initValue);
                    }
                    if (!(annotation.transformer().isAssignableFrom(NoopTransformer.class))) {
                        contentMap.put(initValue.getClass(), initValue);
                        final Class<?> transformer = annotation.transformer();
                        final Method[] transformerMethods = transformer.getMethods();
                        for (Method transformerMethod : transformerMethods) {
                            if (transformerMethod.isAnnotationPresent(ContextTransformable.class)) {
                                final Class<?>[] parameterTypes = transformerMethod.getParameterTypes();
                                List<Object> parameters = new ArrayList<Object>();
                                for (Class<?> clazz : parameterTypes) {
                                    if (contentMap.containsKey(clazz)) {
                                        parameters.add(contentMap.get(clazz));
                                    } else {
                                        boolean found = false;
                                        for (Object obj : contentMap.values()) {
                                            if (clazz.isInstance(obj)) {
                                                parameters.add(obj);
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            parameters.add(null);
                                        }
                                    }
                                }
                                if (Modifier.isStatic(transformerMethod.getModifiers())) {
                                    value = transformerMethod.invoke(null, parameters.toArray());
                                } else {
                                    value = transformerMethod.invoke(transformer.newInstance(), parameters.toArray());
                                }
                                break;
                            }
                        }

                    }
                    if (value != null && !(value instanceof String && ((String) value).isBlank())) {
                        BeanUtils.setProperty(destination, name, value);
                    }
                } catch (Exception e) {
                    log.error("test", e);
                }

            }
        }
        return destination;
    }



}
