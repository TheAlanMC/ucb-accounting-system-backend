package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.ExpenseTransaction
import ucb.accounting.backend.dto.ExpenseTransactionDto
import java.math.BigDecimal

class ExpenseTransactionMapper {
    companion object {
        fun entityToDto(expenseTransaction: ExpenseTransaction, totalAmountBs: BigDecimal): ExpenseTransactionDto {
            return ExpenseTransactionDto(
                expenseTransactionId = expenseTransaction.expenseTransactionId.toInt(),
                transactionType = TransactionTypeMapper.entityToDto(expenseTransaction.transactionType!!),
                expenseTransactionNumber = expenseTransaction.expenseTransactionNumber,
                expenseTransactionDate = expenseTransaction.expenseTransactionDate,
                supplier = SupplierPartialMapper.entityToDto(expenseTransaction.supplier!!),
                gloss = expenseTransaction.gloss,
                totalAmountBs = totalAmountBs,
                expenseTransactionAccepted = expenseTransaction.expenseTransactionAccepted
            )
        }
    }
}