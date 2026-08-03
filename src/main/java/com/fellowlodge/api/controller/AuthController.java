package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.dto.auth.AuthResponse;
import com.fellowlodge.api.dto.auth.ChangePasswordRequest;
import com.fellowlodge.api.dto.auth.ForgotPasswordRequest;
import com.fellowlodge.api.dto.auth.LoginRequest;
import com.fellowlodge.api.dto.auth.MessageResponse;
import com.fellowlodge.api.dto.auth.RefreshTokenRequest;
import com.fellowlodge.api.dto.auth.RegisterRequest;
import com.fellowlodge.api.dto.auth.ResetPasswordRequest;
import com.fellowlodge.api.dto.auth.UserResponse;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", authService.login(request, clientIp()));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok("Token refreshed", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<MessageResponse> logout(@RequestBody(required = false) RefreshTokenRequest request,
                                               @RequestHeader(value = "X-Refresh-Token", required = false)
                                               String refreshTokenHeader) {
        String token = request != null ? request.refreshToken() : refreshTokenHeader;
        authService.logout(token);
        return ApiResponse.<MessageResponse>ok("Logged out successfully", null);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.created("Account created", authService.registerGuest(request, clientIp()));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ApiResponse<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.ok(authService.resetPassword(request));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ApiResponse.ok(authService.changePassword(SecurityUtils.currentUserId(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(authService.me(SecurityUtils.currentUserId()));
    }

    @PostMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> mePost() {
        return ApiResponse.ok(authService.me(SecurityUtils.currentUserId()));
    }

    @PostMapping("/verify-email")
    public ApiResponse<MessageResponse> verifyEmail(@RequestParam(required = false) String token,
                                                    @RequestBody(required = false) java.util.Map<String, String> body) {
        String verificationToken = token != null ? token : (body != null ? body.get("token") : null);
        return ApiResponse.ok(authService.verifyEmail(verificationToken));
    }

    private String clientIp() {
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest().getRemoteAddr();
    }
}
