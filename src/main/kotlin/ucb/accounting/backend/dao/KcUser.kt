package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "kc_user")
class KcUser {
    @Id
    @Column(name = "kc_uuid")
    var kcUuid: String = ""

    @Column(name = "s3_profile_picture")
    var s3ProfilePicture: Int = 0

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @OneToMany(mappedBy = "kcUser")
    var kcUserCompany: List<KcUserCompany>? = null
}