package com.example.games_extractor.dto;

//Autorizovani detalji korisnika
public class UserAuthenticationDetails {
	private final Long userId;
	private final String tenant;
	private final Long tenantId;
	private final String username;

	public UserAuthenticationDetails(
            Long userId,
            String username,
            Long tenantId,
            String tenant
    ) {
        this.userId = userId;
        this.username = username;
        this.tenantId = tenantId;
        this.tenant = tenant;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTenant() {
        return tenant;
    }
}
