package ucb.accounting.backend.bl

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import ucb.accounting.backend.dto.PasswordUpdateDto
import ucb.accounting.backend.dto.UserDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

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

    fun findUser(kcUuid: String): UserDto {
        logger.info("Getting user info")
        // Validation that the user exists
        try {
            keycloak
                .realm(realm)
                .users()
                .get(kcUuid)
                .toRepresentation()
        } catch (e: Exception) {
            throw UasException("404-01")
        }
        // Validation of user KcUuid belongs is the same as the logged user
        if (kcUuid != KeycloakSecurityContextHolder.getSubject()) {
            throw UasException("403-01")
        }
        val user: UserRepresentation = keycloak
            .realm(realm)
            .users()
            .get(kcUuid)
            .toRepresentation()
        // TODO: FIX COMPANY ID
        val companyIdString = user.attributes?.get("company_id")?.get(0)
        val companyId = companyIdString?.toLongOrNull() ?: 1
        val profilePicture = user.attributes?.get("s3_profile_picture")?.get(0)?: "1"

        return UserDto(
            companyId,
            user.firstName,
            user.lastName,
            user.email,
            profilePicture
        )
    }
    fun updateUser(kcUuid: String, userDto: UserDto): UserDto {
        logger.info("Updating user info")
        // Validation that the user exists
        try {
            keycloak
                .realm(realm)
                .users()
                .get(kcUuid)
                .toRepresentation()
        } catch (e: Exception) {
            throw UasException("404-01")
        }
        // Validation of user KcUuid belongs is the same as the logged user
        if (kcUuid != KeycloakSecurityContextHolder.getSubject()) {
            throw UasException("403-03")
        }

        // Get user info from keycloak
        val user: UserRepresentation = keycloak
            .realm(realm)
            .users()
            .get(kcUuid)
            .toRepresentation()
        user.firstName = userDto.firstName ?: user.firstName
        user.lastName = userDto.lastName ?: user.lastName
        // Update user info in keycloak
        keycloak
            .realm(realm)
            .users()
            .get(kcUuid)
            .update(user)
        logger.info("User info updated")

        // TODO: Add logic to update profile picture
        return UserDto(
            companyId = 1,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            profilePicture = "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50?s=200",
        )
    }

    fun updateUserPassword (kcUuid: String, passwordUpdateDto: PasswordUpdateDto) {
        logger.info("Updating user password")
        // Validation that the user exists
        try {
            keycloak
                .realm(realm)
                .users()
                .get(kcUuid)
                .toRepresentation()
        } catch (e: Exception) {
            throw UasException("404-01")
        }
        // Validation of user KcUuid belongs is the same as the logged user
        if (kcUuid != KeycloakSecurityContextHolder.getSubject()) {
            throw UasException("403-03")
        }
        // Validation of new password and confirm new password are the same
        if (passwordUpdateDto.newPassword != passwordUpdateDto.confirmNewPassword) {
            throw UasException("400-04")
        }
        // TODO Probably add password policy validation
        // Get username from keycloak
        val username = KeycloakSecurityContextHolder.getUsername() ?: throw UasException("403-03")
        // Check if current password is correct
        val keycloakUser: Keycloak = KeycloakBuilder.builder()
            .grantType(OAuth2Constants.PASSWORD)
            .serverUrl(authUrl)
            .realm(realm)
            .clientId(frontendClientId)
            .username(username)
            .password(passwordUpdateDto.currentPassword)
            .build()
        keycloakUser.tokenManager().accessToken
        logger.info("Current password is correct")
        // Update password in keycloak
        val credentialRepresentation = CredentialRepresentation()
        credentialRepresentation.isTemporary = false
        credentialRepresentation.type = CredentialRepresentation.PASSWORD
        credentialRepresentation.value = passwordUpdateDto.newPassword
        keycloak
            .realm(realm)
            .users()
            .get(kcUuid)
            .resetPassword(credentialRepresentation)
        logger.info("User password updated")
    }

}