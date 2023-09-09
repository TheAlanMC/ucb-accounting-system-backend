package ucb.accounting.backend.dao

import java.math.BigDecimal
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

    @Column(name = "account_id")
    var accountId: Int = 0

    @Column(name = "currency_type_id")
    var currencyTypeId: Int = 0

    @Column(name = "amount")
    var amount: BigDecimal = BigDecimal.ZERO

    @Column(name = "description")
    var description: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id", insertable = false, updatable = false)
    var transaction: Transaction? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    var account: Account? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_type_id", referencedColumnName = "currency_type_id", insertable = false, updatable = false)
    var currencyType: CurrencyType? = null
}