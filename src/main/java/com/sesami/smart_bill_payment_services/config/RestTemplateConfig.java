package com.sesami.smart_bill_payment_services.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("${resttemplate.connection.timeout}")
    private int connectionTimeout;

    @Value("${resttemplate.read.timeout}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {
		/*
		 * try { CloseableHttpClient httpClient = HttpClients.custom()
		 * .setSSLContext(SSLContextBuilder.create().build()) .build();
		 * 
		 * HttpComponentsClientHttpRequestFactory factory = new
		 * HttpComponentsClientHttpRequestFactory(httpClient);
		 * factory.setConnectTimeout(connectionTimeout);
		 * factory.setReadTimeout(readTimeout);
		 * 
		 * return new RestTemplate(factory); } catch (Exception e) {
		 * e.printStackTrace(); return new RestTemplate(); }
		 */
    	return new RestTemplate();
    }
}