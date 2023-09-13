package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "kc_user_company")
class KcUserCompany {
    @EmbeddedId
    var id: KcUserCompanyId = KcUserCompanyId()

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

    @Column(name = "status")
    var status: Boolean = true


}