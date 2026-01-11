package com.example.smartbuy.controller;

import com.example.smartbuy.dao.CartDAO;
import com.example.smartbuy.model.CartItem;
import com.example.smartbuy.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Shopping cart controller
 */
public class CartController {
    
    @FXML
    private VBox cartItemsContainer;
    
    @FXML
    private VBox emptyCartMessage;
    
    @FXML
    private Label itemCountLabel;
    
    @FXML
    private Label subtotalLabel;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Button checkoutButton;
    
    private CartDAO cartDAO = new CartDAO();
    private List<CartItem> cartItems;
    
    @FXML
    private void initialize() {
        if (!Session.getInstance().isLoggedIn()) {
            // If the user is not logged in, they will be redirected to the login page.
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) cartItemsContainer.getScene().getWindow();
                stage.setScene(new Scene(root, 400, 500));
                stage.setTitle("Login - SmartBuy");
                
                showAlert(Alert.AlertType.WARNING, "Notice", "Please log in first to view cart!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot open login page");
            }
            return;
        }
        
        loadCartItems();
    }
    
    /**
     * Load items into shopping cart
     */
    private void loadCartItems() {
        if (!Session.getInstance().isLoggedIn()) {
            return; // Prevent subsequent operations from being performed while not logged in.
        }
        
        try {
            int userId = Session.getInstance().getCurrentUser().getUserId();
            cartItems = cartDAO.getCartItems(userId);
            
            if (cartItems.isEmpty()) {
                cartItemsContainer.setVisible(false);
                emptyCartMessage.setVisible(true);
                checkoutButton.setDisable(true);
            } else {
                cartItemsContainer.setVisible(true);
                emptyCartMessage.setVisible(false);
                checkoutButton.setDisable(false);
                displayCartItems();
                updateSummary();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load cart: " + e.getMessage());
        }
    }
    
    /**
     * Show items in shopping cart
     */
    private void displayCartItems() {
        cartItemsContainer.getChildren().clear();
        
        for (CartItem item : cartItems) {
            HBox itemBox = createCartItemBox(item);
            cartItemsContainer.getChildren().add(itemBox);
        }
    }
    
    /**
     * Create shopping cart items
     */
    private HBox createCartItemBox(CartItem item) {
        // Integral container
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        // Product Information
        VBox infoBox = new VBox(8);
        // Reduce the size of the product information area to make more space for the "Subtotal" and "Delete" buttons.
        infoBox.setPrefWidth(200);
        
        Label nameLabel = new Label(item.getProductName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        
        Label brandLabel = new Label("Brand: " + item.getBrand());
        brandLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #666;");
        
        Label priceLabel = new Label("Unit Price: ¥" + item.getPrice().setScale(2, BigDecimal.ROUND_HALF_UP));
        priceLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #f44336;");
        
        infoBox.getChildren().addAll(nameLabel, brandLabel, priceLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Quantity control
        HBox quantityBox = new HBox(8);
        quantityBox.setAlignment(Pos.CENTER);
        
        Button decreaseBtn = new Button("-");
        decreaseBtn.setPrefSize(26, 26);
        decreaseBtn.setStyle("-fx-background-color: #f0f0f0; -fx-font-size: 16; -fx-font-weight: bold;");
        decreaseBtn.setOnAction(e -> updateQuantity(item, item.getQuantity() - 1));
        
        TextField quantityField = new TextField(String.valueOf(item.getQuantity()));
        quantityField.setPrefWidth(45);
        quantityField.setAlignment(Pos.CENTER);
        quantityField.setEditable(false);
        quantityField.setStyle("-fx-font-size: 14;");
        
        Button increaseBtn = new Button("+");
        increaseBtn.setPrefSize(26, 26);
        increaseBtn.setStyle("-fx-background-color: #f0f0f0; -fx-font-size: 16; -fx-font-weight: bold;");
        increaseBtn.setOnAction(e -> updateQuantity(item, item.getQuantity() + 1));
        
        quantityBox.getChildren().addAll(decreaseBtn, quantityField, increaseBtn);
        
        // Subtotal
        Label subtotalLabel = new Label("Subtotal: ¥" + item.getSubtotal().setScale(2, BigDecimal.ROUND_HALF_UP));
        // Widen the subtotal column to ensure the full amount is displayed.
        subtotalLabel.setPrefWidth(170);
        subtotalLabel.setAlignment(Pos.CENTER_RIGHT);
        subtotalLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #f44336;");
        
        // Delete button
        Button deleteBtn = new Button("Delete");
        deleteBtn.setPrefWidth(60);
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> removeItem(item));
        
        box.getChildren().addAll(infoBox, spacer, quantityBox, subtotalLabel, deleteBtn);
        
        return box;
    }
    
    /**
     * Update product quantity
     */
    private void updateQuantity(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(item);
            return;
        }
        
        try {
            boolean success = cartDAO.updateCartItemQuantity(item.getCartId(), newQuantity);
            if (success) {
                loadCartItems();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update quantity");
        }
    }
    
    /**
     * Delete product
     */
    private void removeItem(CartItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to remove this item from the cart?");
        
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        confirm.getButtonTypes().setAll(yesButton, noButton);
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try {
                boolean success = cartDAO.removeFromCart(item.getCartId());
                if (success) {
                    loadCartItems();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete");
            }
        }
    }
    
    /**
     * Update order summary
     */
    private void updateSummary() {
        int totalItems = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
            totalAmount = totalAmount.add(item.getSubtotal());
        }
        
        itemCountLabel.setText(totalItems + " items");
        subtotalLabel.setText("¥" + totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
        totalLabel.setText("¥" + totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
    }
    
    /**
     * Empty shopping cart
     */
    @FXML
    private void handleClearCart() {
        if (cartItems.isEmpty()) {
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Clear");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to clear the cart?");
        
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        confirm.getButtonTypes().setAll(yesButton, noButton);
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try {
                int userId = Session.getInstance().getCurrentUser().getUserId();
                boolean success = cartDAO.clearCart(userId);
                if (success) {
                    loadCartItems();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to clear cart");
            }
        }
    }
    
    /**
     * Go to settlement
     */
    @FXML
    private void handleCheckout() {
        if (!Session.getInstance().isLoggedIn()) {
            // If the user is not logged in, they will be redirected to the login page.
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) checkoutButton.getScene().getWindow();
                stage.setScene(new Scene(root, 400, 500));
                stage.setTitle("Login - SmartBuy");
                
                showAlert(Alert.AlertType.WARNING, "Notice", "Please log in first to proceed with checkout!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot open login page");
            }
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Checkout.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) checkoutButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("Checkout - SmartBuy");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open checkout page");
        }
    }
    
    /**
     * Return to homepage
     */
    @FXML
    private void handleBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cartItemsContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("SmartBuy - Electronics Store");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Log out
     */
    @FXML
    private void handleLogout() {
        Session.getInstance().logout();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cartItemsContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
            stage.setTitle("Login - SmartBuy");
            
        } catch (IOException e) {
            e.printStackTrace();
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
