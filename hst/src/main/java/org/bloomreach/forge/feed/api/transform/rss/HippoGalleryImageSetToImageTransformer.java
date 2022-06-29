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

package org.bloomreach.forge.feed.api.transform.rss;

import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.bloomreach.forge.feed.api.annot.ContextTransformable;

import com.rometools.rome.feed.rss.Image;

public class HippoGalleryImageSetToImageTransformer {

    @ContextTransformable
    public Image getImage(HstRequestContext context, HippoGalleryImageSet imageSet) {
        Image image = new Image();
        final HstLinkCreator hstLinkCreator = context.getHstLinkCreator();
        final String baseLink = hstLinkCreator.create("/", context.getResolvedMount().getMount()).toUrlForm(context, true);
        final String imageLink = hstLinkCreator.create(imageSet, context).toUrlForm(context, true);
        image.setUrl(imageLink);
        image.setLink(baseLink);
        image.setTitle(imageSet.getDescription());
        image.setHeight(imageSet.getOriginal().getHeight());
        image.setWidth(imageSet.getOriginal().getWidth());
        return image;
    }

}
