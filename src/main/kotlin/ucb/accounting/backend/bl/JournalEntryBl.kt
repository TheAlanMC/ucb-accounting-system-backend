package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.Transaction
import ucb.accounting.backend.dao.TransactionAttachment
import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.JournalEntryDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Controller
class JournalEntryBl @Autowired constructor(
    private val journalEntryRepository: JournalEntryRepository,
    private val companyRepository: CompanyRepository,
    private val documentTypeRepository: DocumentTypeRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionAttachmentRepository: TransactionAttachmentRepository,
    private val attachmentRepository: AttachmentRepository,
    private val transactionDetailRepository: TransactionDetailRepository

) {
    companion object{
        private val logger = LoggerFactory.getLogger(JournalEntryBl::class.java.name)
    }
    fun createJournalEntry(companyId: Long, journalEntryDto: JournalEntryDto){
        logger.info("Starting the BL call to get company info")
        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")
        // Validation that document type exists
        documentTypeRepository.findByDocumentTypeIdAndStatusTrue(journalEntryDto.documentTypeId) ?: throw UasException("404-12")
        // Validate that journal entry number is unique
        // TODO: Maybe validate that is sequential
        if (journalEntryRepository.findByCompanyIdAndJournalEntryNumberAndStatusTrue(companyId.toInt(), journalEntryDto.journalEntryNumber) != null) throw UasException("409-04")
        // Validate that the user belongs to the company
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

        logger.info("Saving attachments")
        journalEntryDto.attachments.map {
            val transactionAttachmentEntity = TransactionAttachment()
            transactionAttachmentEntity.transaction = savedTransaction
            // TODO: Validate that all the attachments exist
            transactionAttachmentEntity.attachment = attachmentRepository.findByAttachmentIdAndStatusTrue(it.attachmentId)!!
            transactionAttachmentRepository.save(transactionAttachmentEntity)
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