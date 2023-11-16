package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "subaccount")
class Subaccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subaccount_id")
    var subaccountId: Long = 0

    @Column(name = "account_id")
    var accountId: Int = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "subaccount_code")
    var subaccountCode: Int = 0

    @Column(name = "subaccount_name")
    var subaccountName: String = ""

    @Column(name = "is_editable")
    var isEditable: Boolean = true

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    var account: Account? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @OneToMany(mappedBy = "subaccount")
    var transactionDetails: List<TransactionDetail>? = null

    @OneToMany(mappedBy = "subaccount")
    var subaccountTaxTypes: List<SubaccountTaxType>? = null

    @OneToMany(mappedBy = "subaccount")
    var customers: List<Customer>? = null

    @OneToMany(mappedBy = "subaccount")
    var saleTransactions: List<SaleTransaction>? = null

    @OneToMany(mappedBy = "subaccount")
    var saleTransactionDetails: List<SaleTransactionDetail>? = null

    @OneToMany(mappedBy = "subaccount")
    var suppliers: List<Supplier>? = null

    @OneToMany(mappedBy = "subaccount")
    var expenseTransactions: List<ExpenseTransaction>? = null

    @OneToMany(mappedBy = "subaccount")
    var expenseTransactionDetails: List<ExpenseTransactionDetail>? = null
}