package ucb.accounting.backend.dao

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

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    var account: Account? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null
}