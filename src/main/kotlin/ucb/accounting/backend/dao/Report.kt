package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Date
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "report")
class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    var reportId: Long = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "report_type_id")
    var reportTypeId: Int = 0

    @Column(name = "currency_type_id")
    var currencyTypeId: Int = 0

    @Column(name = "atattachment_id")
    var attachmentId: Int = 0

    @Column(name = "period_start_date")
    var periodStartDate: Date = Date(System.currentTimeMillis())

    @Column(name = "period_end_date")
    var periodEndDate: Date = Date(System.currentTimeMillis())

    @Column(name = "description")
    var description: String = ""

    @Column(name = "is_finantial_statement")
    var isFinancialStatement: Boolean = false

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_type_id", referencedColumnName = "report_type_id", insertable = false, updatable = false)
    var reportType: ReportType? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_type_id", referencedColumnName = "currency_type_id", insertable = false, updatable = false)
    var currencyType: CurrencyType? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atattachment_id", referencedColumnName = "attachment_id", insertable = false, updatable = false)
    var attachment: Attachment? = null
}
