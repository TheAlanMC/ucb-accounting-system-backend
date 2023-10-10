package ucb.accounting.backend.bl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.AccountGroup
import ucb.accounting.backend.dao.repository.AccountCategoryRepository
import ucb.accounting.backend.dao.repository.AccountGroupRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dto.AccountGroupPartialDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.AccountGroupPartialMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class AccountGroupBl @Autowired constructor(
    private val accountCategoryRepository: AccountCategoryRepository,
    private val accountGroupRepository: AccountGroupRepository,
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
){

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AccountGroupBl::class.java)
    }

    fun createAccountGroup(companyId: Long, accountGroupPartialDto: AccountGroupPartialDto) {
        logger.info("Creating account group")
        // Validation that all the fields were sent
        if (accountGroupPartialDto.accountCategoryId == null || accountGroupPartialDto.accountGroupCode == null || accountGroupPartialDto.accountGroupName == null) {
            throw UasException("400-07")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-18")
        logger.info("User $kcUuid is creating account group to company $companyId")

        // Validation that the account category exists
        accountCategoryRepository.findByAccountCategoryIdAndStatusIsTrue(accountGroupPartialDto.accountCategoryId)?: throw UasException("404-06")

        val accountGroupEntity = AccountGroup ()
        accountGroupEntity.companyId = companyId.toInt()
        accountGroupEntity.accountCategoryId = accountGroupPartialDto.accountCategoryId.toInt()
        accountGroupEntity.accountGroupCode = accountGroupPartialDto.accountGroupCode
        accountGroupEntity.accountGroupName = accountGroupPartialDto.accountGroupName
        accountGroupRepository.save(accountGroupEntity)
        logger.info("Account group created")
    }

    fun getAccountGroups(companyId: Long): List<AccountGroupPartialDto> {
        logger.info("Getting account groups")
        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-07")
        logger.info("User $kcUuid is getting account groups from company $companyId")

        // Get all the account groups from the company
        val accountGroupEntities = accountGroupRepository.findAllByCompanyIdAndStatusIsTrueOrderByAccountGroupIdAsc(companyId.toInt())
        val accountGroups = accountGroupEntities.map {AccountGroupPartialMapper.entityToDto(it)}
        logger.info("Account groups retrieved")
        return accountGroups
    }

    fun getAccountGroup(companyId: Long, accountGroupId: Long): AccountGroupPartialDto {
        logger.info("Getting account group")
        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-07")
        logger.info("User $kcUuid is getting account group to company $companyId")

        // Validation that the account group exists
        val accountGroupEntity =
            accountGroupRepository.findByAccountGroupIdAndStatusIsTrue(accountGroupId) ?: throw UasException("404-07")

        // Validation that the account group belongs to the company
        if (accountGroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-07")
        }

        logger.info("Account group retrieved")
        return AccountGroupPartialMapper.entityToDto(accountGroupEntity)
    }

    fun updateAccountGroup(companyId: Long, accountGroupId: Long, accountGroupPartialDto: AccountGroupPartialDto): AccountGroupPartialDto{
        logger.info("Updating account group")
        // Validation that at least one parameter was sent
        if (accountGroupPartialDto.accountCategoryId == null && accountGroupPartialDto.accountGroupCode == null && accountGroupPartialDto.accountGroupName == null) {
            throw UasException("400-08")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-08")
        logger.info("User $kcUuid is updating account group to company $companyId")

        // Validation that the account group exists
        val accountGroupEntity = accountGroupRepository.findByAccountGroupIdAndStatusIsTrue(accountGroupId)?: throw UasException("404-07")

        //validation that the account group belongs to the company
        if (accountGroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-08")
        }

        // Validation that the account category exists
        if (accountGroupPartialDto.accountCategoryId != null) accountCategoryRepository.findByAccountCategoryIdAndStatusIsTrue(accountGroupPartialDto.accountCategoryId)?: throw UasException("404-06")

        // Update the account group
        accountGroupEntity.accountCategoryId = (accountGroupPartialDto.accountCategoryId ?: accountGroupEntity.accountCategoryId).toInt()
        accountGroupEntity.accountGroupCode = accountGroupPartialDto.accountGroupCode ?: accountGroupEntity.accountGroupCode
        accountGroupEntity.accountGroupName = accountGroupPartialDto.accountGroupName ?: accountGroupEntity.accountGroupName
        val savedAccountGroupEntity = accountGroupRepository.save(accountGroupEntity)

        logger.info("Account group updated")
        return AccountGroupPartialMapper.entityToDto(savedAccountGroupEntity)
    }

}