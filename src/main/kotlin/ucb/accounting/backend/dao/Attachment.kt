package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "attachment")
class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    var attachmentId: Long = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "content_type")
    var contentType: String = ""

    @Column(name = "filename")
    var filename: String = ""

    @Column(name = "file_data")
    var fileData: ByteArray = ByteArray(0)

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

    @OneToMany(mappedBy = "attachment")
    var reports: List<Report>? = null

    @OneToMany(mappedBy = "attachment")
    var transactionAttachments: List<TransactionAttachment>? = null

}