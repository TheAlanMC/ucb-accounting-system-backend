package ucb.accounting.backend.mapper

import org.springframework.beans.factory.annotation.Autowired
import ucb.accounting.backend.dao.AccountCategory
import ucb.accounting.backend.dao.repository.AccountGroupRepository
import ucb.accounting.backend.dto.AccountCategoryDto

//class AccountCategoryMapper @Autowired constructor(
//
//){
//
//    companion object {
//        fun entityToDto(category: AccountCategory, companyId: Long): AccountCategoryDto {
//            val accountGroupRepository: AccountGroupRepository?
//            return AccountCategoryDto(
//                accountCategoryId = category.accountCategoryId,
//                accountCategoryCode = category.accountCategoryCode,
//                accountCategoryName = category.accountCategoryName,
//                accountGroups = category.accountGroups?.map { AccountGroupMapper.entityToDto(it) }
//            )
//        }
//    }
//
//}