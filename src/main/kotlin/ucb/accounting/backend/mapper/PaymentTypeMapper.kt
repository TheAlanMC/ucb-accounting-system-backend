package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.PaymentType
import ucb.accounting.backend.dto.PaymentTypeDto

class PaymentTypeMapper {

    companion object{
        fun entityToDto(paymentType: PaymentType): PaymentTypeDto {
            return PaymentTypeDto(
                paymentTypeId = paymentType.paymentTypeId,
                paymentTypeName = paymentType.paymentTypeName
            )
        }
    }
}