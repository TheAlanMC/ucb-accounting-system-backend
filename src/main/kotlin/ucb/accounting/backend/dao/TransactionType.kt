package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "transaction_type")
class TransactionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_type_id")
    var transactionTypeId: Long = 0

    @Column(name = "transaction_type_name")
    var transactionTypeName: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @OneToMany(mappedBy = "transactionType", fetch = FetchType.LAZY)
    var saleTransactions: List<SaleTransaction>? = null

    @OneToMany(mappedBy = "transactionType", fetch = FetchType.LAZY)
    var expenseTransactions: List<ExpenseTransaction>? = null
}