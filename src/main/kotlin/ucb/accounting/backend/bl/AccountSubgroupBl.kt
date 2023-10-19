package ucb.accounting.backend.bl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.AccountSubgroup
import ucb.accounting.backend.dao.repository.AccountGroupRepository
import ucb.accounting.backend.dao.repository.AccountSubgroupRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dto.AccountSubgroupPartialDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.AccountSubgroupPartialMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class AccountSubgroupBl @Autowired constructor(
    private val accountGroupRepository: AccountGroupRepository,
    private val accountSubgroupRepository: AccountSubgroupRepository,
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
){

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AccountGroupBl::class.java)
    }

    fun createAccountSubgroup(companyId: Long, accountSubgroupPartialDto: AccountSubgroupPartialDto) {
        logger.info("Creating account sub group")
        // Validation that all the parameters were sent
        if (accountSubgroupPartialDto.accountGroupId == null || accountSubgroupPartialDto.accountSubgroupCode == null || accountSubgroupPartialDto.accountSubgroupName == null) {
            throw UasException("400-09")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")
        logger.info("User $kcUuid is creating account sub group to company $companyId")

        // Validation that the account group exists
        val accountGroupEntity = accountGroupRepository.findByAccountGroupIdAndStatusIsTrue(accountSubgroupPartialDto.accountGroupId.toLong())?: throw UasException("404-07")
        if (accountGroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-09")
        }

        val accountSubgroupEntity = AccountSubgroup ()
        accountSubgroupEntity.companyId = companyId.toInt()
        accountSubgroupEntity.accountGroupId = accountSubgroupPartialDto.accountGroupId.toInt()
        accountSubgroupEntity.accountSubgroupCode = accountSubgroupPartialDto.accountSubgroupCode
        accountSubgroupEntity.accountSubgroupName = accountSubgroupPartialDto.accountSubgroupName
        accountSubgroupRepository.save(accountSubgroupEntity)
        logger.info("Account sub group created")
    }

    fun getAccountSubgroups(companyId: Long): List<AccountSubgroupPartialDto> {
            logger.info("Getting account sub groups")
            // Validation that the company exists
            companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

            // Validation that the user belongs to the company
            val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
            kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-07")
            logger.info("User $kcUuid is getting account sub groups to company $companyId")

            // Get all the account subgroups from the company
            val accountSubgroupsEntities = accountSubgroupRepository.findAllByCompanyIdAndStatusIsTrueOrderByAccountSubgroupIdAsc(companyId.toInt())
            val accountSubgroupsDto = accountSubgroupsEntities.map { AccountSubgroupPartialMapper.entityToDto(it)}
            logger.info("Account sub groups retrieved")
            return accountSubgroupsDto
    }

    fun getAccountSubgroup(companyId: Long, accountSubgroupId: Long): AccountSubgroupPartialDto {
        logger.info("Getting account sub group")
        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-10")
        logger.info("User $kcUuid is getting account sub group to company $companyId")

        // Validation that the account subgroup exists
        val accountSubgroupEntity = accountSubgroupRepository.findByAccountSubgroupIdAndStatusIsTrue(accountSubgroupId)?: throw UasException("404-08")

        // Validation that the account subgroup belongs to the company
        if (accountSubgroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-10")
        }

        logger.info("Account subgroup retrieved")
        return AccountSubgroupPartialMapper.entityToDto(accountSubgroupEntity)
    }

    fun updateAccountSubgroup(companyId: Long, accountSubgroupId: Long, accountSubgroupPartialDto: AccountSubgroupPartialDto): AccountSubgroupPartialDto {
        logger.info("Updating account sub group")
        // Validation that at least one parameter was sent
        if (accountSubgroupPartialDto.accountGroupId == null && accountSubgroupPartialDto.accountSubgroupCode == null && accountSubgroupPartialDto.accountSubgroupName == null) {
            throw UasException("400-10")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId)?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-11")
        logger.info("User $kcUuid is updating account sub group to company $companyId")

        // Validation that the account subgroup exists
        val accountSubgroupEntity = accountSubgroupRepository.findByAccountSubgroupIdAndStatusIsTrue(accountSubgroupId)?: throw UasException("404-08")

        // Validation that the account subgroup belongs to the company
        if (accountSubgroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-11")
        }

        // Validation that the account group exists
        if (accountSubgroupPartialDto.accountGroupId != null) {
            val accountGroupEntity = accountGroupRepository.findByAccountGroupIdAndStatusIsTrue(accountSubgroupPartialDto.accountGroupId)?: throw UasException("404-07")
            if (accountGroupEntity.companyId != companyId.toInt()) {
                throw UasException("403-11")
            }
        }

        accountSubgroupEntity.accountGroupId = (accountSubgroupPartialDto.accountGroupId ?: accountSubgroupEntity.accountGroupId).toInt()
        accountSubgroupEntity.accountSubgroupCode = accountSubgroupPartialDto.accountSubgroupCode ?: accountSubgroupEntity.accountSubgroupCode
        accountSubgroupEntity.accountSubgroupName = accountSubgroupPartialDto.accountSubgroupName ?: accountSubgroupEntity.accountSubgroupName
        accountSubgroupRepository.save(accountSubgroupEntity)

        logger.info("Account subgroup updated")
        return AccountSubgroupPartialMapper.entityToDto(accountSubgroupEntity)
    }

}