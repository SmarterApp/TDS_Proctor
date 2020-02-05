package TDS.Proctor.configuration;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.okta.jwt.AccessTokenVerifier;
import com.okta.jwt.JwtVerificationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.EXPIRES_IN;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.GRANT_TYPE;

/**
 * An implementation of {@link ResourceServerTokenServices} backed by Okta. It expects the access token to be
 * a JWT containing all the necessary OAuth information in its claims.
 *
 * @see SecurityConfigurer
 * @see SbacTokenConverter
 */
class OktaTokenServices implements ResourceServerTokenServices {
    private static final Logger logger = LoggerFactory.getLogger(OktaTokenServices.class);

    // Expiration claim, used to calculate EXPIRES_IN used by the SbacTokenConverter.
    private static final String EXP = "exp";
    private static final String SBAC_TENANCY_CHAIN = "sbacTenancyChain";

    private OktaJwtHelper jwtHelper;

    private final AccessTokenConverter tokenConverter;
    private final LoadingCache<String, OAuth2Authentication> cache;

    OktaTokenServices(final AccessTokenConverter tokenConverter) {
        this.tokenConverter = tokenConverter;
        this.cache = new ExpiringAuthenticationCache(key -> tokenConverter.extractAuthentication(getTokenInfo(key)));
        this.jwtHelper = new OktaJwtHelper();
    }

    public void setJwtHelper(final OktaJwtHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    @SuppressWarnings("unused")
    public void setTokenInfoUrl(final String tokenInfoUrl) {
        jwtHelper.setIssuer(tokenInfoUrl);
    }

    @SuppressWarnings("unused")
    public void setAudience(final String audience) {
        jwtHelper.setAudience(audience);
    }

    @SuppressWarnings("unused")
    public void setConnectionTimeout(final Long connectionTimeout) {
        jwtHelper.setConnectionTimeout(connectionTimeout);
    }

    @Override
    public OAuth2Authentication loadAuthentication(final String accessToken) throws AuthenticationException, InvalidTokenException {
        try {
            return cache.get(accessToken);
        } catch (final ExecutionException | UncheckedExecutionException e) {
            // propagate the cause if we can, otherwise wrap in generic auth exception
            throw e.getCause() instanceof RuntimeException ? (RuntimeException) e.getCause()
                    : new AuthenticationServiceException(e.getMessage());
        }
    }

    @Override
    public OAuth2AccessToken readAccessToken(final String accessToken) {
        return tokenConverter.extractAccessToken(accessToken, getTokenInfo(accessToken));
    }

    private Map<String, Object> getTokenInfo(final String token) {

        try {
            final AccessTokenVerifier jwtVerifier = jwtHelper.getJwtVerifier();
            final Map<String, Object> claims = jwtVerifier.decode(token).getClaims();
            return sanitize(claims);
        } catch (JwtVerificationException e) {
            logger.debug("getTokenInfo exception: {}", e.getMessage());
            throw new InvalidTokenException(token + ": " + e.getMessage());
        }
    }

    // Adjusts contents of the JWT claims so they synch with what the token convert expects.
    private Map<String, Object> sanitize(final Map<String, Object> claims) {
        Map<String,Object> sanitized = new HashMap<>(claims);

        // The EXPIRES_IN seconds can be computed from EXP
        if (sanitized.containsKey(EXP) && !sanitized.containsKey(EXPIRES_IN)) {
            try {
                long now = System.currentTimeMillis() / 1000L;
                long exp = ((Number)sanitized.get(EXP)).longValue();
                sanitized.put(EXPIRES_IN, (int)(exp - now));
            } catch (Exception e) {
                logger.info("Cannot compute expires-in value from exp: " + sanitized.get(EXP));
            }
        }

        // Tenancy chain may be in a list, but must be joined into a string.
        final Object tenancyChain = sanitized.get(SBAC_TENANCY_CHAIN);
        if (tenancyChain instanceof Collection) {
            String tenancyChainString = StringUtils.join(((Collection) tenancyChain), ",");
            sanitized.put(SBAC_TENANCY_CHAIN, tenancyChainString);
        }

        if (!sanitized.containsKey(GRANT_TYPE)) {
            sanitized.put(GRANT_TYPE, "password");
        }

        return sanitized;
    }
}

