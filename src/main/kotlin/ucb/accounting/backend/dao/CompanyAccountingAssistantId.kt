package ucb.accounting.backend.dao

import javax.persistence.Column
import javax.persistence.Embeddable


@Embeddable
class CompanyAccountingAssistantId : java.io.Serializable{
    @Column(name = "company_id")
    var companyId: Long = 0

    @Column(name = "accounting_assistant_id")
    var accountingAssistantId: Long = 0
}