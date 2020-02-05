package TDS.Proctor.configuration;

import com.okta.jwt.AccessTokenVerifier;
import com.okta.jwt.JwtVerifiers;

import java.time.Duration;

/**
 * Helper class to create the JWT verifier, which is used to decode the JWT token.
 */
public class OktaJwtHelper {

    private String issuer;
    private String audience;
    private Long connectionTimeout = 1000L;

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public void setAudience(final String audience) {
        this.audience = audience;
    }

    public void setConnectionTimeout(final Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public AccessTokenVerifier getJwtVerifier() {
        return JwtVerifiers.accessTokenVerifierBuilder()
                .setIssuer(this.issuer)
                .setAudience(this.audience)
                .setConnectionTimeout(Duration.ofMillis(this.connectionTimeout))
                .setReadTimeout(Duration.ofMillis(connectionTimeout))
                .build();
    }
}
