package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "transaction_attachment")
class TransactionAttachment {
    @EmbeddedId
    var id: TransactionAttachmentId = TransactionAttachmentId()

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("transactionId")
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id", insertable = false, updatable = false)
    var transaction: Transaction? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("attachmentId")
    @JoinColumn(name = "attachment_id", referencedColumnName = "attachment_id", insertable = false, updatable = false)
    var attachment: Attachment? = null
}