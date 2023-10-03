package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.KcGroup

interface KcGroupRepository: JpaRepository<KcGroup, Long>{
    fun findByGroupNameAndStatusIsTrue (groupName: String): KcGroup?
}