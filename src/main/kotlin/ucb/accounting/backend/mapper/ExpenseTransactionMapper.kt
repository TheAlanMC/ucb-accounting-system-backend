package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.ExpenseTransaction
import ucb.accounting.backend.dto.ExpenseTransactionPartialDto
import java.math.BigDecimal

class ExpenseTransactionMapper {
    companion object {
        fun entityToDto(expenseTransaction: ExpenseTransaction, totalAmountBs: BigDecimal): ExpenseTransactionPartialDto {
            return ExpenseTransactionPartialDto(
                expenseTransactionId = expenseTransaction.expenseTransactionId.toInt(),
                expenseTransactionNumber = expenseTransaction.expenseTransactionNumber,
                expenseTransactionDate = expenseTransaction.expenseTransactionDate,
                supplierPartial = SupplierPartialMapper.entityToDto(expenseTransaction.supplier!!),
                gloss = expenseTransaction.gloss,
                totalAmountBs = totalAmountBs,
                expenseTransactionAccepted = expenseTransaction.expenseTransactionAccepted
            )
        }
    }
}