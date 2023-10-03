package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.Account
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.AccountPartialDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.AccountPartialMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class AccountBl @Autowired constructor(
    private val accountRepository: AccountRepository,
    private val accountSubgroupRepository: AccountSubgroupRepository,
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountBl::class.java.name)
    }

    fun createCompanyAccount(companyId: Long, accountDto: AccountPartialDto){
        logger.info("Starting the business logic to post an account")
        logger.info("BL call to post an account")
        // Validation that all the parameters were sent
        if (accountDto.accountSubgroupId == null || accountDto.accountCode == null || accountDto.accountName == null) {
            throw UasException("400-11")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-12")
        logger.info("User $kcUuid is posting an account")

        // Validation that the account subgroup exists
        val accountSubgroupEntity = accountSubgroupRepository.findByAccountSubgroupIdAndStatusIsTrue(accountDto.accountSubgroupId.toLong()) ?: throw UasException("404-08")
        if (accountSubgroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-12")
        }

        logger.info("Creating Account")
        val accountEntity = Account()
        accountEntity.companyId = companyId.toInt()
        accountEntity.accountSubgroupId = accountDto.accountSubgroupId.toInt()
        accountEntity.accountCode = accountDto.accountCode
        accountEntity.accountName = accountDto.accountName
        accountRepository.save(accountEntity)
        logger.info("Account saved successfully")
    }

    fun getCompanyAccounts(companyId: Long): List<AccountPartialDto>{
        logger.info("Starting the business logic to get company accounts")
        logger.info("BL call to get company accounts")
        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-13")
        logger.info("User $kcUuid is getting accounts")

        // Get accounts from the company
        val accountsEntities = accountRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        val accountsDto = accountsEntities.map { AccountPartialMapper.entityToDto(it) }
        logger.info("Finishing the business logic to get company accounts")
        return accountsDto
    }

    fun getCompanyAccount(companyId: Long, accountId: Long): AccountPartialDto{
        logger.info("Starting the business logic to get an account")
        logger.info("BL call to get an account")
        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-13")
        logger.info("User $kcUuid is getting an account")

        // Get account from the company
        val accountEntity = accountRepository.findByAccountIdAndStatusIsTrue( accountId) ?: throw UasException("404-09")

        // Validation that the account belongs to the company
        if (accountEntity.companyId != companyId.toInt()) {
            throw UasException("403-13")
        }

        val accountDto = AccountPartialMapper.entityToDto(accountEntity)
        logger.info("Finishing the business logic to get an account")
        return accountDto
    }

    fun updateCompanyAccount(companyId: Long, accountId: Long, accountPartialDto: AccountPartialDto): AccountPartialDto{
        logger.info("Starting the business logic to update an account")
        logger.info("BL call to update an account")
        // Validation that at least one parameter was sent
        if (accountPartialDto.accountSubgroupId == null && accountPartialDto.accountCode == null && accountPartialDto.accountName == null) {
            throw UasException("400-12")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-14")
        logger.info("User $kcUuid is updating an account")

        // Validation that the account exists
        val accountEntity = accountRepository.findByAccountIdAndStatusIsTrue(accountId) ?: throw UasException("404-09")

        // Validation that the account belongs to the company
        if (accountEntity.companyId != companyId.toInt()) {
            throw UasException("403-14")
        }

        // Validation that the account subgroup exists and belongs to the company
        if (accountPartialDto.accountSubgroupId != null) {
           val accountSubgroupEntity = accountSubgroupRepository.findByAccountSubgroupIdAndStatusIsTrue(accountPartialDto.accountSubgroupId.toLong()) ?: throw UasException("404-08")
            if (accountSubgroupEntity.companyId != companyId.toInt()) {
                throw UasException("403-14")
            }
        }

        accountEntity.accountSubgroupId = (accountPartialDto.accountSubgroupId ?: accountEntity.accountSubgroupId).toInt()
        accountEntity.accountCode = accountPartialDto.accountCode ?: accountEntity.accountCode
        accountEntity.accountName = accountPartialDto.accountName ?: accountEntity.accountName
        accountRepository.save(accountEntity)

        logger.info("Account updated successfully")
        return AccountPartialMapper.entityToDto(accountEntity)
    }
}