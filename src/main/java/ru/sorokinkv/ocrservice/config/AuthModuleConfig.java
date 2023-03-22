// package ru.sorokinkv.ocrservice.config;
//
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
// import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
//
//
// /**
//  * AuthModuleConfig class.
//  * {@link AuthModuleConfig}
//  *
//  * @author Konstantin Sorokin
//  */
//
// @Configuration
// public class AuthModuleConfig {
//
//     @Value("${token.expiration.minutes}")
//     private Integer tokenExpirationMinutes;
//
//     @Value("${token.issuer}")
//     private String tokenIssuer;
//
//     @Value("${token.secret}")
//     private String tokenSecret;
//
//     @Value("${token.header}")
//     private String tokenHeader;
//
//     @Value("${token.prefix}")
//     private String tokenPrefix;
//
//     @Value("${token.audience}")
//     private String tokenAudience;
//
//     @Value("${token.type}")
//     private String tokenType;
//
//     @Value("${role.hierarchy}")
//     private String hierarchy;
//
//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }
//
//     /**
//      * RoleHierarchy bean.
//      *
//      * @return
//      */
//     @Bean
//     public RoleHierarchy roleHierarchy() {
//         RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
//         roleHierarchy.setHierarchy(hierarchy);
//         return roleHierarchy;
//     }
//
//     public Integer getTokenExpirationMinutes() {
//         return tokenExpirationMinutes;
//     }
//
//     public void setTokenExpirationMinutes(Integer tokenExpirationMinutes) {
//         this.tokenExpirationMinutes = tokenExpirationMinutes;
//     }
//
//     public String getTokenIssuer() {
//         return tokenIssuer;
//     }
//
//     public void setTokenIssuer(String tokenIssuer) {
//         this.tokenIssuer = tokenIssuer;
//     }
//
//     public String getTokenSecret() {
//         return tokenSecret;
//     }
//
//     public void setTokenSecret(String tokenSecret) {
//         this.tokenSecret = tokenSecret;
//     }
//
//     public String getTokenHeader() {
//         return tokenHeader;
//     }
//
//     public void setTokenHeader(String tokenHeader) {
//         this.tokenHeader = tokenHeader;
//     }
//
//     public String getTokenPrefix() {
//         return tokenPrefix;
//     }
//
//     public void setTokenPrefix(String tokenPrefix) {
//         this.tokenPrefix = tokenPrefix;
//     }
//
//     public String getTokenAudience() {
//         return tokenAudience;
//     }
//
//     public void setTokenAudience(String tokenAudience) {
//         this.tokenAudience = tokenAudience;
//     }
//
//     public String getTokenType() {
//         return tokenType;
//     }
//
//     public void setTokenType(String tokenType) {
//         this.tokenType = tokenType;
//     }
// }
