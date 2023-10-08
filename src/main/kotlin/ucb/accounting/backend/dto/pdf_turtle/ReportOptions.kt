package ucb.accounting.backend.dto.pdf_turtle

data class ReportOptions (
    val excludeBuiltStyles: Boolean,
    val landscape: Boolean,
    val margins: Margins,
    val pageFormat: String,
    val pageSize: PageSize
)