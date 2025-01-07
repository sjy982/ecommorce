package com.ecommerce.user.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ecommerce.user.model.User;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private Map<String, User> memory = new HashMap<>();
    @Override
    public User findByLoginId(String loginId) {
        return memory.get(loginId);
    }

    @Override
    public User save(User user) {
        memory.put(user.getProviderId(), user);
        return user;
    }
}
