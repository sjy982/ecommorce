package com.ecommerce.product.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterProductRequestDto {

    @NotBlank(message = "name is required.")
    private String name;

    @NotNull(message = "price is required.")
    private Integer price;

    @NotNull(message = "stock is required.")
    private Integer stock;

    @NotBlank(message = "description is required.")
    private String description;

    @NotNull(message = "categoryId is required.")
    private Integer categoryId;
}
