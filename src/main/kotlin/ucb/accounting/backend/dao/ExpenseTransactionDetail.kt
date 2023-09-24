package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "expense_transaction_detail")
class ExpenseTransactionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_transaction_detail_id")
    var expenseTransactionDetailId: Long = 0

    @Column(name = "expense_transaction_id")
    var expenseTransactionId: Long = 0

    @Column(name = "subaccount_id")
    var subaccountId: Long = 0

    @Column(name = "quantity")
    var quantity: Long = 0

    @Column(name = "unit_price_bs")
    var unitPriceBs: BigDecimal = BigDecimal.ZERO

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @ManyToOne
    @JoinColumn(name = "subaccount_id", insertable = false, updatable = false)
    var subaccount: Subaccount? = null

    @ManyToOne
    @JoinColumn(name = "expense_transaction_id", insertable = false, updatable = false)
    var expenseTransaction: ExpenseTransaction? = null
}