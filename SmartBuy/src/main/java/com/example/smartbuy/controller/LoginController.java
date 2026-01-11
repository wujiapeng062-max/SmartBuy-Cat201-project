package com.example.smartbuy.controller;

import com.example.smartbuy.dao.UserDAO;
import com.example.smartbuy.model.User;
import com.example.smartbuy.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Login Interface Controller
 */
public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Hyperlink registerLink;
    
    @FXML
    private Hyperlink adminLoginLink;
    
    private UserDAO userDAO = new UserDAO();
    
    @FXML
    private void initialize() {
        errorLabel.setText("");
    }
    
    /**
     * Login handling
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty!");
            return;
        }
        
        try {
            User user = userDAO.login(username, password);
            
            if (user != null) {
                // Save user to session
                Session.getInstance().setCurrentUser(user);
                
                // Users are redirected to different screens based on their role.
                if (user.isAdmin()) {
                    loadAdminDashboard();
                } else {
                    loadCustomerHome();
                }
            } else {
                errorLabel.setText("Username or password is incorrect!");
            }
            
        } catch (Exception e) {
            errorLabel.setText("Login failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Redirect to the registration page
     */
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Register.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User Registration - SmartBuy");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open registration page");
        }
    }
    
    /**
     * Administrator login prompt
     */
    @FXML
    private void handleAdminLogin() {
        showAlert(Alert.AlertType.INFORMATION, "Admin Login", 
                 "Please use admin account to login:\nUsername: admin\nPassword: admin123");
    }
    
    /**
     * Loading user homepage
     */
    private void loadCustomerHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("SmartBuy - Electronics Store");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open main page");
        }
    }
    
    /**
     * Load admin panel
     */
    private void loadAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Admin Dashboard - SmartBuy");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open admin panel");
        }
    }
    
    /**
     * Display a prompt box
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // For information dialog boxes, set the OK button to English.
        if (type == Alert.AlertType.INFORMATION || type == Alert.AlertType.WARNING || type == Alert.AlertType.ERROR) {
            ButtonType okButton = new ButtonType("OK");
            alert.getButtonTypes().setAll(okButton);
        } else if (type == Alert.AlertType.CONFIRMATION) {
            // The confirmation dialog box uses Yes/No buttons.
            ButtonType yesButton = new ButtonType("Yes");
            ButtonType noButton = new ButtonType("No");
            alert.getButtonTypes().setAll(yesButton, noButton);
        }
        
        alert.showAndWait();
    }
}
