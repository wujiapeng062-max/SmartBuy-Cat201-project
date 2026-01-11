package com.example.smartbuy.dao;

import com.example.smartbuy.model.Product;
import com.example.smartbuy.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *Product Data Access Object
 */
public class ProductDAO {
    
    /**
     * Get all available products (user front-end)
     */
    public List<Product> getAllProducts() throws SQLException {
        String sql = "SELECT p.*, c.category_name FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "WHERE p.is_available = TRUE ORDER BY p.created_at DESC";
        
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        }
        return products;
    }
    
    /**
     * Get all products (admin backend, including those that are no longer available)
     */
    public List<Product> getAllProductsForAdmin() throws SQLException {
        String sql = "SELECT p.*, c.category_name FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "ORDER BY p.created_at DESC";
        
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        }
        return products;
    }
    
    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        String sql = "SELECT p.*, c.category_name FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "WHERE p.category_id = ? AND p.is_available = TRUE";
        
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        }
        return products;
    }
    
    /**
     * Get product by ID
     */
    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT p.*, c.category_name FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "WHERE p.product_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }
        }
        return null;
    }
    
    /**
     *Search for products (by name or brand)
     */
    public List<Product> searchProducts(String keyword) throws SQLException {
        String sql = "SELECT p.*, c.category_name FROM products p " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "WHERE (p.product_name LIKE ? OR p.brand LIKE ?) AND p.is_available = TRUE";
        
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        }
        return products;
    }
    
    /**
     *Add Product (Administrator)
     */
    public boolean addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (product_name, brand, category_id, price, stock, description, specs, image_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getBrand());
            pstmt.setInt(3, product.getCategoryId());
            pstmt.setBigDecimal(4, product.getPrice());
            pstmt.setInt(5, product.getStock());
            pstmt.setString(6, product.getDescription());
            pstmt.setString(7, product.getSpecs());
            pstmt.setString(8, product.getImageUrl());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update Product
     */
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET product_name = ?, brand = ?, category_id = ?, " +
                    "price = ?, stock = ?, description = ?, specs = ?, image_url = ?, is_available = ? " +
                    "WHERE product_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getBrand());
            pstmt.setInt(3, product.getCategoryId());
            pstmt.setBigDecimal(4, product.getPrice());
            pstmt.setInt(5, product.getStock());
            pstmt.setString(6, product.getDescription());
            pstmt.setString(7, product.getSpecs());
            pstmt.setString(8, product.getImageUrl());
            pstmt.setBoolean(9, product.isAvailable());
            pstmt.setInt(10, product.getProductId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete product
     */
    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update Inventory
     */
    public boolean updateStock(int productId, int newStock) throws SQLException {
        String sql = "UPDATE products SET stock = ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, productId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Extract Product object from ResultSet
     */
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setBrand(rs.getString("brand"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setDescription(rs.getString("description"));
        product.setSpecs(rs.getString("specs"));
        product.setImageUrl(rs.getString("image_url"));
        product.setAvailable(rs.getBoolean("is_available"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return product;
    }
}
