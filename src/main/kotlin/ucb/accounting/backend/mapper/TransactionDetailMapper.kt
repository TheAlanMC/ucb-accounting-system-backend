package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dto.TransactionDetailDto

class TransactionDetailMapper {

    companion object{
        fun entityToDto(transactionDetail: TransactionDetail): TransactionDetailDto {
            return TransactionDetailDto(
                subaccountId = transactionDetail.subaccountId.toLong(),
                debitAmountBs = transactionDetail.debitAmountBs,
                creditAmountBs = transactionDetail.creditAmountBs
            )
        }
    }

}