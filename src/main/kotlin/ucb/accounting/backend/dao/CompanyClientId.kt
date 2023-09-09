package ucb.accounting.backend.dao

import javax.persistence.*

@Embeddable
class CompanyClientId : java.io.Serializable {
    @Column(name = "company_id")
    var companyId: Long = 0

    @Column(name = "client_id")
    var clientId: Long = 0
}