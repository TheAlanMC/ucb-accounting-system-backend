package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.TransactionType

interface TransactionTypeRepository : JpaRepository<TransactionType, Long> {
    fun findByTransactionTypeIdAndStatusIsTrue(transactionTypeId: Long): TransactionType?

    fun findByTransactionTypeNameAndStatusIsTrue(transactionTypeName: String): TransactionType?

    fun findAllByStatusIsTrue(): List<TransactionType>

}