# Property Filter Testing Guide

This guide explains how to test the PropertyFilterModifier implementation.

## Prerequisites

- Bloomreach XM running
- Feeds plugin installed
- Demo project deployed
- Test news documents created

## Manual Testing Steps

### Step 1: Create Test Documents

1. In the CMS, create a test folder: `/content/documents/test-feeds`
2. Create several news documents with different statuses:
   - `news-published.xml`: status = "published", featured = "false"
   - `news-draft.xml`: status = "draft", featured = "false"
   - `news-featured.xml`: status = "published", featured = "true"

### Step 2: Configure Feed Without Filter

1. Create an RSS feed descriptor: `/content/documents/test-feeds/all-news-rss`
2. Configure it with:
   - Document Type: `feedsdemo:newsdocument`
   - Scope: `/content/documents/test-feeds`
   - Item Count: 10

3. Add feed sitemap item:
   - Path: `/feed/test/all.xml`
   - Related Content: `all-news-rss`

4. Visit `http://localhost:8080/site/feed/test/all.xml`
   - Expected: All 3 news documents appear in feed

### Step 3: Enable Property Filtering

1. Edit `demo/site/components/src/main/resources/META-INF/hst-assembly/overrides/hippo-essentials-spring.xml`

2. Uncomment and modify the RSS resource bean:
   ```xml
   <bean id="jaxrsRSSResource" class="org.bloomreach.forge.feed.resource.RssSyndicationResource" scope="prototype">
     <property name="modifier" ref="publishedNewsModifier"/>
   </bean>
   ```

3. Rebuild and redeploy the demo project

4. Visit `http://localhost:8080/site/feed/test/all.xml` again
   - Expected: Only published items appear (news-published.xml and news-featured.xml)
   - Draft document is filtered out

### Step 4: Test Multiple Filters

1. Edit the Spring configuration to use custom modifier with multiple filters:
   ```xml
   <bean id="propertyFilterModifier" class="org.example.feed.PropertyFilterModifier">
     <property name="filters">
       <map>
         <entry key="status" value="published"/>
         <entry key="featured" value="true"/>
       </map>
     </property>
   </bean>

   <bean id="jaxrsRSSResource" class="org.bloomreach.forge.feed.resource.RssSyndicationResource" scope="prototype">
     <property name="modifier" ref="propertyFilterModifier"/>
   </bean>
   ```

2. Rebuild and redeploy

3. Visit `http://localhost:8080/site/feed/test/all.xml`
   - Expected: Only featured published items appear (news-featured.xml)
   - Both draft and non-featured items are filtered out

## Automated Unit Tests

Run the unit tests to verify core functionality:

```bash
cd demo/site/components
mvn test -Dtest=PropertyFilterModifierTest
```

Expected output:
- 11 tests executed
- All tests passed
- Test coverage includes:
  - Filter addition and removal
  - Multiple filter support
  - Filter operators
  - PublishedNewsModifier functionality

## Code Review Checklist

- [ ] PropertyFilterModifier class structure is sound
- [ ] PublishedNewsModifier extends correctly
- [ ] Spring configuration examples are valid XML
- [ ] Documentation is comprehensive
- [ ] Unit tests cover all public methods
- [ ] No breaking changes to existing APIs
- [ ] Backward compatibility maintained

## Expected Behavior

| Scenario | Modifier | Expected Result |
|----------|----------|-----------------|
| No modifier | None | All documents matching document type |
| PublishedNewsModifier | Published | Only documents with status="published" |
| Multiple filters | Published + Featured | Only published AND featured documents |
| Non-existent property | PropertyFilterModifier | All documents (filter silently ignored) |

## Troubleshooting

### Feed returns no items

1. Check that property names match your content model
2. Verify documents have the properties set correctly
3. Check server logs for filter errors
4. Verify feed descriptor document type is correct

### Spring configuration errors

1. Ensure class names are fully qualified
2. Verify package names are correct
3. Check that classes are in the classpath
4. Review Spring error messages in server logs

### Performance issues

1. Ensure filtered properties are JCR indexed
2. Limit number of filters used
3. Use item count limit to reduce result set
4. Monitor server logs for query execution time

## Next Steps After Testing

1. If tests pass, commit the changes
2. Update release notes
3. Create user documentation
4. Announce feature availability
5. Gather user feedback

## Additional Testing

### Property Type Testing

Test with different property types:
- String: status, category
- Boolean: featured, published
- Date: publishDate, lastModified
- Long: viewCount, priority

### Load Testing

Test with large result sets:
- Create 1000+ documents
- Apply filters
- Monitor performance
- Check memory usage

### Integration Testing

Test with other modifiers:
- Combine PropertyFilterModifier with ImageModifier
- Stack multiple custom modifiers
- Test modifier execution order
