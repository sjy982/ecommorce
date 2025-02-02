package com.ecommerce.store.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginStoreRequestDto {
    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "password is required")
    private String password;
}
