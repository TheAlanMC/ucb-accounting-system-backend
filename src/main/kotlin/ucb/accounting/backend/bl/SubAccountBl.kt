package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.Subaccount
import ucb.accounting.backend.dao.repository.AccountRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dao.repository.SubAccountRepository
import ucb.accounting.backend.dto.InsertSubAccountDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class SubAccountBl @Autowired constructor(
    private val subAccountRepository: SubAccountRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val companyRepository: CompanyRepository,
    private val accountRepository: AccountRepository
){

    companion object {
        private val logger = LoggerFactory.getLogger(SubAccountBl::class.java.name)
    }

    fun createSubAccount(companyId: Long, subAccountDto: InsertSubAccountDto){
        logger.info("Starting the business logic to post a subaccount")
        logger.info("BL call to post a subaccount")

        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is posting a subaccount")
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-12")

        if(subAccountDto.subaccountCode == null) throw UasException("400-11")
        if(subAccountDto.subaccountName == null) throw UasException("400-11")

        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw  UasException("404-05")
        accountRepository.findByCompanyIdAndAccountIdAndStatusIsTrue(companyId.toInt(), subAccountDto.accountId!!.toLong()) ?: throw UasException("404-09")

        logger.info("Creating SubAccount")
        val subAccountEntity = Subaccount()

        subAccountEntity.accountId = subAccountDto.accountId
        subAccountEntity.subaccountCode = subAccountDto.subaccountCode
        subAccountEntity.subaccountName = subAccountDto.subaccountName
        subAccountEntity.companyId = companyId.toInt()

        subAccountRepository.save(subAccountEntity)
        logger.info("Finishing the business logic to post a subaccount")
    }

    fun getCompanySubAccounts(companyId: Long): List<InsertSubAccountDto>{
        logger.info("Starting the business logic to get company subaccounts")
        logger.info("BL call to get company subaccounts")

        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is getting subaccounts")
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-13")

        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")

        val subAccounts = subAccountRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        val subAccountsDto = subAccounts.map { subAccount -> InsertSubAccountDto(subAccount.subaccountId,subAccount.accountId, subAccount.subaccountCode, subAccount.subaccountName)}
        logger.info("Finishing the business logic to get company subaccounts")
        return subAccountsDto
    }

    fun updateSubAccount(companyId: Long, subAccountDto: InsertSubAccountDto){
        logger.info("Starting the business logic to put a subaccount")
        logger.info("BL call to put a subaccount")

        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is putting a subaccount")
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-12")

        if(subAccountDto.subaccountCode == null) throw UasException("400-11")
        if(subAccountDto.subaccountName == null) throw UasException("400-11")

        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw  UasException("404-05")
        accountRepository.findByCompanyIdAndAccountIdAndStatusIsTrue(companyId.toInt(), subAccountDto.accountId!!.toLong()) ?: throw UasException("404-09")

        logger.info("Updating SubAccount")
        val subAccountEntity = Subaccount()

        subAccountEntity.subaccountId = subAccountDto.subaccountId!!
        subAccountEntity.accountId = subAccountDto.accountId
        subAccountEntity.subaccountCode = subAccountDto.subaccountCode
        subAccountEntity.subaccountName = subAccountDto.subaccountName
        subAccountEntity.companyId = companyId.toInt()

        subAccountRepository.save(subAccountEntity)
        logger.info("Finishing the business logic to put a subaccount")
    }

}