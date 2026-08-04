package com.knowledge.base.ai.rag.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rag.enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchConfig {
    @Bean(destroyMethod = "close")
    public RestClient ragRestClient(@Value("${spring.elasticsearch.uris:http://localhost:9200}") String uris) {
        return RestClient.builder(HttpHost.create(uris.split(",")[0].trim())).build();
    }
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient ragRestClient) {
        return new ElasticsearchClient(new RestClientTransport(ragRestClient, new JacksonJsonpMapper()));
    }
}
