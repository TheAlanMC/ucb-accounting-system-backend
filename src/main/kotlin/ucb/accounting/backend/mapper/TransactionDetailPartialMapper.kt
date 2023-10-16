package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dto.TransactionDetailPartialDto

class TransactionDetailPartialMapper {

    companion object{
        fun entityToDto(transactionDetail: TransactionDetail): TransactionDetailPartialDto {
            return TransactionDetailPartialDto(
                subaccount = SubaccountMapper.entityToDto(transactionDetail.subaccount!!),
                debitAmountBs = transactionDetail.debitAmountBs,
                creditAmountBs = transactionDetail.creditAmountBs
            )
        }
    }

}