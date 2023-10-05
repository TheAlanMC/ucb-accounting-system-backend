package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.TransactionType
import ucb.accounting.backend.dto.TransactionTypeDto

class TransactionTypeMapper {

    companion object {
        fun entityToDto(transactionType: TransactionType): TransactionTypeDto {
            return TransactionTypeDto(
                transactionTypeId = transactionType.transactionTypeId.toInt(),
                transactionTypeName = transactionType.transactionTypeName,
            )
        }
    }
}