package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import ucb.accounting.backend.dao.*
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.dto.Account
import ucb.accounting.backend.dto.AccountCategory
import ucb.accounting.backend.dto.AccountGroup
import ucb.accounting.backend.dto.AccountSubgroup
import ucb.accounting.backend.dto.Subaccount
import ucb.accounting.backend.dto.pdf_turtle.Margins
import ucb.accounting.backend.dto.pdf_turtle.PageSize
import ucb.accounting.backend.dto.pdf_turtle.ReportOptions
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.*
import ucb.accounting.backend.service.MinioService
import ucb.accounting.backend.service.PdfTurtleService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Service
class ReportBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val currencyTypeRepository: CurrencyTypeRepository,
    private val subaccountPartialRepository: SubaccountPartialRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val minioService: MinioService,
    private val reportTypeRepository: ReportTypeRepository,
    private val s3ObjectRepository: S3ObjectRepository,
    private val subaccountRepository: SubaccountRepository,
    private val pdfTurtleService: PdfTurtleService,
    private val reportRepository: ReportRepository,
    private val journalBookRepository: JournalBookRepository,
    private val generalLedgerRepository: GeneralLedgerRepository,
    private val trialBalanceRepository: TrialBalanceRepository,
    private val worksheetRepository: WorksheetRepository,
    private val financialStatementRepository: FinancialStatementRepository,
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
        companyId: Long,
        dateFrom: String,
        dateTo: String,
    ): ReportDto<List<JournalBookReportDto>> {
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-22")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo)) {
            throw UasException("400-15")
        }

        val currencyTypeEntity: CurrencyType = currencyTypeRepository.findByCurrencyCodeAndStatusIsTrue("Bs")!!
        val currencyType = CurrencyTypeDto(
            currencyCode = currencyTypeEntity.currencyCode,
            currencyName = currencyTypeEntity.currencyName
        )
        val journalBooks = journalBookRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt(), newDateFrom, newDateTo)

        // Company info
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val journalBook: List<JournalBookReportDto> = journalBooks.groupBy { it.journalEntryId }.map { (key, rows) ->
            val journalBook = rows.first()
                JournalBookReportDto (
                journalEntryId = journalBook.journalEntryId.toInt(),
                documentType = DocumentTypeDto(
                    documentTypeId = journalBook.documentTypeId.toLong(),
                    documentTypeName = journalBook.documentTypeName,
                ),
                journalEntryNumber = journalBook.journalEntryNumber,
                gloss = journalBook.gloss,
                description = journalBook.description,
                transactionDate = journalBook.transactionDate,
                transactionDetails = rows.map {
                    JournalBookTransactionDetailDto(
                        subaccount = SubaccountDto(
                            subaccountId = it.subaccountId.toLong(),
                            subaccountCode = it.subaccountCode,
                            subaccountName = it.subaccountName,
                        ),
                        debitAmountBs = it.debitAmountBs,
                        creditAmountBs = it.creditAmountBs
                    )
                },
                totalDebitAmountBs = rows.sumOf { it.debitAmountBs },
                totalCreditAmountBs = rows.sumOf { it.creditAmountBs }
            )
        }
       val journalBookReport = ReportDto(
            company = companyDto,
            startDate = newDateFrom,
            endDate = newDateTo,
            currencyType = currencyType,
            reportData = journalBook
        )
        logger.info("Finishing the BL call to get journal entries")
        return journalBookReport
    }

    fun getAvailableSubaccounts(companyId: Long, dateFrom: String, dateTo: String): List<SubaccountDto> {
        logger.info("Starting the BL call to get available subaccounts")
        // Validate that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-16")
        logger.info("User $kcUuid is trying to get available subaccounts from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)
        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo)) {
            throw UasException("400-16")
        }
        val subaccounts: List<SubaccountPartial> = subaccountPartialRepository.findAllSubaccounts(companyId.toInt(), newDateFrom, newDateTo)

        logger.info("Found ${subaccounts.size} subaccounts")
        logger.info("Finishing the BL call to get available subaccounts")
        return subaccounts.map {
            SubaccountDto(
                subaccountId = it.subaccountId,
                subaccountCode = it.subaccountCode,
                subaccountName = it.subaccountName,
            )
        }
    }

    fun getGeneralLedger(
        companyId: Long,
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
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)
        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo) || subaccountIds.isEmpty()) {
            throw UasException("400-16")
        }
        // Parse subaccountIds to Long
        val newSubaccountIds: List<Int> = subaccountIds.map { it.toInt() }
        // Validation of subaccountIds
        newSubaccountIds.map {
            val subaccount =
                subaccountRepository.findBySubaccountIdAndStatusIsTrue(it.toLong()) ?: throw UasException("404-10")
            if (subaccount.companyId.toLong() != companyId) {
                throw UasException("403-22")
            }
            subaccount
        }
        val currencyTypeEntity: CurrencyType = currencyTypeRepository.findByCurrencyCodeAndStatusIsTrue("Bs")!!
        val currencyType = CurrencyTypeDto(
            currencyCode = currencyTypeEntity.currencyCode,
            currencyName = currencyTypeEntity.currencyName
        )
        val generalLedgers: List<GeneralLedger> = generalLedgerRepository.findAllInSubaccountsByCompanyIdAndStatusIsTrue (companyId.toInt(), newDateFrom, newDateTo, newSubaccountIds)

        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)


        val generalLedgerReports: List<GeneralLedgerReportDto> = generalLedgers.groupBy { it.subaccountId }.map { (key, rows) ->
            val generalLedger = rows.first()
            var accumulatedBalance = BigDecimal(0.00)
            val transactionDetails = rows.map {
                accumulatedBalance += it.debitAmountBs - it.creditAmountBs
                GeneralLedgerTransactionDetailDto(
                    transactionDate = it.transactionDate,
                    gloss = it.gloss,
                    description = it.description,
                    creditAmount = it.creditAmountBs,
                    debitAmount = it.debitAmountBs,
                    balanceAmount = accumulatedBalance
                )
            }

            val generalLedgerReportDto = GeneralLedgerReportDto(
                subaccount = SubaccountDto(
                    subaccountId = generalLedger.subaccountId,
                    subaccountCode = generalLedger.subaccountCode,
                    subaccountName = generalLedger.subaccountName,
                ),
                transactionDetails = transactionDetails,
                totalDebitAmount = transactionDetails.sumOf { it.debitAmount },
                totalCreditAmount = transactionDetails.sumOf { it.creditAmount },
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

    fun getTrialBalance(
        companyId: Long,
        dateFrom: String,
        dateTo: String,
    ): ReportDto<List<TrialBalanceReportDto>> {
        logger.info("Starting the BL call to get trial balance report")
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-23")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        val currencyTypeEntity: CurrencyType = currencyTypeRepository.findByCurrencyCodeAndStatusIsTrue("Bs")!!
        val currencyType = CurrencyTypeDto(
            currencyCode = currencyTypeEntity.currencyCode,
            currencyName = currencyTypeEntity.currencyName
        )

        val trialBalance: List<TrialBalance> = trialBalanceRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt(), newDateFrom, newDateTo)
        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val trialBalanceDetails: List<TrialBalanceReportDetailDto> =
            trialBalance.map { transactionDetail ->
                val totalCreditAmount = transactionDetail.creditAmountBs
                val totalDebitAmount = transactionDetail.debitAmountBs
                val balanceDebtor = if (totalDebitAmount > totalCreditAmount) totalDebitAmount - totalCreditAmount else BigDecimal(0.00)
                val balanceCreditor = if (totalCreditAmount > totalDebitAmount) totalCreditAmount - totalDebitAmount else BigDecimal(0.00)
                val trialBalanceDetail = TrialBalanceReportDetailDto(
                    subaccount = SubaccountDto(
                        subaccountId = transactionDetail.subaccountId.toLong(),
                        subaccountCode = transactionDetail.subaccountCode,
                        subaccountName = transactionDetail.subaccountName,
                    ),
                    debitAmount = totalDebitAmount,
                    creditAmount = totalCreditAmount,
                    balanceDebtor = balanceDebtor,
                    balanceCreditor = balanceCreditor
                )
                trialBalanceDetail
            }
        val totalDebitAmount = trialBalanceDetails.sumOf { it.debitAmount }
        val totalCreditAmount = trialBalanceDetails.sumOf { it.creditAmount }
        val totalBalanceDebtor = trialBalanceDetails.sumOf{ it.balanceDebtor }
        val totalBalanceCreditor = trialBalanceDetails.sumOf { it.balanceCreditor }
        val trialBalanceReportDto = TrialBalanceReportDto(
            trialBalanceDetails = trialBalanceDetails,
            totalDebitAmount = totalDebitAmount,
            totalCreditAmount = totalCreditAmount,
            totalBalanceDebtor = totalBalanceDebtor,
            totalBalanceCreditor = totalBalanceCreditor
        )
        logger.info("Found trial balance report")
        return ReportDto(
            company = companyDto,
            startDate = newDateFrom,
            endDate = newDateTo,
            currencyType = currencyType,
            reportData = listOf(trialBalanceReportDto)
        )
    }


    fun getWorksheet(
        companyId: Long,
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
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        val currencyTypeEntity: CurrencyType = currencyTypeRepository.findByCurrencyCodeAndStatusIsTrue("Bs")!!
        val currencyType = CurrencyTypeDto(
            currencyCode = currencyTypeEntity.currencyCode,
            currencyName = currencyTypeEntity.currencyName
        )

        val worksheet: List<Worksheet> = worksheetRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt(), newDateFrom, newDateTo)

        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val worksheetDetails: List<WorksheetReportDetailDto> =
            worksheet.map { transactionDetail ->
                val accountCategoryName = transactionDetail.accountCategoryName
                val totalDebitAmount = transactionDetail.debitAmountBs
                val totalCreditAmount = transactionDetail.creditAmountBs
                val balanceDebtor = if (totalDebitAmount > totalCreditAmount) totalDebitAmount - totalCreditAmount else BigDecimal(0.00)
                val balanceCreditor = if (totalCreditAmount > totalDebitAmount) totalCreditAmount - totalDebitAmount else BigDecimal(0.00)
                val worksheetDetail = WorksheetReportDetailDto(
                    subaccount = SubaccountDto(
                        subaccountId = transactionDetail.subaccountId.toLong(),
                        subaccountCode = transactionDetail.subaccountCode,
                        subaccountName = transactionDetail.subaccountName,
                    ),
                    balanceDebtor = balanceDebtor,
                    balanceCreditor = balanceCreditor,
                    incomeStatementExpense = if (accountCategoryName == "EGRESOS") if (totalDebitAmount > totalCreditAmount) balanceDebtor else if (totalCreditAmount > totalDebitAmount) (totalDebitAmount - totalCreditAmount) else BigDecimal(0.00) else BigDecimal(0.00),
                    incomeStatementIncome = if (accountCategoryName == "INGRESOS") if (totalCreditAmount > totalDebitAmount) balanceCreditor else if (totalDebitAmount > totalCreditAmount) (totalCreditAmount - totalDebitAmount) else BigDecimal(0.00) else BigDecimal(0.00),
                    balanceSheetAsset = if (accountCategoryName == "ACTIVO") if (totalDebitAmount > totalCreditAmount) balanceDebtor else if (totalCreditAmount > totalDebitAmount) (totalDebitAmount - totalCreditAmount) else BigDecimal(0.00) else BigDecimal(0.00),
                    balanceSheetLiability = if (accountCategoryName == "PASIVO" || accountCategoryName == "PATRIMONIO") if (totalCreditAmount > totalDebitAmount) balanceCreditor else if (totalDebitAmount > totalCreditAmount) (totalCreditAmount - totalDebitAmount) else BigDecimal(0.00) else BigDecimal(0.00),
                )
                worksheetDetail
            }
        val totalDebtor = worksheetDetails.sumOf { it.balanceDebtor }
        val totalCreditor = worksheetDetails.sumOf { it.balanceCreditor }
        val totalIncomeStatementIncome = worksheetDetails.sumOf { it.incomeStatementIncome }
        val totalIncomeStatementExpense = worksheetDetails.sumOf { it.incomeStatementExpense }
        val totalBalanceSheetAsset = worksheetDetails.sumOf { it.balanceSheetAsset }
        val totalBalanceSheetLiability = worksheetDetails.sumOf { it.balanceSheetLiability }
        val worksheetReportDto = WorksheetReportDto(
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

    fun getIncomeStatement(
        companyId: Long,
        dateFrom: String,
        dateTo: String,
    ): ReportDto<FinancialStatementReportDto> {
        logger.info("Starting the BL call to get income statement report")
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-25")
        logger.info("User $kcUuid is trying to get financial statement report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        val currencyTypeEntity: CurrencyType = currencyTypeRepository.findByCurrencyCodeAndStatusIsTrue("Bs")!!
        val currencyType = CurrencyTypeDto(
            currencyCode = currencyTypeEntity.currencyCode,
            currencyName = currencyTypeEntity.currencyName
        )

        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val accountCategoryNames: List<String> = listOf("INGRESOS", "EGRESOS")
        val descriptions: List<String> = listOf("TOTAL CUENTAS DE INGRESOS", "TOTAL CUENTAS DE EGRESOS")

        val incomeStatementReportDetailDto: List<FinancialStatementReportDetailDto> = getFinancialStatement(companyId, newDateFrom, newDateTo, accountCategoryNames, descriptions)
        logger.info("Found income statement report")
        return ReportDto(
            company = companyDto,
            startDate = newDateFrom,
            endDate = newDateTo,
            currencyType = currencyType,
            reportData = FinancialStatementReportDto(
                financialStatementDetails = incomeStatementReportDetailDto,
                description = "UTILIDADES ESTADO DE RESULTADOS",
                totalAmountBs = incomeStatementReportDetailDto[0].totalAmountBs - incomeStatementReportDetailDto[1].totalAmountBs
            )
        )
    }

    fun getFinancialStatement(companyId: Long, dateFrom: Date, dateTo: Date, accountCategoryNames: List<String>, descriptions: List<String>): List<FinancialStatementReportDetailDto> {
        val financialStatement: List<FinancialStatement> = financialStatementRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt(), dateFrom, dateTo, accountCategoryNames)
        var index = 0
        val financialStatementReportDetailDto: List<FinancialStatementReportDetailDto> = financialStatement.groupBy { it.accountCategoryId }.map { (key, rows) ->
            val accountGroup = rows.groupBy { it.accountGroupId }.map { (key, rows) ->
                val accountSubgroup = rows.groupBy { it.accountSubgroupId }.map { (key, rows) ->
                    val account = rows.groupBy { it.accountId }.map { (key, rows) ->
                        val subaccount = rows.groupBy { it.subaccountId }.map { (key, rows) ->
                            val totalDebitAmount = rows.first().debitAmountBs
                            val totalCreditAmount = rows.first().creditAmountBs
                            val accountCategoryName = accountCategoryNames[index]
                            val balanceDebtor = if (totalDebitAmount > totalCreditAmount) totalDebitAmount - totalCreditAmount else BigDecimal(0.00)
                            val balanceCreditor = if (totalCreditAmount > totalDebitAmount) totalCreditAmount - totalDebitAmount else BigDecimal(0.00)
                            val incomeStatementExpense = if (accountCategoryName == "EGRESOS") if (totalDebitAmount > totalCreditAmount) balanceDebtor else if (totalCreditAmount > totalDebitAmount) (totalDebitAmount - totalCreditAmount) else BigDecimal(0.00) else BigDecimal(0.00);
                            val incomeStatementIncome = if (accountCategoryName == "INGRESOS") if (totalCreditAmount > totalDebitAmount) balanceCreditor else if (totalDebitAmount > totalCreditAmount) (totalCreditAmount - totalDebitAmount) else BigDecimal(0.00) else BigDecimal(0.00);
                            val balanceSheetAsset = if (accountCategoryName == "ACTIVO") if (totalDebitAmount > totalCreditAmount) balanceDebtor else if (totalCreditAmount > totalDebitAmount) (totalDebitAmount - totalCreditAmount) else BigDecimal(0.00) else BigDecimal(0.00);
                            val balanceSheetLiability = if (accountCategoryName == "PASIVO" || accountCategoryName == "PATRIMONIO") if (totalCreditAmount > totalDebitAmount) balanceCreditor else if (totalDebitAmount > totalCreditAmount) (totalCreditAmount - totalDebitAmount) else BigDecimal(0.00) else BigDecimal(0.00);
                            val subaccount = Subaccount(
                                subaccountId = rows.first().subaccountId.toLong(),
                                subaccountCode = rows.first().subaccountCode,
                                subaccountName = rows.first().subaccountName,
                                totalAmountBs = incomeStatementExpense + incomeStatementIncome + balanceSheetAsset + balanceSheetLiability
                            )
                            subaccount
                        }
                        val account = Account(
                            accountId = rows.first().accountId.toLong(),
                            accountCode = rows.first().accountCode,
                            accountName = rows.first().accountName,
                            subaccounts = subaccount,
                            totalAmountBs = subaccount.sumOf { it.totalAmountBs }
                        )
                        account
                    }
                    val accountSubgroup = AccountSubgroup(
                        accountSubgroupId = rows.first().accountSubgroupId.toLong(),
                        accountSubgroupCode = rows.first().accountSubgroupCode,
                        accountSubgroupName = rows.first().accountSubgroupName,
                        accounts = account,
                        totalAmountBs = account.sumOf { it.totalAmountBs }
                    )
                    accountSubgroup
                }
                val accountGroup = AccountGroup(
                    accountGroupId = rows.first().accountGroupId.toLong(),
                    accountGroupCode = rows.first().accountGroupCode,
                    accountGroupName = rows.first().accountGroupName,
                    accountSubgroups = accountSubgroup,
                    totalAmountBs = accountSubgroup.sumOf { it.totalAmountBs }
                )
                accountGroup
            }
            val accountCategory = AccountCategory(
                accountCategoryId = rows.first().accountCategoryId.toLong(),
                accountCategoryCode = rows.first().accountCategoryCode,
                accountCategoryName = rows.first().accountCategoryName,
                accountGroups = accountGroup,
                totalAmountBs = accountGroup.sumOf { it.totalAmountBs }
            )
            val totalAmountBs = accountCategory.totalAmountBs
            FinancialStatementReportDetailDto(
                accountCategory = accountCategory,
                description = descriptions[index++],
                totalAmountBs = totalAmountBs
            )
        }
        return financialStatementReportDetailDto
    }

    private val options = ReportOptions(
        false,
        false,
        Margins(
            20,
            20,
            20,
            20
        ),
        "Letter",
        PageSize(
            279,
            216
        )
    )

    private val templateEngine = "golang"

    fun generateModelForJournalBookByDates(
        companyId: Long,
        dateFrom: String,
        dateTo: String,
    ):Map<String, Any>{

        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-22")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val formatDate: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = formatDate.parse(dateFrom)
        val newDateTo: Date = formatDate.parse(dateTo)

        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo)) {
            throw UasException("400-15")
        }

        val journalBookData = journalBookRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt(), newDateFrom, newDateTo)

        // Company info
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)


        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val locale = Locale("en", "EN")
        val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))

        val journalBookList = journalBookData.groupBy { it.journalEntryId }.map { (key, rows) ->
            val journalBook = rows.first()
            val fecha = journalBook.transactionDate
            val numeroComprobante = journalBook.journalEntryNumber
            val tipoDocumento = journalBook.documentTypeName

            val numeroComprobanteTexto = "Comprobante de $tipoDocumento No. $numeroComprobante"
            val transacciones = rows.map {
                val tabulacion = if (it.creditAmountBs.compareTo(BigDecimal(0.00)) == 0) "" else "\t"
                mapOf(
                    "codigo" to it.subaccountCode.toString(),
                    "detalle" to tabulacion + it.subaccountName,
                    "debe" to format.format(it.debitAmountBs),
                    "haber" to format.format(it.creditAmountBs)
                )
            }
            val totalDebe = format.format(rows.sumOf { it.debitAmountBs})
            val totalHaber = format.format(rows.sumOf { it.creditAmountBs})
            val glosa = journalBook.gloss
            mapOf(
                "fecha" to sdf.format(fecha),
                "numeroDeComprobante" to numeroComprobanteTexto,
                "glosa" to glosa,
                "transacciones" to transacciones,
                "totales" to mapOf("debe" to totalDebe, "haber" to totalHaber)
            )
        }
        val totalDebe = format.format(journalBookData.sumOf { it.debitAmountBs })
        val totalHaber = format.format(journalBookData.sumOf { it.creditAmountBs })
        return mapOf(
            "empresa" to companyDto.companyName,
            "subtitulo" to "Libro Diario",
            "icono" to  preSignedUrl,
            "expresadoEn" to "Expresado en Bolivianos",
            "ciudad" to "La Paz - Bolivia",
            "nit" to company.companyNit,
            "periodo" to "Del ${sdf.format(newDateFrom)} al ${sdf.format(newDateTo)}",
            "libroDiario" to journalBookList,
            "totales" to mapOf("debe" to totalDebe, "haber" to totalHaber)
        )
    }

    fun generateJournalBookByDates(
        companyId: Long,
        dateFrom: String,
        dateTo: String,
    ): ByteArray {
        logger.info("Generating Journal Book report")
        logger.info("GET api/v1/report/journal-book/companies/${companyId}?dateFrom=${dateFrom}&dateTo=${dateTo}")
        val footerHtmlTemplate = readResourceAsString("templates/journal_book_report/Footer.html")
        val headerHtmlTemplate = readResourceAsString("templates/journal_book_report/Header.html")
        val htmlTemplate = readResourceAsString("templates/journal_book_report/Body.html")
        val model = generateModelForJournalBookByDates(companyId, dateFrom, dateTo)
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

    fun generateModelForLedgerAccountReport(
        companyId: Long,
        dateFrom: String,
        dateTo: String,
        subaccountIds: List<String>,
    ):Map<String, Any>{
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-22")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val formatDate: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = formatDate.parse(dateFrom)
        val newDateTo: Date = formatDate.parse(dateTo)

        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo)) {
            throw UasException("400-15")
        }

        // Company info
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)


        val newSubaccountIds: List<Int> = subaccountIds.map { it.toInt() }
        // Validation of subaccountIds
        newSubaccountIds.map {
            val subaccount =
                subaccountRepository.findBySubaccountIdAndStatusIsTrue(it.toLong()) ?: throw UasException("404-10")
            if (subaccount.companyId.toLong() != companyId) {
                throw UasException("403-22")
            }
            subaccount
        }
        val  ledgerAccountData = generalLedgerRepository.findAllInSubaccountsByCompanyIdAndStatusIsTrue(companyId.toInt(), newDateFrom, newDateTo, newSubaccountIds)

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val locale = Locale("en", "EN")
        val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))
        var saldoActual = BigDecimal(0.00)

        val ledgerAccountModel = ledgerAccountData.groupBy { it.subaccountId }.map { (key, rows) ->
            val libroMayor = rows.first()
            val codigoDeCuenta = libroMayor.subaccountCode.toString()
            val nombreDeCuenta = libroMayor.subaccountName
            val transacciones = rows.map { transaction ->
                val debe = transaction.debitAmountBs
                val haber = transaction.creditAmountBs
                val saldoAnterior = saldoActual
                saldoActual = saldoAnterior + debe - haber

                mapOf(
                    "fecha" to sdf.format(transaction.transactionDate),
                    "descripcion" to transaction.description,
                    "debe" to format.format(transaction.debitAmountBs),
                    "haber" to format.format(transaction.creditAmountBs),
                    "saldo" to format.format(saldoActual),
                )
            }
            val totalDebe = format.format(rows.sumOf { it.debitAmountBs })
            val totalHaber = format.format(rows.sumOf { it.creditAmountBs })

            saldoActual = BigDecimal(0.00)
            mapOf(
                "codigoDeCuenta" to codigoDeCuenta,
                "nombreDeCuenta" to nombreDeCuenta,
                "transacciones" to transacciones,
                "totales" to mapOf("debe" to totalDebe, "haber" to totalHaber)
            )
        }

        return mapOf(
            "empresa" to companyDto.companyName,
            "subtitulo" to "Libro Mayor",
            "icono" to preSignedUrl,
            "expresadoEn" to "Expresado en Bolivianos",
            "ciudad" to "La Paz - Bolivia",
            "nit" to company.companyNit,
            "periodo" to "Del ${sdf.format(newDateFrom)} al ${sdf.format(newDateTo)}",
            "libroMayor" to ledgerAccountModel
        )
    }

    fun generateLedgerAccountReport(
        companyId: Long,
        dateFrom: String,
        dateTo: String,
        subaccountIds: List<String>
    ): ByteArray {
        logger.info("Generating Ledger Account report")
        logger.info("GET api/v1/report/ledger-account-report/companies/${companyId}?dateFrom=${dateFrom}&dateTo=${dateTo}&accountCode=${subaccountIds}")
        val footerHtmlTemplate = readResourceAsString("templates/general_ledger_report/Footer.html")
        val headerHtmlTemplate = readResourceAsString("templates/general_ledger_report/Header.html")
        val htmlTemplate = readResourceAsString("templates/general_ledger_report/Body.html")
        val model = generateModelForLedgerAccountReport(companyId, dateFrom, dateTo, subaccountIds)
        val customOptions = ReportOptions(
            false,
            false,
            Margins(
                20,
                20,
                20,
                65
            ),
            "Letter",
            PageSize(
                279,
                216
            )
        )
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, customOptions, templateEngine)
    }

    fun generateModelForWorksheetsReport(
        companyId: Long,
        dateFrom: String,
        dateTo: String,
    ): Map<String, Any>{
        logger.info("Starting the BL call to get worksheet report")
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-24")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val formatDate: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = formatDate.parse(dateFrom)
        val newDateTo: Date = formatDate.parse(dateTo)

        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)


        val worksheetData = worksheetRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt(), newDateFrom, newDateTo)

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val locale = Locale("en", "EN")
        val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))

        val worksheet = worksheetData.map{
            val categoria = it.accountCategoryName
            val codigoDeCuenta = it.subaccountCode.toString()
            val nombreDeCuenta = it.subaccountName
            val totalDebeFinal = it.debitAmountBs
            val totalHaberFinal = it.creditAmountBs

            val saldoDeudor = if (totalDebeFinal > totalHaberFinal) totalDebeFinal - totalHaberFinal else ""
            val saldoAcreedor = if (totalHaberFinal > totalDebeFinal) totalHaberFinal - totalDebeFinal else ""

            mapOf(
                "nombreDeCuenta" to nombreDeCuenta,
                "debeBalanceDeComprobacion" to saldoDeudor.toString(),
                "haberBalanceDeComprobacion" to saldoAcreedor.toString(),
                "debeEstadoDeResultados" to (if(categoria == "EGRESOS")  if (totalDebeFinal > totalHaberFinal) saldoDeudor else if (totalHaberFinal > totalDebeFinal) (totalDebeFinal - totalHaberFinal) else "" else "").toString(),
                "haberEstadoDeResultados" to (if(categoria == "INGRESOS") if (totalHaberFinal > totalDebeFinal) saldoAcreedor else if (totalDebeFinal > totalHaberFinal) (totalHaberFinal - totalDebeFinal) else "" else "").toString(),
                "debeBalanceGeneral" to (if(categoria == "ACTIVO") if (totalDebeFinal > totalHaberFinal) saldoDeudor else if (totalHaberFinal > totalDebeFinal) (totalDebeFinal - totalHaberFinal) else "" else "").toString(),
                "haberBalanceGeneral" to (if(categoria == "PASIVO" || categoria == "PATRIMONIO") if (totalHaberFinal > totalDebeFinal) saldoAcreedor else if (totalDebeFinal > totalHaberFinal) (totalHaberFinal - totalDebeFinal) else "" else "").toString()
            )
        }

        val totalDebeBalanceDeComprobacion = worksheet.sumOf { row ->
            val value = row["debeBalanceDeComprobacion"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val totalHaberBalanceDeComprobacion = worksheet.sumOf { row ->
            val value = row["haberBalanceDeComprobacion"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val totalDebeEstadoDeResultados = worksheet.sumOf { row ->
            val value = row["debeEstadoDeResultados"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val totalHaberEstadoDeResultados = worksheet.sumOf { row ->
            val value = row["haberEstadoDeResultados"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val totalDebeBalanceGeneral = worksheet.sumOf { row ->
            val value = row["debeBalanceGeneral"].toString()
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val totalHaberBalanceGeneral = worksheet.sumOf { row ->
            val value = row["haberBalanceGeneral"].toString()
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val utilidadEstadoDeResultados = totalHaberEstadoDeResultados - totalDebeEstadoDeResultados
        val utilidadBalanceGeneral = totalDebeBalanceGeneral - totalHaberBalanceGeneral

        return mapOf(
            "empresa" to companyDto.companyName,
            "subtitulo" to "Hoja de trabajo",
            "icono" to preSignedUrl,
            "expresadoEn" to "Expresado en Bolivianos",
            "ciudad" to "La Paz - Bolivia",
            "nit" to companyDto.companyNit,
            "fecha" to "del ${sdf.format(newDateFrom)} al ${sdf.format(newDateTo)}",
            "hojaDeTrabajo" to worksheet,
            "totales" to mapOf(
                    "totalDebeBalanceDeComprobacion" to format.format(totalDebeBalanceDeComprobacion),
                    "totalHaberBalanceDeComprobacion" to format.format(totalHaberBalanceDeComprobacion),
                    "totalDebeEstadoDeResultados" to format.format(totalDebeEstadoDeResultados),
                    "totalHaberEstadoDeResultados" to format.format(totalHaberEstadoDeResultados),
                    "totalDebeBalanceGeneral" to format.format(totalDebeBalanceGeneral),
                    "totalHaberBalanceGeneral" to format.format(totalHaberBalanceGeneral)
                ),
            "utilidadEstadoDeResultados" to format.format(utilidadEstadoDeResultados),
            "utilidadBalanceGeneral" to format.format(utilidadBalanceGeneral)
        )

    }

    fun generateWorksheetsReport(
        companyId: Long,
        dateFrom: String,
        dateTo: String,
    ): ByteArray{
        logger.info("Generating Worksheets report")
        logger.info("GET api/v1/report/worksheets-report/companies/${companyId}?dateFrom=${dateFrom}&dateTo=${dateTo}")
        val footerHtmlTemplate = readResourceAsString("templates/worksheet_report/Footer.html")
        val headerHtmlTemplate = readResourceAsString("templates/worksheet_report/Header.html")
        val htmlTemplate = readResourceAsString("templates/worksheet_report/Body.html")
        val model = generateModelForWorksheetsReport(companyId, dateFrom, dateTo)
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

    fun generateModelForTrialBalancesReportByDates(
        companyId: Long,
        dateFrom: String,
        dateTo: String
    ): Map<String, Any>{
        logger.info("Starting the BL call to get trial balance report")
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-23")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val formatDate: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = formatDate.parse(dateFrom)
        val newDateTo: Date = formatDate.parse(dateTo)

        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val trialBalancesData = trialBalanceRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt(), newDateFrom, newDateTo)

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val locale = Locale("en", "EN")
        val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))

        val trialBalances = trialBalancesData.map{
            val codigoDeCuenta = it.subaccountCode.toString()
            val nombreDeCuenta = it.subaccountName
            val totalDebeFinal = it.debitAmountBs
            val totalHaberFinal = it.creditAmountBs

            val saldo = totalDebeFinal - totalHaberFinal

            mapOf(
                "codigoDeCuenta" to codigoDeCuenta,
                "nombreDeCuenta" to nombreDeCuenta,
                "cargos" to if (totalDebeFinal > BigDecimal(0.0)) format.format(totalDebeFinal) else "",
                "abonos" to if (totalHaberFinal >BigDecimal(0.0)) format.format(totalHaberFinal) else "",
                "deudor" to if (saldo>BigDecimal(0.0)) format.format(saldo.abs()) else "",
                "acreedor" to if (saldo<BigDecimal(0.0)) format.format(saldo.abs()) else ""
            )
        }

        val totalCargos = trialBalances.sumOf { row ->
            val value = row["cargos"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val totalAbonos = trialBalances.sumOf { row ->
            val value = row["abonos"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }
        val totalDeudor = trialBalances.sumOf { row ->
            val value = row["deudor"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val totalAcreedor = trialBalances.sumOf { row ->
            val value = row["acreedor"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }



        return mapOf(
            "empresa" to companyDto.companyName,
            "subtitulo" to "Balance de sumas y saldos",
            "icono" to preSignedUrl,
            "expresadoEn" to "Expresado en Bolivianos",
            "ciudad" to "La Paz - Bolivia",
            "nit" to company.companyNit,
            "fecha" to "del ${sdf.format(newDateFrom)} al ${sdf.format(newDateTo)}",
            "balanceSumasySaldos" to trialBalances,
            "totales" to mapOf(
                "totalCargos" to format.format(totalCargos),
                "totalAbonos" to format.format(totalAbonos),
                "totalDeudor" to format.format(totalDeudor),
                "totalAcreedor" to format.format(totalAcreedor),
            )
        )

    }

    fun generateTrialBalancesReportByDates(
        copmanyId: Long,
        dateFrom: String,
        dateTo: String,
    ): ByteArray{
        logger.info("Generating Trial Balances report")
        logger.info("GET api/v1/report/trial-balances-report/companies/${copmanyId}?dateFrom=${dateFrom}&dateTo=${dateTo}")
        val footerHtmlTemplate = readResourceAsString("templates/trial_balances_report/Footer.html")
        val headerHtmlTemplate = readResourceAsString("templates/trial_balances_report/Header.html")
        val htmlTemplate = readResourceAsString("templates/trial_balances_report/Body.html")
        val model = generateModelForTrialBalancesReportByDates(copmanyId, dateFrom, dateTo)
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

    fun saveReport(
        companyId: Long,
        reportTypeId: Long,
        currencyTypeId: Long,
        attachmentId: Long,
        dateFrom: String,
        dateTo: String,
        description: String,
        isFinancialStatement: Boolean
    ){
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val perioddateFrom: Date = format.parse(dateFrom)
        val perioddateTo: Date = format.parse(dateTo)
        val report = Report()
        logger.info("Saving report")
        report.companyId = companyId.toInt()
        report.reportTypeId = reportTypeId.toInt()
        report.currencyTypeId = currencyTypeId.toInt()
        report.attachmentId = attachmentId.toInt()
        report.periodStartDate = java.sql.Date(perioddateFrom.time)
        report.periodEndDate = java.sql.Date(perioddateTo.time)
        report.description = description
        report.isFinancialStatement = isFinancialStatement
        reportRepository.save(report)
        logger.info("Report saved")
    }

    fun readTextFile(filePath: String): String{
        try{
            val bytes = Files.readAllBytes(Paths.get(filePath))
            return String(bytes, Charsets.UTF_8)
        }catch (e: Exception){
            throw UasException("500-00")
        }
    }
    private fun readResourceAsString(resourcePath: String): String {
        try {
            val resource = ClassPathResource(resourcePath)
            // Get an InputStream from the ClassPathResource
            val inputStream = resource.inputStream

            // Convert the InputStream to a String using StreamUtils from Spring
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            throw UasException("500-00")
        }
    }
}
