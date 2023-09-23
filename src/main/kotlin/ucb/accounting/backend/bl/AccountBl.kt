package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.Account
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.AccountDto
import ucb.accounting.backend.dto.ReducedAccountDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.SubaccountMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class AccountBl @Autowired constructor(
    private val accountRepository: AccountRepository,
    private val companyRepository: CompanyRepository,
    private val accountSubGroupRepository: AccountSubGroupRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountBl::class.java.name)
    }

    fun getCompanyAccounts(companyId: Long): List<ReducedAccountDto>{
        logger.info("Starting the business logic to get company accounts")
        logger.info("BL call to get company accounts")

        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is getting accounts")
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-13")

        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")

        val accounts = accountRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        val accountsDto = accounts.map { account -> ReducedAccountDto(account.accountId, account.accountSubgroupId, account.accountCode, account.accountName)}
        logger.info("Finishing the business logic to get company accounts")
        return accountsDto
    }

    fun createAccount(companyId: Long, accountDto: ReducedAccountDto){
        logger.info("Starting the business logic to post an account")
        logger.info("BL call to post an account")

        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is posting an account")
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-12")

        if(accountDto.accountSubgroupId == null) throw UasException("400-11")
        if(accountDto.accountCode == null) throw UasException("400-11")
        if(accountDto.accountName == null) throw UasException("400-11")

        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw  UasException("404-05")
        accountSubGroupRepository.findByCompanyIdAndAccountSubgroupIdAndStatusTrue(companyId.toInt(), accountDto.accountSubgroupId.toLong()) ?: throw UasException("404-08")

        logger.info("Creating Account")
        val accountEntity = Account()

        accountEntity.accountSubgroupId = accountDto.accountSubgroupId
        accountEntity.companyId = companyId.toInt()
        accountEntity.accountCode = accountDto.accountCode
        accountEntity.accountName = accountDto.accountName

        logger.info("Account saved successfully")

        accountRepository.save(accountEntity)
    }

    fun updateAccount(companyId: Long, accountDto: ReducedAccountDto){
        logger.info("Starting the business logic to update an account")
        logger.info("BL call to update an account")

        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is updating an account")
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-14")

        if(accountDto.accountId == null) throw UasException("400-12")
        if(accountDto.accountSubgroupId == null) throw UasException("400-12")
        if(accountDto.accountCode == null) throw UasException("400-12")
        if(accountDto.accountName == null) throw UasException("400-12")

        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw  UasException("404-05")
        accountSubGroupRepository.findByCompanyIdAndAccountSubgroupIdAndStatusTrue(companyId.toInt(), accountDto.accountSubgroupId.toLong()) ?: throw UasException("404-08")
        val existingAccount = accountRepository.findByCompanyIdAndAccountIdAndStatusIsTrue(companyId.toInt(),
            accountDto.accountId
        ) ?: throw UasException("404-09")

        logger.info("Updating Account")

        existingAccount.accountSubgroupId = accountDto.accountSubgroupId
        existingAccount.accountCode = accountDto.accountCode
        existingAccount.accountName = accountDto.accountName

        logger.info("Account updated successfully")
        accountRepository.save(existingAccount)
    }
}