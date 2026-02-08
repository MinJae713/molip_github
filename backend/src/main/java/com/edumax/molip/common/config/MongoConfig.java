package com.edumax.molip.common.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
  @Value("${spring.data.mongodb.uri}")
  private String mongoUri;

  @Override
  protected String getDatabaseName() {
    return "S14P13A309";
  }

  @Override
  public MongoClient mongoClient() {
    return MongoClients.create(mongoUri);
  }

  @Override
  protected boolean autoIndexCreation() {
    return true; // 이 메서드를 오버라이드하면 끝!
  }
}
