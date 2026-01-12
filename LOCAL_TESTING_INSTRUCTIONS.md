# Local Testing Instructions for PropertyFilterModifier

## Overview

This guide walks you through testing the property filtering implementation in your local Bloomreach XM environment.

**Note on Implementation**: PropertyFilterModifier has been refactored to filter at the entry level (in modifyEntry()) rather than at the query level. This ensures compatibility with all versions of Bloomreach/HST, as it doesn't depend on specific HST Query constraint APIs.

## Step 1: Build the Project

```bash
cd /Users/josephliechty/Desktop/XM/feeds

# Build the entire project
mvn clean install -DskipTests

# Or if you want to run tests immediately:
mvn clean install
```

This will:
- Compile all modules
- Create the JAR with PropertyFilterModifier and PublishedNewsModifier
- Package the demo application

Expected output: `BUILD SUCCESS`

## Step 2: Verify Code Compiles

The demo package should compile successfully. Check for any compilation errors related to the new classes.

```bash
# Just compile the demo components module
cd demo/site/components
mvn clean compile
```

If there are import errors, verify:
- All dependencies are available (hst-api, rome, etc.)
- No typos in class names or package names

## Step 3: Run Unit Tests

Run the PropertyFilterModifier unit tests to verify the implementation is correct:

```bash
cd /Users/josephliechty/Desktop/XM/feeds/demo/site/components

mvn test -Dtest=PropertyFilterModifierTest -X
```

Expected output:
```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**What the tests verify:**
- ✅ Filter addition and retrieval
- ✅ Multiple filters support
- ✅ Filter clearing
- ✅ Filter objects with operators
- ✅ PublishedNewsModifier initialization
- ✅ All FilterOperator enum values
- ✅ String representation of filters

If tests fail, check:
- Java version compatibility
- JUnit dependency in pom.xml
- Class imports

## Step 4: Deploy to Local Bloomreach XM

Build the full demo application:

```bash
cd /Users/josephliechty/Desktop/XM/feeds/demo

# Build the complete demo
mvn clean install -DskipTests
```

This creates the CMS and site applications ready for deployment.

## Step 5: Start Bloomreach XM

If using Docker or local Tomcat:

```bash
# If using Docker Compose (if available)
docker-compose up

# Or start Tomcat manually
# Access: http://localhost:8080/cms and http://localhost:8080/site
```

Wait for both applications to fully start (check logs for "Server startup").

## Step 6: Create Test Content

### 6a. Create Test Folder

1. Open CMS: http://localhost:8080/cms
2. Navigate to **Content** → **Documents**
3. Create new folder: `test-feeds`

### 6b. Create Test News Documents

Create 3 news documents with different properties:

**Document 1: Published News**
- Name: `published-article`
- Type: `News Document` (feedsdemo:newsdocument)
- Title: "Published Article"
- Introduction: "This is a published article"
- Date: Today
- Status/Properties: Set `status` property to `published` (if available in your content model)

**Document 2: Draft News**
- Name: `draft-article`
- Type: `News Document`
- Title: "Draft Article"
- Introduction: "This is a draft article"
- Date: Today
- Status: Set to `draft` (or leave unset)

**Document 3: Featured Article**
- Name: `featured-article`
- Type: `News Document`
- Title: "Featured Article"
- Introduction: "This is a featured article"
- Date: Today
- Status: Set to `published`
- Featured: Set to `true` (if available)

### 6c. Publish Documents

Publish all three documents so they're available in the site.

## Step 7: Create Test Feed Descriptors

### 7a. Create First Feed (All News - No Filter)

1. Create new document: `all-news-feed`
2. Type: `RSS 2.0 Feed` (feed:rss20descriptor)
3. Configure:
   - **Title**: "All News Feed"
   - **Description**: "All news items"
   - **Scope**: `test-feeds`
   - **Document Type**: `feedsdemo:newsdocument`
   - **Item Count**: `10`

Save and publish this document.

### 7b. Create Second Feed (Published Only - With Filter)

1. Create new document: `published-news-feed`
2. Type: `RSS 2.0 Feed`
3. Configure same as above but with a different name:
   - **Title**: "Published News Feed"
   - **Description**: "Only published news items"

Save and publish this document.

## Step 8: Create Sitemap Items

1. Open **Site** configuration (CMS → Tools → Site Manager → Channels/Mounts)
2. Navigate to **Sitemap**
3. Add two new items under `/feed/`:

**Item 1: All News (No Filter)**
```
- Path: /feed/all.xml
- Related Content: /content/documents/test-feeds/all-news-feed
- JAX-RS Resource: Default RSS resource
```

**Item 2: Published Only (With Filter)**
```
- Path: /feed/published.xml
- Related Content: /content/documents/test-feeds/published-news-feed
- JAX-RS Resource: RSS with PropertyFilterModifier (after Step 9)
```

## Step 9: Enable PropertyFilterModifier

1. Edit the Spring configuration file:
   ```
   demo/site/components/src/main/resources/META-INF/hst-assembly/overrides/hippo-essentials-spring.xml
   ```

2. Uncomment the PublishedNewsModifier bean usage (around line 57):
   ```xml
   <bean id="jaxrsRSSResource" class="org.bloomreach.forge.feed.resource.RssSyndicationResource" scope="prototype">
     <property name="modifier" ref="publishedNewsModifier"/>
   </bean>
   ```

3. Rebuild and redeploy:
   ```bash
   cd /Users/josephliechty/Desktop/XM/feeds/demo/site/webapp
   mvn clean install -DskipTests
   ```

4. Restart the site application or wait for hot deployment

## Step 10: Test the Feeds

### Test 1: Feed Without Filter (Baseline)

Open in browser: http://localhost:8080/site/feed/all.xml

**Expected result:**
- Feed shows all 3 news items (published-article, draft-article, featured-article)
- All titles and descriptions visible
- No filtering applied

**Verify:**
- See `<item>` tags for all 3 articles
- Check item count in feed

### Test 2: Feed With PropertyFilterModifier

Open in browser: http://localhost:8080/site/feed/published.xml

**Expected result:**
- Feed shows only 2 items (published-article, featured-article)
- Draft article is NOT visible
- Only documents with status="published" appear

**Verify:**
- See `<item>` tags for 2 articles only
- Draft article missing from feed
- Published articles are present

**If this works correctly:** ✅ PropertyFilterModifier is functioning!

## Step 11: Test Multiple Filters

(Advanced - Optional)

1. Edit Spring config to use PropertyFilterModifier with multiple filters:
   ```xml
   <bean id="propertyFilterModifier" class="org.example.feed.PropertyFilterModifier">
     <property name="filters">
       <map>
         <entry key="status" value="published"/>
         <entry key="featured" value="true"/>
       </map>
     </property>
   </bean>
   ```

2. Create a new sitemap item `/feed/featured.xml` pointing to a descriptor with this modifier

3. Open: http://localhost:8080/site/feed/featured.xml

**Expected result:**
- Only 1 item (featured-article)
- Both published-article and draft-article are filtered out
- Only documents matching BOTH filters appear

## Step 12: Check Logs

If anything doesn't work as expected, check the server logs:

```bash
# If using Docker
docker logs -f <container-name>

# Or if running Tomcat locally, check:
# TOMCAT_HOME/logs/catalina.out
# TOMCAT_HOME/logs/localhost-YYYY-MM-DD.log
```

Look for:
- PropertyFilterModifier debug messages
- Filter application logs
- Any stack traces

## Troubleshooting

### Issue: Feed shows all items (filter not applied)

**Cause**: Modifier not properly injected or sitemap not pointing to custom resource

**Fix**:
1. Verify Spring bean definition is uncommented
2. Check sitemap item configuration
3. Restart application
4. Check logs for errors

### Issue: Feed shows no items

**Cause**: Filter too restrictive or property names don't match

**Fix**:
1. Check property names in your content model
2. Verify test documents have properties set correctly
3. Try without any filters first
4. Check logs for "Failed to apply filter" messages

### Issue: Compilation errors

**Cause**: Missing dependencies or incorrect imports

**Fix**:
1. Run `mvn clean install -DskipTests` to download dependencies
2. Check that all required JARs are present
3. Verify package names match

### Issue: Tests don't run

**Cause**: Maven configuration or test framework issue

**Fix**:
```bash
# Clear Maven cache and rebuild
rm -rf ~/.m2/repository/org/bloomreach
cd /Users/josephliechty/Desktop/XM/feeds
mvn clean install -U
```

## Success Criteria

You'll know the implementation is working when:

✅ Unit tests pass (11/11)
✅ Code compiles without errors
✅ Feed without filter shows 3 items
✅ Feed with published filter shows 2 items
✅ Feed with multiple filters shows 1 item
✅ No errors in application logs
✅ Modifier methods are called (check logs or add debugging)

## Next Steps After Testing

If all tests pass:
1. Proceed with code review
2. Test with your own content model/properties
3. Create custom modifiers for your use cases
4. Commit the changes: `git add . && git commit -m "FORGE-325: Add property filtering..."`

If issues found:
1. Check logs and error messages carefully
2. Verify content model has the properties you're filtering on
3. Try simpler test case (single filter first)
4. Review PropertyFilterModifier.java for potential issues
5. Add debug logging to trace execution

## Tips for Success

1. **Start simple**: Test basic filtering before multiple filters
2. **Check property names**: Ensure they match your JCR property names exactly
3. **Verify publication**: Make sure test documents are published
4. **Monitor logs**: Keep application logs open during testing
5. **Clear cache**: If seeing stale results, clear browser cache and restart app
