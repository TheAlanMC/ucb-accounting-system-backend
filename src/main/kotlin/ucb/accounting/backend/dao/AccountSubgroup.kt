package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "account_subgroup")
class AccountSubgroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_subgroup_id")
    var accountSubgroupId: Long = 0

    @Column(name = "account_group_id")
    var accountGroupId: Int = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "account_subgroup_code")
    var accountSubgroupCode: Int = 0

    @Column(name = "account_subgroup_name")
    var accountSubgroupName: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_group_id", referencedColumnName = "account_group_id", insertable = false, updatable = false)
    var accountGroup: AccountGroup? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null
}