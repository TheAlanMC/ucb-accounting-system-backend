package ucb.accounting.backend.dao

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "tax")
class Tax {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_id")
    var taxId: Long = 0

    @Column(name = "year")
    var year: Int = 0

    @Column(name = "tax_percentage")
    var taxPercentage: BigDecimal = BigDecimal.ZERO

    @Column(name = "tax_name")
    var taxName: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @OneToMany(mappedBy = "tax")
    var subaccountTaxes: List<SubaccountTax>? = null
}
