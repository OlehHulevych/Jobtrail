package com.huleych.jobtrail.identity.filters;

import com.huleych.jobtrail.identity.infrastructure.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;


@Component

public class JwtFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;

    public JwtFilter(TokenService tokenService, UserDetailsService userDetailsService, HandlerExceptionResolver handlerExceptionResolver){
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;

    }
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if(authHeader==null|| !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        try{
            final String jwt = authHeader.substring(7);
            final String userEmail  = tokenService.getUserEmail(jwt);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(userEmail != null && authentication == null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if(tokenService.tokenValid(jwt, userDetails)){
                    UsernamePasswordAuthenticationToken authToken =new UsernamePasswordAuthenticationToken(userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request,response,null,e);
            return;
        }
        filterChain.doFilter(request,response);
    }
}
