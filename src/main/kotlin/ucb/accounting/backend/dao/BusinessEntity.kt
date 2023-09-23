package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "business_entity")
class BusinessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_entity_id")
    var businessEntityId: Long = 0

    @Column(name = "business_entity_name")
    var businessEntityName: String = ""

    @Column(name = "description")
    var description: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @OneToMany(mappedBy = "businessEntity")
    var companies: List<Company>? = null
}