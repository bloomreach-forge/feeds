/*
 * Copyright 2013-2024 Hippo B.V. (http://www.onehippo.com)
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
package org.bloomreach.forge.feed.resource;


import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Item;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@Produces({MediaType.APPLICATION_XML})
@Path("/feed:rss20descriptor/")
public class RssSyndicationResource extends AbstractSyndicationResource<Channel, Item> {


}
