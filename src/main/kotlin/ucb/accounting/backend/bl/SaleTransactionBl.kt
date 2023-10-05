package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.*
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.InvoiceDto
import ucb.accounting.backend.dto.SaleTransactionDto
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
    private val paymentTypeRepository: PaymentTypeRepository,
    private val saleTransactionDetailRepository: SaleTransactionDetailRepository,
    private val saleTransactionRepository: SaleTransactionRepository,
    private val subaccountRepository: SubaccountRepository,
    private val transactionAttachmentRepository: TransactionAttachmentRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionTypeRepository: TransactionTypeRepository,
){
    companion object{
        private val logger = LoggerFactory.getLogger(SaleTransactionBl::class.java.name)
    }
    fun createInvoiceSaleTransaction(companyId: Long, invoiceDto: InvoiceDto){
        logger.info("Starting the BL call to create sale transaction")
        // Validate that no field is null but attachments
        if (invoiceDto.clientId == null || invoiceDto.paymentTypeId == null ||
            invoiceDto.gloss.isNullOrEmpty() || invoiceDto.description.isNullOrEmpty() ||
            invoiceDto.invoiceDate == null || invoiceDto.invoiceDetails == null ||
            invoiceDto.invoiceNumber == null) throw UasException("400-25")
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
            // Validation that subaccount belongs to company
            if (subaccountEntities.companyId != companyId.toInt()) throw UasException("403-33")
        }

        // Validation that the transaction type exists
        val transactionTypeEntity = transactionTypeRepository.findByTransactionTypeNameAndStatusIsTrue("Factura") ?: throw UasException("404-17")
        // Validation that the payment type exists
        paymentTypeRepository.findByPaymentTypeIdAndStatusIsTrue(invoiceDto.paymentTypeId.toLong()) ?: throw UasException("404-18")

        // Validation customer exists
        val customerEntity = customerRepository.findByCustomerIdAndStatusIsTrue(invoiceDto.clientId.toLong()) ?: throw UasException("404-14")
        // Validation that the customer belongs to the company
        if (customerEntity.companyId != companyId.toInt()) throw UasException("403-33")

        // Validation that the invoice sale transaction number is unique
        if (saleTransactionRepository.findByCompanyIdAndTransactionTypeIdAndSaleTransactionNumberAndStatusIsTrue(companyId.toInt(), transactionTypeEntity.transactionTypeId.toInt(), invoiceDto.invoiceNumber) != null) throw UasException("409-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-33")
        logger.info("User $kcUuid is registering a new sale transaction")

        // Creating journal entry
        logger.info("Saving journal entry")
        val journalEntryEntity = JournalEntry()
        journalEntryEntity.companyId = companyId.toInt()
        journalEntryEntity.documentTypeId = documentTypeRepository.findByDocumentTypeNameAndStatusIsTrue("Ingreso")!!.documentTypeId.toInt()
        journalEntryEntity.journalEntryNumber = invoiceDto.invoiceNumber
        journalEntryEntity.gloss = invoiceDto.gloss
        val savedJournalEntry = journalEntryRepository.save(journalEntryEntity)

        logger.info("Saving sale transaction")
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

        logger.info("Saving transaction details, debit")
        invoiceDto.invoiceDetails.map {
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
        transactionDetailEntity.subaccountId = customerEntity.subaccountId
        transactionDetailEntity.debitAmountBs = BigDecimal.ZERO
        transactionDetailEntity.creditAmountBs = invoiceDto.invoiceDetails.sumOf {it.unitPriceBs.times(it.quantity.toBigDecimal())}
        transactionDetailRepository.save(transactionDetailEntity)

        logger.info("Saving sale transaction")
        val saleTransactionEntity = SaleTransaction()
        saleTransactionEntity.journalEntryId = savedJournalEntry.journalEntryId.toInt()
        saleTransactionEntity.transactionTypeId = transactionTypeEntity.transactionTypeId.toInt()
        saleTransactionEntity.paymentTypeId = invoiceDto.paymentTypeId.toInt()
        saleTransactionEntity.companyId = companyId.toInt()
        saleTransactionEntity.customerId = invoiceDto.clientId.toInt()
        saleTransactionEntity.subaccountId = customerEntity.subaccountId
        saleTransactionEntity.saleTransactionNumber = invoiceDto.invoiceNumber
        saleTransactionEntity.saleTransactionReference = invoiceDto.reference ?: invoiceDto.invoiceNumber.toString()
        saleTransactionEntity.saleTransactionDate = invoiceDto.invoiceDate
        saleTransactionEntity.description = invoiceDto.description
        saleTransactionEntity.gloss = invoiceDto.gloss
        val savedSaleTransaction = saleTransactionRepository.save(saleTransactionEntity)

        logger.info("Saving sale transaction details")
        invoiceDto.invoiceDetails.map {
            val saleTransactionDetailEntity = SaleTransactionDetail()
            saleTransactionDetailEntity.saleTransactionId = savedSaleTransaction.saleTransactionId
            saleTransactionDetailEntity.subaccountId = it.subaccountId
            saleTransactionDetailEntity.quantity = it.quantity
            saleTransactionDetailEntity.unitPriceBs = it.unitPriceBs
            saleTransactionDetailRepository.save(saleTransactionDetailEntity)
        }
        logger.info("Sale transaction created successfully")
    }

    fun getSubaccountsForInvoiceSaleTransaction(companyId: Long):List<SubaccountDto>{
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

    fun getSaleTransactions(companyId: Long): List<SaleTransactionDto>{
        logger.info("Starting the BL call to get sale transactions")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-35")
        logger.info("User $kcUuid is getting sale transactions")

        // Getting sale transactions
        val saleTransactionEntities = saleTransactionRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        logger.info("Sale transactions obtained successfully")
        return saleTransactionEntities.map { saleTransactionEntity ->
            SaleTransactionMapper.entityToDto(saleTransactionEntity,
                saleTransactionDetailRepository.findAllBySaleTransactionIdAndStatusIsTrue(saleTransactionEntity.saleTransactionId).sumOf { it.unitPriceBs.times(it.quantity.toBigDecimal()) }
            )
        }
    }

    fun getLastInvoiceSaleTransactionNumber(companyId: Long): Int {
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