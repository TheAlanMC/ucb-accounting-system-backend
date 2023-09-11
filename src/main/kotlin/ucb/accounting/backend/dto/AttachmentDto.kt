package ucb.accounting.backend.dto

data class AttachmentDto(
    val attachmentId: Long,
    val companyId: Int,
    val contentType: String,
    val filename: String,
    val fileData: ByteArray,
    val status: Boolean,
    val company: CompanyDto?
)