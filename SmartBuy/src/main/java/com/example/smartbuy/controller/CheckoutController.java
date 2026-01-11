package com.example.smartbuy.controller;

import com.example.smartbuy.dao.CartDAO;
import com.example.smartbuy.dao.OrderDAO;
import com.example.smartbuy.dao.ProductDAO;
import com.example.smartbuy.model.CartItem;
import com.example.smartbuy.model.Order;
import com.example.smartbuy.model.OrderItem;
import com.example.smartbuy.model.Product;
import com.example.smartbuy.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.example.smartbuy.util.DatabaseUtil;

/**
 * Order settlement controller
 */
public class CheckoutController {
    
    @FXML
    private Label receiverNameLabel;
    
    @FXML
    private Label receiverPhoneLabel;
    
    @FXML
    private TextArea shippingAddressField;
    
    @FXML
    private VBox orderItemsContainer;
    
    @FXML
    private ToggleGroup paymentMethodGroup;
    
    @FXML
    private Label itemCountLabel;
    
    @FXML
    private Label subtotalLabel;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Button submitOrderButton;
    
    private CartDAO cartDAO = new CartDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private ProductDAO productDAO = new ProductDAO();
    
    private List<CartItem> cartItems;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @FXML
    private void initialize() {
        loadUserInfo();
        loadOrderItems();
    }
    
    /**
     *Load user information
     */
    private void loadUserInfo() {
        if (!Session.getInstance().isLoggedIn()) {
            // If the user is not logged in, they will be redirected to the login page.
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) receiverNameLabel.getScene().getWindow();
                stage.setScene(new Scene(root, 400, 500));
                stage.setTitle("Login - SmartBuy");
                
                showAlert(Alert.AlertType.WARNING, "Notice", "Please log in first to proceed with checkout!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot open login page");
            }
            return;
        }
        
        var user = Session.getInstance().getCurrentUser();
        receiverNameLabel.setText(user.getFullName());
        receiverPhoneLabel.setText(user.getPhone() != null ? user.getPhone() : "Not set");
        shippingAddressField.setText(user.getAddress() != null ? user.getAddress() : "");
    }
    
    /**
     * Loading order items
     */
    private void loadOrderItems() {
        if (!Session.getInstance().isLoggedIn()) {
            // If the user is not logged in, they will be redirected to the login page.
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) receiverNameLabel.getScene().getWindow();
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
            int userId = Session.getInstance().getCurrentUser().getUserId();
            cartItems = cartDAO.getCartItems(userId);
            
            if (cartItems.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Notice", "Cart is empty, please add items first");
                handleBackToCart();
                return;
            }
            
            displayOrderItems();
            updateSummary();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load order: " + e.getMessage());
        }
    }
    
    /**
     * Display order items
     */
    private void displayOrderItems() {
        orderItemsContainer.getChildren().clear();
        
        for (CartItem item : cartItems) {
            HBox itemBox = createOrderItemBox(item);
            orderItemsContainer.getChildren().add(itemBox);
        }
    }
    
    /**
     * Create order item
     */
    private HBox createOrderItemBox(CartItem item) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        VBox infoBox = new VBox(5);
        infoBox.setPrefWidth(350);
        
        Label nameLabel = new Label(item.getProductName());
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        
        Label brandLabel = new Label("Brand: " + item.getBrand());
        brandLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        
        infoBox.getChildren().addAll(nameLabel, brandLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label priceLabel = new Label("¥" + item.getPrice());
        priceLabel.setPrefWidth(100);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);
        priceLabel.setStyle("-fx-font-size: 14;");
        
        Label quantityLabel = new Label("x " + item.getQuantity());
        quantityLabel.setPrefWidth(60);
        quantityLabel.setAlignment(Pos.CENTER);
        quantityLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");
        
        Label subtotalLabel = new Label("¥" + item.getSubtotal());
        subtotalLabel.setPrefWidth(100);
        subtotalLabel.setAlignment(Pos.CENTER_RIGHT);
        subtotalLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #f44336;");
        
        box.getChildren().addAll(infoBox, spacer, priceLabel, quantityLabel, subtotalLabel);
        
        return box;
    }
    
    /**
     * Update order summary
     */
    private void updateSummary() {
        int totalItems = 0;
        totalAmount = BigDecimal.ZERO;
        
        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
            totalAmount = totalAmount.add(item.getSubtotal());
        }
        
        itemCountLabel.setText(totalItems + " items");
        subtotalLabel.setText("¥" + totalAmount);
        totalLabel.setText("¥" + totalAmount);
    }
    
    /**
     * Submit an order
     */
    @FXML
    private void handleSubmitOrder() {
        // Verify if the user is logged in
        if (!Session.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please log in first to submit an order!");
            return;
        }
        
        String shippingAddress = shippingAddressField.getText().trim();
        
        // Verify shipping address
        if (shippingAddress.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Notice", "Please fill in the shipping address!");
            return;
        }
        
        // Get payment method
        RadioButton selectedPayment = (RadioButton) paymentMethodGroup.getSelectedToggle();
        String paymentMethod = selectedPayment.getUserData().toString();
        
        try {
            int userId = Session.getInstance().getCurrentUser().getUserId();
            
            // Check inventory
            for (CartItem item : cartItems) {
                Product product = productDAO.getProductById(item.getProductId());
                if (product.getStock() < item.getQuantity()) {
                    showAlert(Alert.AlertType.ERROR, "Insufficient Stock", 
                             product.getProductName() + " Insufficient stock!\nCurrent stock: " + product.getStock());
                    return;
                }
            }
            
            // Create an order
            Order order = new Order(userId, totalAmount, shippingAddress, paymentMethod);
            
            // Add order details
            for (CartItem item : cartItems) {
                OrderItem orderItem = new OrderItem(
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPrice()
                );
                order.addOrderItem(orderItem);
            }
            
            // Save the order and deduct the inventory
            int orderId = createOrderAndUpdateStock(order);
            
            if (orderId > 0) {
                // Empty shopping cart
                cartDAO.clearCart(userId);
                
                // Success message displayed
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Order Submitted Successfully");
                successAlert.setHeaderText("Congratulations, your order has been submitted successfully!");
                successAlert.setContentText("Order ID: " + orderId + "\nTotal Amount: ¥" + totalAmount + 
                                           "\n\nYou can view order details in order history.");
                
                // Set the button text to English.
                ButtonType okButton = new ButtonType("OK");
                successAlert.getButtonTypes().setAll(okButton);
                
                successAlert.showAndWait();
                
                // Return to homepage
                handleBackToHome();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Order submission failed: " + e.getMessage());
        }
    }
    
    /**
     * Create an order and update inventory (transaction processing)
     */
    private int createOrderAndUpdateStock(Order order) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Create an order
            int orderId = orderDAO.createOrder(order);
            
            // Deduct inventory
            String updateStockSql = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateStockSql)) {
                for (OrderItem item : order.getOrderItems()) {
                    pstmt.setInt(1, item.getQuantity());
                    pstmt.setInt(2, item.getProductId());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            
            conn.commit();
            return orderId;
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Return to shopping cart
     */
    @FXML
    private void handleBackToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Cart.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) submitOrderButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Cart - SmartBuy");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Return to homepage
     */
    private void handleBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) submitOrderButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("SmartBuy - Electronics Store");
            
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
        alert.showAndWait();
    }
}
