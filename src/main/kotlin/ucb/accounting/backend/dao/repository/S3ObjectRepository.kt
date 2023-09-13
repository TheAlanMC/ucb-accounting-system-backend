package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.S3Object

interface S3ObjectRepository: JpaRepository<S3Object, Long> {
    fun findByS3ObjectIdAndStatusIsTrue (s3ObjectId: Long): S3Object?
}