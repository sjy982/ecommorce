package com.ecommerce.cartItem.projection;

public interface CartItemProjection {
    Long getCartItemId();
    Long getProductId();
    String getProductName();
    Long getProductPrice();
    Integer getQuantity();
}
