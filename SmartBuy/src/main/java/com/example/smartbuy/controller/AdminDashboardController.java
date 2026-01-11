package com.example.smartbuy.controller;

import com.example.smartbuy.dao.OrderDAO;
import com.example.smartbuy.dao.ProductDAO;
import com.example.smartbuy.dao.UserDAO;
import com.example.smartbuy.model.Order;
import com.example.smartbuy.model.Product;
import com.example.smartbuy.model.User;
import com.example.smartbuy.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Administrator console controller
 */
public class AdminDashboardController {
    
    @FXML
    private Label adminLabel;
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private Text totalProductsLabel;
    
    @FXML
    private Text totalOrdersLabel;
    
    @FXML
    private Text lowStockLabel;
    
    @FXML
    private Tab productsTab;
    
    @FXML
    private Tab ordersTab;
    
    @FXML
    private TableView<Product> productsTable;
    
    @FXML
    private TableView<Order> ordersTable;
    
    @FXML
    private TableView<User> usersTable;
    
    @FXML
    private TableView<CategoryStat> categoryStatsTable;
    
    @FXML
    private DatePicker startDatePicker;
    
    @FXML
    private DatePicker endDatePicker;
    
    @FXML
    private Label totalRevenueLabel;
    
    @FXML
    private Label totalOrderCountLabel;
    
    @FXML
    private Label totalItemsSoldLabel;
    
    @FXML
    private Label dateRangeOrderCountLabel;
    
    @FXML
    private Label dateRangeRevenueLabel;
    
    private ProductDAO productDAO = new ProductDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private UserDAO userDAO = new UserDAO();
    
    private ObservableList<Product> productsList = FXCollections.observableArrayList();
    private ObservableList<Order> ordersList = FXCollections.observableArrayList();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    
    @FXML
    private void initialize() {
        // Display administrator name
        if (Session.getInstance().getCurrentUser() != null) {
            adminLabel.setText("Admin: " + Session.getInstance().getCurrentUser().getFullName());
        }
        
        // Load statistics
        loadStatistics();
        
        // Initialize the product table
        setupProductsTable();
        
        // Initialize the order form
        setupOrdersTable();
        
        //Initialize user table
        setupUsersTable();
        
        // Load sales report data
        loadSalesReport();
    }
    
    /**
     * Load statistics
     */
    private void loadStatistics() {
        try {
            List<Product> products = productDAO.getAllProductsForAdmin();
            int productCount = products.size();
            int orderCount = orderDAO.getAllOrders().size();
            
            // Low inventory warning (inventory < 10)）
            long lowStockCount = products.stream()
                .filter(p -> p.getStock() < 10)
                .count();
            
            totalProductsLabel.setText(String.valueOf(productCount));
            totalOrdersLabel.setText(String.valueOf(orderCount));
            lowStockLabel.setText(String.valueOf(lowStockCount));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize the product table
     */
    @SuppressWarnings("unchecked")
    private void setupProductsTable() {
        // Clear existing columns
        productsTable.getColumns().clear();
        
        // ID column
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        idCol.setPrefWidth(50);
        
        // Name column
        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        nameCol.setPrefWidth(200);
        
        // Brand List
        TableColumn<Product, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        brandCol.setPrefWidth(100);
        
        // Category Column
        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryCol.setPrefWidth(100);
        
        // Price list
        TableColumn<Product, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(80);
        
        // Inventory column
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setPrefWidth(60);
        
        // Status column
        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isAvailable() ? "In Stock" : "Out of Stock")
        );
        statusCol.setPrefWidth(80);
        
        // Operation column
        TableColumn<Product, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(new Callback<TableColumn<Product, Void>, TableCell<Product, Void>>() {
            @Override
            public TableCell<Product, Void> call(TableColumn<Product, Void> param) {
                return new TableCell<Product, Void>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final Button toggleBtn = new Button("Toggle");
                    private final HBox hbox = new HBox(5, editBtn, toggleBtn, deleteBtn);
                    
                    {
                        hbox.setAlignment(Pos.CENTER);
                        editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11;");
                        toggleBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11;");
                        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11;");
                        
                        editBtn.setOnAction(e -> {
                            Product product = getTableView().getItems().get(getIndex());
                            handleEditProduct(product);
                        });
                        
                        toggleBtn.setOnAction(e -> {
                            Product product = getTableView().getItems().get(getIndex());
                            handleToggleProduct(product);
                        });
                        
                        deleteBtn.setOnAction(e -> {
                            Product product = getTableView().getItems().get(getIndex());
                            handleDeleteProduct(product);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : hbox);
                    }
                };
            }
        });
        
        productsTable.getColumns().addAll(idCol, nameCol, brandCol, categoryCol, priceCol, stockCol, statusCol, actionCol);
        
        // Loading data
        loadProducts();
    }
    
    /**
     * Initialize the order form
     */
    @SuppressWarnings("unchecked")
    private void setupOrdersTable() {
        ordersTable.getColumns().clear();
        
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        idCol.setPrefWidth(60);
        
        TableColumn<Order, Integer> userCol = new TableColumn<>("User ID");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userCol.setPrefWidth(60);
        
        TableColumn<Order, String> dateCol = new TableColumn<>("Order Date");
        dateCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        );
        dateCol.setPrefWidth(130);
        
        TableColumn<Order, String> addressCol = new TableColumn<>("Shipping Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("shippingAddress"));
        addressCol.setPrefWidth(200);
        
        TableColumn<Order, BigDecimal> amountCol = new TableColumn<>("Total Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        amountCol.setPrefWidth(80);
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        
        TableColumn<Order, String> paymentCol = new TableColumn<>("Payment Method");
        paymentCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentCol.setPrefWidth(80);
        
        TableColumn<Order, Void> detailCol = new TableColumn<>("Details");
        detailCol.setPrefWidth(60);
        detailCol.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button detailBtn = new Button("Details");
            private final HBox hbox = new HBox(detailBtn);
            
            {
                hbox.setAlignment(Pos.CENTER);
                detailBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                detailBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleViewOrderDetail(order);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
        
        TableColumn<Order, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button updateBtn = new Button("Update Status");
            private final HBox hbox = new HBox(updateBtn);
            
            {
                hbox.setAlignment(Pos.CENTER);
                updateBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                updateBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleUpdateOrderStatus(order);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
        
        ordersTable.getColumns().addAll(idCol, userCol, dateCol, addressCol, amountCol, statusCol, paymentCol, detailCol, actionCol);
        loadOrders();
    }
    
    /**
     * Load product data
     */
    private void loadProducts() {
        try {
            List<Product> products = productDAO.getAllProductsForAdmin();
            productsList.setAll(products);
            productsTable.setItems(productsList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products");
        }
    }
    
    /**
     * Load order data
     */
    private void loadOrders() {
        try {
            List<Order> orders = orderDAO.getAllOrders();
            ordersList.setAll(orders);
            ordersTable.setItems(ordersList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders");
        }
    }
    
    /**
     * Switch to Product Management
     */
    @FXML
    private void switchToProducts() {
        tabPane.getSelectionModel().select(productsTab);
    }
    
    /**
     * Switch to order management
     */
    @FXML
    private void switchToOrders() {
        tabPane.getSelectionModel().select(ordersTab);
    }
    
    /**
     * Add product
     */
    @FXML
    private void handleAddProduct() {
        showProductDialog(null);
    }
    
    /**
     * Edit product
     */
    private void handleEditProduct(Product product) {
        showProductDialog(product);
    }
    
    /**
     * Delete product
     */
    private void handleDeleteProduct(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete product '" + product.getProductName() + "'?");
        
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        confirm.getButtonTypes().setAll(yesButton, noButton);
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try {
                boolean success = productDAO.deleteProduct(product.getProductId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
                    loadProducts();
                    loadStatistics();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Deletion failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * Add/remove products
     */
    private void handleToggleProduct(Product product) {
        try {
            product.setAvailable(!product.isAvailable());
            boolean success = productDAO.updateProduct(product);
            if (success) {
                String status = product.isAvailable() ? "listed" : "delisted";
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product has been " + status + "!");
                loadProducts();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Operation failed");
        }
    }
    
    /**
     * Display the product editing dialog box
     */
    private void showProductDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/ProductDialog.fxml"));
            Parent root = loader.load();
            
            ProductDialogController controller = loader.getController();
            if (product != null) {
                controller.setProduct(product);
            }
            controller.setOnSaveCallback(() -> {
                loadProducts();
                loadStatistics();
            });
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(product == null ? "Add Product" : "Edit Product");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open edit dialog");
        }
    }
    
    /**
     * Update order status
     */
    private void handleUpdateOrderStatus(Order order) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(order.getStatus(), 
            "Pending", "Paid", "Shipped", "Completed", "Cancelled");
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Order ID: " + order.getOrderId());
        dialog.setContentText("Select new status:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().equals(order.getStatus())) {
            try {
                boolean success = orderDAO.updateOrderStatus(order.getOrderId(), result.get());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Order status updated successfully!");
                    loadOrders();
                    loadSalesReport();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Update failed");
            }
        }
    }
    
    /**
     * View order details
     */
    private void handleViewOrderDetail(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbuy/fxml/OrderDetailDialog.fxml"));
            Parent root = loader.load();
            
            OrderDetailDialogController controller = loader.getController();
            controller.setOrder(order);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Order Details");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open details page");
        }
    }
    
    /**
     * Initialize user table
     */
    @SuppressWarnings("unchecked")
    private void setupUsersTable() {
        usersTable.getColumns().clear();
        
        TableColumn<User, Integer> idCol = new TableColumn<>("User ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        idCol.setPrefWidth(80);
        
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(120);
        
        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(120);
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);
        
        TableColumn<User, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(250);
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(80);
        
        TableColumn<User, String> dateCol = new TableColumn<>("Registration Date");
        dateCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        );
        dateCol.setPrefWidth(150);
        
        usersTable.getColumns().addAll(idCol, usernameCol, nameCol, emailCol, phoneCol, addressCol, roleCol, dateCol);
        loadUsers();
    }
    
    /**
     * Load user data
     */
    private void loadUsers() {
        try {
            List<User> users = userDAO.getAllUsers();
            usersList.setAll(users);
            usersTable.setItems(usersList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user list");
        }
    }
    
    /**
     * Load sales report data
     */
    private void loadSalesReport() {
        try {
            List<Order> orders = orderDAO.getAllOrders();
            
            // Total number of orders
            int totalOrders = orders.size();
            totalOrderCountLabel.setText(String.valueOf(totalOrders));
            
            // Total sales
            BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalRevenueLabel.setText("￥" + totalRevenue);
            
            // Total sales (units)
            int totalItems = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToInt(item -> item.getQuantity())
                .sum();
            totalItemsSoldLabel.setText(String.valueOf(totalItems));
            
            // Statistics by Category
            loadCategoryStatistics();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load category statistics
     */
    @SuppressWarnings("unchecked")
    private void loadCategoryStatistics() {
        try {
            Map<String, CategoryStat> categoryStats = new HashMap<>();
            List<Order> orders = orderDAO.getAllOrders();
            
            // Category needs to be obtained through product information
            ProductDAO productDAO = new ProductDAO();
            
            for (Order order : orders) {
                for (var item : order.getOrderItems()) {
                    // Obtain category information from the product list
                    Product product = productDAO.getProductById(item.getProductId());
                    if (product != null) {
                        String category = product.getCategoryName();
                        categoryStats.putIfAbsent(category, new CategoryStat(category));
                        CategoryStat stat = categoryStats.get(category);
                        stat.addQuantity(item.getQuantity());
                        stat.addRevenue(item.getSubtotal());
                    }
                }
            }
            
            categoryStatsTable.getColumns().clear();
            
            TableColumn<CategoryStat, String> catCol = new TableColumn<>("Category");
            catCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
            catCol.setPrefWidth(150);
            
            TableColumn<CategoryStat, Integer> qtyCol = new TableColumn<>("Sales Volume");
            qtyCol.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));
            qtyCol.setPrefWidth(100);
            
            TableColumn<CategoryStat, BigDecimal> revenueCol = new TableColumn<>("Revenue");
            revenueCol.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
            revenueCol.setPrefWidth(150);
            
            categoryStatsTable.getColumns().addAll(catCol, qtyCol, revenueCol);
            ObservableList<CategoryStat> statsList = FXCollections.observableArrayList(categoryStats.values());
            categoryStatsTable.setItems(statsList);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Search by date range
     */
    @FXML
    private void handleDateRangeQuery() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Notice", "Please select start and end dates!");
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.WARNING, "Notice", "Start date cannot be later than end date!");
            return;
        }
        
        try {
            List<Order> allOrders = orderDAO.getAllOrders();
            List<Order> filteredOrders = new ArrayList<>();
            for (Order order : allOrders) {
                LocalDate orderDate = order.getOrderDate().toLocalDate();
                if (!orderDate.isBefore(startDate) && !orderDate.isAfter(endDate)) {
                    filteredOrders.add(order);
                }
            }
            
            int count = filteredOrders.size();
            BigDecimal revenue = BigDecimal.ZERO;
            for (Order order : filteredOrders) {
                revenue = revenue.add(order.getTotalAmount());
            }
            
            dateRangeOrderCountLabel.setText(String.valueOf(count));
            dateRangeRevenueLabel.setText("￥" + revenue);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Query failed");
        }
    }
    
    /**
     * Classification and statistics internal class
     */
    public static class CategoryStat {
        private String categoryName;
        private int totalQuantity;
        private BigDecimal totalRevenue;
        
        public CategoryStat(String categoryName) {
            this.categoryName = categoryName;
            this.totalQuantity = 0;
            this.totalRevenue = BigDecimal.ZERO;
        }
        
        public void addQuantity(int qty) {
            this.totalQuantity += qty;
        }
        
        public void addRevenue(BigDecimal amount) {
            this.totalRevenue = this.totalRevenue.add(amount);
        }
        
        public String getCategoryName() { return categoryName; }
        public int getTotalQuantity() { return totalQuantity; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
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
            
            Stage stage = (Stage) adminLabel.getScene().getWindow();
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
