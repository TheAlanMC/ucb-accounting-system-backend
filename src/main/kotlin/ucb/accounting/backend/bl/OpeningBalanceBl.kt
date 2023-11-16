package ucb.accounting.backend.bl

import com.fasterxml.jackson.databind.deser.std.DateDeserializers.SqlDateDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.FinancialStatement
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.Transaction
import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.util.*


@Service
class OpeningBalanceBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val accountPlanRepository: AccountPlanRepository,
    private val subaccountRepository: SubaccountRepository,
    private val journalEntryRepository: JournalEntryRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(OpeningBalanceBl::class.java.name)
    }

    fun getOpeningBalance(companyId: Long): List<FinancialStatementReportDetailDto> {
        logger.info("Getting opening balance")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-05")
        logger.info("User $kcUuid is getting opening balance of company $companyId")

        val accountCategoryNames = listOf("ACTIVO", "PASIVO", "PATRIMONIO")
        val accountPlanEntities =
            accountPlanRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt(), accountCategoryNames)
        val openingBalance: List<FinancialStatementReportDetailDto> =
            accountPlanEntities.groupBy { it.accountCategoryId }.map { (key, rows) ->
                val accountGroup = rows.groupBy { it.accountGroupId }.map { (key, rows) ->
                    val accountSubgroup = rows.groupBy { it.accountSubgroupId }.map { (key, rows) ->
                        val account = rows.groupBy { it.accountId }.map { (key, rows) ->
                            val subaccount = rows.groupBy { it.subaccountId }.map { (key, rows) ->
                                val subaccount = Subaccount(
                                    subaccountId = rows.first().subaccountId.toLong(),
                                    subaccountCode = rows.first().subaccountCode,
                                    subaccountName = rows.first().subaccountName,
                                    totalAmountBs = BigDecimal(0.00)
                                )
                                subaccount
                            }
                            val account = Account(
                                accountId = rows.first().accountId.toLong(),
                                accountCode = rows.first().accountCode,
                                accountName = rows.first().accountName,
                                subaccounts = subaccount,
                                totalAmountBs = subaccount.sumOf { it.totalAmountBs }
                            )
                            account
                        }
                        val accountSubgroup = AccountSubgroup(
                            accountSubgroupId = rows.first().accountSubgroupId.toLong(),
                            accountSubgroupCode = rows.first().accountSubgroupCode,
                            accountSubgroupName = rows.first().accountSubgroupName,
                            accounts = account,
                            totalAmountBs = account.sumOf { it.totalAmountBs }
                        )
                        accountSubgroup
                    }
                    val accountGroup = AccountGroup(
                        accountGroupId = rows.first().accountGroupId.toLong(),
                        accountGroupCode = rows.first().accountGroupCode,
                        accountGroupName = rows.first().accountGroupName,
                        accountSubgroups = accountSubgroup,
                        totalAmountBs = accountSubgroup.sumOf { it.totalAmountBs }
                    )
                    accountGroup
                }
                val accountCategory = AccountCategory(
                    accountCategoryId = rows.first().accountCategoryId.toLong(),
                    accountCategoryCode = rows.first().accountCategoryCode,
                    accountCategoryName = rows.first().accountCategoryName,
                    accountGroups = accountGroup,
                    totalAmountBs = accountGroup.sumOf { it.totalAmountBs }
                )
                val totalAmountBs = accountCategory.totalAmountBs
                FinancialStatementReportDetailDto(
                    accountCategory = accountCategory,
                    description = accountCategory.accountCategoryName,
                    totalAmountBs = totalAmountBs
                )
            }
        logger.info("Opening balance found")
        return openingBalance
    }

    fun createOpeningBalance(
        companyId: Long,
        openingBalance: OpeningBalanceDto
    ) {
        logger.info("Creating opening balance")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-49")
        logger.info("User $kcUuid is creating opening balance of company $companyId")

        // Validation that the opening balance has not been created
        val journalEntry: JournalEntry? = journalEntryRepository.findByJournalEntryNumberAndGlossAndCompanyIdAndStatusIsTrue(0, "Asiento de apertura", companyId.toInt())
        if (journalEntry != null) {
            logger.error("Opening balance already created")
            throw UasException("409-08")
        }
        var totalAmountAssets = BigDecimal(0.00)
        var totalAmountLiabilities = BigDecimal(0.00)
        var totalAmountEquity = BigDecimal(0.00)

        // Validation that subaccounts IDs exist
        openingBalance.financialStatementReports.map {
            val subaccounts =
                it.accountCategory.accountGroups.flatMap { accountGroups -> accountGroups.accountSubgroups.flatMap { accountSubgroups -> accountSubgroups.accounts.flatMap { accounts -> accounts.subaccounts } } }
            subaccounts.map { subaccount ->
                val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(subaccount.subaccountId)
                    ?: throw UasException("404-10")
                // Validation that subaccounts belong to company
                if (subaccountEntity.companyId != companyId.toInt()) {
                    throw UasException("403-49")
                }
                if (it.accountCategory.accountCategoryName == "ACTIVO") {
                    totalAmountAssets += subaccount.totalAmountBs
                }
                if (it.accountCategory.accountCategoryName == "PASIVO") {
                    totalAmountLiabilities += subaccount.totalAmountBs
                }
                if (it.accountCategory.accountCategoryName == "PATRIMONIO") {
                    totalAmountEquity += subaccount.totalAmountBs
                }
            }
        }

        if (totalAmountAssets != totalAmountLiabilities + totalAmountEquity) {
            logger.error("Total amount of assets is not equal to total amount of liabilities plus total amount of equity")
            throw UasException("400-31")
        }

        logger.info("Saving journal entry")
        val journalEntryEntity = JournalEntry()
        journalEntryEntity.companyId = companyId.toInt()
        journalEntryEntity.documentTypeId = 1
        journalEntryEntity.journalEntryNumber = 0
        journalEntryEntity.gloss = "Asiento de apertura"
        journalEntryEntity.journalEntryAccepted = true
        val savedJournalEntry = journalEntryRepository.save(journalEntryEntity)

        val transactionEntity = Transaction()
        transactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        transactionEntity.transactionDate = java.sql.Date(openingBalance.openingBalanceDate.time)
        transactionEntity.description = "Asiento de apertura"
        val savedTransaction = transactionRepository.save(transactionEntity)

        logger.info("Saving transaction details")
        openingBalance.financialStatementReports.map {
            val subaccounts =
                it.accountCategory.accountGroups.flatMap { accountGroups -> accountGroups.accountSubgroups.flatMap { accountSubgroups -> accountSubgroups.accounts.flatMap { accounts -> accounts.subaccounts } } }
            if (it.accountCategory.accountCategoryName == "ACTIVO") {
                subaccounts.map {subaccount ->
                    if (subaccount.totalAmountBs > BigDecimal(0.00)) {
                        val transactionDetailEntity = TransactionDetail()
                        transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
                        transactionDetailEntity.subaccountId = subaccount.subaccountId.toInt()
                        transactionDetailEntity.debitAmountBs = subaccount.totalAmountBs
                        transactionDetailEntity.creditAmountBs = BigDecimal(0.00)
                        transactionDetailRepository.save(transactionDetailEntity)
                    }
                }
            }
            if (it.accountCategory.accountCategoryName == "PASIVO") {
                subaccounts.map {subaccount ->
                    if (subaccount.totalAmountBs > BigDecimal(0.00)) {
                        val transactionDetailEntity = TransactionDetail()
                        transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
                        transactionDetailEntity.subaccountId = subaccount.subaccountId.toInt()
                        transactionDetailEntity.debitAmountBs = BigDecimal(0.00)
                        transactionDetailEntity.creditAmountBs = subaccount.totalAmountBs
                        transactionDetailRepository.save(transactionDetailEntity)
                    }
                }
            }
            if (it.accountCategory.accountCategoryName == "PATRIMONIO") {
                subaccounts.map {subaccount ->
                    if (subaccount.totalAmountBs > BigDecimal(0.00)) {
                        val transactionDetailEntity = TransactionDetail()
                        transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
                        transactionDetailEntity.subaccountId = subaccount.subaccountId.toInt()
                        transactionDetailEntity.debitAmountBs = BigDecimal(0.00)
                        transactionDetailEntity.creditAmountBs = subaccount.totalAmountBs
                        transactionDetailRepository.save(transactionDetailEntity)
                    }
                }
            }
        }
        logger.info("Opening balance created")
    }
}
