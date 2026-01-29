# Quick Reference Card

## Filter Syntax at a Glance

### Exact Match (Case-Insensitive)
```
Filter: published
Matches: published, PUBLISHED, Published
```

### Wildcard Patterns (Use *)
```
pub*       → starts with (published, public, publisher)
*ed        → ends with (published, archived, reviewed)
*ublish*   → contains (published, republished, publishing)
*pub*ed    → complex (published, republished)
```

## Common Filter Configurations

| Use Case | Property | Value | Result |
|----------|----------|-------|--------|
| Published only | `status` | `published` | Only published docs |
| Multiple statuses | `status` | `pub*` | published, public, etc |
| Featured items | `featured` | `true` | Only featured docs |
| Language filter | `language` | `en*` | en, en-US, en-GB, etc |
| Geographic filter | `location` | `*new*` | Contains "new" |

## Configuration Steps

1. Open feed descriptor (RSS, Atom, or Generic)
2. Fill in filter fields:
   - **Filter by Property**: `status` (or your property name)
   - **Filter by Value**: `published` (or your value/pattern)
3. Save and publish
4. Feed will now show only matching items

## Enrichment Fields

Atom feeds automatically include (if available):
- `introduction` - Brief summary
- `content` - Full article body
- `location` - Geographic location
- `author` - Document author
- `language` - Document language
- `source` - Original source
- `copyright` - Copyright info

## Testing Quick Checklist

```
□ Build project: mvn clean install
□ Run tests: mvn test -Dtest=PropertyFilterModifierTest
□ Create test documents with different property values
□ Create feeds with and without filters
□ Verify unfiltered feed shows all items
□ Verify filtered feed shows only matching items
□ Test wildcard patterns
□ Verify Atom enrichment fields appear
□ Load feed in feed reader (Feedly, etc)
```

## Common Issues

| Problem | Solution |
|---------|----------|
| Filter not working | Check property name (case-sensitive) |
| Too many/few results | Verify filter value matches your data |
| Case mismatch (old) | Now case-insensitive by default |
| Enrichment fields missing | Verify bean has getter method |
| No results in feed | Check documents have the property |

## Troubleshooting Workflow

1. **Enable debug logging**:
   ```
   log4j.logger.org.bloomreach.forge.feed.api.modifier=DEBUG
   ```

2. **Check server logs** for error messages

3. **Verify content model**:
   - Property exists on documents
   - Getter method follows naming convention (get<PropertyName>)
   - Property value is not null/empty

4. **Test incrementally**:
   - Start with no filters (baseline)
   - Add single filter
   - Test exact match first
   - Then test wildcards

## Atom Feed Enrichment Configuration

Default (no config needed):
```
All standard fields automatically included
```

Custom enrichment fields:
```xml
<bean id="atomModifier" class="org.bloomreach.forge.feed.api.modifier.Atom10EnrichedModifier">
  <property name="enrichmentFields">
    <list>
      <value>introduction</value>
      <value>location</value>
      <value>customField</value>
    </list>
  </property>
</bean>
```

## Build & Test Commands

```bash
# Build entire project
mvn clean install -DskipTests

# Run tests
cd demo/site/components
mvn test -Dtest=PropertyFilterModifierTest

# Build demo for deployment
cd demo
mvn clean install
```

## Useful Endpoints

```
http://localhost:8080/site/feed/all.xml              # Feed URL
http://localhost:8080/cms                            # CMS Admin
http://localhost:8080/site                           # Site
```

## Property Naming Convention

For property → getter method mapping:

```
Property    →  Getter Method
status      →  getStatus()
featured    →  getFeatured()
location    →  getLocation()
myField     →  getMyField()
```

## For More Details

- **Full User Guide**: See `USER_GUIDE.md`
- **Property Filtering**: USER_GUIDE.md § Property-Based Feed Filtering
- **Atom Enrichment**: USER_GUIDE.md § Atom Feed Enrichment
- **Testing**: USER_GUIDE.md § Testing & Validation
- **Troubleshooting**: USER_GUIDE.md § Troubleshooting

## Feature Summary

✅ **Property Filtering** - Filter feeds by document properties
✅ **Wildcard Support** - Use * for flexible pattern matching
✅ **Case Insensitive** - "published" matches "PUBLISHED"
✅ **Atom Enrichment** - Add custom fields to Atom feeds
✅ **Zero Code** - Configure everything via CMS UI
✅ **Fully Compatible** - No breaking changes, backward compatible
