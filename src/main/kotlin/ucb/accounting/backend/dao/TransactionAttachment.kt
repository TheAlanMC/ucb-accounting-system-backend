package ucb.accounting.backend.dao

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "transaction_attachment")
class TransactionAttachment {
    @EmbeddedId
    var id: TransactionAttachmentId = TransactionAttachmentId()

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("transactionId")
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id", insertable = false, updatable = false)
    var transaction: Transaction? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("attachmentId")
    @JoinColumn(name = "attachment_id", referencedColumnName = "attachment_id", insertable = false, updatable = false)
    var attachment: Attachment? = null
}