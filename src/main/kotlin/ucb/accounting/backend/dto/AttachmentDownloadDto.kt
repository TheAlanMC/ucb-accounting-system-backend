package ucb.accounting.backend.dto

data class AttachmentDownloadDto (
    val contentType: String = "",
    val filename: String = "",
    val fileUrl: String = ""
)
