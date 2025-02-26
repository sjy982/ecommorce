package com.ecommerce.product.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.ecommerce.category.model.Category;
import com.ecommerce.category.repository.CategoryRepository;
import com.ecommerce.product.dto.PriceStoreIdDto;
import com.ecommerce.product.dto.RegisterProductRequestDto;
import com.ecommerce.product.dto.RegisterProductResponseDto;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.projection.PriceStoreIdProjection;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.repository.StoreRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    public RegisterProductResponseDto registerProduct(String storeName, RegisterProductRequestDto dto) {
        Store store = storeRepository.findByName(storeName)
                .orElseThrow(() -> new UsernameNotFoundException(storeName + "Store not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Product newProduct = Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .description(dto.getDescription())
                .store(store)
                .category(category)
                .build();

        productRepository.save(newProduct);
        return new RegisterProductResponseDto(storeName, newProduct.getName(), newProduct.getPrice(), newProduct.getStock(), newProduct.getDescription(), category.getName());
    }

    public Product findById(long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("product not found"));
        return product;
    }

    //@CacheEvict Price가 변경되는 부분에 이 어노테이션을 붙여줘야 됨 왜냐하면 캐시 데이터와 불일치가 발생하기 때문 -> 쉽게 말해 캐시 제거용
    @Cacheable(value = "productPriceStoreId", key = "#productId")
    public PriceStoreIdDto findPriceAndStoreIdByProductId(Long productId) {
        PriceStoreIdProjection priceStoreIdProjection = productRepository.findPriceAndStoreIdByProductId(productId)
                .orElseThrow(() -> new UsernameNotFoundException("product not found"));

        return PriceStoreIdDto.builder()
                .price(priceStoreIdProjection.getPrice())
                .storeId(priceStoreIdProjection.getStoreId())
                              .build();
    }

    public void decreaseStock(Long id, int quantity) {
        int updated = productRepository.decreaseStock(id, quantity);
        if(updated == 0) {
            throw new UsernameNotFoundException("product not found");
        }
    }

    public Product checkQuantity(long id, int quantity) {
        Product product = findById(id);
        if(product.getStock() < quantity) {
            throw new UsernameNotFoundException("Out of stock.");
        }
        return product;

    }
}
