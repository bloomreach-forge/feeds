# Property-Based Feed Filtering Guide

## Overview

The feeds plugin now supports built-in property-based filtering for all feed types (RSS 2.0, Atom 1.0, and Generic syndication). This allows content editors to configure filters directly on the feed component without requiring custom code or application restarts.

## How It Works

When a feed is generated, the filtering modifier:
1. Reads the configured filter property name and value from the feed descriptor
2. Iterates through each document that would be included in the feed
3. Evaluates the specified property on each document
4. Excludes any entries that don't match the filter criteria
5. Returns the filtered feed

### Filtering Logic

- **Match Type**: Property value is matched against the filter pattern
- **Case Sensitivity**: Matching is **case-insensitive** (e.g., `"PUBLISHED"` matches `"published"`)
- **Wildcard Support**: Filter value can include `*` wildcard for pattern matching:
  - `published` - Exact match (case-insensitive)
  - `pub*` - Starts with "pub" (e.g., matches "published", "public")
  - `*lished` - Ends with "lished" (e.g., matches "published", "established")
  - `*ublish*` - Contains "ublish" (e.g., matches "published", "republished")
- **Missing Properties**: If a property is not found on a document, the entry is excluded
- **No Filter**: If either filter field is empty/null, no filtering is applied

## Configuration

### For RSS 2.0 Feeds

1. Create or edit an RSS feed descriptor
2. On the **right panel**, locate the filtering section:
   - **Filter by Property**: Enter the property name (e.g., `status`)
   - **Filter by Value**: Enter the expected value (e.g., `published`)
3. Save the descriptor
4. Generate the RSS feed - only matching entries will be included

### For Atom 1.0 Feeds

1. Create or edit an Atom feed descriptor
2. On the **right panel**, locate the filtering section:
   - **Filter by Property**: Enter the property name (e.g., `status`)
   - **Filter by Value**: Enter the expected value (e.g., `published`)
3. Save the descriptor
4. Generate the Atom feed - only matching entries will be included

### For Generic Syndication Feeds

1. Create or edit a Generic feed descriptor
2. On the **right panel**, locate the filtering section:
   - **Filter by Property**: Enter the property name (e.g., `status`)
   - **Filter by Value**: Enter the expected value (e.g., `published`)
3. Save the descriptor
4. Generate the feed - only matching entries will be included

## Examples

### Example 1: Filter Published Articles (Exact Match)

To show only published articles in an RSS feed:
- **Filter by Property**: `status`
- **Filter by Value**: `published`

This will include documents where `status` is "published", "PUBLISHED", or "Published" (case-insensitive).

### Example 2: Filter by Feature Flag (Exact Match)

To show only featured documents in an Atom feed:
- **Filter by Property**: `featured`
- **Filter by Value**: `true`

This will exclude any documents where the `featured` property is not `true`.

### Example 3: Filter by Location (Exact Match)

To show only documents from a specific location:
- **Filter by Property**: `location`
- **Filter by Value**: `Amsterdam`

This will only include documents where the `location` property equals `Amsterdam` (case-insensitive).

### Example 4: Filter Using Wildcards (Start Pattern)

To show documents with status starting with "publish":
- **Filter by Property**: `status`
- **Filter by Value**: `publish*`

This will include: "published", "publishing", "publisher" (and variations like "PUBLISHED", "Publishing", etc.)

### Example 5: Filter Using Wildcards (End Pattern)

To show documents with status ending with "ed":
- **Filter by Property**: `status`
- **Filter by Value**: `*ed`

This will include: "published", "archived", "reviewed" (and case variations).

### Example 6: Filter Using Wildcards (Contains Pattern)

To show documents with location containing "New":
- **Filter by Property**: `location`
- **Filter by Value**: `*new*`

This will include: "New York", "New Delhi", "Denver" (and variations like "NEW YORK", "new delhi", etc.)

## Wildcard and Case-Insensitive Matching

### Quick Reference Table

| Filter Value | Pattern Type | Example Matches | Example Non-Matches |
|---|---|---|---|
| `published` | Exact (case-insensitive) | "published", "PUBLISHED", "Published" | "publishing", "republished" |
| `pub*` | Prefix (starts with) | "published", "public", "publisher" | "republished", "unpublished" |
| `*lished` | Suffix (ends with) | "published", "established", "cherished" | "publishing", "established" |
| `*ublish*` | Contains (middle) | "published", "republished", "publishing" | "public", "publish" |
| `*pub*ed` | Complex (multiple parts) | "published", "republished", "unpublished" | "public", "publishing" |

### Case-Insensitive Matching

All filter comparisons are case-insensitive by default, making filters more flexible and user-friendly.

**Examples**:
- Filter: `published` matches property values: `published`, `PUBLISHED`, `Published`, `pUbLiShEd`
- Filter: `Amsterdam` matches property values: `amsterdam`, `AMSTERDAM`, `AmStErDaM`
- Filter: `true` matches property values: `true`, `TRUE`, `True`

This eliminates common frustrations from case mismatches and makes filters work regardless of how data is stored.

### Wildcard Pattern Matching

Use `*` (asterisk) in the filter value to match multiple values. The `*` represents zero or more characters.

#### Pattern Types

**1. Prefix Pattern** (e.g., `pub*`)
- Matches any string starting with the prefix
- Filter: `pub*` matches: "published", "public", "publisher", "publishing"

**2. Suffix Pattern** (e.g., `*lished`)
- Matches any string ending with the suffix
- Filter: `*lished` matches: "published", "established", "cherished"

**3. Contains Pattern** (e.g., `*ublish*`)
- Matches any string containing the substring
- Filter: `*ublish*` matches: "published", "republished", "publishing", "unpublished"

**4. Multiple Wildcards** (e.g., `*pub*ed`)
- Matches strings with pattern in the middle
- Filter: `*pub*ed` matches: "published", "republished", "unpublished", "republicated"

#### Common Use Cases

**Match Multiple Status Values**:
- Filter by Property: `status`
- Filter by Value: `pub*` (matches "published", "publishing", "public")

**Match Multiple Languages**:
- Filter by Property: `language`
- Filter by Value: `en*` (matches "en", "en-US", "en-GB", "english")

**Exclude Draft Versions**:
- Filter by Property: `status`
- Filter by Value: `*release` (matches "pre-release", "release", "post-release")

**Match Multiple Geographic Regions**:
- Filter by Property: `region`
- Filter by Value: `*Europe*` (matches "Western Europe", "Central Europe", "Northern Europe", etc.)

### Combining Wildcard and Case-Insensitive

These features work together seamlessly:

**Example**:
- Filter by Property: `status`
- Filter by Value: `PUB*`

This will match any of:
- "published", "publishing", "public", "publisher"
- "Published", "Publishing", "Public", "Publisher"
- "PUBLISHED", "PUBLISHING", "PUBLIC", "PUBLISHER"

## Advanced Usage

### Custom Filtering

If you need more complex filtering (multiple properties, different operators, etc.), you can create a custom modifier:

1. Extend `PropertyFilteringModifier` in the appropriate feed type:
   - `RSS20PropertyFilteringModifier` for RSS feeds
   - `Atom10PropertyFilteringModifier` for Atom feeds
   - `GenericPropertyFilteringModifier` for generic feeds

2. Override the `matchesFilter()` method to implement custom logic

3. Register your custom modifier in Spring configuration (`hst-cms-rest.xml`):
   ```xml
   <bean id="rssModifier" class="com.example.MyCustomRSSModifier" scope="singleton"/>
   ```

### Chaining Modifiers

You can extend an existing modifier to add additional functionality:

```java
public class MyEnrichedRSSModifier extends RSS20PropertyFilteringModifier {
    @Override
    public void modifyEntry(HstRequestContext context, Item entry, HippoBean bean) {
        // First apply property filtering
        super.modifyEntry(context, entry, bean);

        // Then add custom enrichment
        // ...
    }
}
```

## Technical Details

### Implementation

- **Base Class**: `PropertyFilteringModifier<K, V, E extends FeedDescriptor>`
  - Handles core filtering logic using thread-local storage
  - Called during three modifier hooks: `modifyHstQuery()`, `modifyEntry()`, and `modifyFeed()`

- **Type-Specific Classes**:
  - `RSS20PropertyFilteringModifier` - Removes filtered items from RSS Channel
  - `Atom10PropertyFilteringModifier` - Removes filtered entries from Atom Feed
  - `GenericPropertyFilteringModifier` - Removes filtered entries from SyndFeed

- **Thread Safety**: Uses `ThreadLocal` to track filtered entries per request, avoiding concurrency issues

### Performance

- Filtering happens **after** the HST query executes
- Does not reduce database queries (evaluated in memory)
- For large result sets, consider using custom HST query filtering for better performance

## Troubleshooting

### Entries Not Being Filtered

**Symptom**: Configured filters but entries still appear in feed

**Solutions**:
1. Verify the property name is correct (case-sensitive)
2. Check that the property exists on the documents
3. Remember: matching is case-insensitive, so "published" matches "PUBLISHED"
4. If using wildcards, verify the pattern format (e.g., `pub*` not `pub%`)
5. Leave both filter fields empty to disable filtering

### Property Not Found

**Symptom**: The filter seems to exclude everything

**Solutions**:
1. Verify the bean has a getter method for the property
2. Method name should follow Java naming convention: property `foo` -> `getFoo()`
3. Check logs for "Property not found" debug messages

### Feed Returns No Entries

**Symptom**: All entries are filtered out

**Solutions**:
1. Review the filter value - it might not match any documents
2. Try filtering by a property that exists on all documents
3. Temporarily clear the filter to verify documents are being returned

## Migration from Demo

If you were previously using the demo's `PropertyFilterModifier`:

### Before (Demo Implementation)
```xml
<!-- Custom modifier with hardcoded filters -->
<bean id="publishedNewsModifier" class="org.example.feed.PublishedNewsModifier"/>
<bean id="jaxrsRSSResource" class="org.bloomreach.forge.feed.resource.RssSyndicationResource">
  <property name="modifier" ref="publishedNewsModifier"/>
</bean>
```

### After (Built-in Implementation)
```
<!-- Simple configuration, filters set on feed descriptor -->
No Spring changes needed - configure filters directly in the CMS UI
```

## See Also

- [Feeds Plugin Documentation](README.md)
- [Feed Descriptor Configuration](FEED_DESCRIPTORS.md)
- [Modifier Extension Guide](MODIFIER_EXTENSION.md)
