package ucb.accounting.backend.dto

data class NewFileDto (
    val filename: String,
    val bucket: String,
    val contentType: String,
    val fileUrl: String
)