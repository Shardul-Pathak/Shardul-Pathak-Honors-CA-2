package com.rbu.parking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartParkingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Parking Management System RESTful API")
                        .description("Production-grade RESTful backend service demonstrating clean architecture, defensive programming, Jakarta Bean Validation, and dynamic fare billing rules.\n\n" +
                                     "### Core Rules:\n" +
                                     "- **Pricing Engine:** Flat ₹50 per 3 hours (or fraction thereof).\n" +
                                     "- **Defensive Guarantees:** Duplicate parking prevention, optimistic locking, standardized error responses (400, 404, 409, 500).\n" +
                                     "- **Database:** PostgreSQL with Flyway automated schema migrations.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Shardul Pathak")
                                .email("shardulpathak796@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .tags(List.of(
                        new Tag().name("Slot Management").description("Real-time parking slot availability and inspection"),
                        new Tag().name("Booking & Billing Lifecycle").description("Vehicle entry, occupancy tracking, real-time fare, and checkout"),
                        new Tag().name("Revenue & Analytics").description("Financial aggregates and date-filtered revenue reporting")
                ));
    }
}
