package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.KcUser

interface KcUserRepository: JpaRepository<KcUser, Long> {
    fun findByKcUuidAndStatusIsTrue (kcUuid: String): KcUser?
}