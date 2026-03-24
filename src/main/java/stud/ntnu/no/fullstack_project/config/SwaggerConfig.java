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
 * OpenAPI document.</p>
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
            .title("IK System API")
            .version("1.0")
            .description("Digital Internal Control System API for restaurants and food/alcohol serving "
                + "establishments. Provides endpoints for compliance management including checklists, "
                + "temperature logging, deviation tracking, and user management.")
            .contact(new Contact()
                .name("IK System Team")
                .email("scottld@ntnu.no"))
            .license(new License()
                .name("Internal project use")));
  }
}
