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

package org.bloomreach.forge.feed.api.modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bloomreach.forge.feed.beans.Atom10FeedDescriptor;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.atom.Entry;

/**
 * <p>
 * Atom 1.0 modifier that enriches feed entries with custom namespace fields from documents.
 * </p>
 *
 * <p>
 * <strong>How it works:</strong> This modifier extends Atom property filtering and automatically
 * adds custom namespace elements for document properties that aren't part of the Atom spec.
 * This allows rich document fields (introduction, content, author, location, etc.) to be
 * included in Atom feeds while maintaining spec compliance by using extension elements.
 * </p>
 *
 * <p>
 * <strong>Atom Compliance:</strong> Per RFC 4287, Atom entries support "extension elements" -
 * elements in other namespaces that feed readers can ignore or process as needed. This modifier
 * uses this feature to add document-specific fields without violating the Atom spec.
 * </p>
 *
 * <h2>Configuration Example:</h2>
 *
 * <pre>
 * &lt;bean id="atomModifier" class="org.bloomreach.forge.feed.api.modifier.Atom10EnrichedModifier"&gt;
 *   &lt;property name="enrichmentFields"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;introduction&lt;/value&gt;
 *       &lt;value&gt;content&lt;/value&gt;
 *       &lt;value&gt;location&lt;/value&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <h2>Default Fields:</h2>
 * <p>If no fields are configured, the following are included by default:</p>
 * <ul>
 *   <li>introduction - Brief description/summary</li>
 *   <li>content - Full content/body</li>
 *   <li>author - Document author</li>
 *   <li>location - Geographic location</li>
 *   <li>source - Source/origin</li>
 *   <li>language - Document language</li>
 *   <li>copyright - Copyright notice</li>
 * </ul>
 */
public class Atom10EnrichedModifier extends Atom10PropertyFilteringModifier {

    private static final Logger log = LoggerFactory.getLogger(Atom10EnrichedModifier.class);

    /**
     * Custom namespace for enriched feed content.
     * Uses a stable namespace URI per RFC 4287 extension element guidelines.
     */
    private static final Namespace ENRICHMENT_NS =
            Namespace.getNamespace("br-feed", "http://bloomreach-forge.example.org/feed");

    /**
     * Default fields to include in enrichment if not explicitly configured.
     */
    private static final List<String> DEFAULT_ENRICHMENT_FIELDS = Arrays.asList(
            "introduction",
            "content",
            "author",
            "location",
            "source",
            "language",
            "copyright"
    );

    /**
     * Fields to include in custom namespace enrichment.
     * Can be overridden via Spring configuration.
     */
    private List<String> enrichmentFields = new ArrayList<>(DEFAULT_ENRICHMENT_FIELDS);

    @Override
    public void modifyEntry(final HstRequestContext context, final Entry entry, final HippoBean bean) {
        // First apply property filtering from parent class
        super.modifyEntry(context, entry, bean);

        // Then add custom namespace enrichment
        addEnrichmentFields(context, entry, bean);
    }

    /**
     * Add custom namespace fields to the Atom entry based on document properties.
     * Fields are added as extension elements per RFC 4287.
     *
     * @param context the HST request context
     * @param entry the Atom entry to enrich
     * @param bean the source document bean
     */
    private void addEnrichmentFields(final HstRequestContext context, final Entry entry, final HippoBean bean) {
        try {
            List<Element> foreignMarkup = entry.getForeignMarkup();

            // Process each configured enrichment field
            for (String fieldName : enrichmentFields) {
                addEnrichmentField(fieldName, bean, foreignMarkup);
            }

        } catch (Exception e) {
            log.warn("Error adding enrichment fields to Atom entry: {}", e.getMessage(), e);
        }
    }

    /**
     * Add a single enrichment field to the entry if it exists on the bean.
     *
     * @param fieldName the field name (property name without "get" prefix)
     * @param bean the source bean
     * @param foreignMarkup the list to add the element to
     */
    private void addEnrichmentField(final String fieldName, final HippoBean bean,
                                    final List<Element> foreignMarkup) {
        try {
            // Build getter method name: "introduction" -> "getIntroduction"
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Object fieldValue = bean.getClass().getMethod(methodName).invoke(bean);

            if (fieldValue == null) {
                log.debug("Field '{}' is null on bean {}, skipping enrichment", fieldName, bean.getName());
                return;
            }

            String stringValue = fieldValue.toString().trim();
            if (stringValue.isEmpty()) {
                log.debug("Field '{}' is empty on bean {}, skipping enrichment", fieldName, bean.getName());
                return;
            }

            // Create element with custom namespace
            Element fieldElement = new Element(fieldName, ENRICHMENT_NS);
            fieldElement.setText(stringValue);
            foreignMarkup.add(fieldElement);

            log.debug("Added enrichment field '{}' to Atom entry from bean {}", fieldName, bean.getName());

        } catch (NoSuchMethodException e) {
            log.debug("Field '{}' not found on bean {} (no getter method)", fieldName, bean.getName());
        } catch (Exception e) {
            log.debug("Error adding enrichment field '{}' from bean {}: {}",
                    fieldName, bean.getName(), e.getMessage());
        }
    }

    /**
     * Set the fields to include in custom namespace enrichment.
     * If not set, uses DEFAULT_ENRICHMENT_FIELDS.
     *
     * @param enrichmentFields list of field names to include
     */
    public void setEnrichmentFields(List<String> enrichmentFields) {
        if (enrichmentFields != null && !enrichmentFields.isEmpty()) {
            this.enrichmentFields = new ArrayList<>(enrichmentFields);
            log.info("Configured custom enrichment fields: {}", enrichmentFields);
        }
    }

    /**
     * Get the current enrichment fields configuration.
     *
     * @return list of field names being included in enrichment
     */
    public List<String> getEnrichmentFields() {
        return new ArrayList<>(enrichmentFields);
    }

    /**
     * Add a single field to the enrichment configuration.
     *
     * @param fieldName the field name to add
     */
    public void addEnrichmentField(String fieldName) {
        if (fieldName != null && !fieldName.trim().isEmpty() && !enrichmentFields.contains(fieldName)) {
            enrichmentFields.add(fieldName);
        }
    }

    /**
     * Remove a field from the enrichment configuration.
     *
     * @param fieldName the field name to remove
     */
    public void removeEnrichmentField(String fieldName) {
        enrichmentFields.remove(fieldName);
    }

    /**
     * Reset enrichment fields to defaults.
     */
    public void resetToDefaults() {
        enrichmentFields = new ArrayList<>(DEFAULT_ENRICHMENT_FIELDS);
    }
}
