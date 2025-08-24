package com.storypublisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import com.storypublisher.config.CategoryConfig;
import com.storypublisher.config.TranslationConfig;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({CategoryConfig.class, TranslationConfig.class})
public class StoryPublisherApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoryPublisherApiApplication.class, args);
	}

}
