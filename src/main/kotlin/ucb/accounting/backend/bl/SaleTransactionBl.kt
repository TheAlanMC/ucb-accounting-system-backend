package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.*
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.SaleTransactionDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal

@Service
class SaleTransactionBl @Autowired constructor(
    private val journalEntryRepository: JournalEntryRepository,
    private val companyRepository: CompanyRepository,
    private val documentTypeRepository: DocumentTypeRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionAttachmentRepository: TransactionAttachmentRepository,
    private val attachmentRepository: AttachmentRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val saleTransactionRepository: SaleTransactionRepository,
    private val saleTransactionDetailRepository: SaleTransactionDetailRepository,
    private val subAccountRepository: SubAccountRepository,
    private val customerRepository: CustomerRepository
){
    companion object{
        private val logger = LoggerFactory.getLogger(SaleTransactionBl::class.java.name)
    }
    fun createSaleTransaction(companyId: Long, saleTransactionDto: SaleTransactionDto){
        logger.info("Starting the BL call to create sale transaction")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")

        // Validation that attachments were sent
        if (!saleTransactionDto.attachments.isNullOrEmpty()) {
            // Validation that attachments exist
            saleTransactionDto.attachments.map {
                attachmentRepository.findByAttachmentIdAndStatusTrue(it.attachmentId) ?: throw UasException("404-11")
            }
        }
        // Validation that subaccounts exist
        saleTransactionDto.saleTransactionDetails.map {
            subAccountRepository.findBySubaccountIdAndStatusTrue(it.subaccountId) ?: throw UasException("404-10")
        }
        subAccountRepository.findBySubaccountIdAndStatusTrue(saleTransactionDto.subaccountId) ?: throw UasException("404-10")
        // Validation customer exists
        customerRepository.findByCustomerIdAndStatusTrue(saleTransactionDto.customerId) ?: throw UasException("404-14")
        // Validation that the sale transaction number is unique
        // TODO: Maybe validate that is sequential
        if (saleTransactionRepository.findByCompanyIdAndSaleTransactionNumberAndStatusIsTrue(companyId.toInt(), saleTransactionDto.saleTransactionNumber) != null) throw UasException("409-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is registering a new sale transaction")
        // Validation of user belongs to company
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-30")

        // Creating journal entry
        logger.info("Saving journal entry")
        val journalEntryEntity = JournalEntry()
        journalEntryEntity.companyId = companyId.toInt()
        journalEntryEntity.documentTypeId = documentTypeRepository.findByDocumentTypeNameAndStatusIsTrue("Ingreso")!!.documentTypeId.toInt()
        journalEntryEntity.journalEntryNumber = saleTransactionDto.saleTransactionNumber
        journalEntryEntity.gloss = saleTransactionDto.gloss
        val savedJournalEntry = journalEntryRepository.save(journalEntryEntity)

        logger.info("Saving sale transaction")
        val transactionEntity = Transaction()
        transactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        transactionEntity.transactionDate = saleTransactionDto.saleTransactionDate
        transactionEntity.description = saleTransactionDto.description
        val savedTransaction = transactionRepository.save(transactionEntity)

        if (!saleTransactionDto.attachments.isNullOrEmpty()) {
            logger.info("Saving attachments")
            saleTransactionDto.attachments.map {
                val transactionAttachmentEntity = TransactionAttachment()
                transactionAttachmentEntity.transaction = savedTransaction
                transactionAttachmentEntity.attachment = attachmentRepository.findByAttachmentIdAndStatusTrue(it.attachmentId)!!
                transactionAttachmentRepository.save(transactionAttachmentEntity)
            }
        } else {
            logger.info("No attachments were sent")
        }

        logger.info("Saving transaction details, debit")
        saleTransactionDto.saleTransactionDetails.map {
            val transactionDetailEntity = TransactionDetail()
            transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
            transactionDetailEntity.subaccountId = it.subaccountId.toInt()
            transactionDetailEntity.debitAmountBs = it.unitPriceBs.times(it.quantity.toBigDecimal())
            transactionDetailEntity.creditAmountBs = BigDecimal.ZERO
            transactionDetailRepository.save(transactionDetailEntity)
        }
        logger.info("Saving the total of the sale transaction, total debit")
        val transactionDetailEntity = TransactionDetail()
        transactionDetailEntity.transactionId = savedTransaction.transactionId.toInt()
        transactionDetailEntity.subaccountId = saleTransactionDto.subaccountId.toInt()
        transactionDetailEntity.debitAmountBs = BigDecimal.ZERO
        transactionDetailEntity.creditAmountBs = saleTransactionDto.saleTransactionDetails.sumOf {it.unitPriceBs.times(it.quantity.toBigDecimal())}
        transactionDetailRepository.save(transactionDetailEntity)

        logger.info("Saving sale transaction")
        val saleTransactionEntity = SaleTransaction()
        saleTransactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        saleTransactionEntity.companyId = companyId.toInt()
        saleTransactionEntity.customerId = saleTransactionDto.customerId.toInt()
        saleTransactionEntity.subaccountId = saleTransactionDto.subaccountId.toInt()
        saleTransactionEntity.saleTransactionNumber = saleTransactionDto.saleTransactionNumber
        saleTransactionEntity.saleTransactionDate = saleTransactionDto.saleTransactionDate
        saleTransactionEntity.description = saleTransactionDto.description
        saleTransactionEntity.gloss = saleTransactionDto.gloss
        val savedSaleTransaction = saleTransactionRepository.save(saleTransactionEntity)

        logger.info("Saving sale transaction details")
        saleTransactionDto.saleTransactionDetails.map {
            val saleTransactionDetailEntity = SaleTransactionDetail()
            saleTransactionDetailEntity.saleTransactionId = savedSaleTransaction.saleTransactionId
            saleTransactionDetailEntity.subaccountId = it.subaccountId
            saleTransactionDetailEntity.quantity = it.quantity
            saleTransactionDetailEntity.unitPriceBs = it.unitPriceBs
            saleTransactionDetailRepository.save(saleTransactionDetailEntity)
        }
        logger.info("Sale transaction created successfully")
    }
}