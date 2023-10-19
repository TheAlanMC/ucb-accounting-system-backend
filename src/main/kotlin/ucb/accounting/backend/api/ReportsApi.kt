package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.AccountBl
import ucb.accounting.backend.bl.FilesBl
import ucb.accounting.backend.bl.ReportBl
import ucb.accounting.backend.dto.AttachmentDownloadDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

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
    fun generateJournalBookReport (
        @PathVariable("companyId") companyId: Long,
    ): ResponseEntity<ResponseDto<AttachmentDownloadDto>>
    {
        logger.info("Generating Journal Book report")
        logger.info("GET api/v1/report/journal-book/companies/${companyId}")
        val report:ByteArray = reportBl.generateJournalBook(companyId)
        val uploadedReport = fileBl.uploadFile(report, companyId)
        val downloadReport = fileBl.downloadFile(uploadedReport.attachmentId, companyId)
        val code = "200-22"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, downloadReport), responseInfo.httpStatus)
    }
}