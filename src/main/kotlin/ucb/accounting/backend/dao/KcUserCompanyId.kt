package ucb.accounting.backend.dao

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class KcUserCompanyId: Serializable {
    @Column(name = "kc_uuid")
    var kcUuid: String = ""

    @Column(name = "company_id")
    var companyId: Long = 0
}