package ucb.accounting.backend.dto.pdf_turtle

data class ReportRequestDto(
    val footerHtmlTemplate: String,
    val headerHtmlTemplate: String,
    val htmlTemplate: String,
    val model: Map<String, Any>,
    val options: ReportOptions
)