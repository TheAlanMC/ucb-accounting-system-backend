package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "subaccount_tax")
class SubaccountTax {
    @EmbeddedId
    var id: SubaccountTaxId = SubaccountTaxId()

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subaccountId")
    @JoinColumn(name = "subaccount_id", referencedColumnName = "subaccount_id", insertable = false, updatable = false)
    var subaccount: Subaccount? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taxId")
    @JoinColumn(name = "tax_id", referencedColumnName = "tax_id", insertable = false, updatable = false)
    var tax: Tax? = null

}