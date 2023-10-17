package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import ucb.accounting.backend.dao.SaleTransaction

interface SaleTransactionRepository: PagingAndSortingRepository<SaleTransaction, Long> {
    fun findByCompanyIdAndTransactionTypeIdAndSaleTransactionNumberAndStatusIsTrue (companyId: Int, transactionTypeId: Int, saleTransactionNumber: Int): SaleTransaction?

    fun findAll (specification: Specification<SaleTransaction>, pageable: Pageable): Page<SaleTransaction>

    fun findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderBySaleTransactionNumberDesc (companyId: Int, transactionTypeId: Int): SaleTransaction?

    @Query(value = "SELECT journal_entry_id FROM sale_transaction WHERE company_id = :companyId AND status = true", nativeQuery = true)
    fun findAllJournalEntryId(companyId: Int): List<Long>

    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Int): SaleTransaction?
}