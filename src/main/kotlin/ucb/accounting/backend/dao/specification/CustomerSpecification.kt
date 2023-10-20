package ucb.accounting.backend.dao.specification

import org.springframework.data.jpa.domain.Specification
import ucb.accounting.backend.dao.Customer

class CustomerSpecification {
    companion object {

        fun customerKeyword(keyword: String): Specification<Customer> {
            return Specification { root, _, cb ->
                cb.or(
                    cb.like(cb.lower(root.get("displayName")), "%${keyword.lowercase()}%"),
                    cb.like(cb.lower(root.get("companyName")), "%${keyword.lowercase()}%"),
                    cb.like(cb.lower(root.get("companyPhoneNumber")), "%${keyword.lowercase()}%"),
                )
            }
        }
        fun companyId(companyId: Int): Specification<Customer> {
            return Specification { root, _, cb ->
                cb.equal(root.get<Customer>("companyId"), companyId)
            }
        }

        fun statusIsTrue(): Specification<Customer> {
            return Specification { root, _, cb ->
                cb.equal(root.get<Customer>("status"), true)
            }
        }
    }
}