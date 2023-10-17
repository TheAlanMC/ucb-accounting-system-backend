package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.TransactionTypeRepository
import ucb.accounting.backend.dto.TransactionTypeDto
import ucb.accounting.backend.mapper.TransactionTypeMapper

@Service
class TransactionTypeBl @Autowired constructor(
    private val transactionTypeRepository: TransactionTypeRepository,
) {
    companion object{
        private val logger = LoggerFactory.getLogger(TransactionTypeBl::class.java.name)
    }

    fun getTransactionTypes(): List<TransactionTypeDto> {
        logger.info("Starting the BL call to get transaction types")
        val transactionTypes = transactionTypeRepository.findAllByStatusIsTrue()
        logger.info("Found ${transactionTypes.size} transaction types")
        logger.info("Finishing the BL call to get transaction types")
        return transactionTypes.map { TransactionTypeMapper.entityToDto(it) }
    }
}