package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "report_type")
class ReportType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_type_id")
    var reportTypeId: Long = 0

    @Column(name = "report_name")
    var reportName: String = ""

    @Column(name = "status")
    var status: Boolean = true
}