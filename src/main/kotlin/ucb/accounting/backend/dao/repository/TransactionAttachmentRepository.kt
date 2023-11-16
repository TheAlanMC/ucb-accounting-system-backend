package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.TransactionAttachment

interface TransactionAttachmentRepository: JpaRepository<TransactionAttachment, Long> {
    fun findAllByTransactionJournalEntryIdAndStatusIsTrue(journalEntryId: Long): List<TransactionAttachment>?
}