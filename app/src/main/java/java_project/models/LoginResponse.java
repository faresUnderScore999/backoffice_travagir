package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(
    Tokens tokens,
    String message,
    User user
) {
    public record Tokens(String accessToken, String refreshToken) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(int id, String name, String email, String imageUrl) {}
}