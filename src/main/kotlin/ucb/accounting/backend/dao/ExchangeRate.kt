package ucb.accounting.backend.dao

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "exchange_rate")
class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_rate_id")
    var exchangeRateId: Long = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "currency_from")
    var currencyFrom: String = ""

    @Column(name = "currency_to")
    var currencyTo: String = ""

    @Column(name = "rate")
    var rate: BigDecimal = BigDecimal.ZERO

    @Column(name = "exchange_month")
    var exchangeMonth: Int = 0

    @Column(name = "exchange_year")
    var exchangeYear: Int = 0

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null
}
