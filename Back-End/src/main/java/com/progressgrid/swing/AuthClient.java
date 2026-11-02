package com.progressgrid.swing;

import com.progressgrid.dao.DatabaseConnection;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * AuthClient - Bridges the Swing UI to the Spring Boot REST API and/or direct MySQL database.
 * If Spring Boot backend is running (port 8080), it uses the REST endpoints (/api/auth).
 * If the backend is offline, it seamlessly falls back to direct MySQL via DatabaseConnection.
 */
public class AuthClient {

    private static final String API_BASE = "http://localhost:8080/api/auth";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final UserSession.UserInfo user;
        private final String source; // "Backend REST API" or "Direct MySQL Database"

        public AuthResult(boolean success, String message, UserSession.UserInfo user, String source) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.source = source;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public UserSession.UserInfo getUser() { return user; }
        public String getSource() { return source; }
    }

    /**
     * Attempts to register a user.
     */
    public static AuthResult register(String username, String email, String password, String name) {
        // Try Backend REST API first
        try {
            String jsonPayload = String.format(
                    "{\"username\": \"%s\", \"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\"}",
                    escapeJson(username), escapeJson(email), escapeJson(password), escapeJson(name != null ? name : username)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/signup"))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                long id = extractJsonLong(body, "id");
                String token = extractJsonString(body, "token");
                UserSession.UserInfo info = new UserSession.UserInfo(id, username, email, name, token);
                UserSession.setCurrentUser(info);
                return new AuthResult(true, "Account created successfully in database via Backend API! User ID: #" + id, info, "Backend API");
            } else if (response.statusCode() == 400 || response.statusCode() == 409) {
                String errorMsg = extractJsonString(response.body(), "message");
                if (errorMsg == null || errorMsg.isEmpty()) errorMsg = "Registration failed with status " + response.statusCode();
                return new AuthResult(false, errorMsg, null, "Backend API");
            }
        } catch (Exception backendEx) {
            // Backend offline or unreachable, fall back to direct MySQL
        }

        // Direct MySQL Fallback
        try {
            DatabaseConnection.UserRecord record = DatabaseConnection.registerUser(username, email, password, name);
            UserSession.UserInfo info = new UserSession.UserInfo(record.getId(), record.getUsername(), record.getEmail(), record.getName(), null);
            UserSession.setCurrentUser(info);
            return new AuthResult(true, "Account created successfully in MySQL database (users table)! User ID: #" + record.getId(), info, "Direct MySQL");
        } catch (IllegalArgumentException ex) {
            return new AuthResult(false, ex.getMessage(), null, "Validation");
        } catch (Exception ex) {
            return new AuthResult(false, "Database error: " + ex.getMessage(), null, "Direct MySQL Error");
        }
    }

    /**
     * Attempts to login a user.
     */
    public static AuthResult login(String usernameOrEmail, String password) {
        // Try Backend REST API first
        try {
            String jsonPayload = String.format(
                    "{\"username\": \"%s\", \"password\": \"%s\"}",
                    escapeJson(usernameOrEmail), escapeJson(password)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/login"))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                long id = extractJsonLong(body, "id");
                String username = extractJsonString(body, "username");
                String email = extractJsonString(body, "email");
                String token = extractJsonString(body, "token");
                UserSession.UserInfo info = new UserSession.UserInfo(id, username, email, username, token);
                UserSession.setCurrentUser(info);
                return new AuthResult(true, "Login successful via Backend API! Welcome, " + username, info, "Backend API");
            } else if (response.statusCode() == 401 || response.statusCode() == 400) {
                String errorMsg = extractJsonString(response.body(), "message");
                if (errorMsg == null || errorMsg.isEmpty()) errorMsg = "Invalid username or password";
                return new AuthResult(false, errorMsg, null, "Backend API");
            }
        } catch (Exception backendEx) {
            // Backend offline, fall back to direct MySQL
        }

        // Direct MySQL Fallback
        try {
            DatabaseConnection.UserRecord record = DatabaseConnection.authenticateUser(usernameOrEmail, password);
            if (record != null) {
                UserSession.UserInfo info = new UserSession.UserInfo(record.getId(), record.getUsername(), record.getEmail(), record.getName(), null);
                UserSession.setCurrentUser(info);
                return new AuthResult(true, "Login successful via MySQL database! Welcome, " + record.getName(), info, "Direct MySQL");
            } else {
                return new AuthResult(false, "Invalid username/email or password.", null, "Direct MySQL");
            }
        } catch (Exception ex) {
            return new AuthResult(false, "Database connection error: " + ex.getMessage(), null, "Direct MySQL Error");
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractJsonString(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        return end != -1 ? json.substring(start, end) : null;
    }

    private static long extractJsonLong(String json, String key) {
        if (json == null) return 0;
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return 0;
        start += search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Long.parseLong(json.substring(start, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }
}

