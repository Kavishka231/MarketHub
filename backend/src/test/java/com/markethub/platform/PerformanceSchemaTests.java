package com.markethub.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PerformanceSchemaTests {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void migrationCreatesIndexesForPrimaryListingPaths() {
        List<String> indexes = jdbc.queryForList(
                "select index_name from information_schema.indexes where table_schema='PUBLIC'",
                String.class);

        assertThat(indexes).contains(
                "IDX_PRODUCTS_PUBLIC_NEWEST",
                "IDX_PRODUCTS_PUBLIC_CATEGORY_NEWEST",
                "IDX_ORDERS_CUSTOMER_NEWEST",
                "IDX_REVIEWS_PRODUCT_NEWEST");
    }
}