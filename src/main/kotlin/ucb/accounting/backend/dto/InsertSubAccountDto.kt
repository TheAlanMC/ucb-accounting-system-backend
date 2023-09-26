package ucb.accounting.backend.dto

data class InsertSubAccountDto (
    val subaccountId: Long?,
    val accountId: Int?,
    val subaccountCode: Int?,
    val subaccountName: String?
){
    constructor(accountId: Int?, subaccountCode: Int?, subaccountName: String?) : this(null, accountId, subaccountCode, subaccountName)
}