package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.SaleTransaction
import ucb.accounting.backend.dto.SaleTransactionPartialDto
import java.math.BigDecimal

class SaleTransactionMapper {
    companion object {
        fun entityToDto(saleTransaction: SaleTransaction,totalAmountBs: BigDecimal): SaleTransactionPartialDto {
            return SaleTransactionPartialDto(
                saleTransactionId = saleTransaction.saleTransactionId.toInt(),
                saleTransactionNumber = saleTransaction.saleTransactionNumber,
                saleTransactionDate = saleTransaction.saleTransactionDate,
                customer = CustomerPartialMapper.entityToDto(saleTransaction.customer!!),
                gloss = saleTransaction.gloss,
                totalAmountBs = totalAmountBs,
                saleTransactionAccepted = saleTransaction.saleTransactionAccepted
            )
        }
    }
}