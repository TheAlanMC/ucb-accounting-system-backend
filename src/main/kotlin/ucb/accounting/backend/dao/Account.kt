package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import javax.persistence.*
import java.sql.Timestamp

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
    @JoinColumn(name = "account_subgroup_id", referencedColumnName = "account_subgroup_id", insertable = false, updatable = false)
    var accountSubgroup: AccountSubgroup? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @OneToMany(mappedBy = "account")
    var accountSubaccounts: List<Subaccount>? = null
}