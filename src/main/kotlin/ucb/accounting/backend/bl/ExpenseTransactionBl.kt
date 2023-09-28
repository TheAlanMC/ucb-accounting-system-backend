package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.*
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.ExpenseTransactionDto
import ucb.accounting.backend.dto.ExpenseTransactionPartialDto
import ucb.accounting.backend.dto.SubaccountDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.ExpenseTransactionMapper
import ucb.accounting.backend.mapper.SubaccountMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal

@Service
class ExpenseTransactionBl @Autowired constructor(
    private val attachmentRepository: AttachmentRepository,
    private val companyRepository: CompanyRepository,
    private val documentTypeRepository: DocumentTypeRepository,
    private val expenseTransactionDetailRepository: ExpenseTransactionDetailRepository,
    private val expenseTransactionRepository: ExpenseTransactionRepository,
    private val journalEntryRepository: JournalEntryRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val subaccountRepository: SubaccountRepository,
    private val supplierRepository: SupplierRepository,
    private val transactionAttachmentRepository: TransactionAttachmentRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val transactionRepository: TransactionRepository,
){
    companion object{
        private val logger = LoggerFactory.getLogger(ExpenseTransactionBl::class.java.name)
    }
    fun createExpenseTransaction(companyId: Long, expenseTransactionDto: ExpenseTransactionDto){
        logger.info("Starting the BL call to create expense transaction")
        // Validate that no field is null but attachments
        if (expenseTransactionDto.supplierId == null || expenseTransactionDto.subaccountId == null ||
            expenseTransactionDto.gloss.isNullOrEmpty() || expenseTransactionDto.description.isNullOrEmpty() ||
            expenseTransactionDto.expenseTransactionDate == null || expenseTransactionDto.expenseTransactionDetails == null ||
            expenseTransactionDto.expenseTransactionNumber == null) throw UasException("400-28")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that attachments were sent
        if (!expenseTransactionDto.attachments.isNullOrEmpty()) {
            // Validation that attachments exist
            expenseTransactionDto.attachments.map {
                attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId) ?: throw UasException("404-11")
            }
        }

        // Validation that subaccounts exist
        expenseTransactionDto.expenseTransactionDetails.map {
            val subaccountEntities = subaccountRepository.findBySubaccountIdAndStatusIsTrue(it.subaccountId) ?: throw UasException("404-10")
            // Validation that subaccounts belong to the company
            if (subaccountEntities.companyId != companyId.toInt()) throw UasException("403-34")
        }

        // Validation subaccount exists
        val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(expenseTransactionDto.subaccountId) ?: throw UasException("404-10")
        // Validation that subaccount belongs to the company
        if (subaccountEntity.companyId != companyId.toInt()) throw UasException("403-34")

        // Validation supplier exists
        supplierRepository.findBySupplierIdAndStatusIsTrue(expenseTransactionDto.supplierId) ?: throw UasException("404-15")
        // Validation that the expense transaction number is unique
        if (expenseTransactionRepository.findByCompanyIdAndExpenseTransactionNumberAndStatusIsTrue(companyId.toInt(), expenseTransactionDto.expenseTransactionNumber) != null) throw UasException("409-06")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-34")
        logger.info("User $kcUuid is registering a new expense transaction")

        // Creating journal entry
        logger.info("Saving journal entry")
        val journalEntryEntity = JournalEntry()
        journalEntryEntity.companyId = companyId.toInt()
        journalEntryEntity.documentTypeId = documentTypeRepository.findByDocumentTypeNameAndStatusIsTrue("Egreso")!!.documentTypeId.toInt()
        journalEntryEntity.journalEntryNumber = expenseTransactionDto.expenseTransactionNumber
        journalEntryEntity.gloss = expenseTransactionDto.gloss
        val savedJournalEntry = journalEntryRepository.save(journalEntryEntity)

        logger.info("Saving expense transaction")
        val transactionEntity = Transaction()
        transactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        transactionEntity.transactionDate = expenseTransactionDto.expenseTransactionDate
        transactionEntity.description = expenseTransactionDto.description
        val savedTransaction = transactionRepository.save(transactionEntity)

        if (!expenseTransactionDto.attachments.isNullOrEmpty()) {
            logger.info("Saving attachments")
            expenseTransactionDto.attachments.map {
                val transactionAttachmentEntity = TransactionAttachment()
                transactionAttachmentEntity.transaction = savedTransaction
                transactionAttachmentEntity.attachment = attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId)!!
                transactionAttachmentRepository.save(transactionAttachmentEntity)
            }
        } else {
            logger.info("No attachments were sent")
        }

        logger.info("Saving transaction details, credit")
        expenseTransactionDto.expenseTransactionDetails.map {
            val transactionDetailEntity = TransactionDetail()
            transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
            transactionDetailEntity.subaccountId = it.subaccountId.toInt()
            transactionDetailEntity.debitAmountBs = BigDecimal.ZERO
            transactionDetailEntity.creditAmountBs = it.amountBs
            transactionDetailRepository.save(transactionDetailEntity)
        }

        logger.info("Saving the total of the expense transaction, total credit")
        val transactionDetailEntity = TransactionDetail()
        transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
        transactionDetailEntity.subaccountId = expenseTransactionDto.subaccountId.toInt()
        transactionDetailEntity.debitAmountBs = expenseTransactionDto.expenseTransactionDetails.sumOf {it.amountBs}
        transactionDetailEntity.creditAmountBs = BigDecimal.ZERO
        transactionDetailRepository.save(transactionDetailEntity)

        logger.info("Saving expense transaction")
        val expenseTransactionEntity = ExpenseTransaction()
        expenseTransactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        expenseTransactionEntity.companyId = companyId.toInt()
        expenseTransactionEntity.supplierId = expenseTransactionDto.supplierId.toInt()
        expenseTransactionEntity.subaccountId = expenseTransactionDto.subaccountId.toInt()
        expenseTransactionEntity.expenseTransactionNumber = expenseTransactionDto.expenseTransactionNumber
        expenseTransactionEntity.expenseTransactionDate = expenseTransactionDto.expenseTransactionDate
        expenseTransactionEntity.description = expenseTransactionDto.description
        expenseTransactionEntity.gloss = expenseTransactionDto.gloss
        val savedExpenseTransaction = expenseTransactionRepository.save(expenseTransactionEntity)

        logger.info("Saving expense transaction details")
        expenseTransactionDto.expenseTransactionDetails.map {
            val expenseTransactionDetailEntity = ExpenseTransactionDetail()
            expenseTransactionDetailEntity.expenseTransactionId = savedExpenseTransaction.expenseTransactionId
            expenseTransactionDetailEntity.subaccountId = it.subaccountId
            expenseTransactionDetailEntity.amountBs = it.amountBs
            expenseTransactionDetailRepository.save(expenseTransactionDetailEntity)
        }
        logger.info("Expense transaction created successfully")
    }

    fun getSubaccountsForExpenseTransaction(companyId: Long):List<SubaccountDto>{
        logger.info("Starting the BL call to get subaccounts for expense transaction")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-16")
        logger.info("User $kcUuid is getting subaccounts for expense transaction")

        // Getting subaccounts
        val subaccountEntities = subaccountRepository.findAllByAccountAccountSubgroupAccountGroupAccountCategoryAccountCategoryNameAndCompanyIdAndStatusIsTrue("Egresos", companyId.toInt())
        logger.info("Subaccounts for expense transaction obtained successfully")
        return subaccountEntities.map { SubaccountMapper.entityToDto(it) }
    }

    fun getExpenseTransactions(companyId: Long): List<ExpenseTransactionPartialDto>{
        logger.info("Starting the BL call to get expense transactions")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-36")
        logger.info("User $kcUuid is getting expense transactions")

        // Getting expense transactions
        val expenseTransactionEntities = expenseTransactionRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        logger.info("Expense transactions obtained successfully")
        return expenseTransactionEntities.map { expenseTransactionEntity ->
            ExpenseTransactionMapper.entityToDto(expenseTransactionEntity,
                expenseTransactionDetailRepository.findAllByExpenseTransactionIdAndStatusIsTrue(expenseTransactionEntity.expenseTransactionId).sumOf { it.amountBs }
            )
        }
    }

    fun getLastExpenseTransactionNumber(companyId: Long): Int {
        logger.info("Starting the BL call to get last expense transaction number")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-44")
        logger.info("User $kcUuid is getting last expense transaction number from company $companyId")

        // Get last expense transaction number
        val lastExpenseTransactionNumber = expenseTransactionRepository.findFirstByCompanyIdAndStatusIsTrueOrderByExpenseTransactionNumberDesc(companyId.toInt())?.expenseTransactionNumber ?: 0
        logger.info("Last expense transaction number is $lastExpenseTransactionNumber")

        return lastExpenseTransactionNumber + 1
    }
}