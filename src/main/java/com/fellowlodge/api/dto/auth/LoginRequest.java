package com.fellowlodge.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @JsonAlias({"email", "identifier"})
        @NotBlank(message = "Username or email is required")
        String username,
        @NotBlank(message = "Password is required")
        String password
) {
    public String identifier() {
        return username;
    }
}
