package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "kc_user_company")
class KcUserCompany {
    @EmbeddedId
    var id: KcUserCompanyId = KcUserCompanyId()

    @Column(name = "kc_group_id")
    var kcGroupId: Long = 0

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("kcUuid")
    @JoinColumn(name = "kc_uuid", referencedColumnName = "kc_uuid", insertable = false, updatable = false)
    var kcUser: KcUser? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("kcGroupId")
    @JoinColumn(name = "kc_group_id", referencedColumnName = "kc_group_id", insertable = false, updatable = false)
    var kcGroup: KcGroup? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null
}