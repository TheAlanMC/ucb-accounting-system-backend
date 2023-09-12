package ucb.accounting.backend.dto

data class AttachmentDto(
    val attachmentId: Long,
    val contentType: String,
    val filename: String,
)