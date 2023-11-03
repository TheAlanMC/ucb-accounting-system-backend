package ucb.accounting.backend.dto

data class ExpenseDashboardDto(
    val description: MutableList<String>?,
    val count: MutableList<Int>?
)