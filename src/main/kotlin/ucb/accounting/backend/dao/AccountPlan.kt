package ucb.accounting.backend.dao

import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class AccountPlan (
    @Id
    val id: Long,
    val accountCategoryId: Int,
    val accountCategoryCode: Int,
    val accountCategoryName: String,
    val accountGroupId: Int,
    val accountGroupCode: Int,
    val accountGroupName: String,
    val accountSubgroupId: Int,
    val accountSubgroupCode: Int,
    val accountSubgroupName: String,
    val accountId: Int,
    val accountCode: Int,
    val accountName: String,
    val subaccountId: Int,
    val subaccountCode: Int,
    val subaccountName: String
) {
    constructor() : this(0, 0, 0, "", 0, 0, "", 0, 0, "", 0, 0, "", 0, 0, "")
}