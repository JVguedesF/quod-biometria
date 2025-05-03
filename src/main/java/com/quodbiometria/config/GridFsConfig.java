package com.quodbiometria.config;

import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class GridFsConfig {

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Bean
    public GridFsTemplate gridFsTemplate(MongoClient mongoClient, MongoTemplate mongoTemplate) {
        return new GridFsTemplate(mongoTemplate.getMongoDatabaseFactory(),
                mongoTemplate.getConverter());
    }
}