package com.progressgrid.api.dto;

public class ResetPasswordDTO {
    private String identifier;
    private String emailOrUsername; // For backwards compatibility
    private String otp;
    private String newPassword;

    public String getIdentifier() {
        return identifier != null ? identifier : emailOrUsername;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
        if (this.emailOrUsername == null) {
            this.emailOrUsername = identifier;
        }
    }

    public String getEmailOrUsername() {
        return emailOrUsername != null ? emailOrUsername : identifier;
    }

    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
        if (this.identifier == null) {
            this.identifier = emailOrUsername;
        }
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
