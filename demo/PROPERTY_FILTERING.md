# Property Filtering for Feeds

This document explains how to use property-based filtering to restrict feed items to specific document properties using the Feeds plugin.

## Overview

The Feeds plugin provides an extensible modifier pattern that allows filtering feed items based on document properties. This is useful when you want to:

- Only include published documents in a feed
- Filter by document status or workflow state
- Include only featured or promoted items
- Apply other custom property-based constraints

## Quick Start

### 1. Basic Configuration

The simplest way to add property filtering is using `PropertyFilterModifier`. Configure it in your Spring XML:

```xml
<bean id="propertyFilterModifier" class="org.example.feed.PropertyFilterModifier">
  <property name="filters">
    <map>
      <entry key="status" value="published"/>
    </map>
  </property>
</bean>

<bean id="jaxrsRSSResource" class="org.bloomreach.forge.feed.resource.RssSyndicationResource" scope="prototype">
  <property name="modifier" ref="propertyFilterModifier"/>
</bean>
```

This configuration ensures that only documents with `status` property set to `published` are included in the feed.

### 2. Using the Pre-built PublishedNewsModifier

For a common use case, use `PublishedNewsModifier`:

```xml
<bean id="publishedNewsModifier" class="org.example.feed.PublishedNewsModifier" scope="singleton"/>

<bean id="jaxrsRSSResource" class="org.bloomreach.forge.feed.resource.RssSyndicationResource" scope="prototype">
  <property name="modifier" ref="publishedNewsModifier"/>
</bean>
```

### 3. Multiple Filters

Apply multiple property filters by adding multiple entries:

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

## Advanced Usage

### Creating Custom Modifiers

Extend `PropertyFilterModifier` to create domain-specific filters:

```java
public class NewsWithImagesModifier extends PropertyFilterModifier {

    public NewsWithImagesModifier() {
        super();
        // Only show published news with images
        addFilter("status", "published");
        addFilter("hasImage", "true");
    }
}
```

Configure it the same way:

```xml
<bean id="newsWithImagesModifier" class="org.example.feed.NewsWithImagesModifier" scope="singleton"/>

<bean id="jaxrsRSSResource" class="org.bloomreach.forge.feed.resource.RssSyndicationResource" scope="prototype">
  <property name="modifier" ref="newsWithImagesModifier"/>
</bean>
```

### Using Different Modifiers for Different Feeds

You can create multiple resource beans with different modifiers:

```xml
<!-- Feed 1: All published articles -->
<bean id="jaxrsRSSResource-published"
      class="org.bloomreach.forge.feed.resource.RssSyndicationResource"
      scope="prototype">
  <property name="modifier" ref="publishedNewsModifier"/>
</bean>

<!-- Feed 2: Only featured articles -->
<bean id="jaxrsRSSResource-featured"
      class="org.bloomreach.forge.feed.resource.RssSyndicationResource"
      scope="prototype">
  <property name="modifier" ref="featuredNewsModifier"/>
</bean>
```

Then reference them differently in your sitemap configuration.

### Combining with Other Modifiers

You can also combine property filtering with other feed customizations:

```java
public class FeaturedNewsWithImages extends PropertyFilterModifier {

    @Override
    public void modifyEntry(HstRequestContext context, Item entry, HippoBean bean) {
        super.modifyEntry(context, entry, bean);

        // Add custom entry modifications
        if (bean instanceof NewsDocument) {
            NewsDocument news = (NewsDocument) bean;
            HippoGalleryImageSetBean image = news.getImage();
            if (image != null) {
                // Add image information to entry
            }
        }
    }
}
```

## Implementation Details

### How It Works

1. When a feed request is made, the HST Query executes and returns matching documents
2. For each result document, the modifier's `modifyEntry()` method is called
3. PropertyFilterModifier evaluates the document properties against filter criteria
4. Entries that match filters are included; non-matching entries are logged
5. The results are converted to feed entries

### Important Note

PropertyFilterModifier uses **entry-level filtering** (post-query) rather than query-level constraints. This approach:

**Advantages**:
- Works with all versions of Bloomreach/HST
- Doesn't depend on version-specific query constraint APIs
- Simple and reliable implementation
- Easy to understand and extend

**Considerations**:
- Filters are applied after query execution (not pre-filtered at database level)
- All matching documents are retrieved first, then evaluated
- For better performance with large result sets, consider using document type and scope filtering in the feed descriptor as first-pass filters

### Class Hierarchy

```
Modifier (interface)
  └── RSS20Modifier (default implementation)
      └── PropertyFilterModifier (property filtering)
          └── PublishedNewsModifier (pre-built example)
          └── Your custom modifiers (extend as needed)
```

### Filter Properties

The `PropertyFilterModifier` supports these operations:

| Property | Example | Description |
|----------|---------|-------------|
| status | `published` | Include documents with specific status |
| featured | `true` | Include only featured documents |
| category | `news` | Include documents from specific category |
| Custom JCR property | any value | Filter by any document property |

## Examples

### Example 1: Published News Only

```xml
<bean id="publishedModifier" class="org.example.feed.PropertyFilterModifier">
  <property name="filters">
    <map>
      <entry key="status" value="published"/>
    </map>
  </property>
</bean>
```

### Example 2: Featured Articles

```xml
<bean id="featuredModifier" class="org.example.feed.PropertyFilterModifier">
  <property name="filters">
    <map>
      <entry key="status" value="published"/>
      <entry key="featured" value="true"/>
    </map>
  </property>
</bean>
```

### Example 3: News by Category

```xml
<bean id="techNewsModifier" class="org.example.feed.PropertyFilterModifier">
  <property name="filters">
    <map>
      <entry key="status" value="published"/>
      <entry key="category" value="technology"/>
    </map>
  </property>
</bean>
```

## Troubleshooting

### Feed returns no items

1. Check that the property name matches your content model (case-sensitive)
2. Verify that documents actually have the property set to the specified value
3. Check the logs for filter application errors
4. Ensure the modifier is properly injected into the feed resource

### Property filter not working

1. Verify the bean is properly configured in Spring XML
2. Check that the property names are correct (JCR property names)
3. Ensure the feed descriptor is valid and accessible
4. Review application logs for any exceptions

### Performance issues

1. Adding indexed properties to filters improves performance
2. Limit the number of filters applied
3. Use the item count limit to restrict result set size
4. Consider using document type filtering in combination with property filters

## See Also

- [Modifier API Documentation](../hst/src/main/java/org/bloomreach/forge/feed/api/modifier/Modifier.java)
- [PropertyFilterModifier Source](./site/components/src/main/java/org/example/feed/PropertyFilterModifier.java)
- [Feeds Plugin Documentation](../README.md)
