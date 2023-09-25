package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "transaction_detail")
class TransactionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_detail_id")
    var transactionDetailId: Long = 0

    @Column(name = "transaction_id")
    var transactionId: Int = 0

    @Column(name = "subaccount_id")
    var subaccountId: Int = 0

    @Column(name = "debit_amount_bs")
    var debitAmountBs: BigDecimal = BigDecimal.ZERO

    @Column(name = "credit_amount_bs")
    var creditAmountBs: BigDecimal = BigDecimal.ZERO

    @Column(name = "debit_amount_usd")
    var debitAmountUsd: BigDecimal = BigDecimal.ZERO

    @Column(name = "credit_amount_usd")
    var creditAmountUsd: BigDecimal = BigDecimal.ZERO

    @Column(name = "debit_amount_ufv")
    var debitAmountUfv: BigDecimal = BigDecimal.ZERO

    @Column(name = "credit_amount_ufv")
    var creditAmountUfv: BigDecimal = BigDecimal.ZERO

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id", insertable = false, updatable = false)
    var transaction: Transaction? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subaccount_id", referencedColumnName = "subaccount_id", insertable = false, updatable = false)
    var subaccount: Subaccount? = null

}