package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
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
@RequestMapping("/api/v1/journal-entries/companies")
class JournalEntryApi @Autowired constructor(private val journalEntryBl: JournalEntryBl){

    companion object {
        private val logger = LoggerFactory.getLogger(CompanyApi::class.java.name)
    }

    @PostMapping("/{companyId}")
    fun postJournalEntry(
        @PathVariable("companyId") companyId: Int,
        @RequestBody request: JournalEntryDto
    ): ResponseEntity<ResponseDto<Null>>{
        logger.info("Starting the API call to post journal entry")
        logger.info("POST /api/v1/journal-entries/companies/{companyId}")
        val journalEntryId = journalEntryBl.createJournalEntry(companyId, request)
        val code = "200-05"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }
}