package ucb.accounting.backend.util

import org.keycloak.KeycloakSecurityContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class KeycloakSecurityContextHolder {

    companion object {
        private fun getKeycloakSecurityContext(): KeycloakSecurityContext? {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            return requestAttributes?.request?.getAttribute(KeycloakSecurityContext::class.java.name) as? KeycloakSecurityContext
        }

        fun getSubject() : String? {
            return getKeycloakSecurityContext()?.token?.subject
        }

        fun getUsername() : String? {
            return getKeycloakSecurityContext()?.token?.preferredUsername
        }

        fun getEmail() : String? {
            return getKeycloakSecurityContext()?.token?.email
        }
    }
}
