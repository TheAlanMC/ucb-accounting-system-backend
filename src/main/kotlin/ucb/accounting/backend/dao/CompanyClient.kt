package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "company_client")
class CompanyClient {
    @EmbeddedId
var id: CompanyClientId = CompanyClientId()
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clientId")
    @JoinColumn(name = "client_id", referencedColumnName = "client_id", insertable = false, updatable = false)
    var client: Client? = null
}