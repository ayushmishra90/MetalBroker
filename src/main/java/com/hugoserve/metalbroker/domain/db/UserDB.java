package com.hugoserve.metalbroker.domain.db;

//import com.hugoserve.metalbroker.domain.enums.UserRole;

import com.hugoserve.metalbroker.proto.MetalRatesProto;

public class UserDB {

    private long id;
    private String email;
    private String password;
    private String currentRefreshToken;
    private MetalRatesProto.UserRole role;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCurrentRefreshToken() {
        return currentRefreshToken;
    }

    public void setCurrentRefreshToken(String currentRefreshToken) {
        this.currentRefreshToken = currentRefreshToken;
    }

    public MetalRatesProto.UserRole getRole() {
        return role;
    }

    public void setRole(MetalRatesProto.UserRole role) {
        this.role = role;
    }
}
