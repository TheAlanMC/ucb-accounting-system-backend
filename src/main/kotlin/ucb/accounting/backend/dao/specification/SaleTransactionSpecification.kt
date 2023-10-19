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

        fun transactionTypeId(transactionType: String): Specification<SaleTransaction> {
            return Specification { root, _, cb ->
                cb.equal(root.get<Any>("transactionType").get<Any>("transactionTypeName"), transactionType)
            }
        }

        fun customers(customers: List<String>): Specification<SaleTransaction> {
            return Specification { root, _, cb ->
                cb.`in`(root.get<Any>("customer").get<Any>("displayName")).value(customers)
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