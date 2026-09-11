package com.evidlock.controller;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class SmtpAuthenticator extends Authenticator {
    private final String username;
    private final String password;

    public SmtpAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
}
