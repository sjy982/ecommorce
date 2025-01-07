package com.ecommerce.user.repository;

import com.ecommerce.user.model.User;

public interface UserRepository {
    User findByLoginId(String loginId);
    User save(User user);
}
