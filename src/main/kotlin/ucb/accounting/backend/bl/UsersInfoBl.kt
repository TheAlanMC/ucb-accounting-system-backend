package ucb.accounting.backend.bl
/*
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import ucb.accounting.backend.dto.UserDto

@Controller
class UsersInfoBl @Autowired constructor(
    private val keycloak: Keycloak,
){
    companion object{
        val logger: Logger = LoggerFactory.getLogger(UsersInfoBl::class.java.name)
    }

    @Value("\${keycloak.auth-server-url}")
    private val authUrl: String? = null

    @Value("\${keycloak.realm}")
    private val realm: String? = null

    @Value("\${frontend-client-id}")
    private val frontendClientId: String? = null
    fun getUsersInfo(kcUuid: String): UserDto {
        logger.info("Starting the business logic to get users info")
        val user: UserRepresentation = keycloak
            .realm(realm)
            .users()
            .get(kcUuid)
            .toRepresentation()

        val usersInfo = UserDto(
            user.attributes["companyId"]?.get(0)?.toLong()!!,
            user.firstName,
            user.lastName,
            user.email,
            user.attributes["s3_profile_picture"]?.get(0)!!
        )
        logger.info("Finishing the business logic to get users info")
        return usersInfo
    }
}*/