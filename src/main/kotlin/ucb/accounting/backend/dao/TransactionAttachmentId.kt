package ucb.accounting.backend.dao

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class TransactionAttachmentId : Serializable {
    @Column(name = "transaction_id")
    var transactionId: Long = 0

    @Column(name = "attachment_id")
    var attachmentId: Long = 0
}