package com.ecommerce.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String providerId;
    private String provider;
    private String subject;
    private String email;
    private String name;
    private String phone;
    private String address;
    private UserRole role;
}
