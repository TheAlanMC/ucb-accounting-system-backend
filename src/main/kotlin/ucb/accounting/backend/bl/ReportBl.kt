package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.*
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.S3Object
import ucb.accounting.backend.dao.TransactionDetail
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.CompanyMapper
import ucb.accounting.backend.mapper.ReportTypeMapper
import ucb.accounting.backend.service.MinioService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.util.*

@Service
class ReportBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val transactionDetailRepository: TransactionDetailRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val minioService: MinioService,
    private val reportTypeRepository: ReportTypeRepository,
    private val s3ObjectRepository: S3ObjectRepository,
    private val subaccountRepository: SubaccountRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DocumentTypeBl::class.java.name)
    }

    fun getReportTypes(): List<ReportTypeDto> {
        logger.info("Starting the BL call to get report types")
        val reportTypes = reportTypeRepository.findAllByStatusIsTrue()
        logger.info("Found ${reportTypes.size} report types")
        logger.info("Finishing the BL call to get report types")
        return reportTypes.map { ReportTypeMapper.entityToDto(it) }
    }

    fun getJournalBook(
        companyId: Long,
        sortBy: String,
        sortType: String,
        page: Int,
        size: Int,
        dateFrom: String,
        dateTo: String,
        subaccountIds: List<String>
    ): Page<ReportDto<GeneralLedgerReportDto>> {
        logger.info("Starting the BL call to get journal book report")
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-22")
        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)
        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo) || subaccountIds.isEmpty()) {
            throw UasException("400-16")
        }
        // Parse subaccountIds to Long
        val newSubaccountIds: List<Int> = subaccountIds.map { it.toInt() }
        // Validation of subaccountIds
        val subaccountsEntities = newSubaccountIds.map {
            val subaccount = subaccountRepository.findBySubaccountIdAndStatusIsTrue(it.toLong()) ?: throw UasException("404-10")
            if (subaccount.companyId.toLong() != companyId) {
                throw UasException("403-22")
            }
            subaccount
        }

        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), sortBy))
        val ledgerBooks: List<TransactionDetail> = transactionDetailRepository.findAll(companyId.toInt(), newDateFrom, newDateTo, newSubaccountIds)

        // Getting company info
        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        val companyDto = CompanyMapper.entityToDto(company, preSignedUrl)

        val reportDto: List<ReportDto<GeneralLedgerReportDto>> = subaccountsEntities.map { subaccount ->
            var accumulatedBalance = BigDecimal(0.00)
            val ledgerBook = ledgerBooks.filter { it.subaccount!!.subaccountId == subaccount.subaccountId }
            val transactionDetails = ledgerBook.map {
                    accumulatedBalance += it.creditAmountBs - it.debitAmountBs
                    GeneralLedgerTransactionDetailDto(
                        transactionDate = it.transaction!!.transactionDate,
                        gloss = it.transaction!!.journalEntry!!.gloss,
                        description = it.transaction!!.description,
                        creditAmount = it.creditAmountBs,
                        debitAmount = it.debitAmountBs,
                        balanceAmount = accumulatedBalance
                    )
                }

            val generalLedgerReportDto: GeneralLedgerReportDto = GeneralLedgerReportDto(
                accountCategory = AccountCategoryDetailDto(
                    accountCategoryId = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountCategory!!.accountCategoryId,
                    accountCategoryCode = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountCategory!!.accountCategoryCode,
                    accountCategoryName = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountCategory!!.accountCategoryName,
                    accountGroup = AccountGroupDetailDto(
                        accountGroupId = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountGroupId,
                        accountGroupCode = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountGroupCode,
                        accountGroupName = subaccount.account!!.accountSubgroup!!.accountGroup!!.accountGroupName,
                        accountSubgroup = AccountSubgroupDetailDto(
                            accountSubgroupId = subaccount.account!!.accountSubgroup!!.accountSubgroupId,
                            accountSubgroupCode = subaccount.account!!.accountSubgroup!!.accountSubgroupCode,
                            accountSubgroupName = subaccount.account!!.accountSubgroup!!.accountSubgroupName,
                            account = AccountDetailDto(
                                accountId = subaccount.account!!.accountId,
                                accountCode = subaccount.account!!.accountCode,
                                accountName = subaccount.account!!.accountName,
                                subaccount = SubaccountDetailDto(
                                    subaccountId = subaccount.subaccountId,
                                    subaccountCode = subaccount.subaccountCode,
                                    subaccountName = subaccount.subaccountName
                                )
                            )
                        )
                    )
                ),
            transactionDetails = transactionDetails,
            totalDebitAmount = if (transactionDetails.isNotEmpty()) transactionDetails.map { it.debitAmount }.reduce { acc, it -> acc + it } else BigDecimal(0.00),
            totalCreditAmount = if (transactionDetails.isNotEmpty()) transactionDetails.map { it.creditAmount }.reduce { acc, it -> acc + it } else BigDecimal(0.00),
            totalBalanceAmount = accumulatedBalance
            )
            ReportDto(
                company = companyDto,
                startDate = newDateFrom,
                endDate = newDateTo,
                reportData = generalLedgerReportDto
            )
        }
        logger.info("Found ${reportDto.size} journal book reports")
        logger.info("Finishing the BL call to get journal book report")
        return PageImpl(reportDto, pageable, reportDto.size.toLong())
    }
}