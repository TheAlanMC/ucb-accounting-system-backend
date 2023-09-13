package ucb.accounting.backend.bl

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

@Service
class AccountingPlanBl @Autowired constructor(
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
        val company = companyRepository.findByCompanyIdAndStatusTrue(id) ?: throw UasException("404-01")
        logger.info("Company found")
        val accountCategoryEntities = accountCategoryRepository.findAllByStatusTrue()
        logger.info("Accounting plan found")
        val accountCategories = accountCategoryEntities.map { accountCategory ->
            AccountCategoryDto (
            accountCategory.accountCategoryId,
            accountCategory.accountCategoryCode,
            accountCategory.accountCategoryName,
            accountGroupRepository.findAllByCompanyIdAndAccountCategoryIdAndStatusIsTrue(company.companyId, accountCategory.accountCategoryId.toLong()).map { accountGroup ->
                AccountGroupDto(
                    accountGroup.accountGroupId,
                    accountGroup.accountGroupCode,
                    accountGroup.accountGroupName,
                    accountSubGroupRepository.findAllByCompanyIdAndAccountGroupIdAndStatusIsTrue(company.companyId, accountGroup.accountGroupId.toLong()).map { accountSubGroup ->
                        AccountSubgroupDto(
                            accountSubGroup.accountSubgroupId,
                            accountSubGroup.accountSubgroupCode,
                            accountSubGroup.accountSubgroupName,
                            accountRepository.findAllByCompanyIdAndAccountSubgroupIdAndStatusIsTrue(company.companyId, accountSubGroup.accountSubgroupId.toLong()).map { account ->
                                ucb.accounting.backend.dto.AccountDto(
                                    account.accountId,
                                    account.accountCode,
                                    account.accountName,
                                    subAccountRepository.findAllByCompanyIdAndAccountIdAndStatusIsTrue(company.companyId, account.accountId.toLong()).map { subAccount ->
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