package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.ExpenseTransaction
import ucb.accounting.backend.dao.ExpenseTransactionEntry
import ucb.accounting.backend.dao.TransactionEntry
import java.util.*


interface ExpenseTransactionEntryRepository: PagingAndSortingRepository<ExpenseTransactionEntry, Long> {
    @Query(
        """
            SELECT  et.expense_transaction_id as expense_transaction_id,
                    tt.transaction_type_id as transaction_type_id,
                    tt.transaction_type_name as transaction_type_name,
                    et.expense_transaction_number as expense_transaction_number,
                    et.expense_transaction_date as expense_transaction_date,
                    s.supplier_id as supplier_id,
                    s.display_name as display_name,
                    s.company_name as company_name,
                    s.company_phone_number as company_phone_number,
                    et.tx_date as creation_date,    
                    et.gloss as gloss,
                    SUM((std.quantity * std.unit_price_bs) + std.amount_bs) AS total_amount_bs,
                    et.expense_transaction_accepted as expense_transaction_accepted
            FROM expense_transaction et
            JOIN expense_transaction_detail std ON std.expense_transaction_id = et.expense_transaction_id
            JOIN supplier s ON s.supplier_id = et.supplier_id
            JOIN transaction_type tt ON tt.transaction_type_id = et.transaction_type_id
            WHERE et.company_id = :companyId
            AND (et.expense_transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)
            AND (tt.transaction_type_name = :transactionType OR CAST(:transactionType AS VARCHAR) IS NULL)
            AND (s.display_name IN (:suppliers))
            AND et.status = true
            GROUP BY et.expense_transaction_id, tt.transaction_type_id, tt.transaction_type_name, et.expense_transaction_number, et.expense_transaction_date, s.supplier_id, s.display_name, s.company_name, s.company_phone_number, et.tx_date, et.gloss, et.expense_transaction_accepted
        """, nativeQuery = true
    )
    fun findAll(
        @Param("companyId") companyId: Long,
        @Param("dateFrom") dateFrom: Date?,
        @Param("dateTo") dateTo: Date?,
        @Param("transactionType") transactionType: String?,
        @Param("suppliers") suppliers: List<String>,
        pageable: Pageable
    ): Page<ExpenseTransactionEntry>
}