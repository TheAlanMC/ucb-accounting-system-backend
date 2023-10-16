package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.UserBl
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/users")
class UserApi @Autowired constructor(private val userBl: UserBl) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserApi::class.java.name)
    }

    @PostMapping("/accountants")
    fun createAccountant(@RequestBody newUserDto: NewUserDto): ResponseEntity<ResponseDto<Nothing>>{
        logger.info("Starting the API call to create accountant")
        logger.info("POST /api/v1/users/accountants")
        userBl.createAccountant(newUserDto, "accountant")
        logger.info("Sending response")
        val code = "201-01"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @PostMapping("/accounting-assistants/companies/{companyId}")
    fun createAccountingAssistant(@RequestBody newUserDto: NewUserDto,
                                  @PathVariable companyId: Long): ResponseEntity<ResponseDto<Nothing>>{
        logger.info("Starting the API call to create accounting assistant")
        logger.info("POST /api/v1/users/accounting-assistants/companies/${companyId}")
        userBl.createAccountAssistant(newUserDto, "accounting_assistant", companyId)
        logger.info("Sending response")
        val code = "201-02"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @PostMapping("/clients/companies/{companyId}")
    fun createClient(@RequestBody newUserDto: NewUserDto,
                     @PathVariable companyId: Long): ResponseEntity<ResponseDto<Nothing>>{
        logger.info("Starting the API call to create client")
        logger.info("POST /api/v1/users/clients/companies/${companyId}")
        userBl.createClient(newUserDto, "client", companyId)
        logger.info("Sending response")
        val code = "201-03"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }
    
    @GetMapping
    fun findUserById(): ResponseEntity<ResponseDto<UserDto>> {
        logger.info("Starting the API call to get user info")
        logger.info("GET /api/v1/users")
        val user: UserDto = userBl.findUser()
        logger.info("Sending response")
        val code = "200-01"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, user), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun findAllUsersByCompanyId(
        @PathVariable companyId: Long,
        @RequestParam(defaultValue = "kcUuid") sortBy: String, // TODO CHECK IF THIS IS CORRECT
        @RequestParam(defaultValue = "asc") sortType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ResponseDto<List<UserPartialDto>>> {
        logger.info("Starting the API call to get all users by company id")
        logger.info("GET /api/v1/users/companies/${companyId}")
        val usersPage: Page<UserPartialDto> = userBl.findAllUsersByCompanyId(companyId, sortBy, sortType, page, size)
        logger.info("Sending response")
        val code = "200-01"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, usersPage.content, usersPage.totalElements), responseInfo.httpStatus)
    }

    @PutMapping
    fun updateUserInfo(
        @RequestBody userDto: UserDto,
    ) : ResponseEntity<ResponseDto<UserDto>> {
        logger.info("Starting the API call to update user info")
        logger.info("PUT /api/v1/users")
        val newUserDto = userBl.updateUser(userDto)
        logger.info("Sending response")
        val code = "200-02"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, newUserDto), responseInfo.httpStatus)
    }

    @PutMapping("/passwords")
    fun updateUserPassword(
        @RequestBody passwordUpdateDto: PasswordUpdateDto,
    ) : ResponseEntity<ResponseDto<Nothing>> {
        logger.info("Starting the API call to update user password")
        logger.info("PUT /api/v1/users/passwords")
        userBl.updateUserPassword(passwordUpdateDto)
        val code = "200-02"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }
}
