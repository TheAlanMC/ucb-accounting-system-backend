package ucb.accounting.backend.dto

data class AttachmentDto (
    var attachmentId: Long = 0,
    var contentType: String = "",
    val filename: String = ""
)