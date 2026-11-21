package com.tabeldata.ecommerce;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.tabeldata.ecommerce")
@EntityScan(basePackages = "com.tabeldata.ecommerce")
@EnableJpaRepositories(basePackages = "com.tabeldata.ecommerce")
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    @Bean
    public OpenAPI ecommerceOpenApiSpec() {
        return new OpenAPI().info(new Info()
                .title("Tabeldata E-Commerce Modular Monolith API")
                .version("1.0.0")
                .description("REST API Sistem Toko, Barang, dan Pembelian (Spring Boot 3.0.x & PostgreSQL 15)")
                .contact(new Contact().name("Backend Engineering Pretest").email("dimas.maryanto@tabeldata.com")));
    }
}
