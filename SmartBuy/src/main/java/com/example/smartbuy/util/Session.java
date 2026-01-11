package com.example.smartbuy.util;

import com.example.smartbuy.model.User;

/**
 *Session Management Class - Save Current Logged-in User Information
 */
public class Session {
    private static Session instance;
    private User currentUser;
    
    private Session() {}
    
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    public void logout() {
        this.currentUser = null;
    }
}
