package com.ecommerce.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.auth.jwt.JwtProvider;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.user.DTO.RegisterUserRequestDto;
import com.ecommerce.user.DTO.RegisterUserResponseDto;
import com.ecommerce.user.DTO.TokenResponseDto;
import com.ecommerce.user.Exception.RefreshTokenException;
import com.ecommerce.user.Exception.SessionExpiredException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.repository.UserRepository;

@Service
public class UserService {
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final JwtProvider jwtProvider;
    private final UserRedisService userRedisService;
    private final UserRepository userRepository;

    @Autowired
    public UserService(RefreshTokenRedisService refreshTokenRedisService,
                       JwtProvider jwtProvider,
                       UserRedisService userRedisService,
                       UserRepository userRepository) {
        this.refreshTokenRedisService = refreshTokenRedisService;
        this.jwtProvider = jwtProvider;
        this.userRedisService = userRedisService;
        this.userRepository = userRepository;
    }

    public RegisterUserResponseDto registerUser(String providerId, RegisterUserRequestDto registerUserRequestDto) {
        User user = userRedisService.get(providerId);
        if(user == null) {
            throw new SessionExpiredException("The temporary registration session has expired.");
        }
        user.setPhone(registerUserRequestDto.getPhone());
        user.setAddress(registerUserRequestDto.getAddress());
        Cart cart = new Cart();
        user.setCart(cart);

        userRepository.save(user);
        userRedisService.delete(providerId);

        String accessToken = jwtProvider.createAccessToken(providerId, UserRole.USER.name());
        String refreshToken = jwtProvider.createRefreshToken(providerId, UserRole.USER.name());

        refreshTokenRedisService.save(providerId, refreshToken);
        return new RegisterUserResponseDto(user, accessToken, refreshToken);
    }

    public TokenResponseDto refreshTokens(String sub, String role, String refreshToken) {
        String redisRefreshToken = refreshTokenRedisService.get(sub);
        if(!refreshToken.equals(redisRefreshToken)) {
            if(redisRefreshToken != null) {
                refreshTokenRedisService.delete(sub);
            }
            throw new RefreshTokenException("Refresh Token mismatch or expired");
        }
        String newAccessToken = jwtProvider.createAccessToken(sub, role);
        String newRefreshToken = jwtProvider.createRefreshToken(sub, role);

        refreshTokenRedisService.save(sub, newRefreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}
