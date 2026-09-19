package com.markethub.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(CategoryCacheTests.Config.class)
class CategoryCacheTests {

    @Autowired
    private CategoryService service;

    @Autowired
    private CategoryRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void resetCache() {
        reset(repository);
        cacheManager.getCache("activeCategories").clear();
    }

    @Test
    void cachesActiveCategoriesAndEvictsAfterMutation() {
        Category category = new Category("Electronics", "electronics", "Devices");
        when(repository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(category));
        when(repository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(repository.existsBySlug(any())).thenReturn(false);
        when(repository.saveAndFlush(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.listActive()).hasSize(1);
        assertThat(service.listActive()).hasSize(1);
        verify(repository, times(1)).findByActiveTrueOrderByNameAsc();

        service.create(new CategoryRequest("Books", "Reading"));
        assertThat(service.listActive()).hasSize(1);
        verify(repository, times(2)).findByActiveTrueOrderByNameAsc();
    }

    @Configuration
    @EnableCaching
    static class Config {
        @Bean
        CategoryRepository repository() {
            return mock(CategoryRepository.class);
        }

        @Bean
        CategoryService service(CategoryRepository repository) {
            return new CategoryService(repository);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("activeCategories");
        }
    }
}