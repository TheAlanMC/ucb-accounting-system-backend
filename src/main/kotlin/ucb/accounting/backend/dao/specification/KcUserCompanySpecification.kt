package ucb.accounting.backend.dao.specification

import org.springframework.data.jpa.domain.Specification
import ucb.accounting.backend.dao.Company
import ucb.accounting.backend.dao.KcUser
import ucb.accounting.backend.dao.KcUserCompany

class KcUserCompanySpecification {
    companion object {

        fun kcUserKeyword(keyword: String): Specification<KcUserCompany> {
            return Specification { root, _, cb ->
                cb.or(
                    cb.like(cb.lower(root.get<KcUser>("kcUser").get("firstName")), "%${keyword.lowercase()}%"),
                    cb.like(cb.lower(root.get<KcUser>("kcUser").get("lastName")), "%${keyword.lowercase()}%"),
                    cb.like(cb.lower(root.get<KcUser>("kcUser").get("email")), "%${keyword.lowercase()}%"),
                )
            }
        }
        fun companyId(companyId: Int): Specification<KcUserCompany> {
            return Specification { root, _, cb ->
                cb.equal(root.get<Company>("company").get<Any>("companyId"), companyId)
            }
        }

        fun statusIsTrue(): Specification<KcUserCompany> {
            return Specification { root, _, cb ->
                cb.equal(root.get<KcUserCompany>("status"), true)
            }
        }
    }
}