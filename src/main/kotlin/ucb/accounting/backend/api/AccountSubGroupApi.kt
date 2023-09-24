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
import ucb.accounting.backend.bl.AccountSubGroupBl
import ucb.accounting.backend.dto.AccoSubGroupDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/account-subgroups")
class AccountSubGroupApi @Autowired constructor(private val accountSubGroupBl: AccountSubGroupBl){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountSubGroupApi::class.java.name)
    }

    @PostMapping("/companies/{companyId}")
    fun createAccountSubGroup(@PathVariable companyId: Long,
                              @RequestBody accoSubGroupDto: AccoSubGroupDto): ResponseEntity<ResponseDto<Nothing>> {
        logger.info("Starting the API call to create account sub group")
        logger.info("POST /api/v1/account-subgroups/companies/${companyId}")
        accountSubGroupBl.createAccountSubGroup(companyId, accoSubGroupDto)
        logger.info("Sending response")
        val code = "201-06"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getAccountSubGroups(@PathVariable companyId: Long): ResponseEntity<ResponseDto<List<AccoSubGroupDto>>>{
        logger.info("Starting the API call to get account sub groups")
        logger.info("GET /api/v1/account-subgroups/companies/${companyId}")
        val accountSubGroups = accountSubGroupBl.getAccountSubGroups(companyId)
        logger.info("Sending response")
        val code = "200-09"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accountSubGroups), responseInfo.httpStatus)
    }

//    @GetMapping("/{accountSubGroupId}/companies/{companyId}")
//    fun getAccountSubGroup(@PathVariable companyId: Long,
//                           @PathVariable accountSubGroupId: Long): ResponseEntity<ResponseDto<AccoSubGroupDto>>{
//        logger.info("Starting the API call to get account sub group")
//        logger.info("GET /api/v1/account-subgroups/${accountSubGroupId}/companies/${companyId}")
//        val accountSubGroup = accountSubGroupBl.getAccountSubGroup(companyId, accountSubGroupId)
//        logger.info("Sending response")
//        val code = "200-09"
//        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
//        logger.info("Code: $code - ${responseInfo.message}")
//        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accountSubGroup), responseInfo.httpStatus)
//    }
//
//    @PutMapping("/{accountSubGroupId}/companies/{companyId}")
//    fun updateAccountSubGroup(@PathVariable companyId: Long,
//                              @PathVariable accountSubGroupId: Long,
//                              @RequestBody accoSubGroupDto: AccoSubGroupDto): ResponseEntity<ResponseDto<Nothing>>{
//        logger.info("Starting the API call to update account sub group")
//        logger.info("PUT /api/v1/account-subgroups/${accountSubGroupId}/companies/${companyId}")
//        accountSubGroupBl.updateAccountSubGroup(companyId, accountSubGroupId, accoSubGroupDto)
//        logger.info("Sending response")
//        val code = "200-10"
//        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
//        logger.info("Code: $code - ${responseInfo.message}")
//        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
//    }

}