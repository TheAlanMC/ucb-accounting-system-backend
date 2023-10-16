package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import ucb.accounting.backend.dao.SaleTransaction

interface SaleTransactionRepository: PagingAndSortingRepository<SaleTransaction, Long> {
    fun findByCompanyIdAndTransactionTypeIdAndSaleTransactionNumberAndStatusIsTrue (companyId: Int, transactionTypeId: Int, saleTransactionNumber: Int): SaleTransaction?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int, pageable: Pageable): Page<SaleTransaction>

    fun findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderBySaleTransactionNumberDesc (companyId: Int, transactionTypeId: Int): SaleTransaction?

    fun findAllByCompanyIdAndJournalEntryIdAndStatusIsTrue (companyId: Int, journalEntryId: Int): List<SaleTransaction>
}