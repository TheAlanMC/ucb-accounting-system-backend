package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.CompanyBl
import ucb.accounting.backend.bl.CustomerBl
import ucb.accounting.backend.dto.CustomerDto
import ucb.accounting.backend.dto.CustomerPartialDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/customers")
class CustomerApi @Autowired constructor(private val customerBl: CustomerBl){

    companion object {
        private val logger = LoggerFactory.getLogger(CustomerApi::class.java.name)
    }

    @PostMapping("/companies/{companyId}")
    fun createCustomer(@PathVariable companyId: Long,
                       @RequestBody customerDto: CustomerDto
    ): ResponseEntity<ResponseDto<Nothing>> {
        logger.info("Starting the API call to create customer")
        logger.info("POST /api/v1/customers/companies/$companyId")
        customerBl.createCustomer(companyId, customerDto)
        logger.info("Sending response")
        val code = "201-10"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getCustomers(@PathVariable companyId: Long): ResponseEntity<ResponseDto<List<CustomerPartialDto>>> {
        logger.info("Starting the API call to get customers")
        logger.info("GET /api/v1/customers/companies/$companyId")
        val customers: List<CustomerPartialDto> = customerBl.getCustomers(companyId)
        logger.info("Sending response")
        val code = "200-28"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, customers), responseInfo.httpStatus)
    }

    @GetMapping("/{customerId}/companies/{companyId}")
    fun getCustomer(@PathVariable customerId: Long, @PathVariable companyId: Long): ResponseEntity<ResponseDto<CustomerDto>> {
        logger.info("Starting the API call to get customer")
        logger.info("GET /api/v1/customers/$customerId/companies/$companyId")
        val customer: CustomerDto = customerBl.getCustomer(customerId, companyId)
        logger.info("Sending response")
        val code = "200-28"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, customer), responseInfo.httpStatus)
    }

    @PutMapping("/{customerId}/companies/{companyId}")
    fun updateCustomer(@PathVariable customerId: Long,
                       @PathVariable companyId: Long,
                       @RequestBody customerDto: CustomerDto
    ): ResponseEntity<ResponseDto<CustomerDto>> {
        logger.info("Starting the API call to update customer")
        logger.info("PUT /api/v1/customers/$customerId/companies/$companyId")
        val customer: CustomerDto = customerBl.updateCustomer(customerId, companyId, customerDto)
        logger.info("Sending response")
        val code = "200-29"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, customer), responseInfo.httpStatus)
    }
}