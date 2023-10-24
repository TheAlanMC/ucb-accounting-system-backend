package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.TransactionDetail
import java.util.*

@Repository
interface TransactionDetailRepository : JpaRepository<TransactionDetail, Long> {

    @Query(
        """
            SELECT *
            FROM transaction_detail td
            JOIN transaction t ON t.transaction_id = td.transaction_id
            JOIN journal_entry je ON t.journal_entry_id = je.journal_entry_id
            AND je.journal_entry_accepted = TRUE
            AND je.status = TRUE
            AND je.company_id = :companyId
            AND t.transaction_date BETWEEN :dateFrom AND :dateTo
            AND td.subaccount_id in :subaccounts
        """,
        nativeQuery = true
    )
    fun findAll (@Param("companyId") companyId: Int, @Param("dateFrom") dateFrom: Date, @Param("dateTo") dateTo: Date, @Param("subaccounts") subaccounts: List<Int>): List<TransactionDetail>

    fun findAll (specification: Specification<TransactionDetail>, sort: Sort): List<TransactionDetail>
}