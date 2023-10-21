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
@Table(name = "expense_transaction")
class ExpenseTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_transaction_id")
    var expenseTransactionId: Long = 0

    @Column(name = "transaction_type_id")
    var transactionTypeId: Int = 0

    @Column(name = "payment_type_id")
    var paymentTypeId: Int = 0

    @Column(name = "journal_entry_id")
    var journalEntryId: Int = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "supplier_id")
    var supplierId: Int = 0

    @Column(name = "subaccount_id")
    var subaccountId: Int = 0

    @Column(name = "expense_transaction_number")
    var expenseTransactionNumber: Int = 0

    @Column(name = "expense_transaction_reference")
    var expenseTransactionReference: String = ""

    @Column(name = "expense_transaction_date")
    var expenseTransactionDate: Date = Date(System.currentTimeMillis())

    @Column(name = "description")
    var description: String = ""

    @Column(name = "gloss")
    var gloss: String = ""

    @Generated(GenerationTime.ALWAYS)
    var totalAmountBs: BigDecimal = BigDecimal.ZERO

    @Column(name = "expense_transaction_accepted")
    var expenseTransactionAccepted: Boolean = false

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
    @JoinColumn(name = "supplier_id", insertable = false, updatable = false)
    var supplier: Supplier? = null

    @ManyToOne
    @JoinColumn(name = "subaccount_id", insertable = false, updatable = false)
    var subaccount: Subaccount? = null

    @ManyToOne
    @JoinColumn(name = "journal_entry_id", insertable = false, updatable = false)
    var journalEntry: JournalEntry? = null

    @ManyToOne
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @OneToMany(mappedBy = "expenseTransaction")
    var expenseTransactionDetails: List<ExpenseTransactionDetail>? = null

}