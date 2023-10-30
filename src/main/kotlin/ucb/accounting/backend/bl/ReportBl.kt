package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.CurrencyType
import ucb.accounting.backend.dao.Report
import ucb.accounting.backend.dao.S3Object
import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.dto.pdf_turtle.Margins
import ucb.accounting.backend.dto.pdf_turtle.PageSize
import ucb.accounting.backend.dto.pdf_turtle.ReportOptions
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.*
import ucb.accounting.backend.service.MinioService
import ucb.accounting.backend.service.PdfTurtleService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Timestamp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

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
    private val journalEntryRepository: JournalEntryRepository,
    private val pdfTurtleService: PdfTurtleService,
    private val reportRepository: ReportRepository

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
                    AttachmentMapper.entityToDto(it.attachment!!)
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

        val newSortBy = sortBy.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
        val pageable: Pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.fromString(sortType), newSortBy))
        val ledgerBooks: Page<TransactionDetail> = transactionDetailRepository.findAllSubaccounts(companyId.toInt(), newDateFrom, newDateTo, pageable)
        val subaccounts: List<SubaccountDto> = ledgerBooks.map { SubaccountMapper.entityToDto(it.subaccount!!) }.content

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

        val newSortBy = sortBy.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
        val pageable: Pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.fromString(sortType), newSortBy))

        val ledgerBooksPage: Page<TransactionDetail> = transactionDetailRepository.findAllInSubaccounts(companyId.toInt(), newDateFrom, newDateTo, newSubaccountIds, pageable)
        val ledgerBooks: List<TransactionDetail> = ledgerBooksPage.content
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

    fun getTrialBalance(
        companyId: Long,
        sortBy: String,
        sortType: String,
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
        val format: java.text.DateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        val currencyTypeEntity: CurrencyType = currencyTypeRepository.findByCurrencyCodeAndStatusIsTrue("Bs")!!
        val currencyType: CurrencyTypeDto = CurrencyTypeDto(
            currencyCode = currencyTypeEntity.currencyCode,
            currencyName = currencyTypeEntity.currencyName
        )

        val newSortBy = sortBy.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
        val pageable: Pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.fromString(sortType), newSortBy))

        val ledgerBookPage: Page<TransactionDetail> = transactionDetailRepository.findAll(companyId.toInt(), newDateFrom, newDateTo, pageable)
        val ledgerBooks: List<TransactionDetail> = ledgerBookPage.content

        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val trialBalanceDetails: List<TrialBalanceReportDetailDto> =
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
                val totalCreditAmount = if (transactionDetails.isNotEmpty()) transactionDetails.map { it.creditAmount }
                    .reduce { acc, it -> acc + it } else BigDecimal(0.00)
                val totalDebitAmount = if (transactionDetails.isNotEmpty()) transactionDetails.map { it.debitAmount }
                    .reduce { acc, it -> acc + it } else BigDecimal(0.00)

                val balanceDebtor = if (totalDebitAmount > totalCreditAmount) totalDebitAmount - totalCreditAmount else BigDecimal(0.00)
                val balanceCreditor = if (totalCreditAmount > totalDebitAmount) totalCreditAmount - totalDebitAmount else BigDecimal(0.00)
                val trialBalanceDetail = TrialBalanceReportDetailDto(
                    subaccount = SubaccountMapper.entityToDto(transactionDetail.value[0].subaccount!!),
                    debitAmount = totalDebitAmount,
                    creditAmount = totalCreditAmount,
                    balanceDebtor = balanceDebtor,
                    balanceCreditor = balanceCreditor
                )
                trialBalanceDetail
            }
        val totalDebitAmount = trialBalanceDetails.map { it.debitAmount }.reduce { acc, it -> acc + it }
        val totalCreditAmount = trialBalanceDetails.map { it.creditAmount }.reduce { acc, it -> acc + it }
        val totalBalanceDebtor = trialBalanceDetails.map { it.balanceDebtor }.reduce { acc, it -> acc + it }
        val totalBalanceCreditor = trialBalanceDetails.map { it.balanceCreditor }.reduce { acc, it -> acc + it }
        val trialBalanceReportDto: TrialBalanceReportDto = TrialBalanceReportDto(
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

        val newSortBy = sortBy.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
        val pageable: Pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.fromString(sortType), newSortBy))

        val ledgerBookPage: Page<TransactionDetail> = transactionDetailRepository.findAll(companyId.toInt(), newDateFrom, newDateTo, pageable)
        val ledgerBooks: List<TransactionDetail> = ledgerBookPage.content

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

    private val options = ReportOptions(
        false,
        false,
        Margins(
            20,
            25,
            25,
            25
        ),
        "A4",
        PageSize(
            297,
            210
        )
    )

    private val templateEngine = "golang"

    fun generateModelForJournalBookByDates(
        companyId: Long,
        startDate: Date,
        endDate: Date,
        documentTypeId: Long
    ):Map<String, Any>{
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(companyEntity.s3CompanyLogo.toLong())!!
        val presignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        logger.info("company logo url: $presignedUrl")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")

        val journalBookData = journalEntryRepository.getJournalBookData(companyId.toInt(), startDate, endDate)

        if (startDate.after(endDate)) {
            throw UasException("400-15")
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val locale = Locale("en", "EN")
        val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))

        val journalBookList = journalBookData.groupBy { it["fecha"] to it["numero_comprobante"]}.map { (key, rows) ->
            val (fecha, numeroComprobante) = key
            val numeroComprobanteTexto = "Comprobante de ingreso Nro. $numeroComprobante"
            val transacciones = rows.map {

                val tabulacion = if ((it["haber"] as Number).toDouble() != 0.00) "\t" else ""

                mapOf(
                    "codigo" to it["codigo"],
                    "detalle" to tabulacion + it["nombre"],
                    "debe" to format.format(it["debe"] as Number),
                    "haber" to format.format(it["haber"] as Number)
                )
            }
            val totalDebe = format.format(rows.sumOf { (it["debe"] as Number).toDouble() })
            val totalHaber = format.format(rows.sumOf { (it["haber"] as Number).toDouble() })
            val glosa = rows.first()["detalle"]

            mapOf(
                "fecha" to sdf.format(fecha),
                "numeroDeComprobante" to numeroComprobanteTexto,
                "glosa" to glosa,
                "transacciones" to transacciones,
                "totales" to mapOf("debe" to totalDebe, "haber" to totalHaber)
            )
        }

        //TODO: obtain icono from company

        return mapOf(
            "empresa" to companyEntity.companyName,
            "subtitulo" to "Libro Diario",
            "icono" to  presignedUrl,
            "expresadoEn" to "Expresado en Bolivianos",
            "ciudad" to "La Paz - Bolivia",
            "nit" to companyEntity.companyNit,
            "periodo" to "Del ${sdf.format(startDate)} al ${sdf.format(endDate)}",
            "libroDiario" to journalBookList
        )
    }

    fun generateJournalBookByDates(
        companyId: Long,
        startDate: Date,
        endDate: Date,
        documentTypeId: Long
    ): ByteArray {
        logger.info("Generating Journal Book report")
        logger.info("GET api/v1/report/journal-book/companies/${companyId}?startDate=${startDate}&endDate=${endDate}&documentTypeId=${documentTypeId}")

        //TODO: Use resources for html templates
        val footerHtmlTemplate = readTextFile("src/main/resources/templates/journal_book_report/Footer.html")
        val headerHtmlTemplate = readTextFile("src/main/resources/templates/journal_book_report/Header.html")
        val htmlTemplate = readTextFile("src/main/resources/templates/journal_book_report/Body.html")
        val model = generateModelForJournalBookByDates(companyId, startDate, endDate, documentTypeId)

        logger.info("model:\n$model")
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

    fun generateModelForLedgerAccountReport(
        companyId: Long,
        startDate: Date,
        endDate: Date,
        subaccountIds: List<String>,
    ):Map<String, Any>{
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(companyEntity.s3CompanyLogo.toLong())!!
        val presignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")

        if (startDate.after(endDate)) {
            throw UasException("400-15")
        }

        val newSubaccountIds: List<Int> = subaccountIds.map { it.toInt() }
        // Validation of subaccountIds

        val  ledgerAccountData = subaccountRepository.getJournalBookData(companyId.toInt(), startDate, endDate, newSubaccountIds)

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val locale = Locale("en", "EN")
        val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))
        var saldoActual = 0.0

        val ledgerAccountModel = ledgerAccountData.groupBy { it["nombreDeCuenta"] }.map { (nombreDeCuenta, rows) ->
            val codigoDeCuenta = rows.first()["codigoDeCuenta"]
            val transacciones = rows.map { transaction ->

                val debe = (transaction["debe"] as Number).toDouble()
                val haber = (transaction["haber"] as Number).toDouble()
                val saldoAnterior = saldoActual
                saldoActual = saldoAnterior + (debe - haber)

                mapOf(
                    "fecha" to sdf.format(transaction["fecha"]),
                    "descripcion" to transaction["descripcion"],
                    "debe" to format.format(transaction["debe"] as Number),
                    "haber" to format.format(transaction["haber"] as Number),
                    "saldo" to format.format(saldoActual),
                )
            }
            val totalDebe = format.format(rows.sumOf { (it["debe"] as Number).toDouble() })
            val totalHaber = format.format(rows.sumOf { (it["haber"] as Number).toDouble() })

            saldoActual = 0.0
            mapOf(
                "codigoDeCuenta" to codigoDeCuenta,
                "nombreDeCuenta" to nombreDeCuenta,
                "transacciones" to transacciones,
                "totales" to mapOf("debe" to totalDebe, "haber" to totalHaber)
            )
        }

        return mapOf(
            "empresa" to companyEntity.companyName,
            "subtitulo" to "Libro Mayor",
            "icono" to presignedUrl,
            "expresadoEn" to "Expresado en Bolivianos",
            "ciudad" to "La Paz - Bolivia",
            "nit" to companyEntity.companyNit,
            "periodo" to "Del ${sdf.format(startDate)} al ${sdf.format(endDate)}",
            "libroMayor" to ledgerAccountModel
        )
    }

    fun generateLedgerAccountReport(
        companyId: Long,
        startDate: Date,
        endDate: Date,
        subaccountIds: List<String>
    ): ByteArray {
        logger.info("Generating Ledger Account report")
        logger.info("GET api/v1/report/ledger-account-report/companies/${companyId}?startDate=${startDate}&endDate=${endDate}&accountCode=${subaccountIds}")
        val footerHtmlTemplate = readTextFile("src/main/resources/templates/general_ledger_report/Footer.html")
        val headerHtmlTemplate = readTextFile("src/main/resources/templates/general_ledger_report/Header.html")
        val htmlTemplate = readTextFile("src/main/resources/templates/general_ledger_report/Body.html")
        val model = generateModelForLedgerAccountReport(companyId, startDate, endDate, subaccountIds)

        val customOptions = ReportOptions(
            false,
            false,
            Margins(
                20,
                25,
                25,
                67
            ),
            "A4",
            PageSize(
                297,
                210
            )
        )

        logger.info("model:\n$model")
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, customOptions, templateEngine)
    }

    fun generateModelForWorksheetsReport(
        companyId: Long,
        startDate: Date,
        endDate: Date,
    ): Map<String, Any>{
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(companyEntity.s3CompanyLogo.toLong())!!
        val presignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")

        if (startDate.after(endDate)) {
            throw UasException("400-15")
        }

        val worksheetData = subaccountRepository.getWorkSheetData(companyId.toInt(), startDate, endDate)

        logger.info("worksheetData: $worksheetData")

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val locale = Locale("en", "EN")
        val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))

        var totalDebe = 0.0
        var totalHaber = 0.0

        val worksheet = worksheetData.groupBy { it["nombreDeCuenta"] }.map{(nombreDeCuenta, rows) ->
            val categoria = rows.first()["categoria"]
            val transacciones = rows.map { transaction ->
                val debe = (transaction["debe"] as Number).toDouble()
                val haber = (transaction["haber"] as Number).toDouble()

                totalDebe += debe
                totalHaber += haber
            }

            val totalDebeFinal = totalDebe
            val totalHaberFinal = totalHaber

            totalDebe = 0.0
            totalHaber = 0.0

            mapOf(
                "nombreDeCuenta" to nombreDeCuenta,
                "debeBalanceDeComprobacion" to if (totalDebeFinal>0) format.format(abs(totalDebeFinal)) else "",
                "haberBalanceDeComprobacion" to if (totalHaberFinal>0) format.format(abs(totalHaberFinal)) else "",
                "debeEstadoDeResultados" to if(categoria == "INGRESOS") format.format(abs(totalDebeFinal)) else "",
                "haberEstadoDeResultados" to if(categoria == "EGRESOS") format.format(abs(totalHaberFinal)) else "",
                "debeBalanceGeneral" to if(categoria == "ACTIVO") format.format(abs(totalDebeFinal)) else "",
                "haberBalanceGeneral" to if(categoria == "PASIVO" || categoria == "PATRIMONIO") format.format(abs(totalHaberFinal)) else "",
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
            val value = row["debeBalanceGeneral"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val totalHaberBalanceGeneral = worksheet.sumOf { row ->
            val value = row["haberBalanceGeneral"] as String
            value.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        return mapOf(
            "empresa" to companyEntity.companyName,
            "subtitulo" to "Hoja de trabajo",
            "icono" to presignedUrl,
            "expresadoEn" to "Expresado en Bolivianos",
            "ciudad" to "La Paz - Bolivia",
            "nit" to companyEntity.companyNit,
            "fecha" to "del ${sdf.format(startDate)} al ${sdf.format(endDate)}",
            "hojaDeTrabajo" to worksheet,
            "totales" to mapOf(
                    "totalDebeBalanceDeComprobacion" to format.format(totalDebeBalanceDeComprobacion),
                    "totalHaberBalanceDeComprobacion" to format.format(totalHaberBalanceDeComprobacion),
                    "totalDebeEstadoDeResultados" to format.format(totalDebeEstadoDeResultados),
                    "totalHaberEstadoDeResultados" to format.format(totalHaberEstadoDeResultados),
                    "totalDebeBalanceGeneral" to format.format(totalDebeBalanceGeneral),
                    "totalHaberBalanceGeneral" to format.format(totalHaberBalanceGeneral)
                )
        )

    }

    fun generateWorksheetsReport(
        companyId: Long,
        startDate: Date,
        endDate: Date,
    ): ByteArray{
        logger.info("Generating Worksheets report")
        logger.info("GET api/v1/report/worksheets-report/companies/${companyId}?startDate=${startDate}&endDate=${endDate}")
        val footerHtmlTemplate = readTextFile("src/main/resources/templates/worksheet_report/Footer.html")
        val headerHtmlTemplate = readTextFile("src/main/resources/templates/worksheet_report/Header.html")
        val htmlTemplate = readTextFile("src/main/resources/templates/worksheet_report/Body.html")
        val model = generateModelForWorksheetsReport(companyId, startDate, endDate)

        logger.info("model:\n$model")
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

    fun generateModelForTrialBalancesReportByDates(
        companyId: Long,
        startDate: Date,
        endDate: Date,
    ): Map<String, Any>{
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(companyEntity.s3CompanyLogo.toLong())!!
        val presignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")

        if (startDate.after(endDate)) {
            throw UasException("400-15")
        }

        val trialBalancesData = subaccountRepository.getWorkSheetData(companyId.toInt(), startDate, endDate)

        logger.info("worksheetData: $trialBalancesData")

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val locale = Locale("en", "EN")
        val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))

        var totalDebe = 0.0
        var totalHaber = 0.0

        val trialBalances = trialBalancesData.groupBy { it["nombreDeCuenta"] }.map{(nombreDeCuenta, rows) ->
            val codigoDeCuennta = rows.first()["codigoDeCuenta"]
            val transacciones = rows.map { transaction ->
                val debe = (transaction["debe"] as Number).toDouble()
                val haber = (transaction["haber"] as Number).toDouble()

                totalDebe += debe
                totalHaber += haber

            }


            val totalDebeFinal = totalDebe
            val totalHaberFinal = totalHaber

            val saldo = totalDebeFinal - totalHaberFinal

            totalDebe = 0.0
            totalHaber = 0.0

            mapOf(
                "codigoDeCuenta" to codigoDeCuennta,
                "nombreDeCuenta" to nombreDeCuenta,
                "cargos" to if (totalDebeFinal>0) format.format(abs(totalDebeFinal)) else "",
                "abonos" to if (totalHaberFinal>0) format.format(abs(totalHaberFinal)) else "",
                "deudor" to if (saldo>0) format.format(abs(abs(saldo))) else "",
                "acreedor" to if (saldo<0) format.format(abs(abs(saldo))) else ""
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
            "empresa" to companyEntity.companyName,
            "subtitulo" to "Balance de sumas y saldos",
            "icono" to presignedUrl,
            "expresadoEn" to "Expresado en Bolivianos",
            "ciudad" to "La Paz - Bolivia",
            "nit" to companyEntity.companyNit,
            "fecha" to "del ${sdf.format(startDate)} al ${sdf.format(endDate)}",
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
        startDate: Date,
        endDate: Date,
    ): ByteArray{
        logger.info("Generating Trial Balances report")
        logger.info("GET api/v1/report/trial-balances-report/companies/${copmanyId}?startDate=${startDate}&endDate=${endDate}")
        val footerHtmlTemplate = readTextFile("src/main/resources/templates/trial_balances_report/Footer.html")
        val headerHtmlTemplate = readTextFile("src/main/resources/templates/trial_balances_report/Header.html")
        val htmlTemplate = readTextFile("src/main/resources/templates/trial_balances_report/Body.html")
        val model = generateModelForTrialBalancesReportByDates(copmanyId, startDate, endDate)

        logger.info("model:\n$model")
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

    fun saveReport(
        companyId: Long,
        reportTypeId: Long,
        currencyTypeId: Long,
        attachmentId: Long,
        periodStartDate: java.sql.Date,
        periodEndDate: java.sql.Date,
        description: String,
        isFinancialStatement: Boolean
    ){
        val report = Report()
        logger.info("Saving report")
        report.companyId = companyId.toInt()
        report.reportTypeId = reportTypeId.toInt()
        report.currencyTypeId = currencyTypeId.toInt()
        report.attachmentId = attachmentId.toInt()
        report.periodStartDate = periodStartDate
        report.periodEndDate = periodEndDate
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
            throw UasException("404-05")
        }
    }
}
