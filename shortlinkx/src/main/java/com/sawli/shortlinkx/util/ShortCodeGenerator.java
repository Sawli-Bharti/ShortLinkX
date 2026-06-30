package com.sawli.shortlinkx.util;

import com.sawli.shortlinkx.constants.ApplicationConstants;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Utility component for generating unique short codes.
 * Thread-safe implementation using SecureRandom.
 */
@Component
public class ShortCodeGenerator {

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a thread-safe random alphanumeric short code.
     * The length of the generated code is randomly picked between
     * {@link ApplicationConstants#MIN_SHORT_CODE_LENGTH} and {@link ApplicationConstants#MAX_SHORT_CODE_LENGTH} (inclusive).
     *
     * @return a unique 6-8 character alphanumeric string
     */
    public String generateCode() {
        int length = ApplicationConstants.MIN_SHORT_CODE_LENGTH +
                random.nextInt(ApplicationConstants.MAX_SHORT_CODE_LENGTH - ApplicationConstants.MIN_SHORT_CODE_LENGTH + 1);

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ApplicationConstants.ALPHANUMERIC_CHARACTERS.length());
            code.append(ApplicationConstants.ALPHANUMERIC_CHARACTERS.charAt(index));
        }

        return code.toString();
    }
}
