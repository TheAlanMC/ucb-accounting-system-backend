package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.Industry

@Repository
interface IndustryRepository: JpaRepository<Industry, Long>{
    fun findByIndustryIdAndStatusIsTrue (industryId: Long): Industry?
    fun findAllByStatusIsTrue(): List<Industry>
}