package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String username;
    private String nombres;
    private String apellidos;
    private String fullName;
    private String email;
    private String role;
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
}
