package ucb.accounting.backend.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.ResponseCodeUtil.Companion.getResponseInfo
import ucb.accounting.backend.util.ResponseCodeUtil
@ControllerAdvice
class ExceptionHandlerController {
    companion object {
        private val logger = LoggerFactory.getLogger(ExceptionHandlerController::class.java.name)
    }

    @ExceptionHandler(UasException::class)
    fun handleUasException(ex: UasException): ResponseEntity<ResponseDto<Nothing>> {
        val responseInfo = getResponseInfo(ex.code)
        logger.error("Error: ${ex.code} - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(ex.code, getResponseInfo(ex.code).message!!,null), getResponseInfo(ex.code).httpStatus)
    }
}