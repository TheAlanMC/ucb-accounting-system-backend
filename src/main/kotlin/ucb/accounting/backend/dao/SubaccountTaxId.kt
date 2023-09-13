package ucb.accounting.backend.dao

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class SubaccountTaxId: java.io.Serializable {
    @Column(name = "subaccount_id")
    var subaccountId: Long = 0

    @Column(name = "tax_id")
    var taxId: Long = 0
}