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
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.KcUser
import ucb.accounting.backend.dao.KcUserCompany
import ucb.accounting.backend.dao.S3Object
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dao.repository.KcUserRepository
import ucb.accounting.backend.dao.repository.S3ObjectRepository
import ucb.accounting.backend.dto.ListUsersDto
import ucb.accounting.backend.dto.PasswordUpdateDto
import ucb.accounting.backend.dto.UserCompanyDto
import ucb.accounting.backend.dto.UserDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.KcUserCompanyMapper
import ucb.accounting.backend.service.MinioService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Date

@Service
class UsersBl @Autowired constructor(
    private val keycloak: Keycloak,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val kcUserRepository: KcUserRepository,
    private val s3ObjectRepository: S3ObjectRepository,
    private val minioService: MinioService
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
        val kcUserEntity: KcUser = kcUserRepository.findByKcUuidAndStatusIsTrue(kcUuid) ?: throw UasException("404-01")

        // Validation of user KcUuid belongs is the same as the logged user
        if (kcUuid != KeycloakSecurityContextHolder.getSubject()) {
            throw UasException("403-01")
        }
        val user: UserRepresentation = keycloak
            .realm(realm)
            .users()
            .get(kcUuid)
            .toRepresentation()

        val userCompanyEntities: List<KcUserCompany> = kcUserCompanyRepository.findAllByKcUser_KcUuidAndStatusIsTrue(kcUuid)
        val userCompanies: List<UserCompanyDto> = userCompanyEntities.map { KcUserCompanyMapper.entityToDto(it) }
        val companies: List<Long> = userCompanies.map { it.companyId }

        // Get s3 object
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(kcUserEntity.s3ProfilePicture.toLong())?: throw UasException("404-13")
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)

        return UserDto(
            companyIds = companies,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            s3ProfilePictureId = kcUserEntity.s3ProfilePicture.toLong(),
            profilePicture = preSignedUrl,
        )
    }

    fun findAllUsersByCompanyId(companyId: Long): List<ListUsersDto> {
        logger.info("Getting all users by company id")
        // Validation that the company exists
        kcUserCompanyRepository.findAllByCompany_CompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Get all users from company
        val userCompanyEntities: List<KcUserCompany> = kcUserCompanyRepository.findAllByCompany_CompanyIdAndStatusIsTrue(companyId)
        //val userCompanies: List<UserCompanyDto> = userCompanyEntities.map { KcUserCompanyMapper.entityToDto(it) }
        val users: List<ListUsersDto> = userCompanyEntities.map { val user: UserRepresentation = keycloak
            .realm(realm)
            .users()
            .get(it.kcUser!!.kcUuid)
            .toRepresentation()
            ListUsersDto(
                kcGroupName = it.kcGroup!!.groupName,
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                creationDate = Date(2021, 1, 1)
            )
        }
        // TODO: Add creation date
        return users
    }

    fun updateUser(kcUuid: String, userDto: UserDto): UserDto {
        logger.info("Updating user info")
        // Validation that the user exists
        val kcUserEntity: KcUser = kcUserRepository.findByKcUuidAndStatusIsTrue(kcUuid) ?: throw UasException("404-01")

        // Validation of user KcUuid belongs is the same as the logged user
        if (kcUuid != KeycloakSecurityContextHolder.getSubject()) {
            throw UasException("403-03")
        }

        // If s3ProfilePictureId is not null, update s3ProfilePicture in KcUser
        if (userDto.s3ProfilePictureId != null) {
            // Validation that the s3ProfilePictureId exists
            s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(userDto.s3ProfilePictureId) ?: throw UasException("404-13")
            kcUserEntity.s3ProfilePicture = userDto.s3ProfilePictureId.toInt()
            kcUserRepository.save(kcUserEntity)
        }

        // Get s3 object
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(kcUserEntity.s3ProfilePicture.toLong())?: throw UasException("404-13")
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)

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

        val userCompanyEntities: List<KcUserCompany> = kcUserCompanyRepository.findAllByKcUser_KcUuidAndStatusIsTrue(kcUuid)
        val userCompanies: List<UserCompanyDto> = userCompanyEntities.map { KcUserCompanyMapper.entityToDto(it) }
        val companies: List<Long> = userCompanies.map { it.companyId }

        return UserDto(
            companyIds = companies,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            s3ProfilePictureId = userDto.s3ProfilePictureId,
            profilePicture = preSignedUrl,
        )
    }

    fun updateUserPassword (kcUuid: String, passwordUpdateDto: PasswordUpdateDto) {
        logger.info("Updating user password")
        // Validation that the user exists
        kcUserRepository.findByKcUuidAndStatusIsTrue(kcUuid) ?: throw UasException("404-01")
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