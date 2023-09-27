package ucb.accounting.backend.bl

import jakarta.ws.rs.core.Response
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.KcUser
import ucb.accounting.backend.dao.KcUserCompany
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dao.repository.KcUserRepository
import ucb.accounting.backend.dto.KcUsersDto
import ucb.accounting.backend.exception.UasException

@Service
class KcUsersBl @Autowired constructor(
    private val keycloak: Keycloak,
    private val kcUserRepository: KcUserRepository,
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
){

    companion object {
        val logger: Logger = LoggerFactory.getLogger(UsersBl::class.java)
    }

    @Value("\${keycloak.auth-server-url}")
    private val authUrl: String? = null

    @Value("\${keycloak.realm}")
    private val realm: String? = null

    @Value("\${frontend-client-id}")
    private val frontendClientId: String? = null

    fun createAccountant(kcUsersDto: KcUsersDto, groupName: String) {
        logger.info("Starting the BL call to create user")
        // Input validation
        //TODO validatePasswordPolicy(accountantDto.password)
        //TODO val passwordRepresentation = preparePasswordRepresentation(accountantDto.password)

        if (kcUsersDto.password != kcUsersDto.confirmPassword) {
            throw UasException("400-01")
        }

        val userRepresentation = prepareUserRepresentation(kcUsersDto, groupName, kcUsersDto.password!!)

        val response: Response = keycloak
            .realm(realm)
            .users()
            .create(userRepresentation)
        if (response.status != 201) {
            throw UasException("400-01")
        }

        val userId = response.location.path.split("/").last()
        //storage of the user id in the database
        val kcuser = KcUser()
        kcuser.kcUuid = userId
        kcuser.s3ProfilePicture = 1
        kcUserRepository.save(kcuser)
        logger.info("User created")
    }

    fun createAccountAssistant(kcUsersDto: KcUsersDto, groupName: String, companyId: Long){
        logger.info("Starting the BL call to create user")
        findByEmail(kcUsersDto.email, groupName)
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId.toLong())?: throw UasException("404-05")
        val userRepresentation = prepareUserRepresentation(kcUsersDto, groupName, companyEntity.companyNit)
        val response: Response = keycloak
            .realm(realm)
            .users()
            .create(userRepresentation)
        if (response.status != 201) {
            throw UasException("400-02")
        }
        //TODO 403 VALIDATIONS
        val userId = response.location.path.split("/").last()
        //storage of the user id in the database
        val kcuser = KcUser()
        kcuser.kcUuid = userId
        kcuser.s3ProfilePicture = 1
        kcUserRepository.save(kcuser)
        logger.info("User created")
        //storage of the user id in the database kc_user_company
        val kcUserCompany = KcUserCompany()
        kcUserCompany.kcUser = kcuser
        kcUserCompany.company = companyEntity
        kcUserCompany.kcGroupId = 2
        kcUserCompanyRepository.save(kcUserCompany)
    }

    fun createClient(kcUsersDto: KcUsersDto, groupName: String, companyId: Long){
        logger.info("Starting the BL call to create user")
        findByEmail(kcUsersDto.email, groupName)
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId.toLong())?: throw UasException("404-05")
        val userRepresentation = prepareUserRepresentation(kcUsersDto, groupName, companyEntity.companyNit)
        val response: Response = keycloak
            .realm(realm)
            .users()
            .create(userRepresentation)
        if (response.status != 201) {
            throw UasException("400-03")
        }
        //TODO 403 VALIDATIONS
        val userId = response.location.path.split("/").last()
        //storage of the user id in the database
        val kcuser = KcUser()
        kcuser.kcUuid = userId
        kcuser.s3ProfilePicture = 1
        kcUserRepository.save(kcuser)
        logger.info("User created")
        //storage of the user id in the database kc_user_company
        val kcUserCompany = KcUserCompany()
        kcUserCompany.kcUser = kcuser
        kcUserCompany.company = companyEntity
        kcUserCompany.kcGroupId = 3
        kcUserCompanyRepository.save(kcUserCompany)
    }

    private fun preparePasswordRepresentation(
        password: String?
    ): CredentialRepresentation {
        val credentialRepresentation = CredentialRepresentation()
        credentialRepresentation.isTemporary = true
        credentialRepresentation.type = CredentialRepresentation.PASSWORD
        credentialRepresentation.value = password
        return credentialRepresentation
    }

    private fun prepareUserRepresentation(
        kcUsersDto: KcUsersDto,
        groupName: String,
        password: String
    ): UserRepresentation {
        val userRepresentation = UserRepresentation()
        userRepresentation.email = kcUsersDto.email
        userRepresentation.firstName = kcUsersDto.firstName
        userRepresentation.lastName = kcUsersDto.lastName
        userRepresentation.credentials = listOf(preparePasswordRepresentation(password))
        userRepresentation.groups = listOf(groupName)
        userRepresentation.isEnabled = true
        userRepresentation.isEmailVerified = true
        return userRepresentation
    }

    fun findByEmail(email: String, groupName: String) {
        logger.info("Starting the BL call to find user by email")
        val users: List<UserRepresentation> = keycloak
            .realm(realm)
            .users()
            .search(email)
        if (!users.isEmpty()) {
            if (groupName == "accounting_assistant") {
                throw UasException("409-02")
            } else {
                throw UasException("409-03")
            }
        }
    }

    /*TODO
    private fun validatePasswordPolicy (password: String?){
        // Check if password is null or empty
        if (password.isNullOrEmpty()) {
            throw UsersException(HttpStatus.BAD_REQUEST, "Empty password not allowed")
        }
        // Check if password has at least 1 special character
        val regex = Regex("[^A-Za-z0-9 ]")
        if (!regex.containsMatchIn(password)) {
            throw UsersException(HttpStatus.BAD_REQUEST, "Invalid password: must contain at least 1 special characters.")
        }
        // Check if password hast at least 1 uppercase letter
        val regex2 = Regex("[A-Z]")
        if (!regex2.containsMatchIn(password)) {
            throw UsersException(HttpStatus.BAD_REQUEST, "Invalid password: must contain at least 1 upper case characters.")
        }
        // Check if password has at least 1 lowercase letter
        val regex3 = Regex("[a-z]")
        if (!regex3.containsMatchIn(password)) {
            throw UsersException(HttpStatus.BAD_REQUEST, "Invalid password: must contain at least 1 lower case characters.")
        }
        // Check if password has length of at least 10 characters
        if (password.length < 10) {
            throw UsersException(HttpStatus.BAD_REQUEST, "Invalid password: minimum length 10.")
        }
    }
     */

}