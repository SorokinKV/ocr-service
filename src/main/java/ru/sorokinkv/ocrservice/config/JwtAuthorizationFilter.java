// package ru.sorokinkv.ocrservice.config;
//
// import java.io.IOException;
// import java.util.List;
// import java.util.stream.Collectors;
// import javax.servlet.FilterChain;
// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
//
// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jws;
// import io.jsonwebtoken.JwtException;
// import io.jsonwebtoken.Jwts;
// import io.micrometer.core.instrument.util.StringUtils;
// import lombok.extern.slf4j.Slf4j;
// import org.slf4j.MDC;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
// import ru.sorokinkv.ocrservice.exception.NotAuthorizedException;
//
//
// /**
//  * The <code>JwtAuthorizationFilter</code> is a JwtAuthorizationFilter
//  * This method are implementations such as {@link BasicAuthenticationFilter }.
//  *
//  *
//  * <p/>
//  *
//  * @author Konstantin Sorokin
//  */
//
// @Slf4j
// public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
//
//     private final ru.sorokinkv.ocrservice.config.AuthModuleConfig authModuleConfig;
//
//     public JwtAuthorizationFilter(AuthenticationManager authenticationManager, AuthModuleConfig authModuleConfig) {
//         super(authenticationManager);
//         this.authModuleConfig = authModuleConfig;
//     }
//
//
//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                     FilterChain filterChain) throws IOException, ServletException {
//         String idToken = request.getHeader("X-Request-UUID");
//         try {
//             UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
//             if (authentication != null) {
//                 SecurityContextHolder.getContext().setAuthentication(authentication);
//             }
//
//             if (idToken != null) {
//                 log.info("положили X-Request-UUID");
//                 MDC.put("X-Request-UUID", idToken);
//                 response.addHeader("X-Request-UUID", idToken);
//             } else {
//                 log.warn("X-Request-UUID не обнаружен.Кладем замену: " + "xxxxx");
//                 MDC.put("X-Request-UUID", "xxxxx");
//             }
//             filterChain.doFilter(request, response);
//         } finally {
//             log.info("сделали очистку X-Request-UUID");
//             MDC.clear();
//         }
//
//     }
//
//     private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
//         String token = request.getHeader(authModuleConfig.getTokenHeader());
//         if (StringUtils.isNotEmpty(token) && token.startsWith(authModuleConfig.getTokenPrefix())) {
//             try {
//
//                 Jws<Claims> parsedToken = Jwts.parser()
//                         .setSigningKey(authModuleConfig.getTokenSecret().getBytes())
//                         .parseClaimsJws(token.replace(authModuleConfig.getTokenPrefix(), ""));
//
//                 String username = parsedToken
//                         .getBody()
//                         .getSubject();
//
//                 List<SimpleGrantedAuthority> authorities = ((List<?>) parsedToken.getBody()
//                         .get("rol")).stream()
//                         .map(authority -> new SimpleGrantedAuthority((String) authority))
//                         .collect(Collectors.toList());
//
//                 if (StringUtils.isNotEmpty(username)) {
//                     return new UsernamePasswordAuthenticationToken(username, null, authorities);
//                 }
//             } catch (JwtException | IllegalArgumentException e) {
//                 throw new NotAuthorizedException();
//             }
//         }
//
//         return null;
//     }
// }
