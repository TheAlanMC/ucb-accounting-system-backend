package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dto.JournalBookTransactionDetailDto

class JournalBookTransactionDetailMapper {

    companion object{
        fun entityToDto(transactionDetail: TransactionDetail): JournalBookTransactionDetailDto {
            return JournalBookTransactionDetailDto(
                subaccount = SubaccountMapper.entityToDto(transactionDetail.subaccount!!),
                debitAmountBs = transactionDetail.debitAmountBs,
                creditAmountBs = transactionDetail.creditAmountBs
            )
        }
    }

}