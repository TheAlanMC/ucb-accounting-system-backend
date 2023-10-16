package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.Transaction
import ucb.accounting.backend.dao.TransactionAttachment
import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.*
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import ucb.accounting.backend.service.MinioService
import java.sql.Date

@Service
class JournalEntryBl @Autowired constructor(
    private val attachmentRepository: AttachmentRepository,
    private val companyRepository: CompanyRepository,
    private val documentTypeRepository: DocumentTypeRepository,
    private val journalEntryRepository: JournalEntryRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val minioService: MinioService,
    private val subaccountRepository: SubaccountRepository,
    private val transactionAttachmentRepository: TransactionAttachmentRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val transactionRepository: TransactionRepository,
    private val expenseTransactionRepository: ExpenseTransactionRepository,
    private val saleTransactionRepository: SaleTransactionRepository,
    ) {
    companion object {
        private val logger = LoggerFactory.getLogger(JournalEntryBl::class.java.name)
    }

    fun createJournalEntry(companyId: Long, journalEntryDto: JournalEntryDto) {
        logger.info("Starting the BL call to create journal entry")
        // Validate that all the fields are not null
        if (journalEntryDto.documentTypeId == null || journalEntryDto.journalEntryNumber == null || journalEntryDto.gloss == null ||
            journalEntryDto.description.isNullOrEmpty() || journalEntryDto.transactionDate == null || journalEntryDto.transactionDetails.isNullOrEmpty()
        ) throw UasException("400-22")

        // Validation of company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that document type exists
        documentTypeRepository.findByDocumentTypeIdAndStatusIsTrue(journalEntryDto.documentTypeId)
            ?: throw UasException("404-12")

        // Validation that attachments were sent
        if (!journalEntryDto.attachments.isNullOrEmpty()) {
            // Validation that attachments exist
            journalEntryDto.attachments.map {
                attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId) ?: throw UasException("404-11")
            }
        }

        // Validation that subaccounts exist
        journalEntryDto.transactionDetails.map {
            val subaccountEntities =
                subaccountRepository.findBySubaccountIdAndStatusIsTrue(it.subaccountId) ?: throw UasException("404-10")
            if (subaccountEntities.companyId != companyId.toInt()) throw UasException("403-20")
        }

        // Validation that accounting principle of double-entry is being followed
        if (journalEntryDto.transactionDetails.sumOf { it.debitAmountBs } != journalEntryDto.transactionDetails.sumOf { it.creditAmountBs }) throw UasException(
            "400-21"
        )

        // Validation that journal entry number is unique
        if (journalEntryRepository.findByCompanyIdAndJournalEntryNumberAndStatusIsTrue(
                companyId.toInt(),
                journalEntryDto.journalEntryNumber
            ) != null
        ) throw UasException("409-04")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-20")
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
                transactionAttachmentEntity.attachment =
                    attachmentRepository.findByAttachmentIdAndStatusIsTrue(it.attachmentId)!!
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
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-42")
        logger.info("User $kcUuid is getting last journal entry number from company $companyId")

        // Get last journal entry number
        val lastJournalEntryNumber =
            journalEntryRepository.findFirstByCompanyIdAndStatusIsTrueOrderByJournalEntryNumberDesc(companyId.toInt())?.journalEntryNumber
                ?: 0
        logger.info("Last journal entry number is $lastJournalEntryNumber")

        return lastJournalEntryNumber + 1
    }

    fun getListOfTransactions(
        companyId: Long,
        sortBy: String,
        sortType: String,
        page: Int,
        size: Int
    ): Page<TransactionDto> {
        logger.info("Starting the BL call to get list of transactions")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-46")
        logger.info("User $kcUuid is getting list of transactions from company $companyId")
        val saleJournalEntryIds = saleTransactionRepository.findAllJournalEntryId(companyId.toInt())
        val expenseJournalEntryIds = expenseTransactionRepository.findAllJournalEntryId(companyId.toInt())

        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), sortBy))

        val journalEntriesPage =
            journalEntryRepository.findAllByJournalEntryIdIsInAndStatusIsTrue(
                saleJournalEntryIds + expenseJournalEntryIds,
                pageable
            )

        val transactionsList: List<TransactionDto> = journalEntriesPage.content.map {
            val saleTransaction =
                saleTransactionRepository.findByJournalEntryIdAndStatusIsTrue(it.journalEntryId.toInt())
            val expenseTransaction =
                expenseTransactionRepository.findByJournalEntryIdAndStatusIsTrue(it.journalEntryId.toInt())
            val transaction = TransactionDto(
                it.journalEntryId.toInt(),
                saleTransaction?.saleTransactionNumber ?: expenseTransaction?.expenseTransactionNumber,
                ClientPartialDto(
                    saleTransaction?.customer?.customerId ?: expenseTransaction?.supplier?.supplierId,
                    saleTransaction?.customer?.displayName ?: expenseTransaction?.supplier?.displayName,
                    saleTransaction?.customer?.companyName ?: expenseTransaction?.supplier?.companyName,
                    saleTransaction?.customer?.companyPhoneNumber ?: expenseTransaction?.supplier?.companyPhoneNumber,
                    Date(saleTransaction?.customer?.txDate?.time ?: expenseTransaction?.supplier?.txDate?.time ?: 0)
                ),
                saleTransaction?.saleTransactionAccepted ?: expenseTransaction?.expenseTransactionAccepted,
                DocumentTypeMapper.entityToDto(it.documentType!!),
                TransactionTypeMapper.entityToDto(
                    saleTransaction?.transactionType ?: expenseTransaction?.transactionType!!
                ),
                it.transaction!!.transactionDetails!!.sumOf { transactionDetail ->
                    transactionDetail.debitAmountBs
                },
                Date(saleTransaction?.txDate?.time ?: expenseTransaction?.txDate?.time!!),
                saleTransaction?.saleTransactionDate ?: expenseTransaction?.expenseTransactionDate,
                saleTransaction?.description ?: expenseTransaction?.description,
            )
            transaction
        }
        logger.info("List of transactions retrieved successfully")
        return PageImpl(transactionsList, pageable, journalEntriesPage.totalElements)
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

        val saleTransaction = saleTransactionRepository.findByJournalEntryIdAndStatusIsTrue(journalEntryId.toInt())
        val expenseTransaction =
            expenseTransactionRepository.findByJournalEntryIdAndStatusIsTrue(journalEntryId.toInt())
        // Get journal entry
        val journalEntryPartialDto = JournalEntryPartialDto(
            journalEntryId = journalEntryEntity.journalEntryId.toInt(),
            transactionNumber = saleTransaction?.saleTransactionNumber ?: expenseTransaction?.expenseTransactionNumber,
            client = ClientPartialDto(
                saleTransaction?.customer?.customerId ?: expenseTransaction?.supplier?.supplierId,
                saleTransaction?.customer?.displayName ?: expenseTransaction?.supplier?.displayName,
                saleTransaction?.customer?.companyName ?: expenseTransaction?.supplier?.companyName,
                saleTransaction?.customer?.companyPhoneNumber ?: expenseTransaction?.supplier?.companyPhoneNumber,
                Date(saleTransaction?.customer?.txDate?.time ?: expenseTransaction?.supplier?.txDate?.time ?: 0)
            ),
            transactionAccepted = journalEntryEntity.journalEntryAccepted,
            documentType = DocumentTypeMapper.entityToDto(journalEntryEntity.documentType!!),
            transactionType = TransactionTypeMapper.entityToDto(
                saleTransaction?.transactionType ?: expenseTransaction?.transactionType!!
            ),
            gloss = journalEntryEntity.gloss,
            description = journalEntryEntity.transaction!!.description,
            transactionDate = journalEntryEntity.transaction!!.transactionDate,
            attachments = journalEntryEntity.transaction!!.transactionAttachments!!.map {
                // Byte array to multipart file
                val bucket = "documents"
                val newFile = minioService.uploadTempFile(
                    it.attachment!!.fileData,
                    it.attachment!!.filename,
                    it.attachment!!.contentType,
                    bucket
                )
                AttachmentDownloadDto(
                    filename = it.attachment!!.filename,
                    contentType = it.attachment!!.contentType,
                    fileUrl = newFile.fileUrl,
                )
            },
            transactionDetails = journalEntryEntity.transaction!!.transactionDetails!!.map {
                TransactionDetailPartialMapper.entityToDto(it)
            }
        )
        logger.info("Journal entry retrieved successfully")
        return journalEntryPartialDto
    }
}