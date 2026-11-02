package com.progressgrid.dao;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * DatabaseConnection - Manages JDBC connectivity to the MySQL database (progressgrid_db)
 * and provides persistent authentication/registration operations for the Swing UI.
 */
public class DatabaseConnection {

    private static String dbUrl = "jdbc:mysql://localhost:3306/progressgrid_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "pg_user";
    private static String dbPassword = "password123";

    // Lazy encoder holder to prevent class loading failure if spring-jcl is absent in standalone execution
    private static Object passwordEncoderInstance = null;

    private static String hashPassword(String plainPassword) {
        try {
            if (passwordEncoderInstance == null) {
                passwordEncoderInstance = new BCryptPasswordEncoder();
            }
            return ((BCryptPasswordEncoder) passwordEncoderInstance).encode(plainPassword);
        } catch (Throwable t) {
            return sha256Hex(plainPassword);
        }
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }

    static {
        // Load properties from application.properties if available
        loadConfiguration();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL Driver not found on classpath: " + e.getMessage());
        }
    }

    private static void loadConfiguration() {
        try (InputStream is = DatabaseConnection.class.getResourceAsStream("/application.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String urlProp = props.getProperty("spring.datasource.url");
                String userProp = props.getProperty("spring.datasource.username");
                String passProp = props.getProperty("spring.datasource.password");

                if (urlProp != null && !urlProp.contains("${")) dbUrl = urlProp;
                if (userProp != null && !userProp.contains("${")) dbUser = userProp;
                if (passProp != null && !passProp.contains("${")) dbPassword = passProp;
            }
        } catch (Exception ignored) {}

        // Environment variable overrides
        String envUrl = System.getenv("SPRING_DATASOURCE_URL");
        String envUser = System.getenv("SPRING_DATASOURCE_USERNAME");
        String envPass = System.getenv("SPRING_DATASOURCE_PASSWORD");
        if (envUrl != null && !envUrl.trim().isEmpty()) dbUrl = envUrl.trim();
        if (envUser != null && !envUser.trim().isEmpty()) dbUser = envUser.trim();
        if (envPass != null && !envPass.trim().isEmpty()) dbPassword = envPass.trim();
    }

    /**
     * Obtains an active connection to the database.
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            // If pg_user fails, try root fallback for local developer setups
            if (!"root".equals(dbUser)) {
                try {
                    return DriverManager.getConnection(dbUrl, "root", "");
                } catch (SQLException ignored) {
                    try {
                        return DriverManager.getConnection(dbUrl, "root", "root");
                    } catch (SQLException ignored2) {}
                }
            }
            throw e;
        }
    }

    /**
     * Checks if MySQL database connection is alive and working.
     */
    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * User record DTO for query results.
     */
    public static class UserRecord {
        private final long id;
        private final String username;
        private final String email;
        private final String name;
        private final Timestamp createdAt;

        public UserRecord(long id, String username, String email, String name, Timestamp createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.name = name != null && !name.trim().isEmpty() ? name : username;
            this.createdAt = createdAt;
        }

        public long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public Timestamp getCreatedAt() { return createdAt; }
    }

    /**
     * Ensures required tables exist in progressgrid_db.
     */
    public static void ensureTablesExist() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "username VARCHAR(100) NOT NULL UNIQUE, "
                + "email VARCHAR(150) NOT NULL UNIQUE, "
                + "password_hash VARCHAR(255) NOT NULL, "
                + "name VARCHAR(100), "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            System.err.println("[DatabaseConnection] Error ensuring tables: " + e.getMessage());
        }
    }

    /**
     * Registers a new user directly into MySQL progressgrid_db.users table.
     * Uses BCrypt hashing matching Spring Boot AuthService.
     */
    public static UserRecord registerUser(String username, String email, String plainPassword, String name) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        ensureTablesExist();

        String trimmedUser = username.trim();
        String trimmedEmail = email.trim();
        String trimmedName = name != null && !name.trim().isEmpty() ? name.trim() : trimmedUser;

        try (Connection conn = getConnection()) {
            // Check for existing username or email
            String checkSql = "SELECT id, username, email FROM users WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, trimmedUser);
                checkStmt.setString(2, trimmedEmail);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String existingUser = rs.getString("username");
                        String existingEmail = rs.getString("email");
                        if (trimmedUser.equalsIgnoreCase(existingUser)) {
                            throw new IllegalArgumentException("Username '" + trimmedUser + "' is already taken");
                        } else {
                            throw new IllegalArgumentException("Email '" + trimmedEmail + "' is already registered");
                        }
                    }
                }
            }

            // Hash password with BCrypt (or SHA-256 fallback)
            String passwordHash = hashPassword(plainPassword);

            // Insert new user
            String insertSql = "INSERT INTO users (username, email, password_hash, name) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, trimmedUser);
                insertStmt.setString(2, trimmedEmail);
                insertStmt.setString(3, passwordHash);
                insertStmt.setString(4, trimmedName);
                insertStmt.executeUpdate();

                long newId = 0;
                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        newId = keys.getLong(1);
                    }
                }

                return new UserRecord(newId, trimmedUser, trimmedEmail, trimmedName, new Timestamp(System.currentTimeMillis()));
            }
        }
    }

    /**
     * Authenticates a user against MySQL database.
     * Supports both BCrypt hash and legacy plaintext hashes.
     */
    public static UserRecord authenticateUser(String usernameOrEmail, String plainPassword) throws Exception {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Username or email is required");
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String query = usernameOrEmail.trim();

        try (Connection conn = getConnection()) {
            String sql = "SELECT id, username, email, password_hash, name, created_at FROM users "
                    + "WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, query);
                stmt.setString(2, query);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong("id");
                        String username = rs.getString("username");
                        String email = rs.getString("email");
                        String storedHash = rs.getString("password_hash");
                        String name = rs.getString("name");
                        Timestamp createdAt = rs.getTimestamp("created_at");

                        if (passwordMatches(plainPassword, storedHash)) {
                            // If it was plain text, update to BCrypt in DB
                            if (storedHash != null && !storedHash.startsWith("$2")) {
                                upgradePasswordHash(id, plainPassword);
                            }
                            return new UserRecord(id, username, email, name, createdAt);
                        }
                    }
                }
            }
        }
        return null; // Not found or password mismatch
    }

    private static boolean passwordMatches(String plainPassword, String storedHash) {
        if (storedHash == null || plainPassword == null) return false;
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            try {
                if (passwordEncoderInstance == null) {
                    passwordEncoderInstance = new BCryptPasswordEncoder();
                }
                return ((BCryptPasswordEncoder) passwordEncoderInstance).matches(plainPassword, storedHash);
            } catch (Throwable ignored) {}
        }
        if (storedHash.equalsIgnoreCase(sha256Hex(plainPassword))) {
            return true;
        }
        // Fallback for plain text passwords in legacy/demo records
        return MessageDigest.isEqual(storedHash.getBytes(StandardCharsets.UTF_8),
                plainPassword.getBytes(StandardCharsets.UTF_8));
    }

    private static void upgradePasswordHash(long userId, String plainPassword) {
        try (Connection conn = getConnection()) {
            String updateSql = "UPDATE users SET password_hash = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, hashPassword(plainPassword));
                stmt.setLong(2, userId);
                stmt.executeUpdate();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Retrieves all users currently in the database so the user can easily view them.
     */
    public static List<UserRecord> getAllUsers() throws Exception {
        List<UserRecord> list = new ArrayList<>();
        ensureTablesExist();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, email, name, created_at FROM users ORDER BY id ASC")) {
            while (rs.next()) {
                list.add(new UserRecord(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("name"),
                        rs.getTimestamp("created_at")
                ));
            }
        }
        return list;
    }
}
