package ucb.accounting.backend.api
/*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.UsersInfoBl
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.UserDto
import ucb.accounting.backend.util.ResponseCodeUtil

@Service
@RestController
@RequestMapping("/api/v1/users")
class UsersInfoApi @Autowired constructor(private val usersInfoBl: UsersInfoBl){
    companion object {
        private val logger = LoggerFactory.getLogger(UsersInfoApi::class.java.name)
    }

    @GetMapping("/{kcUuid}")
    fun getUsersInfo(
            @PathVariable("kcUuid") kcUuid: String
    ) : ResponseEntity<ResponseDto<UserDto>>{
        logger.info("Starting the API call to get users info")
        logger.info("GET /api/v1/users/{kcUuid}")
        val usersInfo = usersInfoBl.getUsersInfo(kcUuid)
        val code = "200-01"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, usersInfo), responseInfo.httpStatus)
    }
}*/