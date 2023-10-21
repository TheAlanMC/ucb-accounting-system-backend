package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.SaleTransaction
import ucb.accounting.backend.dto.SaleTransactionDto
import java.math.BigDecimal

class SaleTransactionMapper {
    companion object {
        fun entityToDto(saleTransaction: SaleTransaction): SaleTransactionDto {
            return SaleTransactionDto(
                saleTransactionId = saleTransaction.saleTransactionId.toInt(),
                transactionType = TransactionTypeMapper.entityToDto(saleTransaction.transactionType!!),
                saleTransactionNumber = saleTransaction.saleTransactionNumber,
                saleTransactionDate = saleTransaction.saleTransactionDate,
                customer = CustomerPartialMapper.entityToDto(saleTransaction.customer!!),
                gloss = saleTransaction.gloss,
                totalAmountBs = saleTransaction.totalAmountBs,
                saleTransactionAccepted = saleTransaction.saleTransactionAccepted
            )
        }
    }
}