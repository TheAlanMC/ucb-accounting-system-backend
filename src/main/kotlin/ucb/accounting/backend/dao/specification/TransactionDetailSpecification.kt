package ucb.accounting.backend.dao.specification

import org.springframework.data.jpa.domain.Specification
import ucb.accounting.backend.dao.*
import java.util.*

class TransactionDetailSpecification {
    companion object {

        fun dateBetween(dateFrom: Date, dateTo: Date): Specification<TransactionDetail> {
            return Specification { root, _, cb ->
                cb.between(root.get<Transaction>("transaction").get("transactionDate"), dateFrom, dateTo)
            }
        }

        fun subaccounts(subaccounts: List<Int>): Specification<TransactionDetail> {
            return Specification { root, _, cb ->
                cb.`in`(root.get<Any>("subaccountId")).value(subaccounts)
            }
        }


        fun accepted(): Specification<TransactionDetail> {
            return Specification { root, _, cb ->
                cb.equal(root.get<Transaction>("transaction").get<JournalEntry>("journalEntry").get<Any>("journalEntryAccepted"), true)
            }
        }
        fun companyId(companyId: Int): Specification<TransactionDetail> {
            return Specification { root, _, cb ->
                cb.equal(root.get<Transaction>("transaction").get<JournalEntry>("journalEntry"), companyId)
            }
        }

        fun statusIsTrue(): Specification<TransactionDetail> {
            return Specification { root, _, cb ->
                cb.equal(root.get<TransactionDetail>("status"), true)
            }
        }
    }
}