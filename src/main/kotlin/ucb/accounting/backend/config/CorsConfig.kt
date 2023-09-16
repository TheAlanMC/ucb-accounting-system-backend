package ucb.accounting.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {

    @Bean
    fun corsFilter(): CorsFilter {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowCredentials = false
        corsConfiguration.allowedOrigins = listOf("*")
//        corsConfiguration.allowedOrigins = listOf(
//            "http://localhosts:4200",
//            "http://uas-frontend:4200",
//            "http://68.183.126.58:4200",
//            "http://localhost",
//            "http://uas-frontend",
//            "http://68.183.126.58",
//            )
        corsConfiguration.allowedHeaders=listOf(
            "Origin",
            "Access-Control-Allow-Origin",
            "Content-Type", "Accept", "Authorization",
            "Origin, Accept", "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        )

        corsConfiguration.exposedHeaders = listOf(
            "Origin", "Content-Type",
            "Accept", "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        )
        corsConfiguration.allowedMethods = listOf(
            "GET", "POST", "PUT", "DELETE")
        val urlBasedCorsConfigurationSource = UrlBasedCorsConfigurationSource()
        urlBasedCorsConfigurationSource
            .registerCorsConfiguration ("/**", corsConfiguration)
        return CorsFilter (urlBasedCorsConfigurationSource)
    }
}