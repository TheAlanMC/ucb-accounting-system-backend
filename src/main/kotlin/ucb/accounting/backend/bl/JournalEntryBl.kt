package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.Transaction
import ucb.accounting.backend.dao.TransactionAttachment
import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.DocumentTypeMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import ucb.accounting.backend.mapper.ExpenseTransactionMapper
import ucb.accounting.backend.mapper.SaleTransactionMapper

@Service
class JournalEntryBl @Autowired constructor(
    private val attachmentRepository: AttachmentRepository,
    private val companyRepository: CompanyRepository,
    private val documentTypeRepository: DocumentTypeRepository,
    private val journalEntryRepository: JournalEntryRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val subaccountRepository: SubaccountRepository,
    private val transactionAttachmentRepository: TransactionAttachmentRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val transactionRepository: TransactionRepository,
    private val expenseTransactionRepository: ExpenseTransactionRepository,
    private val saleTransactionRepository: SaleTransactionRepository,
    private val saleTransactionDetailRepository: SaleTransactionDetailRepository,
    private val expenseTransactionDetailRepository: ExpenseTransactionDetailRepository
    ) {
    companion object{
        private val logger = LoggerFactory.getLogger(JournalEntryBl::class.java.name)
    }
    fun createJournalEntry(companyId: Long, journalEntryDto: JournalEntryDto){
        logger.info("Starting the BL call to create journal entry")
        // Validate that all the fields are not null
        if (journalEntryDto.documentTypeId == null || journalEntryDto.journalEntryNumber == null || journalEntryDto.gloss == null ||
            journalEntryDto.description.isNullOrEmpty() || journalEntryDto.transactionDate == null || journalEntryDto.transactionDetails.isNullOrEmpty()) throw UasException("400-22")

        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that document type exists
        documentTypeRepository.findByDocumentTypeIdAndStatusIsTrue(journalEntryDto.documentTypeId) ?: throw UasException("404-12")

        // Validation that attachments were sent
        if (!journalEntryDto.attachments.isNullOrEmpty()) {
            // Validation that attachments exist
            journalEntryDto.attachments.map {
                attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId) ?: throw UasException("404-11")
            }
        }

        // Validation that subaccounts exist
        journalEntryDto.transactionDetails.map {
            val subaccountEntities = subaccountRepository.findBySubaccountIdAndStatusIsTrue(it.subaccountId) ?: throw UasException("404-10")
            if (subaccountEntities.companyId != companyId.toInt()) throw UasException("403-20")
        }

        // Validation that accounting principle of double-entry is being followed
        if (journalEntryDto.transactionDetails.sumOf { it.debitAmountBs } != journalEntryDto.transactionDetails.sumOf { it.creditAmountBs }) throw UasException("400-21")

        // Validation that journal entry number is unique
        if (journalEntryRepository.findByCompanyIdAndJournalEntryNumberAndStatusIsTrue(companyId.toInt(), journalEntryDto.journalEntryNumber) != null) throw UasException("409-04")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-20")
        logger.info("User $kcUuid is registering a new journal entry")

        logger.info("Saving journal entry")
        val journalEntryEntity = JournalEntry()
        journalEntryEntity.companyId = companyId.toInt()
        journalEntryEntity.documentTypeId = journalEntryDto.documentTypeId.toInt()
        journalEntryEntity.journalEntryNumber = journalEntryDto.journalEntryNumber
        journalEntryEntity.gloss = journalEntryDto.gloss
        journalEntryEntity.journalEntryAccepted = true
        val savedJournalEntry = journalEntryRepository.save(journalEntryEntity)

        logger.info("Saving transaction")
        val transactionEntity = Transaction()
        transactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        transactionEntity.transactionDate = journalEntryDto.transactionDate
        transactionEntity.description = journalEntryDto.description
        val savedTransaction = transactionRepository.save(transactionEntity)

        if (!journalEntryDto.attachments.isNullOrEmpty()) {
            logger.info("Saving attachments")
            journalEntryDto.attachments.map {
                val transactionAttachmentEntity = TransactionAttachment()
                transactionAttachmentEntity.transaction = savedTransaction
                transactionAttachmentEntity.attachment = attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId)!!
                transactionAttachmentRepository.save(transactionAttachmentEntity)
            }
        } else {
            logger.info("No attachments were sent")
        }

        logger.info("Saving transaction details")
        journalEntryDto.transactionDetails.map {
            val transactionDetailEntity = TransactionDetail()
            transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
            transactionDetailEntity.subaccountId = it.subaccountId.toInt()
            transactionDetailEntity.debitAmountBs = it.debitAmountBs
            transactionDetailEntity.creditAmountBs = it.creditAmountBs
            transactionDetailRepository.save(transactionDetailEntity)
        }

        logger.info("Journal entry saved successfully")
    }

    fun getLastJournalEntryNumber(companyId: Long): Int {
        logger.info("Starting the BL call to get last journal entry number")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-42")
        logger.info("User $kcUuid is getting last journal entry number from company $companyId")

        // Get last journal entry number
        val lastJournalEntryNumber = journalEntryRepository.findFirstByCompanyIdAndStatusIsTrueOrderByJournalEntryNumberDesc(companyId.toInt())?.journalEntryNumber ?: 0
        logger.info("Last journal entry number is $lastJournalEntryNumber")

        return lastJournalEntryNumber + 1
    }

    fun getListOfTransactions(companyId: Long): List<TransactionDto> {
        logger.info("Starting the BL call to get list of transactions")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-46")
        logger.info("User $kcUuid is getting list of transactions from company $companyId")
        val journalEntries = journalEntryRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())

        // Get list of transactions
        val transactionsList = mutableListOf<TransactionDto>()
        for (journalEntry in journalEntries) {
            val sales = saleTransactionRepository.findAllByCompanyIdAndJournalEntryIdAndStatusIsTrue(
                companyId.toInt(),
                journalEntry.journalEntryId.toInt()
            )
            val expenses = expenseTransactionRepository.findAllByCompanyIdAndJournalEntryIdAndStatusIsTrue(
                companyId.toInt(),
                journalEntry.journalEntryId.toInt()
            )
            var salesDto: List<SaleTransactionDto> = emptyList()
            var expensesDto: List<ExpenseTransactionDto> = emptyList()
            if (sales.isNotEmpty()) {
                salesDto = sales.map { sale ->
                    SaleTransactionMapper.entityToDto(
                        sale,
                        saleTransactionDetailRepository.findAllBySaleTransactionIdAndStatusIsTrue(sale.saleTransactionId)
                            .sumOf { it.unitPriceBs.times(it.quantity.toBigDecimal()) + it.amountBs }
                    )
                }
            }
            if (expenses.isNotEmpty()) {
                expensesDto = expenses.map { expense ->
                    ExpenseTransactionMapper.entityToDto(
                        expense,
                        expenseTransactionDetailRepository.findAllByExpenseTransactionIdAndStatusIsTrue(expense.expenseTransactionId)
                            .sumOf { it.unitPriceBs.times(it.quantity.toBigDecimal()) + it.amountBs }
                    )
                }
            }
            for (sale in salesDto) {
                val client = ClientPartialDto(
                    sale.customer.customerId,
                    sale.customer.displayName,
                    sale.customer.companyName,
                    sale.customer.companyPhoneNumber,
                    sale.customer.creationDate
                )
                val documentType = DocumentTypeDto(
                    journalEntry.documentType!!.documentTypeId,
                    journalEntry.documentType!!.documentTypeName,
                )
                val transactionDto = TransactionDto(
                    journalEntry.journalEntryId.toInt(),
                    sale.saleTransactionId,
                    sale.transactionType,
                    documentType,
                    sale.saleTransactionNumber,
                    sale.saleTransactionDate,
                    client,
                    sale.gloss,
                    sale.totalAmountBs,
                    sale.saleTransactionAccepted,
                )
                transactionsList.add(transactionDto)
            }

            for (expense in expensesDto) {
                val client = ClientPartialDto(
                    expense.supplier.supplierId,
                    expense.supplier.displayName,
                    expense.supplier.companyName,
                    expense.supplier.companyPhoneNumber,
                    expense.supplier.creationDate
                )
                val documentType = DocumentTypeDto(
                    journalEntry.documentType!!.documentTypeId,
                    journalEntry.documentType!!.documentTypeName,
                )
                val transactionDto = TransactionDto(
                    journalEntry.journalEntryId.toInt(),
                    expense.expenseTransactionId,
                    expense.transactionType,
                    documentType,
                    expense.expenseTransactionNumber,
                    expense.expenseTransactionDate,
                    client,
                    expense.gloss,
                    expense.totalAmountBs,
                    expense.expenseTransactionAccepted,
                )
                transactionsList.add(transactionDto)
            }

        }
        logger.info("List of transactions retrieved successfully")
        return transactionsList
    }

      
    fun getJournalEntry(companyId: Long, journalEntryId: Long): JournalEntryPartialDto {
        logger.info("Starting the BL call to get journal entry")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("403-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-45")
        logger.info("User $kcUuid is getting journal entry $journalEntryId from company $companyId")

        // Validation of journal entry exists
        val journalEntryEntity =
            journalEntryRepository.findByJournalEntryIdAndStatusIsTrue(journalEntryId) ?: throw UasException("404-19")

        // Validation of journal entry belongs to company
        if (journalEntryEntity.companyId != companyId.toInt()) throw UasException("403-45")

        // Get journal entry
        val journalEntryPartialDto = JournalEntryPartialDto(
            documentType = DocumentTypeMapper.entityToDto(journalEntryEntity.documentType!!),
            journalEntryNumber = journalEntryEntity.journalEntryNumber,
            gloss = journalEntryEntity.gloss,
            description = journalEntryEntity.transaction!!.description,
            transactionDate = journalEntryEntity.transaction!!.transactionDate,
            attachments = journalEntryEntity.transaction!!.transactionAttachments!!.map {
                AttachmentDto(
                    attachmentId = it.attachment!!.attachmentId,
                    contentType = it.attachment!!.contentType,
                    filename = it.attachment!!.filename) },
            transactionDetails = journalEntryEntity.transaction!!.transactionDetails!!.map {transactionDetail ->
                TransactionDetailDto(
                    subaccountId = transactionDetail.subaccountId.toLong(),
                    debitAmountBs = transactionDetail.debitAmountBs,
                    creditAmountBs = transactionDetail.creditAmountBs
                )
            }
        )
        return journalEntryPartialDto
    }

}