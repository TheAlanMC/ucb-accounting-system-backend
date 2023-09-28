package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.*
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.SaleTransactionDto
import ucb.accounting.backend.dto.SaleTransactionPartialDto
import ucb.accounting.backend.dto.SubaccountDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.SaleTransactionMapper
import ucb.accounting.backend.mapper.SubaccountMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal

@Service
class SaleTransactionBl @Autowired constructor(
    private val attachmentRepository: AttachmentRepository,
    private val companyRepository: CompanyRepository,
    private val customerRepository: CustomerRepository,
    private val documentTypeRepository: DocumentTypeRepository,
    private val journalEntryRepository: JournalEntryRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val saleTransactionDetailRepository: SaleTransactionDetailRepository,
    private val saleTransactionRepository: SaleTransactionRepository,
    private val subaccountRepository: SubaccountRepository,
    private val transactionAttachmentRepository: TransactionAttachmentRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val transactionRepository: TransactionRepository,
){
    companion object{
        private val logger = LoggerFactory.getLogger(SaleTransactionBl::class.java.name)
    }
    fun createSaleTransaction(companyId: Long, saleTransactionDto: SaleTransactionDto){
        logger.info("Starting the BL call to create sale transaction")
        // Validate that no field is null but attachments
        if (saleTransactionDto.customerId == null || saleTransactionDto.subaccountId == null ||
            saleTransactionDto.gloss.isNullOrEmpty() || saleTransactionDto.description.isNullOrEmpty() ||
            saleTransactionDto.saleTransactionDate == null || saleTransactionDto.saleTransactionDetails == null ||
            saleTransactionDto.saleTransactionNumber == null) throw UasException("400-25")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that attachments were sent
        if (!saleTransactionDto.attachments.isNullOrEmpty()) {
            // Validation that attachments exist
            saleTransactionDto.attachments.map {
                attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId) ?: throw UasException("404-11")
            }
        }
        // Validation that subaccounts exist
        saleTransactionDto.saleTransactionDetails.map {
            val subaccountEntities = subaccountRepository.findBySubaccountIdAndStatusIsTrue(it.subaccountId) ?: throw UasException("404-10")
            // Validation that subaccount belongs to company
            if (subaccountEntities.companyId != companyId.toInt()) throw UasException("403-33")
        }

        // Validation that subaccount exists
        val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(saleTransactionDto.subaccountId) ?: throw UasException("404-10")
        // Validation that subaccount belongs to company
        if (subaccountEntity.companyId != companyId.toInt()) throw UasException("403-33")

        // Validation customer exists
        customerRepository.findByCustomerIdAndStatusIsTrue(saleTransactionDto.customerId) ?: throw UasException("404-14")
        // Validation that the sale transaction number is unique
        if (saleTransactionRepository.findByCompanyIdAndSaleTransactionNumberAndStatusIsTrue(companyId.toInt(), saleTransactionDto.saleTransactionNumber) != null) throw UasException("409-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-33")
        logger.info("User $kcUuid is registering a new sale transaction")

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
                transactionAttachmentEntity.attachment = attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId)!!
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

    fun getSubaccountsForSaleTransaction(companyId: Long):List<SubaccountDto>{
        logger.info("Starting the BL call to get subaccounts for sale transaction")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is getting subaccounts for sale transaction")
        // Validation of user belongs to company
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-16")
        // Getting subaccounts
        val subaccountEntities = subaccountRepository.findAllByAccountAccountSubgroupAccountGroupAccountCategoryAccountCategoryNameAndCompanyIdAndStatusIsTrue("Ingresos", companyId.toInt())
        logger.info("Subaccounts for sale transaction obtained successfully")
        return subaccountEntities.map { SubaccountMapper.entityToDto(it) }
    }

    fun getSaleTransactions(companyId: Long): List<SaleTransactionPartialDto>{
        logger.info("Starting the BL call to get sale transactions")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is getting sale transactions")
        // Validation of user belongs to company
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-35")
        // Getting sale transactions
        val saleTransactionEntities = saleTransactionRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        logger.info("Sale transactions obtained successfully")
        return saleTransactionEntities.map { saleTransactionEntity ->
            SaleTransactionMapper.entityToDto(saleTransactionEntity,
                saleTransactionDetailRepository.findAllBySaleTransactionIdAndStatusIsTrue(saleTransactionEntity.saleTransactionId).sumOf { it.unitPriceBs.times(it.quantity.toBigDecimal()) }
            )
        }
    }

    fun getLastSaleTransactionNumber(companyId: Long): Int {
        logger.info("Starting the BL call to get last sale transaction number")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-43")
        logger.info("User $kcUuid is getting last sale transaction number from company $companyId")

        // Getting last sale transaction number
        val lastSaleTransactionNumber = saleTransactionRepository.findFirstByCompanyIdAndStatusIsTrueOrderBySaleTransactionNumberDesc(companyId.toInt())?.saleTransactionNumber ?: 0
        logger.info("Last sale transaction number obtained successfully")
        return lastSaleTransactionNumber + 1
    }
}