# Story Categorization Configuration Guide

The StoryPublisher platform automatically categorizes stories based on their content length (character count). This guide explains how to customize the categorization system.

## Configuration Overview

The categorization system is highly configurable through Spring Boot properties. You can customize:
- **Thresholds**: Character count limits for each category
- **Names**: What each category is called
- **Descriptions**: Category descriptions (with dynamic placeholders)
- **Colors**: Visual color coding for each category

## Default Configuration

By default, stories are categorized as follows:
- **Short Stories**: < 300 characters
- **Medium Stories**: 300-750 characters  
- **Long Stories**: 750-1500 characters
- **Dramas**: 1500+ characters

## Configuration Files

### 1. application.properties
Add these properties to your `backend/src/main/resources/application.properties` file:

```properties
# Category thresholds
app.categories.thresholds.shortMax=300
app.categories.thresholds.mediumMax=750
app.categories.thresholds.longMax=1500

# Short category
app.categories.short.name=Short Stories
app.categories.short.description=Stories under {0} characters
app.categories.short.color=#FF6B6B

# Medium category
app.categories.medium.name=Medium Stories
app.categories.medium.description=Stories between {0}-{1} characters
app.categories.medium.color=#4ECDC4

# Long category
app.categories.long.name=Long Stories
app.categories.long.description=Stories between {0}-{1} characters
app.categories.long.color=#45B7D1

# Drama category
app.categories.drama.name=Dramas
app.categories.drama.description=Stories over {0} characters
app.categories.drama.color=#FFA726
```

### 2. Environment Variables
You can also use environment variables (useful for Docker/production):

```bash
# Thresholds
export CATEGORY_SHORT_MAX=300
export CATEGORY_MEDIUM_MAX=750
export CATEGORY_LONG_MAX=1500

# Category names
export CATEGORY_SHORT_NAME="Short Stories"
export CATEGORY_MEDIUM_NAME="Medium Stories"
export CATEGORY_LONG_NAME="Long Stories"
export CATEGORY_DRAMA_NAME="Dramas"

# Category descriptions
export CATEGORY_SHORT_DESC="Stories under {0} characters"
export CATEGORY_MEDIUM_DESC="Stories between {0}-{1} characters"
export CATEGORY_LONG_DESC="Stories between {0}-{1} characters"
export CATEGORY_DRAMA_DESC="Stories over {0} characters"

# Category colors
export CATEGORY_SHORT_COLOR="#FF6B6B"
export CATEGORY_MEDIUM_COLOR="#4ECDC4"
export CATEGORY_LONG_COLOR="#45B7D1"
export CATEGORY_DRAMA_COLOR="#FFA726"
```

## Customization Examples

### Example 1: Blog-Style Categorization
```properties
app.categories.thresholds.shortMax=500
app.categories.thresholds.mediumMax=1200
app.categories.thresholds.longMax=2500

app.categories.short.name=Quick Reads
app.categories.medium.name=Blog Posts
app.categories.long.name=Articles
app.categories.drama.name=Long-form Content
```

### Example 2: Creative Writing Focus
```properties
app.categories.thresholds.shortMax=150
app.categories.thresholds.mediumMax=500
app.categories.thresholds.longMax=2000

app.categories.short.name=Flash Fiction
app.categories.medium.name=Short Stories
app.categories.long.name=Novelettes
app.categories.drama.name=Novellas
```

### Example 3: Social Media Style
```properties
app.categories.thresholds.shortMax=280
app.categories.thresholds.mediumMax=1000
app.categories.thresholds.longMax=3000

app.categories.short.name=Posts
app.categories.medium.name=Threads
app.categories.long.name=Articles
app.categories.drama.name=Essays
```

## Description Placeholders

Descriptions support dynamic placeholders:
- `{0}` - First threshold value
- `{1}` - Second threshold value (for ranges)

Examples:
- `"Stories under {0} characters"` → `"Stories under 300 characters"`
- `"Stories between {0}-{1} characters"` → `"Stories between 300-750 characters"`

## Color Format

Colors should be in hex format: `#RRGGBB`

Popular color schemes:
- **Warm**: `#FF6B6B`, `#FFA726`, `#FFD93D`, `#6BCF7F`
- **Cool**: `#4ECDC4`, `#45B7D1`, `#96CEB4`, `#FFEAA7`
- **Professional**: `#2C3E50`, `#34495E`, `#7F8C8D`, `#BDC3C7`

## How It Works

1. **Automatic Categorization**: When a story is created or updated, the system automatically calculates the character count of the content
2. **Threshold Matching**: The system compares the character count against the configured thresholds
3. **Category Assignment**: The story is assigned to the appropriate category
4. **Database Updates**: Existing stories are automatically recategorized when thresholds change

## Applying Changes

After modifying the configuration:

1. **Restart the backend server**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Categories are automatically updated** on startup
3. **Existing stories are recategorized** based on new thresholds

## File Locations

- **Configuration Class**: `backend/src/main/java/com/storypublisher/config/CategoryConfig.java`
- **Service Implementation**: `backend/src/main/java/com/storypublisher/service/CategoryService.java`
- **Application Properties**: `backend/src/main/resources/application.properties`
- **Example Config**: `backend/src/main/resources/category-config-example.properties`

## API Endpoints

- **Get Categories**: `GET /api/categories`
- **Get Stories by Category**: `GET /api/stories/category/{categorySlug}`
- **Get All Stories**: `GET /api/stories` (includes category information)

## Troubleshooting

### Issue: Changes not applied
**Solution**: Restart the backend server after changing configuration

### Issue: Categories not showing in frontend
**Solution**: Check that the frontend is fetching from the correct API endpoint

### Issue: Invalid color format
**Solution**: Ensure colors are in hex format `#RRGGBB`

### Issue: Description placeholders not working
**Solution**: Use `{0}`, `{1}` syntax and ensure MessageFormat is properly configured

## Advanced Configuration

For more advanced customization, you can modify:
- `CategoryConfig.java` - Add new configuration properties
- `CategoryService.java` - Modify categorization logic
- `DataInitializer.java` - Change initialization behavior

The system is designed to be flexible and can be extended to support additional categorization criteria beyond character count.
