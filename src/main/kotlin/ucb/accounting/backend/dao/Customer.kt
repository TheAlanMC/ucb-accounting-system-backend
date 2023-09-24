package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "customer")
class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    var customerId: Long = 0

    @Column(name = "company_id")
    var companyId: Long = 0

    @Column(name = "subaccount_id")
    var subaccountId: Long = 0

    @Column(name = "prefix")
    var prefix: String = ""

    @Column(name = "display_name")
    var displayName: String = ""

    @Column(name = "first_name")
    var firstName: String = ""

    @Column(name = "last_name")
    var lastName: String = ""

    @Column(name = "company_name")
    var companyName: String = ""

    @Column(name = "company_email")
    var companyEmail: String = ""

    @Column(name = "company_phone_number")
    var companyPhoneNumber: String = ""

    @Column(name = "company_address")
    var companyAddress: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @ManyToOne
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @ManyToOne
    @JoinColumn(name = "subaccount_id", insertable = false, updatable = false)
    var subaccount: Subaccount? = null

    @OneToMany(mappedBy = "customer")
    var saleTransactions: List<SaleTransaction>? = null
}
