package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.FilesBl
import ucb.accounting.backend.dto.AttachmentDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.pdf_turtle.Margins
import ucb.accounting.backend.dto.pdf_turtle.PageSize
import ucb.accounting.backend.dto.pdf_turtle.ReportOptions
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.service.PdfTurtleService
import ucb.accounting.backend.util.ResponseCodeUtil


@RestController
@RequestMapping("/api/v1/examples")
class ExampleApi @Autowired constructor(
    private val filesBl: FilesBl
){
    companion object {
        private val logger = LoggerFactory.getLogger(ExampleApi::class.java.name)
    }

    @GetMapping("{responseCode}")
    fun response(@PathVariable responseCode: String): ResponseEntity<ResponseDto<String>> {
        logger.info("Starting the API call to get $responseCode")
        if (responseCode == "200") {
            val code = "200-01"
            val responseInfo = ResponseCodeUtil.getResponseInfo(code)
            return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
        }
        if (responseCode == "201") {
            val code = "201-01"
            val responseInfo = ResponseCodeUtil.getResponseInfo(code)
            return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
        }
        if (responseCode == "400") {
            throw UasException("400-01")
        }
        if (responseCode == "401") {
            throw UasException("401-01")
        }
        if (responseCode == "403") {
            throw UasException("403-01")
        }
        if (responseCode == "404") {
            throw UasException("404-01")
        }
        if (responseCode == "409") {
            throw UasException("409-01")
        }
        val code = "500-01"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("report-example")
    fun reportExample(): ResponseEntity<ResponseDto<AttachmentDto>> {
        val pdfTurtleService = PdfTurtleService()
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
        val pdf = pdfTurtleService.generatePdf(footerHtmlTemplate, headerHtmlTemplate, htmlTemplate, model, options, templateEngine)
        val attachmentDto = filesBl.uploadFile(pdf, 1)
        logger.info("PDF: $pdf")
        val code = "200-01"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, attachmentDto), responseInfo.httpStatus)
    }

}