package com.knowledge.base.document.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.knowledge.base.document.repository")
@EnableMongoAuditing
public class MongoConfig {
}
