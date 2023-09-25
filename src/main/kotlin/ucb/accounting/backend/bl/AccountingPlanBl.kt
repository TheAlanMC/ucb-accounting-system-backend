package ucb.accounting.backend.bl

import org.keycloak.admin.client.Keycloak
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.AccountCategory
import ucb.accounting.backend.dao.AccountGroup
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.AccountCategoryDto
import ucb.accounting.backend.dto.AccountGroupDto
import ucb.accounting.backend.dto.AccountSubgroupDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class AccountingPlanBl @Autowired constructor(
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val companyRepository: CompanyRepository,
    private val accountCategoryRepository: AccountCategoryRepository,
    private val accountGroupRepository: AccountGroupRepository,
    private val accountSubGroupRepository: AccountSubGroupRepository,
    private val accountRepository: AccountRepository,
    private val subAccountRepository: SubAccountRepository){

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AccountingPlanBl::class.java)
    }

    fun getAccountingPlan(id: Long): List<AccountCategoryDto> {
        logger.info("Getting accounting plan")
        // Validation that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(id.toLong())?: throw UasException("404-05")
        val companyId = company.companyId.toInt()
        logger.info("Company found")
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        FilesBl.logger.info("User $kcUuid is uploading file to company $companyId")
        // Validation of user belongs to company
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId.toLong()) ?: throw UasException("403-05")
        val accountCategoryEntities = accountCategoryRepository.findAllByStatusIsTrue()
        logger.info("Accounting plan found")
        val accountCategories = accountCategoryEntities.map { accountCategory ->
            AccountCategoryDto (
            accountCategory.accountCategoryId,
            accountCategory.accountCategoryCode,
            accountCategory.accountCategoryName,
            accountGroupRepository.findAllByCompanyIdAndAccountCategoryIdAndStatusIsTrue(companyId, accountCategory.accountCategoryId.toInt()).map { accountGroup ->
                AccountGroupDto(
                    accountGroup.accountGroupId,
                    accountGroup.accountGroupCode,
                    accountGroup.accountGroupName,
                    accountSubGroupRepository.findAllByCompanyIdAndAccountGroupIdAndStatusIsTrue(companyId, accountGroup.accountGroupId.toInt()).map { accountSubGroup ->
                        AccountSubgroupDto(
                            accountSubGroup.accountSubgroupId,
                            accountSubGroup.accountSubgroupCode,
                            accountSubGroup.accountSubgroupName,
                            accountRepository.findAllByCompanyIdAndAccountSubgroupIdAndStatusIsTrue(companyId, accountSubGroup.accountSubgroupId.toInt()).map { account ->
                                ucb.accounting.backend.dto.AccountDto(
                                    account.accountId,
                                    account.accountCode,
                                    account.accountName,
                                    subAccountRepository.findAllByCompanyIdAndAccountIdAndStatusIsTrue(companyId, account.accountId.toInt()).map { subAccount ->
                                        ucb.accounting.backend.dto.SubAccountDto(
                                            subAccount.subaccountId,
                                            subAccount.subaccountCode,
                                            subAccount.subaccountName
                                        )
                                    }
                                )
                            }
                        )

                    }

                )

            }
        ) }
        return accountCategories
    }

}