package java_project.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.awt.Desktop;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;
import java_project.services.AuthService;
import javafx.scene.control.Label;
import java.net.http.HttpResponse;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    private final AuthService authService = new AuthService();

    private HttpServer oauthServer;

     @FXML
    public void initialize() {
        // Clear error message on input change
        emailField.setText("admin@travagir.com");
        passwordField.setText("securepassword159A@");
    }
    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String pass = passwordField.getText();

        try {
            HttpResponse<String> response = authService.login(email, pass);

            if (response.statusCode() == 200) {
                // SUCCESS: Switch to the Home Page (Trips View)
                Platform.runLater(() -> {
                    try {
                        // Inside Platform.runLater
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Get the current
                                                                                                 // stage
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/java_project/views/MainLayout.fxml"));
                        BorderPane mainLayout = loader.load();
                        // ... rest of your logic

                        // Load default center content (Voyage)
                        FXMLLoader contentLoader = new FXMLLoader(
                                getClass().getResource("/java_project/views/voyageView.fxml"));
                        mainLayout.setCenter(contentLoader.load());

                        Scene scene = new Scene(mainLayout);
                        stage.setScene(scene);
                        stage.setTitle("Trip Manager - Home");
                        stage.show();

                    } catch (IOException e) {
                        System.err.println("Error loading Home Page: " + e.getMessage());
                    }
                });
            } else {
                errorLabel.setText("Invalid credentials. Try again."+ " Status: " + response.statusCode());
            }
        } catch (Exception e) {
            errorLabel.setText("Server error: " + e.getMessage());
        }
    }

    /**
     * Starts the Google OAuth flow by launching a tiny local HTTP server and
     * opening the bundled `verify.html` which redirects to Google's consent page.
     */
    @FXML
    public void startGoogleLogin(ActionEvent event) {
        new Thread(this::startLocalServerAndOpenBrowser).start();
    }

    private void startLocalServerAndOpenBrowser() {
        try {
            oauthServer = HttpServer.create(new InetSocketAddress(8087), 0);

            // Serve a small page that captures the URL fragment (hash) and posts it back to /token
            oauthServer.createContext("/verifyme", (HttpExchange exchange) -> {
                try {
                    String page = "<!doctype html><html><head><meta charset=\"utf-8\"><title>Auth Redirect</title></head><body>"
                            + "<script>/* extract fragment and POST to /token */(function(){var h=window.location.hash;if(h){var p=new URLSearchParams(h.substring(1));var t=p.get('access_token')||p.get('code');if(t){fetch('/token?access_token='+encodeURIComponent(t)).then(function(){document.body.innerHTML='<h3>Success! You may return to the application.</h3>';}).catch(function(){document.body.innerHTML='<h3>Success! (notify failed)</h3>';});}else{document.body.innerHTML='<h3>No token found in fragment</h3>';}}else{document.body.innerHTML='<h3>No fragment found. Close this window.</h3>';}})();</script>"
                            + "</body></html>";

                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, page.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(page.getBytes()); }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // Endpoint to receive the token (not from Google directly, but from the browser JS above)
            oauthServer.createContext("/token", (HttpExchange exchange) -> {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String token = null;
                    if (query != null && query.contains("access_token=")) {
                        token = query.split("access_token=")[1].split("&")[0];
                    }
                    if (token != null) {
                        final String tokenFinal = token;
                        System.out.println("Received OAuth token from browser: " + tokenFinal);

                        // Exchange Google token for backend tokens
                        try {
                            java.net.http.HttpResponse<String> googleLoginResponse = authService.googleLogin(tokenFinal);
                            if (googleLoginResponse.statusCode() == 200) {
                                Platform.runLater(() -> {
                                    errorLabel.setText("Google login successful!");
                                    // Navigate to main layout
                                    try {
                                        Stage stage = new Stage(); // Create new stage or get current
                                        FXMLLoader loader = new FXMLLoader(
                                                getClass().getResource("/java_project/views/MainLayout.fxml"));
                                        BorderPane mainLayout = loader.load();
                                        FXMLLoader contentLoader = new FXMLLoader(
                                                getClass().getResource("/java_project/views/voyageView.fxml"));
                                        mainLayout.setCenter(contentLoader.load());
                                        Scene scene = new Scene(mainLayout);
                                        stage.setScene(scene);
                                        stage.setTitle("Trip Manager - Home");
                                        stage.show();
                                        // Close login window if we have reference to it
                                        if (emailField.getScene() != null && emailField.getScene().getWindow() instanceof Stage) {
                                            ((Stage) emailField.getScene().getWindow()).close();
                                        }
                                    } catch (IOException e) {
                                        System.err.println("Error loading Home Page: " + e.getMessage());
                                        errorLabel.setText("Error loading home page: " + e.getMessage());
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    errorLabel.setText("Google login failed: " + googleLoginResponse.statusCode());
                                });
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Platform.runLater(() -> {
                                errorLabel.setText("Error exchanging token: " + ex.getMessage());
                            });
                        }
                    }

                    String response = "OK";
                    exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(response.getBytes()); }

                    // stop server shortly after receiving token
                    new Thread(() -> {
                        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                        if (oauthServer != null) oauthServer.stop(0);
                    }).start();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            oauthServer.createContext("/", (HttpExchange exchange) -> {
                String response = "<html><body><h3>OAuth bridge running. Awaiting Google redirect to /verifyme</h3></body></html>";
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(response.getBytes()); }
            });

            oauthServer.start();

            URL resource = getClass().getResource("/java_project/verify.html");
            if (resource != null) {
                Desktop.getDesktop().browse(resource.toURI());
            } else {
                File f = new File("verify.html");
                if (f.exists()) Desktop.getDesktop().browse(f.toURI());
                else Platform.runLater(() -> errorLabel.setText("verify.html not found in resources."));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> errorLabel.setText("OAuth server error: " + ex.getMessage()));
        }
    }
}