# Email Configuration Guide for Story Publisher

## 📧 Setting Up Real Email for Password Reset

The application supports multiple email configurations:

### 🔧 **Current Setup (Development Mode)**
- **Status**: Mock email (logs to console)
- **Location**: Backend logs will show reset links
- **Good for**: Development and testing

### 📮 **Option 1: Gmail SMTP (Recommended)**

#### Prerequisites:
1. A Gmail account
2. 2-Factor Authentication enabled
3. App-specific password generated

#### Steps:

1. **Enable 2FA on your Gmail account**:
   - Go to [Google Account Settings](https://myaccount.google.com)
   - Security → 2-Step Verification → Turn On

2. **Generate App Password**:
   - Go to [App Passwords](https://myaccount.google.com/apppasswords)
   - Select app: "Mail" 
   - Select device: "Other (custom name)" → "Story Publisher"
   - Copy the 16-character password

3. **Update application.properties**:
   ```properties
   # Enable real email
   app.mail.enabled=true
   app.mail.from=your-email@gmail.com
   
   # Gmail SMTP Configuration
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-16-character-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

#### Example Configuration:
```properties
app.mail.enabled=true
app.mail.from=storypublisher.app@gmail.com

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=storypublisher.app@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 🌐 **Option 2: SendGrid (Production)**

1. Sign up at [SendGrid](https://sendgrid.com)
2. Create API key
3. Update configuration:
   ```properties
   app.mail.enabled=true
   app.mail.from=noreply@yourdomain.com
   
   spring.mail.host=smtp.sendgrid.net
   spring.mail.port=587
   spring.mail.username=apikey
   spring.mail.password=your-sendgrid-api-key
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

### ☁️ **Option 3: AWS SES (Enterprise)**

1. Set up AWS SES
2. Verify domain/email
3. Update configuration:
   ```properties
   app.mail.enabled=true
   
   spring.mail.host=email-smtp.us-east-1.amazonaws.com
   spring.mail.port=587
   spring.mail.username=your-aws-access-key
   spring.mail.password=your-aws-secret-key
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

## 🧪 **Testing Email Setup**

After configuration, test the forgot password feature:

1. Start the application
2. Go to http://localhost:6001/forgot-password
3. Enter a valid email address
4. Check your email inbox

## 🔍 **Troubleshooting**

### Common Issues:

1. **"Authentication failed"**:
   - Ensure 2FA is enabled
   - Use app-specific password, not regular password
   - Check username/password accuracy

2. **"Connection timed out"**:
   - Check firewall settings
   - Verify SMTP host and port

3. **"Email not received"**:
   - Check spam/junk folder
   - Verify email address is correct
   - Check application logs for errors

### Debug Mode:
Add this to see detailed email logs:
```properties
logging.level.org.springframework.mail=DEBUG
logging.level.com.storypublisher.service.EmailService=DEBUG
```

## 🛡️ **Security Notes**

- **Never commit credentials** to version control
- Use environment variables in production:
  ```bash
  export MAIL_USERNAME=your-email@gmail.com
  export MAIL_PASSWORD=your-app-password
  ```
- Use properties:
  ```properties
  spring.mail.username=${MAIL_USERNAME}
  spring.mail.password=${MAIL_PASSWORD}
  ```

## 📝 **Current Email Template Features**

✅ Professional HTML template  
✅ Responsive design  
✅ Security notice (1-hour expiry)  
✅ Fallback text link  
✅ Branding (Story Publisher)  

Ready to set up real email? Follow the Gmail SMTP steps above! 🚀
