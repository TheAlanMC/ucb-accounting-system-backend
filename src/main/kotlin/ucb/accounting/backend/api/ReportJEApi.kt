package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.ReportJEBl
import ucb.accounting.backend.dto.JournalEntryDto
import ucb.accounting.backend.dto.ReportDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/report")
class ReportJEApi @Autowired constructor(private val reportJEBl: ReportJEBl){

    companion object {
        private val logger = LoggerFactory.getLogger(ReportJEApi::class.java.name)
    }

    @GetMapping("/journal-entry/companies/{companyId}")
    fun getJournalEntries(@PathVariable("companyId") companyId: Int,
                          @RequestParam(required = true) dateFrom: String,
                          @RequestParam(required = true) dateTo: String,
                          @RequestParam(defaultValue = "0") page: Int,
                          @RequestParam(defaultValue = "10") size: Int,
                          @RequestParam(defaultValue = "transaction.transactionDate") sortBy: String,
                          @RequestParam(defaultValue = "asc") sortType: String
    ): ResponseEntity<ResponseDto<List<ReportDto<JournalEntryDto>>>> {
        logger.info("Starting the API call to get journal entries")
        val journalEntries: Page<ReportDto<JournalEntryDto>> = reportJEBl.getJournalEntriesByDateRange(companyId, dateFrom, dateTo, page, size, sortBy, sortType)
        val code = "200-23"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Finishing the API call to get journal entries")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, journalEntries.content, journalEntries.totalElements), responseInfo.httpStatus)
    }

}