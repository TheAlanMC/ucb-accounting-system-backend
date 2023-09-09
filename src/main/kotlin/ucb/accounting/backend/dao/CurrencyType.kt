package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "currency_type")
class CurrencyType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "currency_type_id")
    var currencyTypeId: Long = 0

    @Column(name = "currency_code")
    var currencyCode: String = ""

    @Column(name = "currency_name")
    var currencyName: String = ""

    @Column(name = "status")
    var status: Boolean = true
}
