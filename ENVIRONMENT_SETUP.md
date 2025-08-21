# Environment Configuration

## Gmail SMTP Setup

To enable email functionality, you need to set up environment variables or create a local properties file.

### Option 1: Environment Variables

Set the following environment variables:

```bash
export GMAIL_USERNAME=your-email@gmail.com
export GMAIL_APP_PASSWORD=your-16-character-app-password
export MAIL_ENABLED=true
export MAIL_FROM=your-email@gmail.com
export FRONTEND_URL=http://localhost:6001
```

### Option 2: Local Properties File

Create `backend/src/main/resources/application-local.properties`:

```properties
# Gmail SMTP Configuration
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-character-app-password

# Email Service Configuration
app.mail.enabled=true
app.mail.from=your-email@gmail.com
app.frontend.url=http://localhost:6001
```

Then run the application with the local profile:
```bash
java -jar target/story-publisher-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### Getting Gmail App Password

1. Enable 2-Factor Authentication on your Gmail account
2. Go to Google Account settings: https://myaccount.google.com/
3. Navigate to Security → 2-Step Verification → App passwords
4. Generate a new app password for "Mail"
5. Use the 16-character password in your configuration

For detailed instructions, see [GMAIL_SETUP.md](../GMAIL_SETUP.md)

## Database Configuration

Make sure PostgreSQL is running and the database exists:

```bash
createdb storypublisher
```

The application will auto-create the tables on startup.
