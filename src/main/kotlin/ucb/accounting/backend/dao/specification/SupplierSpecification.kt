package ucb.accounting.backend.dao.specification

import org.springframework.data.jpa.domain.Specification
import ucb.accounting.backend.dao.Supplier

class SupplierSpecification {
    companion object {

        fun supplierKeyword(keyword: String): Specification<Supplier> {
            return Specification { root, _, cb ->
                cb.or(
                    cb.like(cb.lower(root.get("displayName")), "%${keyword.lowercase()}%"),
                    cb.like(cb.lower(root.get("companyName")), "%${keyword.lowercase()}%"),
                    cb.like(cb.lower(root.get("companyPhoneNumber")), "%${keyword.lowercase()}%"),
                )
            }
        }
        fun companyId(companyId: Int): Specification<Supplier> {
            return Specification { root, _, cb ->
                cb.equal(root.get<Supplier>("companyId"), companyId)
            }
        }

        fun statusIsTrue(): Specification<Supplier> {
            return Specification { root, _, cb ->
                cb.equal(root.get<Supplier>("status"), true)
            }
        }
    }
}