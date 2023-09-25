package ucb.accounting.backend.util

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest

class HttpUtil {

    companion object{
        private fun getRequestContext(): HttpServletRequest? {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            return requestAttributes?.request
        }

        fun getRequestIP(): String? {
            return getRequestContext()?.remoteAddr
        }

        fun getRequestHost(): String? {
            return getRequestContext()?.remoteHost
        }

        fun getRequestPort(): Int? {
            return getRequestContext()?.remotePort
        }

        fun getRequestMethod(): String? {
            return getRequestContext()?.method
        }

        fun getRequestUrl(): String {
            return getRequestContext()?.requestURL.toString()
        }

        fun getRequestUri(): String? {
            return getRequestContext()?.requestURI
        }
    }
}
