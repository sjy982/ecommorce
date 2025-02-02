package com.ecommerce.user.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequestDto {

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "Phone number must be in the format 010-XXXX-XXXX.")
    private String phone;

    @NotBlank(message = "Address is required.")
    private String address;
}
