package ucb.accounting.backend.dao.specification

import org.springframework.data.jpa.domain.Specification
import ucb.accounting.backend.dao.ExpenseTransaction
import java.util.*

class ExpenseTransactionSpecification {
    companion object {
        fun dateBetween(dateFrom: Date, dateTo: Date): Specification<ExpenseTransaction> {
            return Specification { root, _, cb ->
                cb.between(root.get("expenseTransactionDate"), dateFrom, dateTo)
            }
        }

        fun transactionTypeId(transactionTypeId: Long): Specification<ExpenseTransaction> {
            return Specification { root, _, cb ->
                cb.equal(root.get<ExpenseTransaction>("transactionTypeId"), transactionTypeId)
            }
        }

        fun customerIds(customerIds: List<Long>): Specification<ExpenseTransaction> {
            return Specification { root, _, cb ->
                cb.`in`(root.get<Any>("customer").get<Any>("customerId")).value(customerIds)
            }
        }

        fun companyId(companyId: Int): Specification<ExpenseTransaction> {
            return Specification { root, _, cb ->
                cb.equal(root.get<ExpenseTransaction>("companyId"), companyId)
            }
        }

        fun statusIsTrue(): Specification<ExpenseTransaction> {
            return Specification { root, _, cb ->
                cb.equal(root.get<ExpenseTransaction>("status"), true)
            }
        }
    }
}