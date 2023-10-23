package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.ReportBl
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/reports")
class ReportApi  @Autowired constructor(private val reportBl: ReportBl) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReportApi::class.java.name)
    }

    @GetMapping("/report-types")
    fun getReportTypes(): ResponseEntity<ResponseDto<List<ReportTypeDto>>> {
        logger.info("Starting the API call to get report types")
        logger.info("GET /api/v1/reports/report-types")
        val reportTypes: List<ReportTypeDto> = reportBl.getReportTypes()
        logger.info("Sending response")
        val code = "200-21"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, reportTypes), responseInfo.httpStatus)
    }
    @GetMapping("/general-ledgers/companies/{companyId}")
    fun getGeneralLedgers(
        @PathVariable("companyId") companyId: Long,
        @RequestParam(defaultValue = "subaccountId") sortBy: String,
        @RequestParam(defaultValue = "asc") sortType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = true) dateFrom: String,
        @RequestParam(required = true) dateTo: String,
        @RequestParam(required = true) subaccountIds: List<String>
    ): ResponseEntity<ResponseDto<List<ReportDto<GeneralLedgerReportDto>>>> {
        logger.info("Starting the API call to get journal book report")
        logger.info("GET /api/v1/reports/general-ledgers/companies/$companyId")
        val journalBook: Page<ReportDto<GeneralLedgerReportDto>> = reportBl.getJournalBook(companyId, sortBy, sortType, page, size, dateFrom, dateTo, subaccountIds)
        logger.info("Sending response")
        val code = "200-23"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, journalBook.content, journalBook.totalElements), responseInfo.httpStatus)
    }
}
