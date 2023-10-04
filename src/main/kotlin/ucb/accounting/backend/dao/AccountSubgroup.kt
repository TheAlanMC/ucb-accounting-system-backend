package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
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
    @JoinColumn(name = "account_group_id", referencedColumnName = "account_group_id", insertable = false, updatable = false)
    var accountGroup: AccountGroup? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @OneToMany(mappedBy = "accountSubgroup")
    var accounts: List<Account>? = null
}