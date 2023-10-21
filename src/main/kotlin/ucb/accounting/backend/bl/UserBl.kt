package ucb.accounting.backend.bl

import jakarta.ws.rs.core.Response
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.*
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.KcUser
import ucb.accounting.backend.dao.KcUserCompany
import ucb.accounting.backend.dao.S3Object
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dao.specification.KcUserCompanySpecification
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.KcUserCompanyMapper
import ucb.accounting.backend.service.MinioService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Date

@Service
class UserBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val kcGroupRepository: KcGroupRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val kcUserRepository: KcUserRepository,
    private val keycloak: Keycloak,
    private val minioService: MinioService,
    private val s3ObjectRepository: S3ObjectRepository,
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(UserBl::class.java)
    }

    @Value("\${keycloak.auth-server-url}")
    private val authUrl: String? = null

    @Value("\${keycloak.realm}")
    private val realm: String? = null

    @Value("\${frontend-client-id}")
    private val frontendClientId: String? = null

    fun findUser(): UserDto {
        logger.info("Getting user info")
        val kcUuid = KeycloakSecurityContextHolder.getSubject() ?: throw UasException("403-01")
        // Validation that the user exists
        val kcUserEntity: KcUser = kcUserRepository.findByKcUuidAndStatusIsTrue(kcUuid) ?: throw UasException("404-01")

        // Get s3 object
        val s3ObjectEntity: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(kcUserEntity.s3ProfilePicture.toLong())?: throw UasException("404-13")
        val preSignedUrl: String = minioService.getPreSignedUrl(s3ObjectEntity.bucket, s3ObjectEntity.filename)

        // Return user info
        return UserDto(
            companyIds = findUserCompanies(kcUuid),
            firstName = kcUserEntity.firstName,
            lastName = kcUserEntity.lastName,
            email = kcUserEntity.email,
            s3ProfilePictureId = kcUserEntity.s3ProfilePicture.toLong(),
            profilePicture = preSignedUrl,
        )
    }

    fun findAllUsersByCompanyId(
        companyId: Long,
        sortBy: String,
        sortType: String,
        page: Int,
        size: Int,
        keyword: String?
    ): Page<UserPartialDto> {
        logger.info("Getting all users by company id")
        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Validation the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-01")
        logger.info("User $kcUuid is getting all users from company $companyId")

        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), sortBy))
        var specification: Specification<KcUserCompany> = Specification.where(null)
        specification = specification.and(specification.and(KcUserCompanySpecification.companyId(companyId.toInt())))
        specification = specification.and(specification.and(KcUserCompanySpecification.statusIsTrue()))

        if (!keyword.isNullOrEmpty() && keyword.isNotBlank()) {
            specification = specification.and(specification.and(KcUserCompanySpecification.kcUserKeyword(keyword)))
        }
        
        // Get all users from company
        val userCompanyEntities: Page<KcUserCompany> = kcUserCompanyRepository.findAll(specification, pageable)

        val users: List<UserPartialDto> = userCompanyEntities.content.map {
            UserPartialDto(
                kcGroupName = it.kcGroup!!.groupName,
                firstName = it.kcUser!!.firstName,
                lastName = it.kcUser!!.lastName,
                email = it.kcUser!!.email,
                creationDate = Date(it.kcUser!!.txDate.time)
            )
        }

        return PageImpl(users, pageable, userCompanyEntities.totalElements)
    }

    fun updateUser(userDto: UserDto): UserDto {
        logger.info("Updating user info")
        // Validate that at least one of the fields is not null
        if (userDto.firstName == null && userDto.lastName == null && userDto.s3ProfilePictureId == null) {
            throw UasException("400-04")
        }
        // Get user id from keycloak
        val kcUuid = KeycloakSecurityContextHolder.getSubject() ?: throw UasException("403-02")

        // Validation that the user exists
        val kcUserEntity: KcUser = kcUserRepository.findByKcUuidAndStatusIsTrue(kcUuid) ?: throw UasException("404-01")

        // If s3ProfilePictureId is not null, update s3ProfilePicture in KcUser
        if (userDto.s3ProfilePictureId != null) {
            // Validation that the s3ProfilePictureId exists
            s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(userDto.s3ProfilePictureId) ?: throw UasException("404-13")
            kcUserEntity.s3ProfilePicture = userDto.s3ProfilePictureId.toInt()
            kcUserRepository.save(kcUserEntity)
        }

        // Get s3 object
        val s3ObjectEntity: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(kcUserEntity.s3ProfilePicture.toLong())?: throw UasException("404-13")
        val preSignedUrl: String = minioService.getPreSignedUrl(s3ObjectEntity.bucket, s3ObjectEntity.filename)

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

        // Update user info in database
        kcUserEntity.firstName = user.firstName
        kcUserEntity.lastName = user.lastName
        kcUserRepository.save(kcUserEntity)

        return UserDto(
            companyIds = findUserCompanies(kcUuid),
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            s3ProfilePictureId = kcUserEntity.s3ProfilePicture.toLong(),
            profilePicture = preSignedUrl,
        )
    }

    fun updateUserPassword (passwordUpdateDto: PasswordUpdateDto) {
        logger.info("Updating user password")
        // Get user id from keycloak
        val kcUuid = KeycloakSecurityContextHolder.getSubject() ?: throw UasException("403-03")

        // Validation that the user exists
        kcUserRepository.findByKcUuidAndStatusIsTrue(kcUuid) ?: throw UasException("404-01")

        // Validation of new password and confirm new password are the same
        if (passwordUpdateDto.newPassword != passwordUpdateDto.confirmNewPassword) {
            throw UasException("400-04")
        }

        // Get username from keycloak
        val username = KeycloakSecurityContextHolder.getUsername() ?: throw UasException("403-03")

        // Check if current password is correct
        try {
            val keycloakUser: Keycloak = KeycloakBuilder.builder()
                .grantType(OAuth2Constants.PASSWORD)
                .serverUrl(authUrl)
                .realm(realm)
                .clientId(frontendClientId)
                .username(username)
                .password(passwordUpdateDto.currentPassword)
                .build()
            keycloakUser.tokenManager().accessToken
        } catch (e: Exception) {
            throw UasException("400-05")
        }

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

    fun createAccountant(newUserDto: NewUserDto, groupName: String) {
        logger.info("Starting the BL call to create an accountant")
        // Validate that none of the fields are null
        if (newUserDto.email == null || newUserDto.firstName == null || newUserDto.lastName == null || newUserDto.password == null || newUserDto.confirmPassword == null) {
            throw UasException("400-01")
        }

        // Validate that the password and confirm password are the same
        if (newUserDto.password != newUserDto.confirmPassword) {
            throw UasException("400-01")
        }

        // Validate that the email is not already in use
        findByEmail(newUserDto.email, "409-01")

        // Create user representation
        val userRepresentation = prepareUserRepresentation(newUserDto, groupName, newUserDto.password)

        // Create user in keycloak
        val response: Response = keycloak
            .realm(realm)
            .users()
            .create(userRepresentation)
        if (response.status != 201) {
            throw UasException("400-01")
        }


        // Storage of the user id in the database
        val kcUserEntity = KcUser()
        kcUserEntity.kcUuid = response.location.path.split("/").last()
        kcUserEntity.firstName = newUserDto.firstName
        kcUserEntity.lastName = newUserDto.lastName
        kcUserEntity.email = newUserDto.email
        kcUserEntity.s3ProfilePicture = 1 // Default profile picture
        kcUserRepository.save(kcUserEntity)
        logger.info("Accountant created")
    }

    fun createAccountAssistant(newUserDto: NewUserDto, groupName: String, companyId: Long){
        logger.info("Starting the BL call to create an accounting assistant")
        // Validate that none of the fields are null but the password and confirm password
        if (newUserDto.email == null || newUserDto.firstName == null || newUserDto.lastName == null) {
            throw UasException("400-02")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-40")
        logger.info("User $kcUuid is creating an accounting assistant")

        // Validate that the email is not already in use
        findByEmail(newUserDto.email, "409-02")

        // Validate that the company exists
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Generate password based on the company nit
        val userRepresentation = prepareUserRepresentation(newUserDto, groupName, companyEntity.companyNit)

        // Create user in keycloak
        val response: Response = keycloak
            .realm(realm)
            .users()
            .create(userRepresentation)
        if (response.status != 201) {
            throw UasException("400-02")
        }

        // Storage of the user id in the database
        val kcUserEntity = KcUser()
        kcUserEntity.kcUuid = response.location.path.split("/").last()
        kcUserEntity.firstName = newUserDto.firstName
        kcUserEntity.lastName = newUserDto.lastName
        kcUserEntity.email = newUserDto.email
        kcUserEntity.s3ProfilePicture = 1 // Default profile picture
        kcUserRepository.save(kcUserEntity)
        logger.info("Accounting assistant created")

        // Storage of the user id in the database kc_user_company
        val kcUserCompany = KcUserCompany()
        kcUserCompany.kcUser = kcUserEntity
        kcUserCompany.company = companyEntity
        kcUserCompany.kcGroupId = kcGroupRepository.findByGroupNameAndStatusIsTrue("Asistente contable")!!.kcGroupId
        kcUserCompanyRepository.save(kcUserCompany)
    }

    fun createClient(newUserDto: NewUserDto, groupName: String, companyId: Long){
        logger.info("Starting the BL call to create a client")
        // Validate that none of the fields are null but the password and confirm password
        if (newUserDto.email == null || newUserDto.firstName == null || newUserDto.lastName == null) {
            throw UasException("400-03")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-41")
        logger.info("User $kcUuid is creating a client")

        // Validate that the email is not already in use
        findByEmail(newUserDto.email, "409-03")

        // Validate that the company exists
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Generate password based on the company nit
        val userRepresentation = prepareUserRepresentation(newUserDto, groupName, companyEntity.companyNit)

        // Create user in keycloak
        val response: Response = keycloak
            .realm(realm)
            .users()
            .create(userRepresentation)
        if (response.status != 201) {
            throw UasException("400-03")
        }

        // Storage of the user id in the database
        val kcUserEntity = KcUser()
        kcUserEntity.kcUuid = response.location.path.split("/").last()
        kcUserEntity.firstName = newUserDto.firstName
        kcUserEntity.lastName = newUserDto.lastName
        kcUserEntity.email = newUserDto.email
        kcUserEntity.s3ProfilePicture = 1
        kcUserRepository.save(kcUserEntity)
        logger.info("Client created")

        // Storage of the user id in the database kc_user_company
        val kcUserCompany = KcUserCompany()
        kcUserCompany.kcUser = kcUserEntity
        kcUserCompany.company = companyEntity
        kcUserCompany.kcGroupId = kcGroupRepository.findByGroupNameAndStatusIsTrue("Cliente")!!.kcGroupId
        kcUserCompanyRepository.save(kcUserCompany)
    }

    fun findUserCompanies(kcUuid: String): List<Long> {
        val userCompanyEntities: List<KcUserCompany> =
            kcUserCompanyRepository.findAllByKcUser_KcUuidAndStatusIsTrue(kcUuid)
        val userCompanies: List<UserCompanyDto> = userCompanyEntities.map { KcUserCompanyMapper.entityToDto(it) }
        return userCompanies.map { it.companyId }
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
        newUserDto: NewUserDto,
        groupName: String,
        password: String
    ): UserRepresentation {
        val userRepresentation = UserRepresentation()
        userRepresentation.email = newUserDto.email
        userRepresentation.firstName = newUserDto.firstName
        userRepresentation.lastName = newUserDto.lastName
        userRepresentation.credentials = listOf(preparePasswordRepresentation(password))
        userRepresentation.groups = listOf(groupName)
        userRepresentation.isEnabled = true
        userRepresentation.isEmailVerified = true // TODO: Change to false in order to send email
        return userRepresentation
    }

    fun findByEmail(email: String, error: String){
        val users: List<UserRepresentation> = keycloak
            .realm(realm)
            .users()
            .search(email)
        if (users.isNotEmpty()) {
            throw UasException(error)
        }
    }
}