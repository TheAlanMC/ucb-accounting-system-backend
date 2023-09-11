package ucb.accounting.backend.config

import org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakClientConfig {

    @Value("\${keycloak.auth-server-url}")
    private val authUrl: String? = null

    @Value("\${keycloak.realm}")
    private val realm: String? = null

    @Value("\${keycloak.resource}")
    private val clientId: String? = null

    @Value("\${keycloak.credentials.secret}")
    private val secretKey: String? = null
    @Bean
    fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .grantType(CLIENT_CREDENTIALS)
            .serverUrl(authUrl)
            .realm(realm)
            .clientId(clientId)
            .clientSecret(secretKey)
            .build()
    }
}