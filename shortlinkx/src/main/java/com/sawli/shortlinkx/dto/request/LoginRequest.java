package com.sawli.shortlinkx.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload containing email and password credentials for logging in.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload to authenticate and log in a user")
public class LoginRequest {

    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Please provide a valid email address.")
    @Schema(description = "Registered email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password must not be blank.")
    @Schema(description = "User account password", example = "SecurePass123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
