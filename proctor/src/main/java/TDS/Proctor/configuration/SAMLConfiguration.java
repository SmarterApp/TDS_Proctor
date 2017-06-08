package TDS.Proctor.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLContextProviderLB;

import javax.servlet.ServletException;

/**
 * This configuration is responsible for providing SAML Configurations.
 */
@Configuration
public class SAMLConfiguration {

    @Bean
    public SAMLContextProvider contextProvider(
        @Value("${spring.saml.loadBalanced:false}") final boolean loadBalanced,
        @Value("${spring.saml.scheme:https}") final String scheme,
        @Value("${spring.saml.serverName}") final String serverName,
        @Value("${spring.saml.contextPath:/proctor}") final String contextPath,
        @Value("${spring.saml.serverPort:0}") final int serverPort,
        @Value("${spring.saml.includeServerPort:false}") final boolean includePort) throws ServletException {
        if (!loadBalanced) {
            return new SAMLContextProviderImpl();
        }

        final SAMLContextProviderLB contextProvider = new SAMLContextProviderLB();
        contextProvider.setScheme(scheme);
        contextProvider.setServerName(serverName);
        contextProvider.setContextPath(contextPath);
        contextProvider.setServerPort(serverPort);
        contextProvider.setIncludeServerPortInRequestURL(includePort);

        return contextProvider;
    }
}
