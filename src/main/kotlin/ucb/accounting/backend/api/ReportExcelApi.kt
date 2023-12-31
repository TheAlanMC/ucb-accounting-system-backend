package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.AccountBl
import ucb.accounting.backend.bl.FilesBl
import ucb.accounting.backend.bl.ReportBl
import ucb.accounting.backend.dto.AttachmentDownloadDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil
import java.sql.Date
import java.text.SimpleDateFormat

@RestController
@RequestMapping("/api/v1/reports")
class ReportExcelApi @Autowired constructor(
    private val reportBl: ReportBl,
    private val fileBl: FilesBl
){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountBl::class.java.name)
        private val formatDate: java.text.DateFormat = SimpleDateFormat("dd-MM-yyyy")
    }

    @GetMapping("/journal-books/companies/{companyId}/excel")
    fun generateJournalBookReportByDates (
        @PathVariable("companyId") companyId: Long,
        @RequestParam("dateFrom") dateFrom: String,
        @RequestParam("dateTo") dateTo: String,
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>>
    {
        logger.info("Generating Journal Book report")
        logger.info("GET api/v1/report/journal-book/companies/${companyId}")
        val report:ByteArray = reportBl.generateJournalBookByDatesExcel(companyId, dateFrom, dateTo)
        val uploadedReport = fileBl.uploadFile(report, companyId, true)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        reportBl.saveReport(companyId, 1, 1, uploadedReport.attachmentId, dateFrom, dateTo, "Reporte de Libro Diario - EXCEL: ${formatDate.format(Date.valueOf(dateFrom))} - ${formatDate.format(Date.valueOf(dateTo))}", false)
        logger.info("Sending response")
        val code = "200-22"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Finishing the API call to get journal entries")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }

    @GetMapping("/general-ledgers/companies/{companyId}/excel")
    fun generateLedgerAccountReportByDates (
        @PathVariable("companyId") companyId: Long,
        @RequestParam("dateFrom") dateFrom: String,
        @RequestParam("dateTo") dateTo: String,
        @RequestParam(required = true) subaccountIds: List<String>
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>>
    {
        logger.info("Generating Ledger Account report")
        logger.info("GET api/v1/report/ledger-account-report/companies/${companyId}")
        val report:ByteArray = reportBl.generateLedgerAccountReportExcel(companyId, dateFrom, dateTo, subaccountIds)
        val uploadedReport = fileBl.uploadFile(report, companyId, true)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        reportBl.saveReport(companyId, 2, 1, uploadedReport.attachmentId, dateFrom, dateTo, "Reporte de Libro Mayor - EXCEL: ${formatDate.format(Date.valueOf(dateFrom))} - ${formatDate.format(Date.valueOf(dateTo))}", false)
        logger.info("Sending response")
        val code = "200-23"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }

    @GetMapping("/trial-balances/companies/{companyId}/excel")
    fun generateTrialBalancesReportByDates(
        @PathVariable("companyId") companyId: Long,
        @RequestParam("dateFrom") dateFrom: String,
        @RequestParam("dateTo") dateTo: String,
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>> {
        logger.info("Generating Trial Balances report")
        logger.info("GET api/v1/report/trial-balances/companies/${companyId}")
        val report: ByteArray = reportBl.generateTrialBalancesReportByDatesExcel(companyId, dateFrom, dateTo)
        val uploadedReport = fileBl.uploadFile(report, companyId, true)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        reportBl.saveReport(companyId, 3, 1, uploadedReport.attachmentId, dateFrom, dateTo, "Balance de Sumas y Saldos - EXCEL: ${formatDate.format(Date.valueOf(dateFrom))} - ${formatDate.format(Date.valueOf(dateTo))}", false)
        val code = "200-24"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }

    @GetMapping("/worksheets/companies/{companyId}/excel")
    fun generateWorksheetsReportByDates (
        @PathVariable("companyId") companyId: Long,
        @RequestParam("dateFrom") dateFrom: String,
        @RequestParam("dateTo") dateTo: String,
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>> {
        logger.info("Generating Worksheets report")
        logger.info("GET api/v1/report/worksheets/companies/${companyId}")
        val report: ByteArray = reportBl.generateWorksheetsReportExcel(companyId, dateFrom, dateTo)
        val uploadedReport = fileBl.uploadFile(report, companyId, true)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        reportBl.saveReport(companyId, 4, 1, uploadedReport.attachmentId, dateFrom, dateTo, "Hojas de Trabajo - EXCEL: ${formatDate.format(Date.valueOf(dateFrom))} - ${formatDate.format(Date.valueOf(dateTo))}", false)
        val code = "200-25"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }
    
}