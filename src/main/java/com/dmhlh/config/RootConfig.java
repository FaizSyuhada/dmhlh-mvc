package com.dmhlh.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Root Application Context Configuration
 * Configures service layer, data access layer, and other non-web components
 */
@Configuration
@ComponentScan(
    basePackages = "com.dmhlh",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = {Controller.class, EnableWebMvc.class}
    )
)
@PropertySource("classpath:application.properties")
public class RootConfig {
    // Root configuration - services, repositories, etc. are auto-scanned
}
