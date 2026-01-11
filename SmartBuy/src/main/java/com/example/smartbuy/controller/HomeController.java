package com.example.smartbuy.controller;

import com.example.smartbuy.dao.CartDAO;
import com.example.smartbuy.dao.CategoryDAO;
import com.example.smartbuy.dao.ProductDAO;
import com.example.smartbuy.model.Category;
import com.example.smartbuy.model.Product;
import com.example.smartbuy.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User Homepage Controller
 */
public class HomeController {
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Label userLabel;
    
    @FXML
    private Button cartButton;
    
    @FXML
    private Label categoryLabel;
    
    @FXML
    private GridPane productGrid;
    
    @FXML
    private HBox hotProductsBox;
    
    @FXML
    private Button categoryBtn1;
    
    @FXML
    private Button categoryBtn2;
    
    @FXML
    private Button categoryBtn3;
    
    @FXML
    private Button categoryBtn4;
    
    @FXML
    private TextField minPriceField;
    
    @FXML
    private TextField maxPriceField;
    
    @FXML
    private ComboBox<String> brandCombo;
    
    @FXML
    private ComboBox<String> sortCombo;
    
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private CartDAO cartDAO = new CartDAO();
    
    private int currentCategoryId = -1;
    private List<Product> allProducts = new ArrayList<>();
    
    @FXML
    private void initialize() {
        // Display username
        if (Session.getInstance().getCurrentUser() != null) {
            userLabel.setText("Welcome, " + Session.getInstance().getCurrentUser().getFullName());
        } else {
            userLabel.setText("Welcome to SmartBuy!");
        }
        
        // Quantity of items loaded into shopping cart
        updateCartCount();
        
        // Initialize the filter control
        initializeFilters();
        
        // Load popular products
        loadHotProducts();
        
        // Load all products
        loadProducts(-1);
    }
    
    /**
     * Initialize the filter control
     */
    private void initializeFilters() {
        // Sort options
        sortCombo.getItems().addAll("Price: Low to High", "Price: High to Low", "Sales Volume");
        
        // The brand options will be dynamically populated after the products are loaded.
        brandCombo.getItems().add("All Brands");
    }
    
    /**
     * Load popular products (sorted by inventory or sales volume; here, low inventory is used as the popular category).
     */
    private void loadHotProducts() {
        try {
            List<Product> allProducts = productDAO.getAllProducts();
            // Select the top 5 products as popular recommendations.
            List<Product> hotProducts = allProducts.stream()
                .limit(5)
                .collect(Collectors.toList());
            
            hotProductsBox.getChildren().clear();
            
            for (Product product : hotProducts) {
                VBox hotCard = createHotProductCard(product);
                hotProductsBox.getChildren().add(hotCard);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create popular product cards (simplified version)）
     */
    private VBox createHotProductCard(Product product) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;");
        card.setPrefWidth(150);
        card.setPrefHeight(180);
        
        card.setOnMouseClicked(e -> handleViewProductDetail(product));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FFF8E1; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;"));
        
        // Product Images
        ImageView imageView = new ImageView();
        Image img = loadProductImage(product.getImageUrl());
        imageView.setImage(img);
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        // Add image loading status listener
        if (img != null) {
        }
        
        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(130);
        nameLabel.setAlignment(Pos.CENTER);
        
        Label priceLabel = new Label("￥" + product.getPrice());
        priceLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #FF6F00;");
        
        Button viewBtn = new Button("View");
        viewBtn.setPrefWidth(120);
        viewBtn.setStyle("-fx-background-color: #FF6F00; -fx-text-fill: white; -fx-font-size: 11;");
        viewBtn.setOnAction(e -> {
            e.consume();
            handleViewProductDetail(product);
        });
        
        card.getChildren().addAll(imageView, nameLabel, priceLabel, viewBtn);
        
        return card;
    }
    
    /**
     * Load products
     */
    private void loadProducts(int categoryId) {
        try {
            currentCategoryId = categoryId;
            
            if (categoryId == -1) {
                allProducts = productDAO.getAllProducts();
                categoryLabel.setText("All Products");
            } else {
                allProducts = productDAO.getProductsByCategory(categoryId);
                Category category = categoryDAO.getCategoryById(categoryId);
                categoryLabel.setText(category != null ? category.getCategoryName() : "Product List");
            }
            
            // Update brand list
            updateBrandList();
            
            displayProducts(allProducts);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
    }
    
    /**
     * Update brand list
     */
    private void updateBrandList() {
        brandCombo.getItems().clear();
        brandCombo.getItems().add("All Brands");
        
        List<String> brands = allProducts.stream()
            .map(Product::getBrand)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        brandCombo.getItems().addAll(brands);
        brandCombo.getSelectionModel().selectFirst();
    }
    
    /**
     * Display product grid
     */
    private void displayProducts(List<Product> products) {
        productGrid.getChildren().clear();
        
        int col = 0;
        int row = 0;
        
        for (Product product : products) {
            VBox productCard = createProductCard(product);
            productGrid.add(productCard, col, row);
            
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }
    
    /**
     * Create product cards
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand;");
        card.setPrefWidth(200);
        card.setPrefHeight(280);
        
        // Click the card to go to the details page.
        card.setOnMouseClicked(e -> handleViewProductDetail(product));
        
        // Mouse hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand;"));
        
        // Product Images
        ImageView imageView = new ImageView();
        Image img = loadProductImage(product.getImageUrl());
        imageView.setImage(img);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        // Add image loading status listener
        if (img != null) {
        }
        
        // Product Name
        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);
        
        // brand
        Label brandLabel = new Label(product.getBrand());
        brandLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        
        // price
        Label priceLabel = new Label("¥" + product.getPrice());
        priceLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #f44336;");
        
        // Inventory status (low inventory alert)
        HBox stockBox = new HBox(5);
        stockBox.setAlignment(Pos.CENTER);
        Label stockLabel = new Label("Stock: " + product.getStock());
        if (product.getStock() < 10) {
            stockLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #ff0000; -fx-font-weight: bold;");
            Label warningLabel = new Label("⚠️");
            stockBox.getChildren().addAll(stockLabel, warningLabel);
        } else {
            stockLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");
            stockBox.getChildren().add(stockLabel);
        }
        
        // Add to cart button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.setPrefWidth(150);
        addToCartBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addToCartBtn.setOnAction(e -> {
            e.consume(); // Preventing event bubbling
            handleAddToCart(product);
        });
        
        if (product.getStock() <= 0) {
            addToCartBtn.setDisable(true);
            addToCartBtn.setText("Out of Stock");
        }
        
        card.getChildren().addAll(imageView, nameLabel, brandLabel, priceLabel, stockBox, addToCartBtn);
        
        return card;
    }
    
    /**
     * View product details
     */
    private void handleViewProductDetail(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/ProductDetail.fxml"));
            Parent root = loader.load();
            
            ProductDetailController controller = loader.getController();
            controller.setProductId(product.getProductId());
            
            Stage stage = (Stage) productGrid.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Product Detail - SmartBuy");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open product detail page");
        }
    }
    
    /**
     * add to the cart
     */
    private void handleAddToCart(Product product) {
        Stage stage = (Stage) productGrid.getScene().getWindow();
        
        if (!LoginPromptController.checkLogin(stage)) {
            // The user chooses not to log in and is directly returned.
            return;
        }
        
        try {
            int userId = Session.getInstance().getCurrentUser().getUserId();
            boolean success = cartDAO.addToCart(userId, product.getProductId(), 1);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Added to cart!");
                updateCartCount();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", "Failed to add to cart!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Operation failed: " + e.getMessage());
        }
    }
    
    /**
     * Update shopping cart quantity
     */
    private void updateCartCount() {
        if (Session.getInstance().isLoggedIn()) {
            try {
                int userId = Session.getInstance().getCurrentUser().getUserId();
                int count = cartDAO.getCartItemCount(userId);
                cartButton.setText("Cart (" + count + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            cartButton.setText("Cart");
        }
    }
    
    /**
     * Search for products
     */
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadProducts(-1);
            return;
        }
        
        try {
            List<Product> products = productDAO.searchProducts(keyword);
            categoryLabel.setText("Search Results: " + keyword);
            displayProducts(products);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Search failed: " + e.getMessage());
        }
    }
    
    /**
     * Show all products
     */
    @FXML
    private void showAllProducts() {
        currentCategoryId = -1;
        loadProducts(-1);
    }
    
    /**
     * Filter by category
     */
    @FXML
    private void filterByCategory(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String buttonText = btn.getText();
        
        // Map Chinese button text to English category names in the database
        String categoryEnglishName = mapDisplayToDatabaseCategory(buttonText);
        
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            for (Category cat : categories) {
                if (cat.getCategoryName().equals(categoryEnglishName)) {
                    currentCategoryId = cat.getCategoryId();
                    loadProducts(currentCategoryId);
                    return;
                }
            }
            
            // If no matching category is found, an error message will be displayed.
            showAlert(Alert.AlertType.WARNING, "Notice", "Category not found: " + buttonText);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Category filtering failed: " + e.getMessage());
        }
    }
    
    /**
     * Map the displayed category names to the category names in the database.
     */
    private String mapDisplayToDatabaseCategory(String displayName) {
        switch (displayName) {
            case "Phones":
                return "Smartphones";
            case "Laptops":
                return "Laptops";
            case "Audio Devices":
                return "Audio";
            case "Accessories":
                return "Accessories";
            default:
                return displayName; // If the name is not a predefined name, return the original value.
        }
    }
    
    /**
     * Open shopping cart
     */
    @FXML
    private void handleCart() {
        Stage stage = (Stage) cartButton.getScene().getWindow();
        
        if (!Session.getInstance().isLoggedIn()) {
            // A user who is not logged in is prompted to log in when clicking on the shopping cart.
            if (!LoginPromptController.checkLogin(stage)) {
                // The user chooses not to log in and is directly returned.
                return;
            }
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Cart.fxml"));
            Parent root = loader.load();
            
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Cart - SmartBuy");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open cart page");
        }
    }
    
    /**
     * Open order history
     */
    @FXML
    private void handleOrders() {
        Stage stage = (Stage) cartButton.getScene().getWindow();
        
        if (!Session.getInstance().isLoggedIn()) {
            // Unlogged-in users are prompted to log in when clicking on order history.
            if (!LoginPromptController.checkLogin(stage)) {
                // The user chooses not to log in and is directly returned.
                return;
            }
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/OrderHistory.fxml"));
            Parent root = loader.load();
            
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("My Orders - SmartBuy");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open order page");
        }
    }
    
    /**
     * Application Filtering
     */
    @FXML
    private void applyFilters() {
        List<Product> filteredProducts = new ArrayList<>(allProducts);
        
        // 1. Price Filter
        String minPriceStr = minPriceField.getText().trim();
        String maxPriceStr = maxPriceField.getText().trim();
        
        try {
            if (!minPriceStr.isEmpty()) {
                BigDecimal minPrice = new BigDecimal(minPriceStr);
                filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
            }
            
            if (!maxPriceStr.isEmpty()) {
                BigDecimal maxPrice = new BigDecimal(maxPriceStr);
                filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Notice", "Please enter a valid price!");
            return;
        }
        
        // 2.Brand Selection
        String selectedBrand = brandCombo.getValue();
        if (selectedBrand != null && !"All Brands".equals(selectedBrand)) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> p.getBrand().equals(selectedBrand))
                .collect(Collectors.toList());
        }
        
        // 3. sequence
        String sortOption = sortCombo.getValue();
        if (sortOption != null) {
            switch (sortOption) {
                case "Price: Low to High":
                    filteredProducts.sort(Comparator.comparing(Product::getPrice));
                    break;
                case "Price: High to Low":
                    filteredProducts.sort(Comparator.comparing(Product::getPrice).reversed());
                    break;
                case "Sales Volume":
                    // Can sort by inventory or sales volume, using inventory as example here
                    filteredProducts.sort(Comparator.comparing(Product::getStock).reversed());
                    break;
            }
        }
        
        displayProducts(filteredProducts);
    }
    
    /**
     * Clear Filter
     */
    @FXML
    private void clearFilters() {
        minPriceField.clear();
        maxPriceField.clear();
        brandCombo.getSelectionModel().selectFirst();
        sortCombo.getSelectionModel().clearSelection();
        
        displayProducts(allProducts);
    }

    /**
     * Unified loading of product images:
     * - If the URL starts with http/https/file, load it directly by URL.
     * - Otherwise, it is assumed to be a resource filename and loaded from /com/example/smartbuy/images/.
     * - If loading fails or the file is empty, use default_product.jpg.
     */
    private Image loadProductImage(String imageUrl) {
        // Load default image
        Image fallback = loadImageFromResource("/com/example/smartbuy/images/default_product.jpg");

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return fallback;
        }

        imageUrl = imageUrl.trim();
        String lower = imageUrl.toLowerCase();

        //1. Full URL (http / https / file)
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:")) {
            try {
                Image img = new Image(imageUrl, false);
                if (!img.isError()) {
                    return img;
                }
            } catch (Exception e) {
            }
        }

        // 2. Handling classpath resources
        String normalized = imageUrl;
        
        // Remove the src/main/resources prefix
        if (normalized.replace("\\", "/").startsWith("src/main/resources")) {
            normalized = normalized.substring("src/main/resources".length());
            if (!normalized.startsWith("/")) {
                normalized = "/" + normalized;
            }
        }
        
        // If it's already a complete classpath (starting with /com/), try loading it directly.
        if (normalized.startsWith("/com/")) {
            Image img = loadImageFromResource(normalized);
            if (img != null) {
                return img;
            }
        }
        
        // 3. Load from the images directory by filename
        String fileName = normalized.contains("/") 
            ? normalized.substring(normalized.lastIndexOf('/') + 1)
            : normalized;
        String resourcePath = "/com/example/smartbuy/images/" + fileName;
        Image img = loadImageFromResource(resourcePath);
        if (img != null) {
            return img;
        }

        return fallback;
    }
    
    /**
     * Loading images from classpath resources
     */
    private Image loadImageFromResource(String resourcePath) {
        try {
            // Load classpath resources directly using JavaFX Image
            var url = getClass().getResource(resourcePath);
            if (url != null) {
                String urlString = url.toExternalForm();
                Image img = new Image(urlString, true);
                return img;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Open profile page
     */
    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/Profile.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 600));
            stage.setTitle("Profile - SmartBuy");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open profile page");
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
            
            Stage stage = (Stage) userLabel.getScene().getWindow();
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
