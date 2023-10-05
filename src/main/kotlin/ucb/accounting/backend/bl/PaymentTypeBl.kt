package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.PaymentTypeRepository
import ucb.accounting.backend.dto.PaymentTypeDto
import ucb.accounting.backend.mapper.PaymentTypeMapper

@Service
class PaymentTypeBl @Autowired constructor(
    private val paymentTypeRepository: PaymentTypeRepository
) {
    companion object{
        private val logger = LoggerFactory.getLogger(DocumentTypeBl::class.java.name)
    }

    fun getPaymentTypes(): List<PaymentTypeDto> {
        logger.info("Starting the BL call to get payment types")
        val paymentTypes = paymentTypeRepository.findAllByStatusIsTrue()
        logger.info("Found ${paymentTypes.size} payment types")
        logger.info("Finishing the BL call to get payment types")
        return paymentTypes.map { PaymentTypeMapper.entityToDto(it) }
    }
}