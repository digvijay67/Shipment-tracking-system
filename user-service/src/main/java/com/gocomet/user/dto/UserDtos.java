package com.gocomet.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserDtos {

    @Data
    public static class RegisterRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 6, max = 100)
        private String password;
        @NotBlank @Size(max = 100)
        private String firstName;
        @NotBlank @Size(max = 100)
        private String lastName;
        private String phone;
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String tokenType = "Bearer";
        private UserResponse user;
        private long expiresIn;

        public AuthResponse(String token, UserResponse user, long expiresIn) {
            this.token = token;
            this.user = user;
            this.expiresIn = expiresIn;
        }
    }

    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserResponse {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String role;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp = LocalDateTime.now();

        public static <T> ApiResponse<T> success(String message, T data) {
            ApiResponse<T> r = new ApiResponse<>();
            r.success = true; r.message = message; r.data = data;
            return r;
        }

        public static <T> ApiResponse<T> error(String message) {
            ApiResponse<T> r = new ApiResponse<>();
            r.success = false; r.message = message;
            return r;
        }
    }
}
