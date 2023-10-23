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

    fun generateJournalBookByDates(
        companyId: Long,
        startDate: Date,
        endDate: Date,
        documentTypeId: Long
    ): ByteArray {
        logger.info("Generating Journal Book report")
        logger.info("GET api/v1/report/journal-book/companies/${companyId}?startDate=${startDate}&endDate=${endDate}&documentTypeId=${documentTypeId}")

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

        //TODO: Use resources for html templates
        val footerHtmlTemplate = "<string>"
        val headerHtmlTemplate = "<string>"
        val htmlTemplate = "<!DOCTYPE html><html><head><style>.report {font-family: Arial, sans-serif;}.title {font-size: 24px;font-weight: bold;text-align: center;}.subtitle {font-size: 18px;text-align: center;}table {width: 100%;border-collapse: collapse;margin-top: 20px;}table, th, td {border: 1px solid black;}th, td {padding: 8px;text-align: left;}th {background-color: #f2f2f2;}</style></head><body class=\"report\"><h1 class=\"title\">{{ .titulo }}</h1><h2 class=\"subtitle\">{{ .subtitulo }}</h2><table><tr><th>Número</th><th>Fecha</th><th>Código</th><th>Nombre</th><th>Referencia</th><th>Debe</th><th>Haber</th></tr>{{range .libroDiario}}<tr><td>{{ .numero }}</td><td>{{ .fecha }}</td><td>{{ .codigo }}</td><td>{{ .nombre }}</td><td>{{ .referencia }}</td><td>{{ .debe }}</td><td>{{ .haber }}</td></tr>{{end}}</table></body></html>"
        val model = mapOf(
            "titulo" to "ProfitWave",
            "subtitulo" to "Reporte de Libro Diario",
            "libroDiario" to journalBookList
        )

        logger.info("model:\n$model")
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

    fun generateJournalBook(
        companyId: Long,
    ): ByteArray{
        logger.info("Generating Journal Book report")
        //TODO: Generate model for report
        val footerHtmlTemplate = "<string>"
        val headerHtmlTemplate = "<!DOCTYPE html><html><head><style>.header-container {padding-top: 5mm;display: flex;align-items: center;gap: 4mm;}</style></head><div class=\"header-container\"><svg style=\"width:1cm; height:1cm;\" viewBox=\"0 0 24 24\"><path fill=\"currentColor\"d=\"M5,19A1,1 0 0,0 6,20H18A1,1 0 0,0 19,19C19,18.79 18.93,18.59 18.82,18.43L13,8.35V4H11V8.35L5.18,18.43C5.07,18.59 5,18.79 5,19M6,22A3,3 0 0,1 3,19C3,18.4 3.18,17.84 3.5,17.37L9,7.81V6A1,1 0 0,1 8,5V4A2,2 0 0,1 10,2H14A2,2 0 0,1 16,4V5A1,1 0 0,1 15,6V7.81L20.5,17.37C20.82,17.84 21,18.4 21,19A3,3 0 0,1 18,22H6M13,16L14.34,14.66L16.27,18H7.73L10.39,13.39L13,16M12.5,12A0.5,0.5 0 0,1 13,12.5A0.5,0.5 0 0,1 12.5,13A0.5,0.5 0 0,1 12,12.5A0.5,0.5 0 0,1 12.5,12Z\" /></svg><h2>{{ .title }}</h2><div style=\"flex-grow: 1;\"><!-- SPACER --></div><div style=\"height: 1.6cm; width: 1.6cm\">{{barcodeQr \"https://github.com/lucas-gaitzsch/pdf-turtle\"}}</div></div></html>"
        val htmlTemplate = "<!DOCTYPE html><html><head><style>.report-heading {font-size: 24px;font-weight: bold;text-align: center;margin-bottom: 20px;}.table-container {width: 100%;border-collapse: collapse;}.table-header {background-color: #f2f2f2;}.table-header th,.table-data td {padding: 10px;border: 1px solid #ddd;text-align: left;}</style></head><body><h1 class=\"report-heading\">{{ .reportTitle}}</h1><table class=\"table-container\"><thead class=\"table-header\"><tr><th>Date</th><th>Description</th><th>Debit</th><th>Credit</th></tr></thead><tbody>{{range .transactions}}<tr class=\"table-data\"><td>{{ .date }}</td><td>{{ .description }}</td><td>{{ .debit }}</td><td>{{ .credit }}</td></tr>{{end}}</tbody></table></body></html>"
        val model = mapOf(
            "reportTitle" to "Financial Transactions Report",
            "transactions" to listOf(
                mapOf(
                    "date" to "2023-01-15",
                    "description" to "Sale of Product A",
                    "debit" to 0,
                    "credit" to 500.00
                ),
                mapOf(
                    "date" to "2023-01-20",
                    "description" to "Purchase of Supplies",
                    "debit" to 200.00,
                    "credit" to 0
                ),
                mapOf(
                    "date" to "2023-02-05",
                    "description" to "Salary Payment",
                    "debit" to 1000.00,
                    "credit" to 0
                )
            )
        )
        val options = ReportOptions(
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
        val templateEngine = "golang"
        return pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
    }

}