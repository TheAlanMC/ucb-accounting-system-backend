package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/examples")
class ExampleApi {
    companion object {
        private val logger = LoggerFactory.getLogger(ExampleApi::class.java.name)
    }

    @GetMapping("{responseCode}")
    fun response(@PathVariable responseCode: String): ResponseEntity<ResponseDto<String>> {
        logger.info("Starting the API call to get $responseCode")
        if (responseCode == "200") {
            val code = "200-01"
            val responseInfo = ResponseCodeUtil.getResponseInfo(code)
            return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
        }
        if (responseCode == "201") {
            val code = "201-01"
            val responseInfo = ResponseCodeUtil.getResponseInfo(code)
            return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
        }
        if (responseCode == "400") {
            throw UasException("400-01")
        }
        if (responseCode == "401") {
            throw UasException("401-01")
        }
        if (responseCode == "403") {
            throw UasException("403-01")
        }
        if (responseCode == "404") {
            throw UasException("404-01")
        }
        if (responseCode == "409") {
            throw UasException("409-01")
        }
        val code = "500-01"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }
}