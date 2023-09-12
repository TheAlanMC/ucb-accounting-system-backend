package ucb.accounting.backend.api.UsersApi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.UsersBl.UsersBl
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.UserDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/users")
class UsersApi @Autowired constructor(
    val usersBl: UsersBl
) {

    @GetMapping("/{kc_uuid}")
    fun findAllUsersById(@PathVariable kc_uuid: String): ResponseEntity<ResponseDto<UserDto>> {
        val user: UserDto = usersBl.findAllUsersById(kc_uuid)
        val code = "200-01"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, user), responseInfo.httpStatus)
    }
}