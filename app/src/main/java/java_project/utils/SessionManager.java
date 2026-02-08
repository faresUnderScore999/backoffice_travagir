package java_project.utils;

import java_project.models.LoginResponse;

public class SessionManager {
    private static SessionManager instance;
    private String accessToken;
    private String refreshToken; 
    private LoginResponse.User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Setters called after login
    public void setSession(String token, String refreshToken, LoginResponse.User user) {
        this.accessToken = token;
        this.refreshToken = refreshToken;
        this.currentUser = user;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public LoginResponse.User getCurrentUser() { return currentUser; }
    
    public void logout() {
        this.accessToken = null;
        this.refreshToken = null;
        this.currentUser = null;
    }
}