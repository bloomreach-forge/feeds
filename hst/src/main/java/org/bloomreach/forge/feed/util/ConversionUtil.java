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

package org.bloomreach.forge.feed.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.bloomreach.forge.feed.api.FeedDescriptor;
import org.bloomreach.forge.feed.api.FeedType;
import org.bloomreach.forge.feed.api.annot.ContextTransformable;
import org.bloomreach.forge.feed.api.annot.SyndicationElement;
import org.bloomreach.forge.feed.api.annot.SyndicationRefs;
import org.bloomreach.forge.feed.api.annot.noop.NoopConverter;
import org.bloomreach.forge.feed.api.annot.noop.NoopTransformer;
import org.bloomreach.forge.feed.api.transform.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version "$Id$"
 */
public class ConversionUtil {

    private static Logger log = LoggerFactory.getLogger(ConversionUtil.class);

    public static <T> T covertToAppropriateSyndicationFeed(final FeedDescriptor<T, ?> descriptor, final Object source, Object... context) {
        final T syndication = descriptor.createSyndication();
        return convertObject(syndication, source, descriptor.type(), context);
    }

    public static <E> E covertToAppropriateSyndicationEntry(final FeedDescriptor<?, E> descriptor, final Object source, Object... context) {
        final E entry = descriptor.createEntry();
        return convertObject(entry, source, descriptor.type(), context);
    }

    /**
     * TODO refactor complexity and enable caching
     *
     * @param destination
     * @param source
     * @param type
     * @param context
     * @param <T>
     * @return
     */
    private static <T> T convertObject(final T destination, final Object source, FeedType type, Object... context) {
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
                final String name = annotation.name();
                try {
                    final Object initValue = method.invoke(source);
                    Object value = initValue;
                    if (!(annotation.converter().isAssignableFrom(NoopConverter.class))) {
                        final Converter converter = annotation.converter().newInstance();
                        value = converter.convert(initValue);
                    }
                    if (!(annotation.transformer().isAssignableFrom(NoopTransformer.class)) && initValue != null) {
                        Map<Class, Object> contentMap = new HashMap<>();
                        for (Object o : context) {
                            contentMap.put(o.getClass(), o);
                        }
                        contentMap.put(initValue.getClass(), initValue);
                        final Class<?> transformer = annotation.transformer();
                        final Method[] transformerMethods = transformer.getMethods();
                        for (Method transformerMethod : transformerMethods) {
                            if (transformerMethod.isAnnotationPresent(ContextTransformable.class)) {
                                final Class<?>[] parameterTypes = transformerMethod.getParameterTypes();
                                List<Object> parameters = new ArrayList<>();
                                for (Class clazz : parameterTypes) {
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
                    log.error(String.format("exception happened while trying to revolve syndication %s", name), e);
                }

            }
        }
        return destination;
    }


}
