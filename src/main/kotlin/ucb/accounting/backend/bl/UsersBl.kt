package ucb.accounting.backend.bl

import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import ucb.accounting.backend.dto.UserDto

@Controller
class UsersBl @Autowired constructor(
    private val keycloak: Keycloak,
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(UsersBl::class.java)
    }

    @Value("\${keycloak.auth-server-url}")
    private val authUrl: String? = null

    @Value("\${keycloak.realm}")
    private val realm: String? = null

    @Value("\${frontend-client-id}")
    private val frontendClientId: String? = null

    fun updateUser(kcUuid: String, userDto: UserDto): UserDto {
        logger.info("Updating user info")
        val user: UserRepresentation = keycloak
            .realm(realm)
            .users()
            .get(kcUuid)
            .toRepresentation()
        user.firstName = userDto.firstName ?: user.firstName
        user.lastName = userDto.lastName ?: user.lastName

        keycloak
            .realm(realm)
            .users()
            .get(kcUuid)
            .update(user)
        logger.info("User info updated")

        // TODO: Add logic to update profile picture
        return UserDto(
            user.firstName,
            user.lastName,
            user.email,
            "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50?s=200",
        )
    }

}