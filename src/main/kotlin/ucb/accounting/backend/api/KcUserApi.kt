package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.KcUsersBl
import ucb.accounting.backend.dto.KcUsersDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/users")
class KcUserApi @Autowired constructor(private val kcUsersBl: KcUsersBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(UsersApi::class.java.name)
    }

    @PostMapping("/accountants")
    fun createAccountant(@RequestBody kcUsersDto: KcUsersDto): ResponseEntity<ResponseDto<Nothing>>{
        logger.info("Starting the API call to create accountant")
        logger.info("POST /api/v1/users/accountants")
        kcUsersBl.createAccountant(kcUsersDto, "accountant")
        logger.info("Sending response")
        val code = "201-01"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @PostMapping("/accounting-assistants/companies/{companyId}")
    fun createAccountingAssistant(@RequestBody kcUsersDto: KcUsersDto,
                                  @PathVariable companyId: Long): ResponseEntity<ResponseDto<Nothing>>{
        logger.info("Starting the API call to create accounting assistant")
        logger.info("POST /api/v1/users/accounting-assistants/companies/${companyId}")
        kcUsersBl.createAccountAssistant(kcUsersDto, "accounting_assistant", companyId)
        logger.info("Sending response")
        val code = "201-02"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @PostMapping("/clients/companies/{companyId}")
    fun createClient(@RequestBody kcUsersDto: KcUsersDto,
                     @PathVariable companyId: Long): ResponseEntity<ResponseDto<Nothing>>{
        logger.info("Starting the API call to create client")
        logger.info("POST /api/v1/users/clients/companies/${companyId}")
        kcUsersBl.createClient(kcUsersDto, "client", companyId)
        logger.info("Sending response")
        val code = "201-03"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

}