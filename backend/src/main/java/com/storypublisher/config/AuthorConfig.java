package com.storypublisher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for default author settings.
 * Allows customization of default author information used in the application.
 */
@Configuration
@ConfigurationProperties(prefix = "app.author.default")
public class AuthorConfig {
    
    private String name = "K V S Ravi";
    private String firstName = "K V S";
    private String lastName = "Ravi";
    private String email = "udayganesh.kanteti@gmail.com";
    private String username = "kvsravi";
    private String bio = "Writer and storyteller passionate about sharing meaningful stories";
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
}
