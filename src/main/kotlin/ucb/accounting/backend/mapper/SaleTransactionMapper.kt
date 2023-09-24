package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.SaleTransaction
import ucb.accounting.backend.dto.SaleTransactionPartialDto
import ucb.accounting.backend.dto.SubAccountDto

class SaleTransactionMapper {
    companion object {
        fun entityToDto(saleTransaction: SaleTransaction): SaleTransactionPartialDto {
            return SaleTransactionPartialDto(
                saleTransactionId = saleTransaction.saleTransactionId.toInt(),
                saleTransactionNumber = saleTransaction.saleTransactionNumber,
                saleTransactionDate = saleTransaction.saleTransactionDate,
                customerPartial = CustomerPartialMapper.entityToDto(saleTransaction.customer!!),
                gloss = saleTransaction.gloss,
                saleTransactionAccepted = saleTransaction.saleTransactionAccepted
            )
        }
    }
}