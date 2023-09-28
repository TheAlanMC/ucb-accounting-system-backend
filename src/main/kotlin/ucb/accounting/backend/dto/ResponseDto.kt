package ucb.accounting.backend.dto

data class ResponseDto<T>(
    val code: String,
    val message: String,
    val data: T?
)