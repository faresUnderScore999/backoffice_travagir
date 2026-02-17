package java_project.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(
    @JsonProperty("tokens") Tokens tokens,
    @JsonProperty("message") String message,
    // Accept API that uses either "admin" or "user" for the logged-in object
    @JsonProperty("admin") @JsonAlias({"user"}) User user
) {
    public record Tokens(String accessToken, String refreshToken) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(int id, String name, String email, String imageUrl) {}
}