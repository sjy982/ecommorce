package com.ecommerce;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
class DatabaseTest {
    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("데이터베이스 연결이 정상적으로 이루어져야 한다.")
    void should_establish_database_connection() {
        // When: H2 데이터베이스 연결 확인
        Integer result = (Integer) entityManager.createNativeQuery("SELECT 1").getSingleResult();

        // Then: 데이터베이스 연결이 성공적으로 되었는지 확인
        assertThat(result).isEqualTo(1);
    }
}
