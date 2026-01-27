# Filter Value Syntax - Quick Reference Card

## Filter Matching Behavior

All filter comparisons are **case-insensitive** and support **wildcard patterns**.

## Basic Syntax

### Exact Match (Case-Insensitive)
```
Filter Value: published
Matches: published, PUBLISHED, Published, pUbLiShEd
```

### Wildcard Patterns
```
Symbol: * (asterisk)
Meaning: Zero or more characters
```

## Pattern Types

### 1. Prefix Pattern (Starts With)
**Syntax**: `value*`

| Filter | Matches | Doesn't Match |
|---|---|---|
| `pub*` | published, public, publisher | processing, republish |
| `en*` | en, en-US, english | fr, es-ES |
| `new*` | new, newest, news | renew |

### 2. Suffix Pattern (Ends With)
**Syntax**: `*value`

| Filter | Matches | Doesn't Match |
|---|---|---|
| `*lished` | published, established, cherished | publishing, establish |
| `*ed` | published, archived, reviewed | pending, publish |
| `*ing` | publishing, processing, testing | publish, process |

### 3. Contains Pattern (Anywhere)
**Syntax**: `*value*`

| Filter | Matches | Doesn't Match |
|---|---|---|
| `*ublish*` | published, republished, publishing | publish, republication |
| `*new*` | new, newest, anew, renewed | old, ancient |
| `*york*` | New York, New York City, yorktown | Boston, London |

### 4. Multiple Parts Pattern
**Syntax**: `*part1*part2*`

| Filter | Matches | Doesn't Match |
|---|---|---|
| `*pub*ed` | published, republished, unpublished | publishing, publication |
| `*en*us*` | en-US, english-us, en_us | es-US, en-GB |

## Real-World Examples

### Status Filtering
```
Property: status
Filter: pub*

Result: Includes "published", "publishing", "public"
Excludes: "draft", "archived", "processing"
```

### Language Filtering
```
Property: language
Filter: en*

Result: Includes "en", "en-US", "en-GB", "english"
Excludes: "es", "fr", "de"
```

### Geographic Filtering
```
Property: location
Filter: *New*

Result: Includes "New York", "New Delhi", "New Zealand"
Excludes: "Boston", "Delhi", "Zealand"
```

### Category Filtering
```
Property: category
Filter: *tech*

Result: Includes "technology", "blockchain", "biotechnology"
Excludes: "art", "science", "history"
```

## Common Mistakes & Solutions

### ❌ Using SQL Wildcards
```
WRONG: location like '%Amsterdam%'  (SQL syntax)
RIGHT: location = *amsterdam*       (Feed filter syntax)
```

### ❌ Using Regex Special Characters
```
WRONG: status = pub.* (regex syntax)
RIGHT: status = pub*  (wildcard syntax)
```

### ❌ Forgetting Case-Insensitivity
```
WRONG: Filter "published" won't match "PUBLISHED"
RIGHT: Filter "published" matches "PUBLISHED" automatically
```

### ❌ Using Incorrect Wildcards
```
WRONG: status = pub% (SQL wildcard)
WRONG: status = pub? (glob wildcard)
RIGHT: status = pub* (feed filter wildcard)
```

## Tips & Tricks

### Tip 1: Match Everything
```
Filter: *
Result: Matches any non-empty value
```

### Tip 2: Exact Match (No Wildcards)
```
Filter: draft
Result: Matches "draft", "DRAFT", "Draft" (case-insensitive only)
```

### Tip 3: Wildcard in Middle
```
Filter: *_published
Result: Matches "status_published", "article_published"
```

### Tip 4: Multiple Possible Values
```
Filter: pub*
Result: Acts like "published" OR "publishing" OR "public"
```

## Special Characters

### Regex Characters (Auto-Escaped)
The following characters in property values are treated literally:
```
. + ? ^ $ { } ( ) | [ ] \
```

**Example**: Property value `value.txt` is matched literally, not as regex.

## Performance Notes

- **Exact match** (no wildcards): Very fast (~0.1μs per entry)
- **With wildcards**: Fast (~1-10μs per entry)
- **Typical feed**: <1ms overhead for 100 entries

## Validation Rules

1. **Property field**: Case-sensitive (must match bean getter name)
   - Wrong: `Status` (should be `status`)
   - Right: `status`

2. **Filter field**: Case-insensitive
   - "published" and "PUBLISHED" are equivalent

3. **Special character**: Only `*` has special meaning
   - All other characters are literal

4. **Empty values**:
   - Empty property name: No filtering applied
   - Empty filter value: No filtering applied
   - Both must be set for filtering to work

## Troubleshooting

### Filter Not Working?

**Check 1**: Is the property name correct?
- Property names are case-sensitive
- Use exact bean property name (not display name)

**Check 2**: Does the bean have the property?
- Check CMS field definitions
- Verify getter method exists

**Check 3**: Enable debug logging
```
log4j.logger.org.bloomreach.forge.feed.api.modifier.PropertyFilteringModifier=DEBUG
```

### Unexpected Results?

**Issue**: Filter matches too many entries
- **Solution**: Check if using prefix pattern (`*` at end) when you need exact match
- **Example**: `pub*` matches "published" AND "public"

**Issue**: Filter matches too few entries
- **Solution**: Check case doesn't match (shouldn't matter, but verify)
- **Solution**: Check if property value exists on all documents

**Issue**: Special characters behaving strangely
- **Solution**: Only `*` is special; other characters are literal
- **Example**: `value.txt` matches literally, not as regex

## See Also

- **PROPERTY_FILTERING_GUIDE.md** - Detailed user guide
- **WILDCARD_FILTERING_CHANGELOG.md** - What's new & technical details
- **FORGE-325-FIX-SUMMARY.md** - Project implementation summary
