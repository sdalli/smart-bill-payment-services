package com.sesami.smart_bill_payment_services.mbme.token.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "token_details")
public class TokenDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String responseCode;
    private String status;
    @Column(length = 8000) // Increase the length as needed
    private String accessToken;
    private String expiresIn;
    private String tokenType;
    private String walletBalance;
    private LocalDateTime expiresAt;
    @Column(length = 8000) // Increase the length as needed
    private String requestJson;
    @Column(length = 8000) // Increase the length as needed
    private String responseJson;
    private LocalDateTime requestTimestamp;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(String expiresIn) {
		this.expiresIn = expiresIn;
	}
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	public String getWalletBalance() {
		return walletBalance;
	}
	public void setWalletBalance(String walletBalance) {
		this.walletBalance = walletBalance;
	}
	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}
	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}
	public String getRequestJson() {
		return requestJson;
	}
	public void setRequestJson(String requestJson) {
		this.requestJson = requestJson;
	}
	public String getResponseJson() {
		return responseJson;
	}
	public void setResponseJson(String responseJson) {
		this.responseJson = responseJson;
	}
	public LocalDateTime getRequestTimestamp() {
		return requestTimestamp;
	}
	public void setRequestTimestamp(LocalDateTime requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}
	@Override
	public String toString() {
		return "TokenDetails [id=" + id + ", responseCode=" + responseCode + ", status=" + status + ", accessToken="
				+ accessToken + ", expiresIn=" + expiresIn + ", tokenType=" + tokenType + ", walletBalance="
				+ walletBalance + ", expiresAt=" + expiresAt + ", requestJson=" + requestJson + ", responseJson="
				+ responseJson + ", requestTimestamp=" + requestTimestamp + "]";
	}
	public TokenDetails(Long id, String responseCode, String status, String accessToken, String expiresIn,
			String tokenType, String walletBalance, LocalDateTime expiresAt, String requestJson, String responseJson,
			LocalDateTime requestTimestamp) {
		super();
		this.id = id;
		this.responseCode = responseCode;
		this.status = status;
		this.accessToken = accessToken;
		this.expiresIn = expiresIn;
		this.tokenType = tokenType;
		this.walletBalance = walletBalance;
		this.expiresAt = expiresAt;
		this.requestJson = requestJson;
		this.responseJson = responseJson;
		this.requestTimestamp = requestTimestamp;
	}
	public TokenDetails() {
		super();
		// TODO Auto-generated constructor stub
	}

    // Getters and setters
    
    
}