package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.PaymentType

interface PaymentTypeRepository : JpaRepository<PaymentType, Long> {
    fun findByPaymentTypeIdAndStatusIsTrue(paymentTypeId: Long): PaymentType?

}