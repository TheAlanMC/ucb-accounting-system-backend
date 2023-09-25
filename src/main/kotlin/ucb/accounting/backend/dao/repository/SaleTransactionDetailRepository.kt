package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.SaleTransactionDetail

interface SaleTransactionDetailRepository: JpaRepository<SaleTransactionDetail, Long> {
    fun findAllBySaleTransactionIdAndStatusIsTrue(saleTransactionId: Long): List<SaleTransactionDetail>

}