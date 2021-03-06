package com.rodrigol.hotel.security.filter

import com.rodrigol.hotel.security.config.HEADER_STRING_AUTH
import com.rodrigol.hotel.security.config.SECRET
import com.rodrigol.hotel.security.config.TOKEN_PREFIX
import io.jsonwebtoken.Jwts
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.util.Collections.emptyList
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthorizationFilter(authManager: AuthenticationManager) : BasicAuthenticationFilter(authManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader(HEADER_STRING_AUTH)
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response)
            return
        }

        val authentication = getAuthentication(request)

        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(request, response)
    }

    private fun getAuthentication(request: HttpServletRequest): Authentication? {
        val token = request.getHeader(HEADER_STRING_AUTH)
        if (token != null) {
            // parse the token.
            val user = Jwts.parser()
                                .setSigningKey(SECRET)
                                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                                .body
                                .subject
            return if (user != null)
                UsernamePasswordAuthenticationToken(user, null, emptyList<GrantedAuthority>())
            else
                null
        }
        return null
    }

}