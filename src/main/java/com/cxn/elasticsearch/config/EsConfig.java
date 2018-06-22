package com.cxn.elasticsearch.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class EsConfig {

    @Bean
    public TransportClient client() throws UnknownHostException {

        InetSocketTransportAddress node = new InetSocketTransportAddress(
                InetAddress.getByName("localhost"),
                9300);

        Settings settings = Settings.builder()
                .put("cluster.name","my-application")
                // set to true to ignore cluster name validation of connected nodes
                // .put("client.transport.ignore_cluster_name", true)
                // The time to wait for a ping response from a node, defaults to 5s
                // .put("client.transport.ping_timeout", 5)
                // How often to sample/ping the nodes listed and connected.defaults to 5s
                // .put("client.transport,nodes_sampler_interval", 5)
                .build();
        TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(node);

        return client;
    }
}
