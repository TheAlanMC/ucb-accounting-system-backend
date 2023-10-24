package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.Attachment
import ucb.accounting.backend.dao.DocumentType
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.DocumentTypeRepository
import ucb.accounting.backend.dao.repository.JournalEntryRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dto.pdf_turtle.Margins
import ucb.accounting.backend.dto.pdf_turtle.PageSize
import ucb.accounting.backend.dto.pdf_turtle.ReportOptions
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.service.PdfTurtleService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.util.*

@Service
class ReportBl @Autowired constructor(
    private val pdfTurtleService: PdfTurtleService,
    private val journalEntryRepository: JournalEntryRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val companyRepository: CompanyRepository,
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
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")

        val journalEntries = journalEntryRepository.findByCompanyIdAndDocumentTypeIdAndTxDate(companyId.toInt(), documentTypeId.toInt() , startDate, endDate)
        logger.info("journalEntries:\n$journalEntries")

        val journalBookList = journalEntries.map {
                journalEntry ->
            val transaction = journalEntry.transaction
            val transactionDetails = transaction?.transactionDetails ?: emptyList()

            val journalBookEntry = mapOf(
                "numero" to journalEntry.journalEntryNumber,
                //TODO: Eliminate time from date
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

    fun generateJournalBookByDates(
        companyId: Long,
        startDate: Date,
        endDate: Date,
        documentTypeId: Long
    ): ByteArray {
        logger.info("Generating Journal Book report")
        logger.info("GET api/v1/report/journal-book/companies/${companyId}?startDate=${startDate}&endDate=${endDate}&documentTypeId=${documentTypeId}")

        //TODO: Use resources for html templates
        val footerHtmlTemplate = "<string>"
        val headerHtmlTemplate = "<string>"
        val htmlTemplate = "<!DOCTYPE html><html><head><style>.report {font-family: Arial, sans-serif;}.title {font-size: 24px;font-weight: bold;text-align: center;}.subtitle {font-size: 18px;text-align: center;}table {width: 100%;border-collapse: collapse;margin-top: 20px;}table, th, td {border: 1px solid black;}th, td {padding: 8px;text-align: left;}th {background-color: #f2f2f2;}</style></head><body class=\"report\"><h1 class=\"title\">{{ .titulo }}</h1><h2 class=\"subtitle\">{{ .subtitulo }}</h2><table><tr><th>Número</th><th>Fecha</th><th>Código</th><th>Nombre</th><th>Referencia</th><th>Debe</th><th>Haber</th></tr>{{range .libroDiario}}<tr><td>{{ .numero }}</td><td>{{ .fecha }}</td><td>{{ .codigo }}</td><td>{{ .nombre }}</td><td>{{ .referencia }}</td><td>{{ .debe }}</td><td>{{ .haber }}</td></tr>{{end}}</table></body></html>"
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
}