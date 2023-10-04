package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.SaleTransaction

interface SaleTransactionRepository: JpaRepository<SaleTransaction, Long> {
    fun findByCompanyIdAndTransactionTypeIdAndSaleTransactionNumberAndStatusIsTrue (companyId: Int, transactionTypeId: Int, saleTransactionNumber: Int): SaleTransaction?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<SaleTransaction>

    fun findFirstByCompanyIdAndStatusIsTrueOrderBySaleTransactionNumberDesc (companyId: Int): SaleTransaction?
}