# 🌍 Translation Feature Guide

The Story Publisher platform now includes powerful translation capabilities that allow you to translate Telugu stories to English and other languages.

## ✨ Features

### 🔍 Automatic Language Detection
- The system automatically detects if your story is written in Telugu, English, or other supported languages
- No need to manually specify the source language in most cases

### 📖 Story Translation
- Translate entire stories from Telugu to English or vice versa
- Preserve formatting and structure during translation
- Cache translations for improved performance

### 🎯 Supported Languages
- **Telugu (te)** - Primary source language
- **English (en)** - Primary target language  
- **Hindi (hi)** - Hindi language support
- **Tamil (ta)** - Tamil language support
- **Kannada (kn)** - Kannada language support
- **Malayalam (ml)** - Malayalam language support

## 🚀 How to Use

### 📱 Frontend Usage

#### 1. Story Detail Page Translation
When viewing any story, you'll find a translation section above the content:

1. **Select Target Language**: Choose your desired translation language from the dropdown
2. **Click Translate**: Hit the "🌍 Translate" button
3. **View Translation**: The translated content appears in a highlighted box
4. **Language Detection**: The system shows which language was detected

#### 2. Quick Text Translation
Use the translation component for any text content:

```tsx
import TranslationComponent from '@/components/TranslationComponent';

<TranslationComponent 
  text="ఇది ఒక తెలుగు కథ"
  onTranslationUpdate={(translated, lang) => console.log(translated)}
/>
```

### 🔧 API Usage

#### 1. Translate Text
```bash
curl -X POST http://localhost:8080/api/translations/text \
  -H "Content-Type: application/json" \
  -d '{
    "text": "ఇది ఒక తెలుగు కథ",
    "targetLanguage": "en"
  }'
```

#### 2. Translate Story
```bash
curl -X POST http://localhost:8080/api/translations/story/123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "targetLanguage": "en"
  }'
```

#### 3. Detect Language
```bash
curl -X POST http://localhost:8080/api/translations/detect \
  -H "Content-Type: application/json" \
  -d '{
    "text": "ఇది ఒక తెలుగు కథ"
  }'
```

#### 4. Get Supported Languages
```bash
curl http://localhost:8080/api/translations/languages
```

## ⚙️ Configuration

### Backend Configuration
Add these properties to your `application.properties`:

```properties
# Translation Service Configuration
app.translation.enabled=true
app.translation.provider=google
app.translation.default-source-language=te
app.translation.default-target-language=en
app.translation.supported-languages=te,en,hi,ta,kn,ml

# Cache Configuration
app.translation.cache.enabled=true
app.translation.cache.ttl=3600

# Google Translate API (Optional)
app.translation.google.api-key=YOUR_API_KEY
app.translation.google.project-id=YOUR_PROJECT_ID
```

### Environment Variables
```bash
export TRANSLATION_ENABLED=true
export GOOGLE_TRANSLATE_API_KEY=your_api_key_here
export DEFAULT_SOURCE_LANG=te
export DEFAULT_TARGET_LANG=en
```

## 🔌 Translation Providers

### Current Implementation
The system currently includes a **demonstration translation service** that:
- Detects Telugu vs English text using Unicode ranges
- Applies simple prefixes to show translation functionality
- Caches translation results for performance

### Production Integration
For production use, integrate with professional translation services:

#### Google Cloud Translation API
```java
// Add to Maven dependencies
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-translate</artifactId>
    <version>2.3.0</version>
</dependency>
```

#### Azure Translator Text API
```java
// Add to Maven dependencies
<dependency>
    <groupId>com.microsoft.azure.cognitiveservices</groupId>
    <artifactId>azure-cognitiveservices-textanalytics</artifactId>
    <version>1.0.2</version>
</dependency>
```

#### AWS Translate
```java
// Add to Maven dependencies
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>translate</artifactId>
    <version>2.17.0</version>
</dependency>
```

## 📊 Features in Detail

### 🗃️ Translation Caching
- Translations are automatically cached in the database
- Configurable TTL (Time To Live) for cache entries
- Reduces API calls and improves performance
- SHA-256 hash-based cache keys for accuracy

### 🎯 Smart Language Detection
```java
// Telugu Unicode range detection
if (text.matches(".*[\\u0C00-\\u0C7F].*")) {
    return "te"; // Telugu detected
}
return "en"; // Default to English
```

### 🔄 Batch Processing
- Translate multiple stories at once
- Queue-based processing for large translation jobs
- Progress tracking and status updates

## 🚀 Advanced Usage

### Custom Translation Pipeline
```typescript
// Frontend service usage
const translatedStory = await translationService.translateStory(storyId, 'en');
const allTranslations = await translationService.getStoryTranslations(storyId);
```

### Bulk Translation
```bash
# Translate all Telugu stories to English
curl -X POST http://localhost:8080/api/translations/bulk \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "sourceLanguage": "te",
    "targetLanguage": "en",
    "categoryId": 1
  }'
```

## 🎨 UI Components

### Translation Component Props
```typescript
interface TranslationComponentProps {
  text: string;                    // Text to translate
  onTranslationUpdate?: Function;  // Callback for translation result
  className?: string;              // CSS classes
}
```

### Language Selector
- Dropdown with language names in native scripts
- Automatic detection indicator
- Visual feedback during translation

## 📈 Performance Optimization

### Caching Strategy
- Database-level caching with expiration
- Content hash-based cache keys
- Automatic cleanup of expired entries

### Rate Limiting
- Configurable translation request limits
- User-based quotas
- API key management for external services

## 🔧 Troubleshooting

### Common Issues

#### 1. Translation Not Working
- Check if translation service is enabled in configuration
- Verify API keys for external translation services
- Check network connectivity to translation APIs

#### 2. Language Detection Issues
- Ensure sufficient text length for accurate detection
- Mix of scripts may cause detection issues
- Override detection by specifying source language

#### 3. Cache Issues
- Check database connectivity
- Verify cache TTL settings
- Clear expired translations manually if needed

### Debug Commands
```bash
# Check translation service status
curl http://localhost:8080/api/translations/languages

# Test language detection
curl -X POST http://localhost:8080/api/translations/detect -d '{"text":"test"}'

# Clear translation cache
curl -X DELETE http://localhost:8080/api/translations/cache
```

## 🌟 Future Enhancements

- **Real-time Translation**: Live translation as you type
- **Translation Quality Scoring**: Rate translation accuracy
- **Custom Dictionary**: Add domain-specific translations
- **Voice Translation**: Audio translation support
- **OCR Integration**: Translate text from images
- **Collaborative Translation**: Community-driven improvements

---

## 📞 Support

For technical support or feature requests:
- Create an issue in the project repository
- Check the API documentation at `/api/docs`
- Review translation logs in the backend console
