# Gmail SMTP Setup for Story Publisher

## 🔧 How to Configure Gmail for Sending Password Reset Emails

### Step 1: Enable 2-Factor Authentication
1. Go to your [Google Account settings](https://myaccount.google.com/)
2. Click on "Security" in the left sidebar
3. Under "Signing in to Google", enable "2-Step Verification"

### Step 2: Generate App Password
1. In Google Account settings → Security
2. Under "Signing in to Google", click "App passwords"
3. Select "Mail" as the app and "Other" as the device
4. Enter "Story Publisher" as the custom name
5. Click "Generate"
6. **Copy the 16-character app password** (something like: `abcd efgh ijkl mnop`)

### Step 3: Update Application Configuration
Edit `backend/src/main/resources/application.properties`:

```properties
# Replace these values with your actual Gmail credentials:
spring.mail.username=your-actual-email@gmail.com
spring.mail.password=your-app-password-from-step-2

# Replace with your actual email for the "from" address:
app.mail.from=your-actual-email@gmail.com
```

### Step 4: Test Configuration
1. Update the properties file with your credentials
2. Restart the backend
3. Test forgot password functionality
4. Check that real emails are sent to the recipient

## 🚨 Security Notes
- **Never commit your actual email credentials to Git**
- Use environment variables for production:
  ```bash
  export SPRING_MAIL_USERNAME=your-email@gmail.com
  export SPRING_MAIL_PASSWORD=your-app-password
  ```
- The app password is different from your Gmail password
- Keep your app password secure and don't share it

## 🧪 Testing
- Test with a real email address you own
- Check spam folder if emails don't appear in inbox
- Backend logs will show success/failure status

## 🔄 To Switch Back to Mock Mode
Set `app.mail.enabled=false` in application.properties to return to development/mock mode.
