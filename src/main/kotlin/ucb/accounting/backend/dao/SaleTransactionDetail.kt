package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "sale_transaction_detail")
class SaleTransactionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_transaction_detail_id")
    var saleTransactionDetailId: Long = 0

    @Column(name = "sale_transaction_id")
    var saleTransactionId: Long = 0

    @Column(name = "subaccount_id")
    var subaccountId: Long = 0

    @Column(name = "quantity")
    var quantity: Long = 0

    @Column(name = "unit_price_bs")
    var unitPriceBs: BigDecimal = BigDecimal.ZERO

    @Column(name = "status")
    var status: Boolean = false

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
    @JoinColumn(name = "sale_transaction_id", insertable = false, updatable = false)
    var saleTransaction: SaleTransaction? = null
}