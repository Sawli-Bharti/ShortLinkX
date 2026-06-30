package com.sawli.shortlinkx.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload containing registration credentials to create a new user account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload to register a new user account")
public class RegisterRequest {

    @NotBlank(message = "Full name must not be blank.")
    @Schema(description = "User's full name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Please provide a valid email address.")
    @Schema(description = "Unique email address to register", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password must not be blank.")
    @Size(min = 6, message = "Password must be at least 6 characters.")
    @Schema(description = "Login password (minimum 6 characters)", example = "SecurePass123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
