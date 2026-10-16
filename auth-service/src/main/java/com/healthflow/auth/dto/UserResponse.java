package com.healthflow.auth.dto;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    String role
) {
}