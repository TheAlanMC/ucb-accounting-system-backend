package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.SubaccountPartial
import ucb.accounting.backend.dao.TransactionDetail
import java.util.*

@Repository
interface SubaccountPartialRepository : PagingAndSortingRepository<SubaccountPartial, Long> {

    @Query(
        """
            SELECT DISTINCT ON (td.subaccount_id) 
                s.subaccount_id AS subaccount_id,
                s.subaccount_code AS subaccount_code,
                s.subaccount_name AS subaccount_name
            FROM transaction_detail td
            JOIN transaction t ON t.transaction_id = td.transaction_id
            JOIN journal_entry je ON t.journal_entry_id = je.journal_entry_id
            JOIN subaccount s ON s.subaccount_id = td.subaccount_id
            AND je.journal_entry_accepted = TRUE
            AND je.status = TRUE
            AND je.company_id = :companyId
            AND t.transaction_date BETWEEN :dateFrom AND :dateTo
            ORDER BY td.subaccount_id
        """,
        nativeQuery = true
    )
    fun findAllSubaccounts (@Param("companyId") companyId: Int, @Param("dateFrom") dateFrom: Date, @Param("dateTo") dateTo: Date): List<SubaccountPartial>
}
