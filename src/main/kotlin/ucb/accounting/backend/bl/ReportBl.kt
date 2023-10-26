package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.Report
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.pdf_turtle.Margins
import ucb.accounting.backend.dto.pdf_turtle.PageSize
import ucb.accounting.backend.dto.pdf_turtle.ReportOptions
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.service.PdfTurtleService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat

@Service
class ReportBl @Autowired constructor(
    private val pdfTurtleService: PdfTurtleService,
    private val journalEntryRepository: JournalEntryRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val companyRepository: CompanyRepository,
    private val reportRepository: ReportRepository
){

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
    companion object {
        private val logger = LoggerFactory.getLogger(AccountBl::class.java.name)
    }

    fun generateModelForJournalBookByDates(
        companyId: Long,
        startDate: Date,
        endDate: Date,
        documentTypeId: Long
    ):Map<String, Any>{
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        val user = kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")

        val journalBookData = journalEntryRepository.getJournalBookData(companyId.toInt(), documentTypeId.toInt(), startDate, endDate)

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val journalBookList = journalBookData.groupBy { it["fecha"] }.map { (fecha, rows) ->
            val numeroComprobante = "Comprobante de ingreso Nro. ${rows.first()["numero_comprobante"]}"
            val transacciones = rows.map {
                mapOf(
                    "codigo" to it["codigo"],
                    "detalle" to it["detalle"],
                    "debe" to String.format("%.2f", it["debe"]),
                    "haber" to String.format("%.2f", it["haber"])
                )
            }
            val totalDebe = String.format(
                "%.2f",
                rows.sumOf { (it["debe"] as Number).toDouble() }
            )
            val totalHaber = String.format(
                "%.2f",
                rows.sumOf { (it["haber"] as Number).toDouble() }
            )

            val fechaEnMilis = (fecha as Timestamp).time
            mapOf(
                "fecha" to sdf.format(Date(fechaEnMilis)),
                "numeroDeComprobante" to numeroComprobante,
                "transacciones" to transacciones,
                "totales" to mapOf("debe" to totalDebe, "haber" to totalHaber)
            )
        }

        //TODO: obtain icono from company

        return mapOf(
            "empresa" to companyEntity.companyName + " " + companyEntity.companyNit ,
            "subtitulo" to "Libro Diario\nDEL ${sdf.format(startDate)} al ${sdf.format(endDate)}",
            "icono" to "https://cdn-icons-png.flaticon.com/512/5741/5741196.png",
            "expresadoEn" to "Expresado en Bolivianos",
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

    fun generateModelForJournalBookByMonth(
        companyId: Long,
        month: Int,
        documentTypeId: Long
    ):Map<String, Any>{
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")

        val journalEntries = journalEntryRepository.findByCompanyIdAndDocumentTypeIdAndMonth(companyId.toInt(), documentTypeId.toInt() , month)
        logger.info("journalEntries:\n$journalEntries")

        val journalBookList = journalEntries.map {
                journalEntry ->
            val transaction = journalEntry.transaction
            val transactionDetails = transaction?.transactionDetails ?: emptyList()

            val journalBookEntry = mapOf(
                "numero" to journalEntry.journalEntryNumber,
                "fecha" to journalEntry.txDate.toString(),
                "codigo" to journalEntry.journalEntryNumber,
                "nombre" to "Activo",
                "referencia" to journalEntry.gloss,
                "debe" to transactionDetails.sumOf { it.debitAmountBs },
                "haber" to transactionDetails.sumOf { it.creditAmountBs }
            )
            journalBookEntry
        }
        return mapOf(
            "titulo" to "ProfitWave",
            "subtitulo" to "Reporte de Libro Diario",
            "libroDiario" to journalBookList
        )
    }

    fun generateJournalBookByMonth(
        companyId: Long,
        month: Int,
        documentTypeId: Long
    ): ByteArray {
        logger.info("Generating Journal Book report")
        logger.info("GET api/v1/report/journal-book/companies/${companyId}?month=${month}&documentTypeId=${documentTypeId}")
        val footerHtmlTemplate = "<string>"
        val headerHtmlTemplate = "<string>"
        val htmlTemplate = "<!DOCTYPE html><html><head><style>.report {font-family: Arial, sans-serif;}.title {font-size: 24px;font-weight: bold;text-align: center;}.subtitle {font-size: 18px;text-align: center;}table {width: 100%;border-collapse: collapse;margin-top: 20px;}table, th, td {border: 1px solid black;}th, td {padding: 8px;text-align: left;}th {background-color: #f2f2f2;}</style></head><body class=\"report\"><h1 class=\"title\">{{ .titulo }}</h1><h2 class=\"subtitle\">{{ .subtitulo }}</h2><table><tr><th>Número</th><th>Fecha</th><th>Código</th><th>Nombre</th><th>Referencia</th><th>Debe</th><th>Haber</th></tr>{{range .libroDiario}}<tr><td>{{ .numero }}</td><td>{{ .fecha }}</td><td>{{ .codigo }}</td><td>{{ .nombre }}</td><td>{{ .referencia }}</td><td>{{ .debe }}</td><td>{{ .haber }}</td></tr>{{end}}</table></body></html>"
        val model = generateModelForJournalBookByMonth(companyId,month, documentTypeId)

        logger.info("model:\n$model")
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

    fun generateModelForLedgerAccountReport(
        companyId: Long,
        startDate: Date,
        endDate: Date,
        accountCode: Int,
        currency: String,
        withBalance: Boolean
    ):Map<String, Any>{
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")

        //TODO: implement generation of model to ledger account report

        //val journalEntries = journalEntryRepository.findByCompanyIdAndAccountCodeAndTxDate(companyId.toInt(), accountCode, startDate, endDate)

        /*val ledgerAccountReportList = journalEntries.map {
                journalEntry ->
            val transaction = journalEntry.transaction
            val transactionDetails = transaction?.transactionDetails ?: emptyList()

            val ledgerAccountReportEntry = mapOf(
                "numero" to journalEntry.journalEntryNumber,
                "fecha" to journalEntry.txDate.toString(),
                "codigo" to journalEntry.journalEntryNumber,
                "nombre" to "Activo",
                "referencia" to journalEntry.gloss,
                "debe" to transactionDetails.sumOf { it.debitAmountBs },
                "haber" to transactionDetails.sumOf { it.creditAmountBs }
            )
            ledgerAccountReportEntry
        }*/
        return mapOf(
            "titulo" to "ProfitWave",
            "subtitulo" to "Reporte de Libro Diario",
            //"libroDiario" to ledgerAccountReportList
        )
    }

    fun generateLedgerAccountReport(
        companyId: Long,
        startDate: Date,
        endDate: Date,
        accountCode: Int,
        currency: String,
        withBalance: Boolean
    ): ByteArray {
        logger.info("Generating Ledger Account report")
        logger.info("GET api/v1/report/ledger-account-report/companies/${companyId}?startDate=${startDate}&endDate=${endDate}&accountCode=${accountCode}&currency=${currency}&withBalance=${withBalance}")
        val footerHtmlTemplate = "<string>"
        val headerHtmlTemplate = "<string>"
        val htmlTemplate = "<!DOCTYPE html><html><head><style>.report {font-family: Arial, sans-serif;}.title {font-size: 24px;font-weight: bold;text-align: center;}.subtitle {font-size: 18px;text-align: center;}table {width: 100%;border-collapse: collapse;margin-top: 20px;}table, th, td {border: 1px solid black;}th, td {padding: 8px;text-align: left;}th {background-color: #f2f2f2;}</style></head><body class=\"report\"><h1 class=\"title\">{{ .titulo }}</h1><h2 class=\"subtitle\">{{ .subtitulo }}</h2><table><tr><th>Número</th><th>Fecha</th><th>Código</th><th>Nombre</th><th>Referencia</th><th>Debe</th><th>Haber</th></tr>{{range .libroDiario}}<tr><td>{{ .numero }}</td><td>{{ .fecha }}</td><td>{{ .codigo }}</td><td>{{ .nombre }}</td><td>{{ .referencia }}</td><td>{{ .debe }}</td><td>{{ .haber }}</td></tr>{{end}}</table></body></html>"
        val model = generateModelForLedgerAccountReport(companyId, startDate, endDate, accountCode, currency, withBalance)

        logger.info("model:\n$model")
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

    fun saveReport(
        companyId: Long,
        reportTypeId: Long,
        currencyTypeId: Long,
        attachmentId: Long,
        periodStartDate: Date,
        periodEndDate: Date,
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
