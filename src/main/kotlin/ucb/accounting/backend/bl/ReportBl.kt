package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.*
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.CurrencyType
import ucb.accounting.backend.dao.S3Object
import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dao.specification.TransactionDetailSpecification
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.*
import ucb.accounting.backend.service.MinioService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.util.*

@Service
class ReportBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val currencyTypeRepository: CurrencyTypeRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val minioService: MinioService,
    private val reportTypeRepository: ReportTypeRepository,
    private val s3ObjectRepository: S3ObjectRepository,
    private val subaccountRepository: SubaccountRepository,
    private val journalEntryRepository: JournalEntryRepository
) {
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

    fun getJournalBook(
        companyId: Int,
        sortBy: String,
        sortType: String,
        page: Int,
        size: Int,
        dateFrom: String,
        dateTo: String,
    ): Page<ReportDto<List<JournalBookReportDto>>> {
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

        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo)) {
            throw UasException("400-15")
        }

        val currencyTypeEntity: CurrencyType = currencyTypeRepository.findByCurrencyCodeAndStatusIsTrue("Bs")!!
        val currencyType: CurrencyTypeDto = CurrencyTypeDto(
            currencyCode = currencyTypeEntity.currencyCode,
            currencyName = currencyTypeEntity.currencyName
        )
        val newSortBy = sortBy.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), newSortBy))
        val journalEntriesEntities = journalEntryRepository.findAllByCompanyIdAndStatusIsTrue(companyId, newDateFrom, newDateTo, pageable)

        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val journalBook: List<JournalBookReportDto> = journalEntriesEntities.map { journalEntryEntity ->
            // Mapear la entidad JournalEntry a un DTO personalizado (JournalEntryDto)
            JournalBookReportDto (
                journalEntryId = journalEntryEntity.journalEntryId.toInt(),
                documentType = DocumentTypeMapper.entityToDto(journalEntryEntity.documentType!!),
                journalEntryNumber = journalEntryEntity.journalEntryNumber,
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
                    JournalBookTransactionDetailMapper.entityToDto(it)
                }
            )
        }.content

       val journalBookReport = ReportDto(
            company = companyDto,
            startDate = newDateFrom,
            endDate = newDateTo,
            currencyType = currencyType,
            reportData = journalBook
        )
        logger.info("Finishing the BL call to get journal entries")
        return PageImpl(listOf(journalBookReport), pageable, journalEntriesEntities.totalElements)
    }

    fun getAvailableSubaccounts(companyId: Long, sortBy:String, sortType: String, dateFrom: String, dateTo: String): List<SubaccountDto> {
        logger.info("Starting the BL call to get available subaccounts")
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-16")
        logger.info("User $kcUuid is trying to get available subaccounts from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)
        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo)) {
            throw UasException("400-16")
        }

        val specification: Specification<TransactionDetail> =
            Specification.where(TransactionDetailSpecification.dateBetween(newDateFrom, newDateTo))
                .and(TransactionDetailSpecification.accepted())
                .and(TransactionDetailSpecification.companyId(companyId.toInt()))
                .and(TransactionDetailSpecification.statusIsTrue())

        val sort: Sort = Sort.by(Sort.Direction.fromString(sortType), sortBy)

        val ledgerBooks: List<TransactionDetail> = transactionDetailRepository.findAll(specification, sort)
        val subaccounts: List<SubaccountDto> = ledgerBooks.map { SubaccountMapper.entityToDto(it.subaccount!!) }.distinct()

        logger.info("Found ${subaccounts.size} subaccounts")
        logger.info("Finishing the BL call to get available subaccounts")
        return subaccounts
    }

    fun getGeneralLedger(
        companyId: Long,
        sortBy: String,
        sortType: String,
        dateFrom: String,
        dateTo: String,
        subaccountIds: List<String>
    ): ReportDto<List<GeneralLedgerReportDto>> {
        logger.info("Starting the BL call to get journal book report")
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-22")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)
        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo) || subaccountIds.isEmpty()) {
            throw UasException("400-16")
        }
        // Parse subaccountIds to Long
        val newSubaccountIds: List<Int> = subaccountIds.map { it.toInt() }
        // Validation of subaccountIds
        val subaccountsEntities = newSubaccountIds.map {
            val subaccount =
                subaccountRepository.findBySubaccountIdAndStatusIsTrue(it.toLong()) ?: throw UasException("404-10")
            if (subaccount.companyId.toLong() != companyId) {
                throw UasException("403-22")
            }
            subaccount
        }
        val currencyTypeEntity: CurrencyType = currencyTypeRepository.findByCurrencyCodeAndStatusIsTrue("Bs")!!
        val currencyType: CurrencyTypeDto = CurrencyTypeDto(
            currencyCode = currencyTypeEntity.currencyCode,
            currencyName = currencyTypeEntity.currencyName
        )

        val sort: Sort = Sort.by(Sort.Direction.fromString(sortType), sortBy)
        val specification: Specification<TransactionDetail> =
            Specification.where(TransactionDetailSpecification.dateBetween(newDateFrom, newDateTo))
                .and(TransactionDetailSpecification.subaccounts(newSubaccountIds))
                .and(TransactionDetailSpecification.accepted())
                .and(TransactionDetailSpecification.companyId(companyId.toInt()))
                .and(TransactionDetailSpecification.statusIsTrue())

        val ledgerBooks: List<TransactionDetail> = transactionDetailRepository.findAll(specification, sort)

        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)


        val generalLedgerReports: List<GeneralLedgerReportDto> = subaccountsEntities.map { subaccount ->
            var accumulatedBalance = BigDecimal(0.00)
            val ledgerBook = ledgerBooks.filter { it.subaccount!!.subaccountId == subaccount.subaccountId }
            val sortedLedgerBook = ledgerBook.sortedBy { it.transaction!!.transactionDate.time }
            val transactionDetails = sortedLedgerBook.map {
                accumulatedBalance += it.creditAmountBs - it.debitAmountBs
                GeneralLedgerTransactionDetailDto(
                    transactionDate = it.transaction!!.transactionDate,
                    gloss = it.transaction!!.journalEntry!!.gloss,
                    description = it.transaction!!.description,
                    creditAmount = it.creditAmountBs,
                    debitAmount = it.debitAmountBs,
                    balanceAmount = accumulatedBalance
                )
            }

            val generalLedgerReportDto: GeneralLedgerReportDto = GeneralLedgerReportDto(
                subaccount = SubaccountMapper.entityToDto(subaccount),
//                accountCategory = AccountCategoryDetailDto(
//                    accountCategoryId = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountCategory!!.accountCategoryId,
//                    accountCategoryCode = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountCategory!!.accountCategoryCode,
//                    accountCategoryName = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountCategory!!.accountCategoryName,
//                    accountGroup = AccountGroupDetailDto(
//                        accountGroupId = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountGroupId,
//                        accountGroupCode = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountGroupCode,
//                        accountGroupName = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountGroupName,
//                        accountSubgroup = AccountSubgroupDetailDto(
//                            accountSubgroupId = subaccount.account!!.accountSubgroup!!.accountSubgroupId,
//                            accountSubgroupCode = subaccount.account!!.accountSubgroup!!.accountSubgroupCode,
//                            accountSubgroupName = subaccount.account!!.accountSubgroup!!.accountSubgroupName,
//                            account = AccountDetailDto(
//                                accountId = subaccount.account!!.accountId,
//                                accountCode = subaccount.account!!.accountCode,
//                                accountName = subaccount.account!!.accountName,
//                                subaccount = SubaccountDetailDto(
//                                    subaccountId = subaccount.subaccountId,
//                                    subaccountCode = subaccount.subaccountCode,
//                                    subaccountName = subaccount.subaccountName
//                                )
//                            )
//                        )
//                    )
//                ),
                transactionDetails = transactionDetails,
                totalDebitAmount = if (transactionDetails.isNotEmpty()) transactionDetails.map { it.debitAmount }
                    .reduce { acc, it -> acc + it } else BigDecimal(0.00),
                totalCreditAmount = if (transactionDetails.isNotEmpty()) transactionDetails.map { it.creditAmount }
                    .reduce { acc, it -> acc + it } else BigDecimal(0.00),
                totalBalanceAmount = accumulatedBalance
            )
            generalLedgerReportDto
            }

        logger.info("Found ${generalLedgerReports.size} general ledger reports")
        logger.info("Finishing the BL call to get journal book report")
        return ReportDto(
            company = companyDto,
            startDate = newDateFrom,
            endDate = newDateTo,
            currencyType = currencyType,
            reportData = generalLedgerReports
        )
    }

    fun getWorksheet(
        companyId: Long,
        sortBy: String,
        sortType: String,
        dateFrom: String,
        dateTo: String,
    ): ReportDto<WorksheetReportDto> {
        logger.info("Starting the BL call to get worksheet report")
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-24")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        val currencyTypeEntity: CurrencyType = currencyTypeRepository.findByCurrencyCodeAndStatusIsTrue("Bs")!!
        val currencyType: CurrencyTypeDto = CurrencyTypeDto(
            currencyCode = currencyTypeEntity.currencyCode,
            currencyName = currencyTypeEntity.currencyName
        )

        val sort: Sort = Sort.by(Sort.Direction.fromString(sortType), sortBy)
        val specification: Specification<TransactionDetail> =
            Specification.where(TransactionDetailSpecification.dateBetween(newDateFrom, newDateTo))
                .and(TransactionDetailSpecification.accepted())
                .and(TransactionDetailSpecification.companyId(companyId.toInt()))
                .and(TransactionDetailSpecification.statusIsTrue())

        val ledgerBooks: List<TransactionDetail> = transactionDetailRepository.findAll(specification, sort)

        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val worksheetDetails: List<WorksheetReportDetailDto> =
            ledgerBooks.groupBy { it.subaccount!!.subaccountId }.map { transactionDetail ->
                val ledgerBook = transactionDetail.value
                val transactionDetails = ledgerBook.map {
                    GeneralLedgerTransactionDetailDto(
                        transactionDate = it.transaction!!.transactionDate,
                        gloss = it.transaction!!.journalEntry!!.gloss,
                        description = it.transaction!!.description,
                        creditAmount = it.creditAmountBs,
                        debitAmount = it.debitAmountBs,
                        balanceAmount = BigDecimal(0.00)
                    )
                }
                val accountCategory =
                    transactionDetail.value[0].subaccount!!.account!!.accountSubgroup!!.accountGroup!!.accountCategory!!
                val totalCreditAmount = if (transactionDetails.isNotEmpty()) transactionDetails.map { it.creditAmount }
                    .reduce { acc, it -> acc + it } else BigDecimal(0.00)
                val totalDebitAmount = if (transactionDetails.isNotEmpty()) transactionDetails.map { it.debitAmount }
                    .reduce { acc, it -> acc + it } else BigDecimal(0.00)

                val balanceDebtor = if (totalDebitAmount > totalCreditAmount) totalDebitAmount - totalCreditAmount else BigDecimal(0.00)
                val balanceCreditor = if (totalCreditAmount > totalDebitAmount) totalCreditAmount - totalDebitAmount else BigDecimal(0.00)
//                println("${transactionDetail.value[0].subaccount!!.subaccountCode} ${transactionDetail.value[0].subaccount!!.subaccountName} ${accountCategory.accountCategoryName}")
                val worksheetDetail = WorksheetReportDetailDto(
                    subaccount = SubaccountMapper.entityToDto(transactionDetail.value[0].subaccount!!),
                    balanceDebtor = balanceDebtor,
                    balanceCreditor = balanceCreditor,
                    incomeStatementExpense = if (accountCategory.accountCategoryName == "EGRESOS") if (totalDebitAmount > totalCreditAmount) balanceDebtor else if (totalCreditAmount > totalDebitAmount) (totalDebitAmount - totalCreditAmount) else BigDecimal(0.00) else BigDecimal(0.00),
                    incomeStatementIncome = if (accountCategory.accountCategoryName == "INGRESOS") if (totalCreditAmount > totalDebitAmount) balanceCreditor else if (totalDebitAmount > totalCreditAmount) (totalCreditAmount - totalDebitAmount) else BigDecimal(0.00) else BigDecimal(0.00),
                    balanceSheetAsset = if (accountCategory.accountCategoryName == "ACTIVO") if (totalDebitAmount > totalCreditAmount) balanceDebtor else if (totalCreditAmount > totalDebitAmount) (totalDebitAmount - totalCreditAmount) else BigDecimal(0.00) else BigDecimal(0.00),
                    balanceSheetLiability = if (accountCategory.accountCategoryName == "PASIVO" || accountCategory.accountCategoryName == "PATRIMONIO") if (totalCreditAmount > totalDebitAmount) balanceCreditor else if (totalDebitAmount > totalCreditAmount) (totalCreditAmount - totalDebitAmount) else BigDecimal(0.00) else BigDecimal(0.00),
                )
                worksheetDetail
            }
        val totalDebtor = worksheetDetails.map { it.balanceDebtor }.reduce { acc, it -> acc + it }
        val totalCreditor = worksheetDetails.map { it.balanceCreditor }.reduce { acc, it -> acc + it }
        val totalIncomeStatementIncome = worksheetDetails.map { it.incomeStatementIncome }
            .reduce { acc, it -> acc + it }
        val totalIncomeStatementExpense = worksheetDetails.map { it.incomeStatementExpense }
            .reduce { acc, it -> acc + it }
        val totalBalanceSheetAsset = worksheetDetails.map { it.balanceSheetAsset }
            .reduce { acc, it -> acc + it }
        val totalBalanceSheetLiability = worksheetDetails.map { it.balanceSheetLiability }
            .reduce { acc, it -> acc + it }
        val worksheetReportDto: WorksheetReportDto = WorksheetReportDto(
            worksheetDetails = worksheetDetails,
            totalBalanceDebtor = totalDebtor,
            totalBalanceCreditor = totalCreditor,
            totalIncomeStatementExpense = totalIncomeStatementExpense,
            totalIncomeStatementIncome = totalIncomeStatementIncome,
            totalIncomeStatementNetIncome = totalIncomeStatementIncome - totalIncomeStatementExpense,
            totalBalanceSheetAsset = totalBalanceSheetAsset,
            totalBalanceSheetLiability = totalBalanceSheetLiability,
            totalBalanceSheetEquity = totalBalanceSheetAsset - totalBalanceSheetLiability
        )
        logger.info("Found worksheet report")
        return ReportDto(
            company = companyDto,
            startDate = newDateFrom,
            endDate = newDateTo,
            currencyType = currencyType,
            reportData = worksheetReportDto
        )
    }
}