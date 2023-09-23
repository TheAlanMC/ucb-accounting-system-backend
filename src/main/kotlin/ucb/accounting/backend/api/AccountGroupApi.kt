package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.AccountGroupBl
import ucb.accounting.backend.dto.AccoGroupDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/account-groups")
class AccountGroupApi @Autowired constructor(private val accountGroupBl: AccountGroupBl){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountGroupApi::class.java.name)
    }

    @PostMapping("/companies/{companyId}")
    fun createAccountGroup(@PathVariable companyId: Long,
                           @RequestBody accoGroupDto: AccoGroupDto): ResponseEntity<ResponseDto<Nothing>>{
        logger.info("Starting the API call to create account group")
        logger.info("POST /api/v1/account-groups/companies/${companyId}")
        accountGroupBl.createAccountGroup(companyId, accoGroupDto)
        logger.info("Sending response")
        val code = "201-05"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getAccountGroups(@PathVariable companyId: Long): ResponseEntity<ResponseDto<List<AccoGroupDto>>>{
        logger.info("Starting the API call to get account groups")
        logger.info("GET /api/v1/account-groups/companies/${companyId}")
        val accountGroups = accountGroupBl.getAccountGroups(companyId)
        logger.info("Sending response")
        val code = "200-08"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accountGroups), responseInfo.httpStatus)
    }

    @GetMapping("/{accountGroupId}/companies/{companyId}")
    fun getAccountGroup(@PathVariable companyId: Long, @PathVariable accountGroupId: Long): ResponseEntity<ResponseDto<AccoGroupDto>>{
        logger.info("Starting the API call to get account group")
        logger.info("GET /api/v1/account-groups/${accountGroupId}/companies/${companyId}")
        val accountGroup = accountGroupBl.getAccountGroup(companyId, accountGroupId)
        logger.info("Sending response")
        val code = "200-08"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accountGroup), responseInfo.httpStatus)
    }

    @PutMapping("/{accountGroupId}/companies/{companyId}")
    fun updateAccountGroup(@PathVariable companyId: Long,
                           @PathVariable accountGroupId: Long,
                           @RequestBody accoGroupDto: AccoGroupDto): ResponseEntity<ResponseDto<AccoGroupDto>>{
        logger.info("Starting the API call to update account group")
        logger.info("PUT /api/v1/account-groups/${accountGroupId}/companies/${companyId}")
        accountGroupBl.updateAccountGroup(companyId, accountGroupId, accoGroupDto)
        logger.info("Sending response")
        val code = "200-09"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accoGroupDto), responseInfo.httpStatus)
    }

}