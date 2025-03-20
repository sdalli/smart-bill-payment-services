package com.sesami.smart_bill_payment_services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.sesami.smart_bill_payment_services.mbme.token.entity.TokenDetails;
import com.sesami.smart_bill_payment_services.mbme.token.service.TokenService;
import com.sesami.smart_bill_payment_services.mbme.token.service.repository.TokenActivityRepository;
import com.sesami.smart_bill_payment_services.mbme.token.service.repository.TokenDetailsRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private TokenDetailsRepository tokenDetailsRepository;

    @Mock
    private TokenActivityRepository tokenActivityRepository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetValidToken_ExpiredToken() throws IOException {
        TokenDetails expiredToken = new TokenDetails();
        expiredToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenDetailsRepository.findTopByOrderByIdDesc()).thenReturn(expiredToken);

        Map<String, String> params = new HashMap<>();
        params.put("username", "username");
        params.put("password", "password");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"accessToken\":\"newToken\",\"expiresIn\":\"60\"}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), eq(request), eq(String.class))).thenReturn(responseEntity);

        TokenDetails newToken = tokenService.getValidToken();

        assertNotNull(newToken);
        assertEquals("newToken", newToken.getAccessToken());
        verify(tokenDetailsRepository, times(1)).save(any(TokenDetails.class));
    }

    @Test
    public void testGetValidToken_ValidToken() throws IOException {
        TokenDetails validToken = new TokenDetails();
        validToken.setExpiresAt(LocalDateTime.now().plusMinutes(1));
        when(tokenDetailsRepository.findTopByOrderByIdDesc()).thenReturn(validToken);

        TokenDetails token = tokenService.getValidToken();

        assertNotNull(token);
        assertEquals(validToken, token);
        verify(tokenDetailsRepository, never()).save(any(TokenDetails.class));
    }
}