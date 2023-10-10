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
import ucb.accounting.backend.bl.JournalEntryBl
import ucb.accounting.backend.dto.JournalEntryDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil
import javax.validation.constraints.Null

@RestController
@RequestMapping("/api/v1/journal-entries")
class JournalEntryApi @Autowired constructor(private val journalEntryBl: JournalEntryBl){

    companion object {
        private val logger = LoggerFactory.getLogger(JournalEntryApi::class.java.name)
    }

    @PostMapping("/companies/{companyId}")
    fun postJournalEntry(
        @PathVariable("companyId") companyId: Long,
        @RequestBody journalEntryDto: JournalEntryDto
    ): ResponseEntity<ResponseDto<Null>>{
        logger.info("Starting the API call to post journal entry")
        logger.info("POST /api/v1/journal-entries/companies/${companyId}")
        journalEntryBl.createJournalEntry(companyId, journalEntryDto)
        logger.info("Sending response")
        val code = "201-09"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/last-numbers/companies/{companyId}")
    fun getLastJournalEntryNumber(
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<Int>>{
        logger.info("Starting the API call to get last journal entry number")
        logger.info("GET /api/v1/journal-entries/last-numbers/companies/${companyId}")
        val lastJournalEntryNumber: Int = journalEntryBl.getLastJournalEntryNumber(companyId)
        logger.info("Sending response")
        val code = "200-37"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, lastJournalEntryNumber), responseInfo.httpStatus)
    }

    @GetMapping("/{journalEntryId}/companies/{companyId}")
    fun getJournalEntry(
        @PathVariable("companyId") companyId: Long,
        @PathVariable("journalEntryId") journalEntryId: Long
    ): ResponseEntity<ResponseDto<JournalEntryDto>>{
        logger.info("Starting the API call to get journal entry")
        logger.info("GET /api/v1/journal-entries/${journalEntryId}/companies/${companyId}")
        val journalEntryDto: JournalEntryDto = journalEntryBl.getJournalEntry(companyId, journalEntryId)
        logger.info("Sending response")
        val code = "200-40"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, journalEntryDto), responseInfo.httpStatus)
    }

}