package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.Transaction
import ucb.accounting.backend.dao.TransactionAttachment
import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.JournalEntryDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class JournalEntryBl @Autowired constructor(
    private val journalEntryRepository: JournalEntryRepository,
    private val companyRepository: CompanyRepository,
    private val documentTypeRepository: DocumentTypeRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionAttachmentRepository: TransactionAttachmentRepository,
    private val attachmentRepository: AttachmentRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val subAccountRepository: SubAccountRepository,

) {
    companion object{
        private val logger = LoggerFactory.getLogger(JournalEntryBl::class.java.name)
    }
    fun createJournalEntry(companyId: Long, journalEntryDto: JournalEntryDto){
        logger.info("Starting the BL call to create journal entry")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")
        // Validation that document type exists
        documentTypeRepository.findByDocumentTypeIdAndStatusTrue(journalEntryDto.documentTypeId) ?: throw UasException("404-12")
        // Validation that attachments were sent
        if (!journalEntryDto.attachments.isNullOrEmpty()) {
            // Validation that attachments exist
            journalEntryDto.attachments.map {
                attachmentRepository.findByAttachmentIdAndStatusTrue(it.attachmentId) ?: throw UasException("404-11")
            }
        }
        // Validation that subaccounts exist
        journalEntryDto.transactionDetails.map {
            subAccountRepository.findBySubaccountIdAndStatusTrue(it.subaccountId) ?: throw UasException("404-10")
        }
        // Validation that accounting principle of double-entry is being followed
        if (journalEntryDto.transactionDetails.sumOf { it.debitAmountBs } != journalEntryDto.transactionDetails.sumOf { it.creditAmountBs }) throw UasException("400-21")
        // Validation that journal entry number is unique
        // TODO: Maybe validate that is sequential
        if (journalEntryRepository.findByCompanyIdAndJournalEntryNumberAndStatusIsTrue(companyId.toInt(), journalEntryDto.journalEntryNumber) != null) throw UasException("409-04")
        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!

        logger.info("User $kcUuid is registering a new journal entry")
        // Validation of user belongs to company
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-20")
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
                transactionAttachmentEntity.attachment = attachmentRepository.findByAttachmentIdAndStatusTrue(it.attachmentId)!!
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
}