package com.oasystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String TOKEN_CACHE_PREFIX = "oa:token:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        log.info("=== JWT Filter === 请求路径: {}, Token存在: {}", request.getRequestURI(), StringUtils.hasText(token));

        if (StringUtils.hasText(token)) {
            try {
                // 验证Token
                boolean valid = jwtUtils.validateToken(token);
                log.info("=== JWT Filter === Token验证结果: {}", valid);
                
                if (valid) {
                    // 直接从Token解析用户信息
                    Long userId = jwtUtils.getUserId(token);
                    String username = jwtUtils.getUsername(token);
                    String roleCode = jwtUtils.getRoleCode(token);
                    log.info("=== JWT Filter === 从Token解析用户: userId={}, username={}, roleCode={}", userId, username, roleCode);

                    LoginUser loginUser = new LoginUser();
                    loginUser.setUserId(userId);
                    loginUser.setUsername(username);
                    loginUser.setRoleCode(roleCode);
                    loginUser.setStatus(1);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("=== JWT Filter === 认证信息设置完成，权限: {}", loginUser.getAuthorities());
                }
            } catch (Exception e) {
                log.error("=== JWT Filter === Token验证失败: {}", e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
