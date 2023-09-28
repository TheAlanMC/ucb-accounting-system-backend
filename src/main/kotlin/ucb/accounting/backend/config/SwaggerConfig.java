package ucb.accounting.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Value("${keycloak.auth-server-url}")
    private String AUTH_SERVER;

    @Value("${keycloak.realm}")
    private String REALM;

    @Value("${frontend-client-id}")
    private String FRONTEND_CLIENT_ID;

    private static final Logger log = LoggerFactory.getLogger(SwaggerConfig.class);

    @Bean
    public SecurityConfiguration securityConfiguration() {

        Map<String, Object> additionalQueryStringParams=new HashMap<>();
        additionalQueryStringParams.put("nonce","123456");

        return SecurityConfigurationBuilder.builder()
                .clientId(FRONTEND_CLIENT_ID).realm(REALM).appName("swagger-ui")
                .additionalQueryStringParams(additionalQueryStringParams)
                .build();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("ucb.accounting.backend"))
                .paths(PathSelectors.any())
                .build().securitySchemes(buildSecurityScheme()).securityContexts(buildSecurityContext());
    }

    private List<SecurityContext> buildSecurityContext() {
        List<SecurityReference> securityReferences = new ArrayList<>();

        securityReferences.add(SecurityReference.builder().reference("oauth2").scopes(scopes().toArray(new AuthorizationScope[]{})).build());

        SecurityContext context = SecurityContext.builder().forPaths(s -> true).securityReferences(securityReferences).build();

        List<SecurityContext> ret = new ArrayList<>();
        ret.add(context);
        return ret;
    }

    private List<? extends SecurityScheme> buildSecurityScheme() {
        List<SecurityScheme> lst = new ArrayList<>();
        List<GrantType> gTypes = new ArrayList<>();
        TokenRequestEndpoint tokenRequest = new TokenRequestEndpointBuilder().url(AUTH_SERVER + "/realms/" + REALM + "/protocol/openid-connect/auth").build();
        TokenEndpoint tokenEndpoint = new TokenEndpointBuilder().url(AUTH_SERVER + "/realms/" + REALM + "/protocol/openid-connect/token").build();
        gTypes.add(new AuthorizationCodeGrant(tokenRequest, tokenEndpoint));
        lst.add(new OAuth("oauth2", scopes(), gTypes));
        return lst;
    }

    private List<AuthorizationScope> scopes() {
        List<AuthorizationScope> scopes = new ArrayList<>();
        for (String scopeItem : new String[]{"openid=openid", "profile=profile"}) {
            String[] scope = scopeItem.split("=");
            if (scope.length == 2) {
                scopes.add(new AuthorizationScopeBuilder().scope(scope[0]).description(scope[1]).build());
            } else {
                log.warn("Scope '{}' is not valid (format is scope=description)", scopeItem);
            }
        }

        return scopes;
    }
}