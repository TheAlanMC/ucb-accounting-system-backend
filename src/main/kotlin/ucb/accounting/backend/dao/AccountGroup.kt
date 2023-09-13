package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "account_group")
class AccountGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_group_id")
    var accountGroupId: Long = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "account_category_id")
    var accountCategoryId: Int = 0

    @Column(name = "account_group_code")
    var accountGroupCode: Int = 0

    @Column(name = "account_group_name")
    var accountGroupName: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_category_id", referencedColumnName = "account_category_id", insertable = false, updatable = false)
    var accountCategory: AccountCategory? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @OneToMany(mappedBy = "accountGroup")
    var accountSubgroups: List<AccountSubgroup>? = null
}