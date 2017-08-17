/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 ******************************************************************************/
package TDS.Proctor.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import tds.dll.common.performance.caching.CacheKeyGenerator;

/**
 * Spring configuration for caching.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public EhCacheManagerFactoryBean cacheFactoryBean() {
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        ehCacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        ehCacheManagerFactoryBean.setShared(true);
        return ehCacheManagerFactoryBean;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new CacheKeyGenerator();
    }

    @Bean
    public CacheManager cacheManager() {
        EhCacheCacheManager cacheManager = new EhCacheCacheManager();
        cacheManager.setCacheManager(cacheFactoryBean().getObject());
        return cacheManager;
    }
}
