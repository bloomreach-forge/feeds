/*
 * Copyright 2018-2020 Hippo B.V. (http://www.onehippo.com)
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

package org.example.feed;

import java.util.List;

import org.bloomreach.forge.feed.api.modifier.RSS20PropertyFilteringModifier;
import org.example.beans.NewsDocument;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSetBean;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.jdom2.Element;
import org.jdom2.Namespace;


import com.rometools.rome.feed.rss.Item;



public class ImageModifier extends RSS20PropertyFilteringModifier {

    @Override
    public void modifyEntry(final HstRequestContext context, final Item entry, final HippoBean bean) {
        super.modifyEntry(context, entry, bean);
        if (bean instanceof NewsDocument) {
            final HippoGalleryImageSetBean image = ((NewsDocument) bean).getImage();
            if (image != null) {
                final HstLink hstLink = context.getHstLinkCreator().create(image, context);
                if (hstLink != null) {
                    final Element element = new Element("image", Namespace.getNamespace("image", "http://web.resource.org/rss/1.0/modules/image/"));
                    element.addContent(hstLink.toUrlForm(context, true));
                    final List<Element> foreignMarkup = entry.getForeignMarkup();
                    foreignMarkup.add(element);
                }
            }
        }
    }
}
