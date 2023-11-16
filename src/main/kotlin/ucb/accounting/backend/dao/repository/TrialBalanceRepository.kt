package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.GeneralLedger
import ucb.accounting.backend.dao.JournalBook
import ucb.accounting.backend.dao.TrialBalance
import java.util.Date

interface TrialBalanceRepository: JpaRepository<TrialBalance, Long> {

    @Query(value =
    """
            SELECT  DISTINCT ON (s.subaccount_id)
                    ROW_NUMBER() OVER (ORDER BY s.subaccount_id) AS id,
                    s.subaccount_id as subaccount_id,
                    s.subaccount_code as subaccount_code,
                    s.subaccount_name as subaccount_name,
                    SUM(td.debit_amount_bs) AS debit_amount_bs,
                    SUM(td.credit_amount_bs) AS credit_amount_bs
            FROM subaccount s
            JOIN transaction_detail td ON s.subaccount_id = td.subaccount_id
            JOIN transaction t ON td.transaction_id = t.transaction_id
            JOIN journal_entry je ON t.journal_entry_id = je.journal_entry_id
            AND je.journal_entry_accepted = TRUE
            AND je.status = TRUE
            AND je.company_id = :companyId
            AND (t.transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)
            GROUP BY s.subaccount_id, s.subaccount_code, s.subaccount_name
            ORDER BY s.subaccount_id
    """, nativeQuery = true)
    fun findAllByCompanyIdAndStatusIsTrue (
        @Param("companyId") companyId: Int,
        @Param("dateFrom") dateFrom: Date?,
        @Param("dateTo") dateTo: Date?,
    ): List<TrialBalance>
}