package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "account")
class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    var accountId: Long = 0

    @Column(name = "account_subgroup_id")
    var accountSubgroupId: Int = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "account_code")
    var accountCode: Int = 0

    @Column(name = "account_name")
    var accountName: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_subgroup_id", referencedColumnName = "account_subgroup_id", insertable = false, updatable = false)
    var accountSubgroup: AccountSubgroup? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null
}