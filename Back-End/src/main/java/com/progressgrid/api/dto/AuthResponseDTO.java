package com.progressgrid.api.dto;

public class AuthResponseDTO {
    private Long id;
    private String username;
    private String email;
    /** Signed session token; the frontend sends it back as "Authorization: Bearer ...". */
    private String token;

    public AuthResponseDTO(Long id, String username, String email, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.token = token;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
}
