package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "account_category")
class AccountCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_category_id")
    var accountCategoryId: Long = 0

    @Column(name = "account_category_code")
    var accountCategoryCode: Int = 0

    @Column(name = "account_category_name")
    var accountCategoryName: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @OneToMany(mappedBy = "accountCategory")
    var accountGroups: List<AccountGroup>? = null
}
