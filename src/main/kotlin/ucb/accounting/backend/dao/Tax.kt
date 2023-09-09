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

    @Column(name = "tax_name")
    var taxName: String = ""

    @Column(name = "tax_rate")
    var taxRate: BigDecimal = BigDecimal.ZERO

    @Column(name = "status")
    var status: Boolean = true
}
