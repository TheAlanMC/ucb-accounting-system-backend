package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.*
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.ExpenseTransactionDto
import ucb.accounting.backend.dto.InvoiceDto
import ucb.accounting.backend.dto.PaymentDto
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
    private val paymentTypeRepository: PaymentTypeRepository,
    private val subaccountRepository: SubaccountRepository,
    private val supplierRepository: SupplierRepository,
    private val transactionAttachmentRepository: TransactionAttachmentRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionTypeRepository: TransactionTypeRepository,
){
    companion object{
        private val logger = LoggerFactory.getLogger(ExpenseTransactionBl::class.java.name)
    }
    fun createInvoiceExpenseTransaction(companyId: Long, invoiceDto: InvoiceDto){
        logger.info("Starting the BL call to create expense transaction")
        // Validate that no field is null but attachments
        if (invoiceDto.clientId == null || invoiceDto.paymentTypeId == null ||
            invoiceDto.gloss.isNullOrEmpty() || invoiceDto.description.isNullOrEmpty() ||
            invoiceDto.invoiceDate == null || invoiceDto.invoiceDetails == null ||
            invoiceDto.invoiceNumber == null) throw UasException("400-28")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that attachments were sent
        if (!invoiceDto.attachments.isNullOrEmpty()) {
            // Validation that attachments exist
            invoiceDto.attachments.map {
                attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId) ?: throw UasException("404-11")
            }
        }

        // Validation that subaccounts exist
        invoiceDto.invoiceDetails.map {
            val subaccountEntities = subaccountRepository.findBySubaccountIdAndStatusIsTrue(it.subaccountId) ?: throw UasException("404-10")
            // Validation that subaccounts belong to the company
            if (subaccountEntities.companyId != companyId.toInt()) throw UasException("403-34")
        }

        // Validation that the transaction type exists
        val transactionTypeEntity = transactionTypeRepository.findByTransactionTypeNameAndStatusIsTrue("Factura") ?: throw UasException("404-17")
        // Validation that the payment type exists
        paymentTypeRepository.findByPaymentTypeIdAndStatusIsTrue(invoiceDto.paymentTypeId.toLong()) ?: throw UasException("404-18")

        // Validation that the supplier exists
        val supplierEntity = supplierRepository.findBySupplierIdAndStatusIsTrue(invoiceDto.clientId.toLong()) ?: throw UasException("404-15")
        // Validation that the supplier belongs to the company
        if (supplierEntity.companyId != companyId.toInt()) throw UasException("403-34")

        // Validation that the invoice expense transaction number is unique
        if (expenseTransactionRepository.findByCompanyIdAndTransactionTypeIdAndExpenseTransactionNumberAndStatusIsTrue(invoiceDto.invoiceNumber, transactionTypeEntity.transactionTypeId.toInt(), companyId.toInt()) != null) throw UasException("400-29")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-34")
        logger.info("User $kcUuid is registering a new expense transaction")

        // Creating journal entry
        logger.info("Saving journal entry")
        val journalEntryEntity = JournalEntry()
        journalEntryEntity.companyId = companyId.toInt()
        journalEntryEntity.documentTypeId = documentTypeRepository.findByDocumentTypeNameAndStatusIsTrue("Egreso")!!.documentTypeId.toInt()
        journalEntryEntity.journalEntryNumber = invoiceDto.invoiceNumber
        journalEntryEntity.gloss = invoiceDto.gloss
        val savedJournalEntry = journalEntryRepository.save(journalEntryEntity)

        logger.info("Saving expense transaction")
        val transactionEntity = Transaction()
        transactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        transactionEntity.transactionDate = invoiceDto.invoiceDate
        transactionEntity.description = invoiceDto.description
        val savedTransaction = transactionRepository.save(transactionEntity)

        if (!invoiceDto.attachments.isNullOrEmpty()) {
            logger.info("Saving attachments")
            invoiceDto.attachments.map {
                val transactionAttachmentEntity = TransactionAttachment()
                transactionAttachmentEntity.transaction = savedTransaction
                transactionAttachmentEntity.attachment = attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId)!!
                transactionAttachmentRepository.save(transactionAttachmentEntity)
            }
        } else {
            logger.info("No attachments were sent")
        }

        logger.info("Saving transaction details, credit")
        invoiceDto.invoiceDetails.map {
            val transactionDetailEntity = TransactionDetail()
            transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
            transactionDetailEntity.subaccountId = it.subaccountId.toInt()
            transactionDetailEntity.debitAmountBs = BigDecimal.ZERO
            transactionDetailEntity.creditAmountBs = it.unitPriceBs.times(it.quantity.toBigDecimal())
            transactionDetailRepository.save(transactionDetailEntity)
        }

        logger.info("Saving the total of the expense transaction, total credit")
        val transactionDetailEntity = TransactionDetail()
        transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
        transactionDetailEntity.subaccountId = supplierEntity.subaccountId
        transactionDetailEntity.debitAmountBs = invoiceDto.invoiceDetails.sumOf { it.unitPriceBs.times(it.quantity.toBigDecimal()) }
        transactionDetailEntity.creditAmountBs = BigDecimal.ZERO
        transactionDetailRepository.save(transactionDetailEntity)

        logger.info("Saving expense transaction")
        val expenseTransactionEntity = ExpenseTransaction()
        expenseTransactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        expenseTransactionEntity.transactionTypeId = transactionTypeEntity.transactionTypeId.toInt()
        expenseTransactionEntity.paymentTypeId = invoiceDto.paymentTypeId.toInt()
        expenseTransactionEntity.companyId = companyId.toInt()
        expenseTransactionEntity.supplierId = invoiceDto.clientId.toInt()
        expenseTransactionEntity.subaccountId = supplierEntity.subaccountId
        expenseTransactionEntity.expenseTransactionNumber = invoiceDto.invoiceNumber
        expenseTransactionEntity.expenseTransactionReference = invoiceDto.reference ?: invoiceDto.invoiceNumber.toString()
        expenseTransactionEntity.expenseTransactionDate = invoiceDto.invoiceDate
        expenseTransactionEntity.description = invoiceDto.description
        expenseTransactionEntity.gloss = invoiceDto.gloss
        val savedExpenseTransaction = expenseTransactionRepository.save(expenseTransactionEntity)

        logger.info("Saving expense transaction details")
        invoiceDto.invoiceDetails.map {
            val expenseTransactionDetailEntity = ExpenseTransactionDetail()
            expenseTransactionDetailEntity.expenseTransactionId = savedExpenseTransaction.expenseTransactionId
            expenseTransactionDetailEntity.subaccountId = it.subaccountId
            expenseTransactionDetailEntity.quantity = it.quantity
            expenseTransactionDetailEntity.unitPriceBs = it.unitPriceBs
            expenseTransactionDetailRepository.save(expenseTransactionDetailEntity)
        }
        logger.info("Expense transaction created successfully")
    }

    fun getSubaccountsForInvoiceExpenseTransaction(companyId: Long):List<SubaccountDto>{
        logger.info("Starting the BL call to get subaccounts for expense transaction")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-16")
        logger.info("User $kcUuid is getting subaccounts for expense transaction")

        // Getting subaccounts
        val subaccountEntities = subaccountRepository.findAllByAccountAccountSubgroupAccountGroupAccountCategoryAccountCategoryNameAndCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc("EGRESOS", companyId.toInt())
        logger.info("Subaccounts for expense transaction obtained successfully")
        return subaccountEntities.map { SubaccountMapper.entityToDto(it) }
    }

    fun getLastInvoiceExpenseTransactionNumber(companyId: Long): Int {
        logger.info("Starting the BL call to get last expense transaction number")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-44")
        logger.info("User $kcUuid is getting last expense transaction number from company $companyId")

        // Get last expense transaction number
        val transactionTypeEntity = transactionTypeRepository.findByTransactionTypeNameAndStatusIsTrue("Factura") ?: throw UasException("404-17")
        val lastExpenseTransactionNumber = expenseTransactionRepository.findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderByExpenseTransactionNumberDesc(companyId.toInt(), transactionTypeEntity.transactionTypeId.toInt())?.expenseTransactionNumber ?: 0
        logger.info("Last expense transaction number is $lastExpenseTransactionNumber")

        return lastExpenseTransactionNumber + 1
    }

    fun createPaymentExpenseTransaction(companyId: Long, paymentDto: PaymentDto){
        logger.info("Starting the BL call to create expense transaction")
        // Validate that no field is null but attachments
        if (paymentDto.clientId == null || paymentDto.paymentTypeId == null ||
            paymentDto.gloss.isNullOrEmpty() || paymentDto.description.isNullOrEmpty() ||
            paymentDto.paymentDate == null || paymentDto.paymentDetail == null ||
            paymentDto.paymentNumber == null) throw UasException("400-28")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that attachments were sent
        if (!paymentDto.attachments.isNullOrEmpty()) {
            // Validation that attachments exist
            paymentDto.attachments.map {
                attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId) ?: throw UasException("404-11")
            }
        }

        // Validation that subaccounts exist
        val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(paymentDto.paymentDetail.subaccountId) ?: throw UasException("404-10")
        // Validation that subaccount belongs to company
        if (subaccountEntity.companyId != companyId.toInt()) throw UasException("403-34")

        // Validation that the transaction type exists
        val transactionTypeEntity = transactionTypeRepository.findByTransactionTypeNameAndStatusIsTrue("Recibo") ?: throw UasException("404-17")
        // Validation that the payment type exists
        paymentTypeRepository.findByPaymentTypeIdAndStatusIsTrue(paymentDto.paymentTypeId.toLong()) ?: throw UasException("404-18")

        // Validation supplier exists
        val supplierEntity = supplierRepository.findBySupplierIdAndStatusIsTrue(paymentDto.clientId.toLong()) ?: throw UasException("404-14")
        // Validation that the supplier belongs to the company
        if (supplierEntity.companyId != companyId.toInt()) throw UasException("403-34")

        // Validation that the payment expense transaction number is unique
        if (expenseTransactionRepository.findByCompanyIdAndTransactionTypeIdAndExpenseTransactionNumberAndStatusIsTrue(companyId.toInt(), transactionTypeEntity.transactionTypeId.toInt(), paymentDto.paymentNumber) != null) throw UasException("409-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-33")
        logger.info("User $kcUuid is registering a new expense transaction")

        // Creating journal entry
        logger.info("Saving journal entry")
        val journalEntryEntity = JournalEntry()
        journalEntryEntity.companyId = companyId.toInt()
        journalEntryEntity.documentTypeId = documentTypeRepository.findByDocumentTypeNameAndStatusIsTrue("Ingreso")!!.documentTypeId.toInt()
        journalEntryEntity.journalEntryNumber = paymentDto.paymentNumber
        journalEntryEntity.gloss = paymentDto.gloss
        val savedJournalEntry = journalEntryRepository.save(journalEntryEntity)

        logger.info("Saving expense transaction")
        val transactionEntity = Transaction()
        transactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        transactionEntity.transactionDate = paymentDto.paymentDate
        transactionEntity.description = paymentDto.description
        val savedTransaction = transactionRepository.save(transactionEntity)

        if (!paymentDto.attachments.isNullOrEmpty()) {
            logger.info("Saving attachments")
            paymentDto.attachments.map {
                val transactionAttachmentEntity = TransactionAttachment()
                transactionAttachmentEntity.transaction = savedTransaction
                transactionAttachmentEntity.attachment = attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId)!!
                transactionAttachmentRepository.save(transactionAttachmentEntity)
            }
        } else {
            logger.info("No attachments were sent")
        }

        logger.info("Saving transaction detail, debit")
        val transactionDetailDebitEntity = TransactionDetail()
        transactionDetailDebitEntity.transactionId = savedTransaction.transactionId.toInt()
        transactionDetailDebitEntity.subaccountId = subaccountEntity.subaccountId.toInt()
        transactionDetailDebitEntity.debitAmountBs = BigDecimal.ZERO
        transactionDetailDebitEntity.creditAmountBs = paymentDto.paymentDetail.amountBs
        transactionDetailRepository.save(transactionDetailDebitEntity)

        logger.info("Saving the total of the expense transaction, credit")
        val transactionDetailCreditEntity = TransactionDetail()
        transactionDetailCreditEntity.transactionId = savedTransaction.transactionId.toInt()
        transactionDetailCreditEntity.subaccountId = supplierEntity.subaccountId
        transactionDetailCreditEntity.debitAmountBs = paymentDto.paymentDetail.amountBs
        transactionDetailCreditEntity.creditAmountBs = BigDecimal.ZERO
        transactionDetailRepository.save(transactionDetailCreditEntity)

        logger.info("Saving expense transaction")
        val expenseTransactionEntity = ExpenseTransaction()
        expenseTransactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        expenseTransactionEntity.transactionTypeId = transactionTypeEntity.transactionTypeId.toInt()
        expenseTransactionEntity.paymentTypeId = paymentDto.paymentTypeId.toInt()
        expenseTransactionEntity.companyId = companyId.toInt()
        expenseTransactionEntity.supplierId = paymentDto.clientId.toInt()
        expenseTransactionEntity.subaccountId = supplierEntity.subaccountId
        expenseTransactionEntity.expenseTransactionNumber = paymentDto.paymentNumber
        expenseTransactionEntity.expenseTransactionReference = paymentDto.reference ?: paymentDto.paymentNumber.toString()
        expenseTransactionEntity.expenseTransactionDate = paymentDto.paymentDate
        expenseTransactionEntity.description = paymentDto.description
        expenseTransactionEntity.gloss = paymentDto.gloss
        val savedExpenseTransaction = expenseTransactionRepository.save(expenseTransactionEntity)

        logger.info("Saving expense transaction detail")
        val expenseTransactionDetailEntity = ExpenseTransactionDetail()
        expenseTransactionDetailEntity.expenseTransactionId = savedExpenseTransaction.expenseTransactionId
        expenseTransactionDetailEntity.subaccountId = paymentDto.paymentDetail.subaccountId
        expenseTransactionDetailEntity.amountBs = paymentDto.paymentDetail.amountBs
        expenseTransactionDetailRepository.save(expenseTransactionDetailEntity)
        logger.info("Sale transaction created successfully")
    }

    fun getSubaccountsForPaymentExpenseTransaction(companyId: Long):List<SubaccountDto>{
        logger.info("Starting the BL call to get subaccounts for expense transaction")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is getting subaccounts for expense transaction")

        // Validation of user belongs to company
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-16")

        // Getting subaccounts
        val subaccountEntities = subaccountRepository.findAllByAccountAccountSubgroupAccountSubgroupNameAndCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc("DISPONIBILIDADES", companyId.toInt())
        logger.info("Subaccounts for expense transaction obtained successfully")
        return subaccountEntities.map { SubaccountMapper.entityToDto(it) }
    }

    fun getLastPaymentExpenseTransactionNumber(companyId: Long): Int {
        logger.info("Starting the BL call to get last expense transaction number")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-43")
        logger.info("User $kcUuid is getting last expense transaction number from company $companyId")

        // Getting last expense transaction number
        val transactionTypeEntity = transactionTypeRepository.findByTransactionTypeNameAndStatusIsTrue("Recibo") ?: throw UasException("404-17")
        val lastExpenseTransactionNumber = expenseTransactionRepository.findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderByExpenseTransactionNumberDesc(companyId.toInt(), transactionTypeEntity.transactionTypeId.toInt())?.expenseTransactionNumber ?: 0
        logger.info("Last expense transaction number obtained successfully")
        return lastExpenseTransactionNumber + 1
    }

    fun getExpenseTransactions(
        companyId: Long,
        sortBy: String,
        sortType: String,
        page: Int,
        size: Int
        ): Page<ExpenseTransactionDto> {
        logger.info("Starting the BL call to get expense transactions")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-36")
        logger.info("User $kcUuid is getting expense transactions")

        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), sortBy))

        // Getting expense transactions
        val expenseTransactionEntities = expenseTransactionRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt(),pageable)
        logger.info("Expense transactions obtained successfully")
        return expenseTransactionEntities.map { expenseTransactionEntity ->
            ExpenseTransactionMapper.entityToDto(expenseTransactionEntity,
                expenseTransactionDetailRepository.findAllByExpenseTransactionIdAndStatusIsTrue(expenseTransactionEntity.expenseTransactionId).sumOf { it.unitPriceBs.times(it.quantity.toBigDecimal()) + it.amountBs }
            )
        }
    }
}