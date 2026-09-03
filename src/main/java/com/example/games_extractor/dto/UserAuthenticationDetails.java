package com.example.games_extractor.dto;

//Autorizovani detalji korisnika
public class UserAuthenticationDetails {
	private final Long userId;
	private final String tenant;
	private final Long tenantId;

	public UserAuthenticationDetails(Long userId, Long tenantId,String tenant) {
		this.userId = userId;
		this.tenantId = tenantId;
		this.tenant=tenant;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getTenantId() {
		return tenantId;
	}
	
	public String getTenant() {
		return tenant;
	}
}
