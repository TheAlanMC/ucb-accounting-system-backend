package ucb.accounting.backend.bl.UsersBl

import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import ucb.accounting.backend.dto.UserDto

@Service
class UsersBl @Autowired constructor(
        val keycloak: Keycloak
) {

    @Value("\${keycloak.realm}")
    private val realm: String? = null

    fun findAllUsersById(kc_uuid: String): UserDto {
        val user: UserRepresentation = keycloak
            .realm(realm)
            .users()
            .get(kc_uuid)
            .toRepresentation()
        val companyId = user.attributes?.get("company_id")?.get(0)
        val profilePicture = user.attributes?.get("s3_profile_picture")?.get(0)
        return UserDto(
            companyId,
            user.firstName,
            user.lastName,
            user.email,
            profilePicture
        )
    }

}