package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "subaccount_tax_type")
class SubaccountTaxType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subaccount_tax_type_id")
    var subaccountTaxTypeId: Long = 0

    @Column(name = "subaccount_id")
    var subaccountId: Int = 0

    @Column(name = "tax_type_id")
    var taxTypeId: Int = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "tax_rate")
    var taxRate: BigDecimal = BigDecimal.ZERO

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subaccountId")
    @JoinColumn(name = "subaccount_id", referencedColumnName = "subaccount_id", insertable = false, updatable = false)
    var subaccount: Subaccount? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taxTypeId")
    @JoinColumn(name = "tax_type_id", referencedColumnName = "tax_type_id", insertable = false, updatable = false)
    var taxType: TaxType? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

}