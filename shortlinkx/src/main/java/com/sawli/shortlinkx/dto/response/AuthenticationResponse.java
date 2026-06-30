package com.sawli.shortlinkx.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload carrying the generated JWT bearer token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing JWT token and expiry after login")
public class AuthenticationResponse {

    @Schema(description = "Bearer JWT token for authorization in protected APIs header", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Expiration duration of the generated token in milliseconds", example = "86400000")
    private long expiresIn;
}
