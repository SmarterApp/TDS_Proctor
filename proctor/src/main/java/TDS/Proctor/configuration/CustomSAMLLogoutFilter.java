package TDS.Proctor.configuration;

import org.springframework.security.core.Authentication;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * Custom SAMLLogoutFilter that allows global vs. local logout handling to be set via a parameter.
 */
public class CustomSAMLLogoutFilter extends SAMLLogoutFilter {
    private final boolean globalLogout;

    public CustomSAMLLogoutFilter(final String successUrl,
                                  final LogoutHandler[] localHandler,
                                  final LogoutHandler[] globalHandlers,
                                  final boolean globalLogout) {
        super(successUrl, localHandler, globalHandlers);
        this.globalLogout = globalLogout;
    }

    public CustomSAMLLogoutFilter(
            final LogoutSuccessHandler logoutSuccessHandler,
            final LogoutHandler[] localHandler,
            final LogoutHandler[] globalHandlers,
            final boolean globalLogout) {
        super(logoutSuccessHandler, localHandler, globalHandlers);
        this.globalLogout = globalLogout;
    }

    @Override
    protected boolean isGlobalLogout(final HttpServletRequest request, final Authentication auth) {
        // If logout parameter is explicitly set or auth credentials not SAML, use default handling
        if(request.getParameter(LOGOUT_PARAMETER) != null ||
                !(auth.getCredentials() instanceof SAMLCredential)) {
            return super.isGlobalLogout(request, auth);
        }

        // Otherwise, set based on globalLogout parameter
        return globalLogout;
    }
}
