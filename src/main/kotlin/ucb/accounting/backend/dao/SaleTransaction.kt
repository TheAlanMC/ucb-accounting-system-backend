package ucb.accounting.backend.dao

import org.hibernate.annotations.Generated
import org.hibernate.annotations.GenerationTime
import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.sql.Date
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "sale_transaction")
class SaleTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_transaction_id")
    var saleTransactionId: Long = 0

    @Column(name = "transaction_type_id")
    var transactionTypeId: Int = 0

    @Column(name = "payment_type_id")
    var paymentTypeId: Int = 0

    @Column(name = "journal_entry_id")
    var journalEntryId: Int = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "customer_id")
    var customerId: Int = 0

    @Column(name = "subaccount_id")
    var subaccountId: Int = 0

    @Column(name = "sale_transaction_number")
    var saleTransactionNumber: Int = 0

    @Column(name = "sale_transaction_reference")
    var saleTransactionReference: String = ""

    @Column(name = "sale_transaction_date")
    var saleTransactionDate: Date = Date(System.currentTimeMillis())

    @Column(name = "description")
    var description: String = ""

    @Column(name = "gloss")
    var gloss: String = ""

    @Column(name = "sale_transaction_accepted")
    var saleTransactionAccepted: Boolean = false

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @ManyToOne
    @JoinColumn(name = "transaction_type_id", insertable = false, updatable = false)
    var transactionType: TransactionType? = null

    @ManyToOne
    @JoinColumn(name = "payment_type_id", insertable = false, updatable = false)
    var paymentType: PaymentType? = null

    @ManyToOne
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    var customer: Customer? = null

    @ManyToOne
    @JoinColumn(name = "subaccount_id", insertable = false, updatable = false)
    var subaccount: Subaccount? = null

    @ManyToOne
    @JoinColumn(name = "journal_entry_id", insertable = false, updatable = false)
    var journalEntry: JournalEntry? = null

    @ManyToOne
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @OneToMany(mappedBy = "saleTransaction")
    var saleTransactionDetails: List<SaleTransactionDetail>? = null

}