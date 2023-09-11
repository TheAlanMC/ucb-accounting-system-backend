package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.UsersBl
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.UserDto
import ucb.accounting.backend.util.ResponseCodeUtil

@Service
@RestController
@RequestMapping("/api/v1/users")
class UsersApi @Autowired constructor(private val usersBl: UsersBl) {
    companion object {
        private val logger = LoggerFactory.getLogger(UsersApi::class.java.name)
    }

    @PutMapping("/{kcUuid}")
    fun updateUserInfo(
        @PathVariable("kcUuid") kcUuid: String,
        @RequestBody userDto: UserDto,
    ) : ResponseEntity<ResponseDto<UserDto>> {
        logger.info("Starting the API call to update user info")
        logger.info("PUT /api/v1/users/{kcUuid}")
        val newUserDto = usersBl.updateUser(kcUuid, userDto)
        logger.info("Sending response")
        val code = "200-02"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, newUserDto), responseInfo.httpStatus)
    }

    @PutMapping("/{kcUuid}/passwords")
    fun updateUserPassword(
        @PathVariable("kcUuid") kcUuid: String,
    ) {
        logger.info("Starting the API call to update user password")
        logger.info("PUT /api/v1/users/{kcUuid}/passwords")
        logger.info("Finishing the API call to update user password")
    }

}
