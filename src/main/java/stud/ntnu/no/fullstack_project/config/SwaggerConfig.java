package stud.ntnu.no.fullstack_project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central OpenAPI configuration for the backend.
 *
 * <p>This configuration exposes API metadata used by Swagger UI and the generated
 * OpenAPI document so the backend has a documented baseline from the start.</p>
 */
@Configuration
public class SwaggerConfig {

  /**
   * Creates the shared OpenAPI metadata definition for the application.
   *
   * @return configured {@link OpenAPI} metadata used by Swagger UI
   */
  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Fullstack Project API")
            .version("1.0")
            .description("Starter API for the Frivillig fullstack project with authentication, "
                + "user, health, and security infrastructure.")
            .contact(new Contact()
                .name("Frivillig backend")
                .email("scottld@ntnu.no"))
            .license(new License()
                .name("Internal project use")));
  }
}
