package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
@RequestMapping("/api/v1/report")
class ReportsApi @Autowired constructor(
    private val reportBl: ReportBl,
    private val fileBl: FilesBl
){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountBl::class.java.name)
    }

    @GetMapping("/journal-book/companies/{companyId}")
    fun generateJournalBookReportByDates (
        @PathVariable("companyId") companyId: Long,
        @RequestParam("startDate") startDate: Date,
        @RequestParam("endDate") endDate: Date,
//        @RequestParam("documentTypeId") documentTypeId: Long
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>>
    {
        logger.info("Generating Journal Book report")
        logger.info("GET api/v1/report/journal-book/companies/${companyId}")
//        val report:ByteArray = reportBl.generateJournalBookByDates(companyId, startDate, endDate, documentTypeId)
        val report:ByteArray = reportBl.generateJournalBookByDates(companyId, startDate, endDate, 1)

        val uploadedReport = fileBl.uploadFile(report, companyId)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        //TODO: obtain real values instead of hardcoded
        reportBl.saveReport(companyId, 1, 1, uploadedReport.attachmentId, startDate, endDate, "Journal Book Report", false)
        val code = "200-22"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }

    @GetMapping("/general-ledger/companies/{companyId}")
    fun generateLedgerAccountReportByDates (
        @PathVariable("companyId") companyId: Long,
        @RequestParam("startDate") startDate: Date,
        @RequestParam("endDate") endDate: Date,
        @RequestParam(required = true) subaccountIds: List<String>
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>>
    {
        logger.info("Generating Ledger Account report")
        logger.info("GET api/v1/report/ledger-account-report/companies/${companyId}")
        val report:ByteArray = reportBl.generateLedgerAccountReport(companyId, startDate, endDate, subaccountIds)
        val uploadedReport = fileBl.uploadFile(report, companyId)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        //TODO: obtain real values instead of hardcoded
        reportBl.saveReport(companyId, 2, 1, uploadedReport.attachmentId, startDate, endDate, "Ledger Account Report", false)
        val code = "200-23"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }

}