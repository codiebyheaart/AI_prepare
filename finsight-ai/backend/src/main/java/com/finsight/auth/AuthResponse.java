package com.finsight.auth;

public record AuthResponse(String token, UserDto user) {}
