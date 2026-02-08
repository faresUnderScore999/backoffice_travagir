package java_project.models;

public record LoginRequest(
    String email, 
    String password
) {}