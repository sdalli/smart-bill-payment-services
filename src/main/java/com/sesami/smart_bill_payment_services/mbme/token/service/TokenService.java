package com.sesami.smart_bill_payment_services.mbme.token.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesami.smart_bill_payment_services.mbme.token.entity.TokenActivity;
import com.sesami.smart_bill_payment_services.mbme.token.entity.TokenDetails;
import com.sesami.smart_bill_payment_services.mbme.token.service.repository.TokenActivityRepository;
import com.sesami.smart_bill_payment_services.mbme.token.service.repository.TokenDetailsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private TokenDetailsRepository tokenDetailsRepository;

    @Autowired
    private TokenActivityRepository tokenActivityRepository;

    @Autowired
    private RestTemplate restTemplate =null;

    @Value("${mbme.api.token.url}")
    private String tokenUrl;
    
    @Value("${mbme.api.username}")
    private String username;
    
    @Value("${mbme.api.password}")
    private String password;
    
   

    public TokenDetails getValidToken() throws IOException {
        logger.info("Fetching valid token for username: {}", username);
        TokenDetails tokenDetails = tokenDetailsRepository.findTopByOrderByIdDesc();

        if (tokenDetails == null || tokenDetails.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.info("Token expired or not found, fetching new token.");
            return fetchAndSaveToken(username, password);
        }

        logger.info("Valid token found in database.");
        return tokenDetails;
    }

    private TokenDetails fetchAndSaveToken(String username, String password) throws IOException {
        logger.info("Fetching new token from external API.");
       // String url = "https://qty.mbme.org:8080/v2/mbme/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", username);
        params.add("password", password);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(params);
        LocalDateTime requestTimestamp = LocalDateTime.now();

        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

        TokenActivity tokenActivity = new TokenActivity();
        tokenActivity.setActivityType("FETCH_TOKEN");
        tokenActivity.setRequestJson(requestJson);
        tokenActivity.setResponseJson(response.getBody());
        tokenActivity.setTimestamp(requestTimestamp);
        tokenActivityRepository.save(tokenActivity);

        if (response.getStatusCode() == HttpStatus.OK) {
            TokenDetails tokenDetails = objectMapper.readValue(response.getBody(), TokenDetails.class);
            tokenDetails.setExpiresAt(LocalDateTime.now().plusMinutes(Long.parseLong(tokenDetails.getExpiresIn())));
            tokenDetails.setRequestJson(requestJson);
            tokenDetails.setResponseJson(response.getBody());
            tokenDetails.setRequestTimestamp(requestTimestamp);
            logger.info("Token fetched and saved successfully.");
            return tokenDetailsRepository.save(tokenDetails);
        }

        logger.error("Failed to fetch token from external API.");
        throw new RuntimeException("Failed to fetch token");
    }
    
    
    public static void main(String[] args) {
    	TokenService tokenService = new TokenService();
		 
		 try {
			 TokenDetails tokenDetails=	tokenService.fetchAndSaveToken("transguardgroup", "y8Hk6aObkf");
			 System.out.println(tokenDetails);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
	}
	
	 
}