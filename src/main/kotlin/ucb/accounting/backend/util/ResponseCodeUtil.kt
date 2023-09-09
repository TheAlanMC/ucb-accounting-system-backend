package ucb.accounting.backend.util

import org.springframework.http.HttpStatus

class ResponseCodeUtil (var httpStatus: HttpStatus, message: String, messageTranslated: String? = null) : Exception(message) {
    companion object {
        fun getResponseInfo(code: String): ResponseCodeUtil {
            return when (code) {
                "200-01" -> ResponseCodeUtil(HttpStatus.OK, "User information retrieved successfully.")
                "200-02" -> ResponseCodeUtil(HttpStatus.OK, "User information has been successfully updated.")
                "200-03" -> ResponseCodeUtil(HttpStatus.OK, "Industries retrieved successfully.")
                "200-04" -> ResponseCodeUtil(HttpStatus.OK, "Business entities retrieved successfully.")
                "200-05" -> ResponseCodeUtil(HttpStatus.OK, "Company information retrieved successfully.")
                "200-06" -> ResponseCodeUtil(HttpStatus.OK, "Company information has been successfully updated.")
                "200-07" -> ResponseCodeUtil(HttpStatus.OK, "Accounting plan retrieved successfully.")
                "200-08" -> ResponseCodeUtil(HttpStatus.OK, "Account group information retrieved successfully.")
                "200-09" -> ResponseCodeUtil(HttpStatus.OK, "Account group information has been successfully updated.")
                "200-10" -> ResponseCodeUtil(HttpStatus.OK, "Account subgroup information retrieved successfully.")
                "200-11" -> ResponseCodeUtil(HttpStatus.OK, "Account subgroup information has been successfully updated.")
                "200-12" -> ResponseCodeUtil(HttpStatus.OK, "Account information retrieved successfully.")
                "200-13" -> ResponseCodeUtil(HttpStatus.OK, "Account information has been successfully updated.")
                "200-14" -> ResponseCodeUtil(HttpStatus.OK, "Subaccount information retrieved successfully.")
                "200-15" -> ResponseCodeUtil(HttpStatus.OK, "Subaccount information has been successfully updated.")
                "200-16" -> ResponseCodeUtil(HttpStatus.OK, "Attachment uploaded successfully.")
                "200-17" -> ResponseCodeUtil(HttpStatus.OK, "File retrieved successfully.")
                "200-18" -> ResponseCodeUtil(HttpStatus.OK, "Picture uploaded successfully.")
                "200-19" -> ResponseCodeUtil(HttpStatus.OK, "Picture retrieved successfully.")
                "200-20" -> ResponseCodeUtil(HttpStatus.OK, "Document types retrieved successfully.")
                "200-21" -> ResponseCodeUtil(HttpStatus.OK, "Report types retrieved successfully.")
                "200-22" -> ResponseCodeUtil(HttpStatus.OK, "Journal book report retrieved successfully.")
                "200-23" -> ResponseCodeUtil(HttpStatus.OK, "General ledger report retrieved successfully.")
                "200-24" -> ResponseCodeUtil(HttpStatus.OK, "Trial balance report retrieved successfully.")
                "200-25" -> ResponseCodeUtil(HttpStatus.OK, "Worksheet report retrieved successfully.")
                "200-26" -> ResponseCodeUtil(HttpStatus.OK, "Income statement report retrieved successfully.")
                "200-27" -> ResponseCodeUtil(HttpStatus.OK, "Balance sheet report retrieved successfully.")
                "201-01" -> ResponseCodeUtil(HttpStatus.CREATED, "The new accountant user has been successfully created.")
                "201-02" -> ResponseCodeUtil(HttpStatus.CREATED, "The new accounting assistant user has been successfully created.")
                "201-03" -> ResponseCodeUtil(HttpStatus.CREATED, "The new client user has been successfully created.")
                "201-04" -> ResponseCodeUtil(HttpStatus.CREATED, "The new company has been successfully created.")
                "201-05" -> ResponseCodeUtil(HttpStatus.CREATED, "The new account group has been successfully created.")
                "201-06" -> ResponseCodeUtil(HttpStatus.CREATED, "The new account subgroup has been successfully created.")
                "201-07" -> ResponseCodeUtil(HttpStatus.CREATED, "The new account has been successfully created.")
                "201-08" -> ResponseCodeUtil(HttpStatus.CREATED, "The new subaccount has been successfully created.")
                "201-09" -> ResponseCodeUtil(HttpStatus.CREATED, "A journal entry has been successfully created.")
                "400-01" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to create the accountant user was not provided.")
                "400-02" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to create the accounting assistant user was not provided.")
                "400-03" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to create the client user was not provided.")
                "400-04" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to update the user information was not provided.")
                "400-05" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to create a company was not provided.")
                "400-06" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to update the company information was not provided.")
                "400-07" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to create an account group was not provided.")
                "400-08" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to update the account group information was not provided.")
                "400-09" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to create an account subgroup was not provided.")
                "400-10" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to update the account subgroup information was not provided.")
                "400-11" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to create an account was not provided.")
                "400-12" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to update the account information was not provided.")
                "400-13" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to create a subaccount was not provided.")
                "400-14" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to update the subaccount information was not provided.")
                "400-15" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to retrieve the journal book report was not provided.")
                "400-16" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to retrieve the general ledger report was not provided.")
                "400-17" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to retrieve the trial balance report was not provided.")
                "400-18" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to retrieve the worksheet report was not provided.")
                "400-19" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to retrieve the income statement report was not provided.")
                "400-20" -> ResponseCodeUtil(HttpStatus.BAD_REQUEST, "The required information to retrieve the balance sheet report was not provided.")
                "403-01" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have permission to view user information.")
                "403-02" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have permission to update user information.")
                "403-03" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have permission to view company information.")
                "403-04" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have permission to update company information.")
                "403-05" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have permission to view the accounting plan.")
                "403-06" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to create an account group.")
                "403-07" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to view the account group.")
                "403-08" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to update the account group.")
                "403-09" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to create an account subgroup.")
                "403-10" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to view the account subgroup.")
                "403-11" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to update the account subgroup.")
                "403-12" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to create an account.")
                "403-13" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to view the account.")
                "403-14" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to update the account.")
                "403-15" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to create a subaccount.")
                "403-16" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to view the subaccount.")
                "403-17" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to update the subaccount.")
                "403-18" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to update an attachment.")
                "403-19" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have permission to view the attachment.")
                "403-20" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to create a journal entry.")
                "403-21" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to request the journal book report.")
                "403-22" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to request the general ledger report.")
                "403-23" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to request the trial balance report.")
                "403-24" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to request the worksheet book report.")
                "403-25" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to request the income statement report.")
                "403-26" -> ResponseCodeUtil(HttpStatus.FORBIDDEN, "You don't have the necessary permissions to request the balance sheet report.")
                "404-01" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "User not found.")
                "404-02" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Accountant user not found.")
                "404-03" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Industry not found.")
                "404-04" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Business entity not found.")
                "404-05" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Company not found.")
                "404-06" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Account category not found.")
                "404-07" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Account group not found.")
                "404-08" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Account subgroup not found.")
                "404-09" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Account not found.")
                "404-10" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Subaccount not found.")
                "404-11" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Attachment not found.")
                "404-12" -> ResponseCodeUtil(HttpStatus.NOT_FOUND, "Document type not found.")
                "409-01" -> ResponseCodeUtil(HttpStatus.CONFLICT, "An accountant with the same email address already exists in the system.")
                "409-02" -> ResponseCodeUtil(HttpStatus.CONFLICT, "An accounting assistant with the same email address already exists in the company.")
                "409-03" -> ResponseCodeUtil(HttpStatus.CONFLICT, "A client with the same email address already exists in the company.")
                "409-04" -> ResponseCodeUtil(HttpStatus.CONFLICT, "A journal entry with the same number already exists in the system.")
                else -> ResponseCodeUtil(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.")
            }
        }
    }
}


