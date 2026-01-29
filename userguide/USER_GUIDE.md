# Bloomreach Feeds Plugin - User Guide

## Table of Contents

1. [Overview](#overview)
2. [Property-Based Feed Filtering](#property-based-feed-filtering)
3. [Atom Feed Enrichment](#atom-feed-enrichment)
4. [Testing & Validation](#testing--validation)
5. [Troubleshooting](#troubleshooting)

---

## Overview

The Bloomreach Feeds Plugin is a powerful extension for Hippo CMS that enables creation of RSS, Atom, and generic web syndication feeds. This guide covers two key features:

- **Property Filtering**: Filter feed items based on document properties without custom code
- **Atom Enrichment**: Automatically add document metadata to Atom feeds using custom namespaces

### Key Capabilities

- Configure filters directly in the CMS UI
- Support for exact matches and wildcard patterns
- Case-insensitive matching
- Atom feeds enriched with custom namespace fields
- Full Atom 1.0 specification compliance
- Zero custom code required for basic usage

---

## Property-Based Feed Filtering

### Overview

Filter feed entries by document properties using the CMS UI. Only documents matching your filter criteria will appear in the feed.

### How It Works

When a feed is generated:
1. The filtering modifier reads the configured filter property and value from the feed descriptor
2. For each document in the query results:
   - Evaluates the specified property on the document
   - Checks if the value matches the filter criteria
   - Excludes entries that don't match
3. Returns only the filtered entries

### Configuration

All feed types (RSS 2.0, Atom 1.0, Generic) support property filtering through two fields:

1. **Filter by Property**: The property name to check (e.g., `status`, `featured`)
2. **Filter by Value**: The value to match (supports exact matches and wildcards)

#### Setup Steps

1. Create or edit a feed descriptor (RSS, Atom, or Generic)
2. Locate the filtering section on the right panel
3. Enter the property name in "Filter by Property"
4. Enter the filter value in "Filter by Value"
5. Save and publish the descriptor
6. Generate the feed - only matching entries will be included

### Filter Syntax

#### Exact Match (Case-Insensitive)

```
Filter Value: published
Matches: published, PUBLISHED, Published, pUbLiShEd
```

#### Wildcard Patterns

Use `*` (asterisk) for zero or more characters:

| Pattern | Syntax | Example | Matches |
|---------|--------|---------|---------|
| **Prefix** | `value*` | `pub*` | "published", "public", "publisher" |
| **Suffix** | `*value` | `*lished` | "published", "established", "cherished" |
| **Contains** | `*value*` | `*ublish*` | "published", "republished", "publishing" |
| **Complex** | `*value*more` | `*pub*ed` | "published", "republished" |

### Practical Examples

#### Example 1: Filter Published Articles

**Configuration:**
- Filter by Property: `status`
- Filter by Value: `published`

**Result:** Only documents with status="published" appear (case-insensitive)

#### Example 2: Filter Featured Documents

**Configuration:**
- Filter by Property: `featured`
- Filter by Value: `true`

**Result:** Only featured documents included

#### Example 3: Filter by Prefix Pattern

**Configuration:**
- Filter by Property: `status`
- Filter by Value: `pub*`

**Result:** Includes documents with status: "published", "publishing", "public"

#### Example 4: Filter by Multiple Languages

**Configuration:**
- Filter by Property: `language`
- Filter by Value: `en*`

**Result:** Includes "en", "en-US", "en-GB", "english"

#### Example 5: Filter by Wildcard Contains

**Configuration:**
- Filter by Property: `location`
- Filter by Value: `*new*`

**Result:** Includes "New York", "New Delhi", "Denver" (case-insensitive)

### Filtering Behavior

- **Case Sensitivity**: Matching is case-insensitive by default
- **Missing Properties**: Documents without the specified property are excluded
- **No Filter**: If either filter field is empty/null, no filtering is applied
- **Multiple Filters**: Each feed supports a single property-value filter pair

### Advanced Usage

#### Custom Filtering Logic

If you need more complex filtering (multiple properties, different operators), extend the base modifier:

```java
public class MyCustomRSSModifier extends RSS20PropertyFilteringModifier {
    @Override
    public void modifyEntry(HstRequestContext context, Item entry, HippoBean bean) {
        // Apply custom filtering logic
        super.modifyEntry(context, entry, bean);
        // Add additional logic here
    }
}
```

Register in Spring configuration:

```xml
<bean id="customModifier" class="com.example.MyCustomRSSModifier" scope="singleton"/>
```

---

## Atom Feed Enrichment

### Overview

Atom feeds automatically include document properties as custom namespace fields, enriching the feed with additional document metadata while maintaining full Atom 1.0 specification compliance (RFC 4287).

### Why Enrichment?

The Atom 1.0 specification defines a strict set of core elements (title, author, updated, entry ID, content, summary). While adequate for basic feeds, documents typically contain additional useful information:

- **introduction** - Brief summary/description
- **content** - Full article body
- **location** - Geographic location/origin
- **author** - Custom author metadata
- **language** - Document language code
- **copyright** - Copyright/licensing info
- **source** - Original source

### How It Works

The `Atom10EnrichedModifier` automatically extracts document properties and adds them as custom namespace fields:

**Field Mapping:**
```
introduction → <br-feed:introduction>...</br-feed:introduction>
content      → <br-feed:content>...</br-feed:content>
author       → <br-feed:author>...</br-feed:author>
location     → <br-feed:location>...</br-feed:location>
source       → <br-feed:source>...</br-feed:source>
language     → <br-feed:language>...</br-feed:language>
copyright    → <br-feed:copyright>...</br-feed:copyright>
```

### Atom Compliance

Per RFC 4287 Section 6 ("Extensibility"):
> "Atom consumers MAY ignore any foreign markup in the document they don't understand."

Custom namespace elements are preserved for clients that understand them and transparent to standard Atom readers. This maintains full specification compliance.

### Default Configuration

No configuration needed! Atom feeds automatically include these enrichment fields if they exist on your documents:
- introduction
- content
- author
- location
- source
- language
- copyright

### Custom Enrichment Fields

To customize which fields are included, override the modifier in your Spring configuration:

**File:** `hst-cms-rest.xml` or your custom Spring config

```xml
<bean id="atomModifier" class="org.bloomreach.forge.feed.api.modifier.Atom10EnrichedModifier" scope="singleton">
  <property name="enrichmentFields">
    <list>
      <value>introduction</value>
      <value>content</value>
      <value>location</value>
      <value>customField1</value>
      <value>customField2</value>
    </list>
  </property>
</bean>
```

### Example: Enriched Atom Entry

```xml
<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom"
      xmlns:br-feed="http://bloomreach-forge.example.org/feed">
  <title>News Feed</title>
  <entry>
    <!-- Standard Atom fields -->
    <title>Breaking News</title>
    <author><name>John Doe</name></author>
    <updated>2024-01-27T10:00:00Z</updated>
    <id>urn:uuid:60a76c80-d399-11d9-b91C-0003939e0af6</id>
    <content>Full article text goes here...</content>

    <!-- Custom enrichment fields (extension elements) -->
    <br-feed:introduction>Brief summary of the news</br-feed:introduction>
    <br-feed:location>Amsterdam, Netherlands</br-feed:location>
    <br-feed:source>Internal News Portal</br-feed:source>
    <br-feed:language>en</br-feed:language>
    <br-feed:copyright>© 2024 Example Corp</br-feed:copyright>
  </entry>
</feed>
```

### Field Resolution

Enrichment uses Java reflection to find getter methods on document beans:

**Field name** → **Getter method** → **XML element**

```
introduction  → getIntroduction()  → <br-feed:introduction>...</br-feed:introduction>
customField1  → getCustomField1()  → <br-feed:customField1>...</br-feed:customField1>
```

### Requirements for Fields

For a field to be included:
1. The bean must have a public getter method (e.g., `getIntroduction()`)
2. The method must return a non-null value
3. The value's `.toString()` must produce a non-empty string
4. The field must be listed in the `enrichmentFields` configuration

### Missing or Null Fields

If a field is configured but not found on a document:
- **Silently skipped** - No error is thrown
- **Logged at DEBUG level** - Check logs if you expect the field to appear
- **Feed generation continues** - Other fields continue to enrich normally

### Best Practices

#### 1. Be Selective with Fields

Only include fields meaningful for feed consumers:

```xml
<!-- Good: focused set -->
<list>
  <value>introduction</value>
  <value>location</value>
</list>

<!-- Avoid: too many fields clutters the feed -->
<list>
  <value>introduction</value>
  <value>location</value>
  <value>internalId</value>
  <value>workflowStatus</value>
  <value>approvalDate</value>
  <!-- 20+ more fields -->
</list>
```

#### 2. Use Meaningful Field Names

Choose names that clearly communicate their purpose:

```
✓ introduction    (clear: brief summary)
✓ fullContent    (clear: complete text)
✗ intro          (ambiguous)
✗ data1          (meaningless)
```

#### 3. Handle HTML Content

If content fields contain HTML, note that it will be XML-escaped in the Atom feed:

```java
public String getHtmlContent() {
    return "<p>Article text with <strong>formatting</strong></p>";
}
```

Feed readers can decode it if they choose.

#### 4. Test with Feed Readers

Verify enriched Atom feeds with various readers:
- **Standard readers** should ignore unknown namespaces
- **Smart readers** may recognize and display custom fields
- **API consumers** can parse custom namespace fields

### Combining Filtering and Enrichment

Both features work together seamlessly:

```xml
<bean id="atomModifier" class="org.bloomreach.forge.feed.api.modifier.Atom10EnrichedModifier" scope="singleton">
  <property name="enrichmentFields">
    <list>
      <value>introduction</value>
      <value>content</value>
      <value>location</value>
    </list>
  </property>
</bean>

<!-- Configure filter on feed descriptor: -->
<!-- Filter by Property: status -->
<!-- Filter by Value: published -->
```

This will:
1. Only include entries where `status == "published"` (filtering)
2. Add introduction, content, and location fields (enrichment)

---

## Testing & Validation

### Build & Compile

```bash
cd /Users/josephliechty/Desktop/XM/feeds

# Build the entire project
mvn clean install -DskipTests

# Or if you want to run tests:
mvn clean install
```

### Run Unit Tests

```bash
cd demo/site/components
mvn test -Dtest=PropertyFilterModifierTest
```

Expected output:
```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Manual Testing Checklist

#### Setup

1. Create test documents with different property values:
   - Document 1: status = "published", featured = "false"
   - Document 2: status = "draft", featured = "false"
   - Document 3: status = "published", featured = "true"

2. Create feed descriptors:
   - Feed 1: No filter (baseline test)
   - Feed 2: Filter by status = "published"
   - Feed 3: Filter by status = "pub*" (wildcard test)

3. Configure sitemap items to serve the feeds

#### Test Cases

- [ ] **Baseline Test**: Feed without filter shows all documents
- [ ] **Exact Match Test**: Feed with status="published" shows only 2 items
- [ ] **Wildcard Test**: Feed with status="pub*" shows correct items
- [ ] **Case Sensitivity Test**: Filter "PUBLISHED" matches "published"
- [ ] **Missing Property Test**: Documents without property are excluded
- [ ] **Atom Enrichment Test**: Enrichment fields appear in Atom feed
- [ ] **Feed Reader Test**: Feed loads correctly in multiple readers

### Validation Steps

1. Open feed URL in browser
2. Verify XML structure is valid
3. Check item count matches expectations
4. Load in feed reader (Feedly, NewsBlur, etc.)
5. Verify content displays correctly
6. For Atom: Verify enrichment fields are present

---

## Troubleshooting

### Entries Not Being Filtered

**Symptom**: Configured filters but entries still appear in feed

**Solutions**:
1. Verify the property name is correct (case-sensitive)
2. Check that the property exists on the documents
3. Remember: matching is case-insensitive for values
4. If using wildcards, verify the pattern format (e.g., `pub*` not `pub%`)
5. Ensure both filter fields are set (leave empty to disable filtering)

### Property Not Found

**Symptom**: Filter seems to exclude everything

**Solutions**:
1. Verify the bean has a getter method for the property
2. Method name should follow Java naming: property `foo` → `getFoo()`
3. Check logs for "Property not found" debug messages

### Feed Returns No Entries

**Symptom**: All entries are filtered out

**Solutions**:
1. Review the filter value - it might not match any documents
2. Try filtering by a property that exists on all documents
3. Temporarily clear the filter to verify documents are returned

### Custom Enrichment Fields Not Appearing

**Symptom**: Configured enrichment fields don't show up in Atom feed

**Diagnosis**:
1. Check that bean has a public getter method
2. Method name must follow pattern: `get<FieldName>()`
   - Field `introduction` → method `getIntroduction()`
3. Check logs for DEBUG messages about field resolution
4. Verify field value is not null/empty

**Enable Debug Logging**:
```log4j
log4j.logger.org.bloomreach.forge.feed.api.modifier=DEBUG
```

### Namespace Not Displaying

**Symptom**: Custom fields appear but namespace prefix is wrong or missing

**Cause**: Different feed readers handle namespaces differently

**Solution**: Standard Atom readers ignore unknown namespaces. Specialized readers will properly recognize the custom fields. This is expected behavior per RFC 4287.

### Spring Configuration Errors

**Solutions**:
1. Ensure class names are fully qualified
2. Verify package names are correct
3. Check that classes are in the classpath
4. Review Spring error messages in server logs

### Compilation Errors

**Solutions**:
1. Run `mvn clean install -DskipTests` to download dependencies
2. Check that all required JARs are present
3. Verify package names and imports

---

## Additional Resources

- **README.md** - Project overview and documentation generation
- **PROPERTY_FILTERING_GUIDE.md** - Detailed filtering documentation (kept for reference)
- **ATOM_ENRICHMENT_GUIDE.md** - Detailed enrichment documentation (kept for reference)

## Support

For issues or questions:
1. Check the Troubleshooting section above
2. Enable debug logging to see detailed execution flow
3. Review server logs for error messages
4. Verify content model has required properties
5. Test with simpler cases first (single filter, no wildcards)

## Version Info

- **Feature**: FORGE-325 Implementation
- **Filtering**: Property-based filtering with wildcard support
- **Enrichment**: Atom 1.0 with custom namespace fields
- **Compatibility**: Fully backward compatible, zero breaking changes
