package ucb.accounting.backend.dto

data class AttachmentDto (
    val attachmentId: Long = 0,
    val contentType: String = "",
    val filename: String = ""
)