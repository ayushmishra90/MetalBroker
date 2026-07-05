package com.hugoserve.metalbroker.security;

public interface AuthService {

    String register(String body);

    String login(String body);

    String refresh(String body);

    String logout(String body);
}

