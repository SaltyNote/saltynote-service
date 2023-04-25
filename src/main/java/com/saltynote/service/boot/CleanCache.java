package com.saltynote.service.boot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({ "dev", "local", "default" })
public class CleanCache implements CommandLineRunner {

    private final CacheManager cacheManager;

    @Override
    public void run(String... args) {
        cacheManager.getCacheNames().forEach(name -> {
            log.info("Clearing cache: {}", name);
            Cache cache = cacheManager.getCache(name);
            if (cache != null)
                cache.clear();
        });

    }

}
