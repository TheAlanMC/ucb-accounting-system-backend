package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "user_company")
class UserCompany {
    @EmbeddedId
    var id: UserCompanyId = UserCompanyId()

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("kcUuid")
    @JoinColumn(name = "kc_uuid", referencedColumnName = "kc_uuid", insertable = false, updatable = false)
    var user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", referencedColumnName = "group_id", insertable = false, updatable = false)
    var group: Group? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

}