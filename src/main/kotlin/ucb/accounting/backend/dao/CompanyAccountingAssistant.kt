package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "company_accounting_assistant")
class CompanyAccountingAssistant {
    @EmbeddedId
    var id: CompanyAccountingAssistantId = CompanyAccountingAssistantId()

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountingAssistantId")
    @JoinColumn(name = "accounting_assistant_id", referencedColumnName = "accounting_assistant_id", insertable = false, updatable = false)
    var accountingAssistant: AccountingAssistant? = null
}