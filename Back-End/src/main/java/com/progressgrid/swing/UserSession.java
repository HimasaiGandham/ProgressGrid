package com.progressgrid.swing;

/**
 * UserSession - Holds the currently authenticated user session across Swing frames.
 */
public class UserSession {

    public static class UserInfo {
        private final long id;
        private final String username;
        private final String email;
        private final String name;
        private final String token;

        public UserInfo(long id, String username, String email, String name, String token) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.name = name != null && !name.trim().isEmpty() ? name : username;
            this.token = token;
        }

        public long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getToken() { return token; }

        public String getDisplayName() {
            return name != null && !name.trim().isEmpty() ? name : username;
        }
    }

    private static UserInfo currentUser = null;

    public static void setCurrentUser(UserInfo user) {
        currentUser = user;
    }

    public static UserInfo getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}

