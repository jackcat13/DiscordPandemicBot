package com.corona.virus

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate


@Configuration
class MongoConfig {

    @Bean
    fun mongo(): com.mongodb.client.MongoClient {
        val connectionString = ConnectionString("mongodb://localhost:27017/pandemic")
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()
        return MongoClients.create(mongoClientSettings)
    }

    @Bean
    fun mongoTemplate(): MongoTemplate? {
        return MongoTemplate(mongo(), "pandemic")
    }

}