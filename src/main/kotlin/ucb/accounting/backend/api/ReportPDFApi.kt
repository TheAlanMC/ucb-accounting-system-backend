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

@RestController
@RequestMapping("/api/v1/reports")
class ReportPDFApi @Autowired constructor(
    private val reportBl: ReportBl,
    private val fileBl: FilesBl
){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountBl::class.java.name)
    }

    @GetMapping("/journal-books/companies/{companyId}/pdf")
    fun generateJournalBookReportByDates (
        @PathVariable("companyId") companyId: Long,
        @RequestParam("dateFrom") dateFrom: String,
        @RequestParam("dateTo") dateTo: String,
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>>
    {
        logger.info("Generating Journal Book report")
        logger.info("GET api/v1/report/journal-book/companies/${companyId}")
        val report:ByteArray = reportBl.generateJournalBookByDates(companyId, dateFrom, dateTo)
        val uploadedReport = fileBl.uploadFile(report, companyId)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        reportBl.saveReport(companyId, 1, 1, uploadedReport.attachmentId, dateFrom, dateTo, "Journal Book Report", false)
        logger.info("Sending response")
        val code = "200-22"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Finishing the API call to get journal entries")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }

    @GetMapping("/general-ledgers/companies/{companyId}/pdf")
    fun generateLedgerAccountReportByDates (
        @PathVariable("companyId") companyId: Long,
        @RequestParam("dateFrom") dateFrom: String,
        @RequestParam("dateTo") dateTo: String,
        @RequestParam(required = true) subaccountIds: List<String>
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>>
    {
        logger.info("Generating Ledger Account report")
        logger.info("GET api/v1/report/ledger-account-report/companies/${companyId}")
        val report:ByteArray = reportBl.generateLedgerAccountReport(companyId, dateFrom, dateTo, subaccountIds)
        val uploadedReport = fileBl.uploadFile(report, companyId)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        reportBl.saveReport(companyId, 2, 1, uploadedReport.attachmentId, dateFrom, dateTo, "Ledger Account Report", false)
        logger.info("Sending response")
        val code = "200-23"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }

    @GetMapping("/trial-balances/companies/{companyId}/pdf")
    fun generateTrialBalancesReportByDates(
        @PathVariable("companyId") companyId: Long,
        @RequestParam("dateFrom") dateFrom: String,
        @RequestParam("dateTo") dateTo: String,
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>> {
        logger.info("Generating Trial Balances report")
        logger.info("GET api/v1/report/trial-balances/companies/${companyId}")
        val report: ByteArray = reportBl.generateTrialBalancesReportByDates(companyId, dateFrom, dateTo)
        val uploadedReport = fileBl.uploadFile(report, companyId)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        reportBl.saveReport(companyId, 3, 1, uploadedReport.attachmentId, dateFrom, dateTo, "Worksheets Report", false)
        val code = "200-24"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }

    @GetMapping("/worksheets/companies/{companyId}/pdf")
    fun generateWorksheetsReportByDates (
        @PathVariable("companyId") companyId: Long,
        @RequestParam("dateFrom") dateFrom: String,
        @RequestParam("dateTo") dateTo: String,
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>> {
        logger.info("Generating Worksheets report")
        logger.info("GET api/v1/report/worksheets/companies/${companyId}")
        val report: ByteArray = reportBl.generateWorksheetsReport(companyId, dateFrom, dateTo)
        val uploadedReport = fileBl.uploadFile(report, companyId)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        reportBl.saveReport(companyId, 4, 1, uploadedReport.attachmentId, dateFrom, dateTo, "Worksheets Report", false)
        val code = "200-25"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }


}