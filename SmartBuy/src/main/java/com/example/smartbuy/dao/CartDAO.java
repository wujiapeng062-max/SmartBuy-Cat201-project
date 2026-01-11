package com.example.smartbuy.dao;

import com.example.smartbuy.model.CartItem;
import com.example.smartbuy.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车数据访问对象
 */
public class CartDAO {
    
    /**
     * 获取用户购物车中的所有商品
     */
    public List<CartItem> getCartItems(int userId) throws SQLException {
        String sql = "SELECT c.*, p.product_name, p.brand, p.price, p.image_url " +
                    "FROM cart c " +
                    "JOIN products p ON c.product_id = p.product_id " +
                    "WHERE c.user_id = ?";
        
        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setCartId(rs.getInt("cart_id"));
                item.setUserId(rs.getInt("user_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setBrand(rs.getString("brand"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setQuantity(rs.getInt("quantity"));
                item.setImageUrl(rs.getString("image_url"));
                item.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                items.add(item);
            }
        }
        return items;
    }
    
    /**
     * 添加商品到购物车
     */
    public boolean addToCart(int userId, int productId, int quantity) throws SQLException {
        String sql = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, quantity);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 更新购物车商品数量
     */
    public boolean updateCartItemQuantity(int cartId, int quantity) throws SQLException {
        String sql = "UPDATE cart SET quantity = ? WHERE cart_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, cartId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 从购物车删除商品
     */
    public boolean removeFromCart(int cartId) throws SQLException {
        String sql = "DELETE FROM cart WHERE cart_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cartId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 清空用户购物车
     */
    public boolean clearCart(int userId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 获取购物车商品总数
     */
    public int getCartItemCount(int userId) throws SQLException {
        String sql = "SELECT SUM(quantity) FROM cart WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
