package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import java.io.IOException;

/**
 * Thrown if a size limit has been exceeded during an IO operation.
 *
 * @author Robert Gacki, robert.gacki@cgi.com
 */
public class SizeLimitExceededException extends IOException {

    private static final long serialVersionUID = -7330482368164293210L;

    private final long maxmimumSize;

    public SizeLimitExceededException(final long maxmimumSize, final String message) {
        super(message);
        this.maxmimumSize = maxmimumSize;
    }

    public SizeLimitExceededException(final long maxmimumSize, final String message, final Throwable cause) {
        super(message, cause);
        this.maxmimumSize = maxmimumSize;
    }

    public long getMaxmimumSize() {
        return maxmimumSize;
    }
}
