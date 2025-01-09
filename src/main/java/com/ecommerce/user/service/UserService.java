package com.ecommerce.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.auth.jwt.JwtProvider;
import com.ecommerce.user.Dto.RegisterUserRequestDto;
import com.ecommerce.user.Dto.RegisterUserResponseDto;
import com.ecommerce.user.Dto.TokenResponseDto;
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

        userRepository.save(user);
        userRedisService.delete(providerId);

        String accessToken = jwtProvider.createAccessToken(providerId, UserRole.USER);
        String refreshToken = jwtProvider.createRefreshToken(providerId);

        refreshTokenRedisService.save(providerId, refreshToken);
        return new RegisterUserResponseDto(user, accessToken, refreshToken);
    }

    public TokenResponseDto refreshTokens(String providerId, String refreshToken) {
        String redisRefreshToken = refreshTokenRedisService.get(providerId);
        if(!refreshToken.equals(redisRefreshToken)) {
            if(redisRefreshToken != null) {
                refreshTokenRedisService.delete(providerId);
            }
            throw new RefreshTokenException("Refresh Token mismatch or expired");
        }
        String newAccessToken = jwtProvider.createAccessToken(providerId, UserRole.USER);
        String newRefreshToken = jwtProvider.createRefreshToken(providerId);

        refreshTokenRedisService.save(providerId, newRefreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}
