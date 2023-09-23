package ucb.accounting.backend.dao

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class SubaccountTaxTypeId: Serializable {
    @Column(name = "subaccount_id")
    var subaccountId: Long = 0

    @Column(name = "tax_type_id")
    var taxTypeId: Long = 0
}