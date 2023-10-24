package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.JournalEntry
import java.util.*

@Repository
interface JournalEntryRepository: PagingAndSortingRepository<JournalEntry, Long> {

    @Query(value =
    """
            SELECT *
        FROM journal_entry je
        LEFT JOIN sale_transaction st ON st.journal_entry_id = je.journal_entry_id
        LEFT JOIN expense_transaction et ON et.journal_entry_id = je.journal_entry_id
        
        WHERE je.company_id = :companyId
        AND je.status = TRUE
        AND st.journal_entry_id IS NULL
        AND et.journal_entry_id IS NULL
        AND je.journal_entry_number = :journalEntryNumber
    """
        , nativeQuery = true)
    fun findByCompanyIdAndJournalEntryNumberAndStatusIsTrue (@Param("companyId") companyId: Int, @Param("journalEntryNumber") journalEntryNumber: Int): JournalEntry?

    @Query(value =
        """
            SELECT *
            FROM journal_entry je
            LEFT JOIN sale_transaction st ON st.journal_entry_id = je.journal_entry_id
            LEFT JOIN expense_transaction et ON et.journal_entry_id = je.journal_entry_id
            
            WHERE je.company_id = :companyId
            AND je.status = TRUE
            AND st.journal_entry_id IS NULL
            AND et.journal_entry_id IS NULL
            ORDER by journal_entry_number DESC LIMIT 1;
        """
        , nativeQuery = true)
    fun findFirstByCompanyIdAndStatusIsTrueOrderByJournalEntryNumberDesc (@Param("companyId") companyId: Int): JournalEntry?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<JournalEntry>
    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Long): JournalEntry?

    fun findAllByJournalEntryIdIsInAndStatusIsTrue(journalEntryId: List<Long>, pageable: Pageable): Page<JournalEntry>

    @Query(
        """
        SELECT *
        FROM journal_entry je
        JOIN transaction t ON je.journal_entry_id = t.journal_entry_id
        AND je.journal_entry_accepted = TRUE
        AND je.status = TRUE
        AND je.company_id = :companyId
        AND t.transaction_date BETWEEN :dateFrom AND :dateTo
        """,
        nativeQuery = true
    )
    fun findAllByCompanyIdAndStatusIsTrue(
        @Param("companyId") companyId: Int,
        @Param("dateFrom") dateFrom: Date,
        @Param("dateTo") dateTo: Date,
        page: Pageable
    ): Page<JournalEntry>

}