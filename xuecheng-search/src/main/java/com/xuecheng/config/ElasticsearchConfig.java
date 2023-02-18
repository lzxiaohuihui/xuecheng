package com.xuecheng.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig{

    @Value("${elasticsearch.hostlist}")
    private String hostlist;

    @Bean
    public RestHighLevelClient restHighLevelClient(){

        String[] split = hostlist.split(",");
        HttpHost[] httpHostArray = new HttpHost[split.length];
        for(int i = 0; i < split.length; ++i){
            String item = split[i];
            httpHostArray[i] = new HttpHost(item.split(":")[0],
                    Integer. parseInt(item.split(":")[1]), "http");

        }
        return new RestHighLevelClient(RestClient.builder(httpHostArray));

    }
}
