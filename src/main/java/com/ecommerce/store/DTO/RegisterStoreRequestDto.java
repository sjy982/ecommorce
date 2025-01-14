package com.ecommerce.store.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterStoreRequestDto {
    @NotBlank(message = "name is required.")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "name must not contain special characters, including underscores (_).")
    private String name;

    @NotBlank(message = "password is required")
    private String password;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "Phone number must be in the format 010-XXXX-XXXX.")
    private String phoneNumber;
}
