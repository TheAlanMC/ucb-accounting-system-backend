package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.mapper.ReportTypeMapper
import org.springframework.data.domain.*
import ucb.accounting.backend.dao.S3Object
import ucb.accounting.backend.dao.TransactionAttachment
import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.AttachmentMapper
import ucb.accounting.backend.mapper.CompanyMapper
import ucb.accounting.backend.mapper.TransactionDetailMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import ucb.accounting.backend.service.MinioService
import java.util.*

@Service
class ReportJEBl @Autowired constructor(
    private val reportTypeRepository: ReportTypeRepository,
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val journalEntryRepository: JournalEntryRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val s3ObjectRepository: S3ObjectRepository,
    private val minioService: MinioService,
){

    companion object {
        private val logger = LoggerFactory.getLogger(DocumentTypeBl::class.java.name)
    }

    fun getReportTypes(): List<ReportTypeDto> {
        logger.info("Starting the BL call to get report types")
        val reportTypes = reportTypeRepository.findAllByStatusIsTrue()
        logger.info("Found ${reportTypes.size} report types")
        logger.info("Finishing the BL call to get report types")
        return reportTypes.map { ReportTypeMapper.entityToDto(it) }
    }

    fun getJournalEntriesByDateRange(
        companyId: Int,
        dateFrom: String,
        dateTo: String,
        page: Int,
        size: Int,
        sortBy: String,
        sortType: String
    ): Page<ReportDto<JournalEntryDto>> {
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId.toLong()) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId.toLong())
            ?: throw UasException("403-22")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), sortBy))
        val journalEntriesEnties = journalEntryRepository.findAllByCompanyIdAndStatusIsTrue(companyId, newDateFrom, newDateTo, pageable)

        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val reportDto: Page<ReportDto<JournalEntryDto>> = journalEntriesEnties.map { journalEntryEntity ->
            // Mapear la entidad JournalEntry a un DTO personalizado (JournalEntryDto)
            JournalEntryDto(
                documentTypeId = journalEntryEntity.documentTypeId.toLong(),
                journalEntryNumber = journalEntryEntity.journalEntryNumber,
                gloss = journalEntryEntity.gloss,
                description = journalEntryEntity.transaction!!.description,
                transactionDate = journalEntryEntity.transaction!!.transactionDate,
                attachments = journalEntryEntity.transaction!!.transactionAttachments!!.map {AttachmentMapper.entityToDto(it.attachment!!)},
                transactionDetails = journalEntryEntity.transaction!!.transactionDetails!!.map { TransactionDetailMapper.entityToDto(it)}
            )
        }.map { journalEntryDto ->
            ReportDto(
                company = companyDto,
                startDate = newDateFrom,
                endDate = newDateTo,
                reportData = journalEntryDto
            )
        }
        logger.info("Finishing the BL call to get journal entries")
        return PageImpl(reportDto.toList(), pageable, journalEntriesEnties.totalElements)
    }

}