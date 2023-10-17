package ucb.accounting.backend.dao.specification

import org.springframework.data.jpa.domain.Specification
import ucb.accounting.backend.dao.SaleTransaction
import java.util.*

class SaleTransactionSpecification {
    companion object {
        fun dateBetween(dateFrom: Date, dateTo: Date): Specification<SaleTransaction> {
            return Specification { root, _, cb ->
                cb.between(root.get("saleTransactionDate"), dateFrom, dateTo)
            }
        }

        fun transactionTypeId(transactionTypeId: Long): Specification<SaleTransaction> {
            return Specification { root, _, cb ->
                cb.equal(root.get<SaleTransaction>("transactionTypeId"), transactionTypeId)
            }
        }

        fun customerIds(customerIds: List<Long>): Specification<SaleTransaction> {
            return Specification { root, _, cb ->
                cb.`in`(root.get<Any>("customer").get<Any>("customerId")).value(customerIds)
            }
        }

        fun companyId(companyId: Int): Specification<SaleTransaction> {
            return Specification { root, _, cb ->
                cb.equal(root.get<SaleTransaction>("companyId"), companyId)
            }
        }

        fun statusIsTrue(): Specification<SaleTransaction> {
            return Specification { root, _, cb ->
                cb.equal(root.get<SaleTransaction>("status"), true)
            }
        }
    }
}