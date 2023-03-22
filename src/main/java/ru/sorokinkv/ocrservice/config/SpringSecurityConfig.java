// package ru.sorokinkv.ocrservice.config;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.boot.context.properties.EnableConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
//
// /**
//  * SpringSecurityConfig class.
//  */
// @Slf4j
// @Configuration
// @EnableWebSecurity
// @RequiredArgsConstructor
// @EnableConfigurationProperties
// public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
//
//     private final AuthModuleConfig authModuleConfig;
//
//     @Override
//     protected void configure(final HttpSecurity http) throws Exception {
//         http
//                 .csrf().disable().cors().and()
//                 .authorizeRequests()
//                 .expressionHandler(defaultWebSecurityExpressionHandler())
//                 .antMatchers("/", "/v2/api-docs", "/v3/api-docs",
//                         "/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "/actuator/**",
//                         "/swagger-ui/index.html", "/swagger-ui/**", "/swagger-resources", "/swagger-ui"
//                 ).permitAll()
//                 .anyRequest().authenticated().and()
//                 .addFilter(new JwtAuthorizationFilter(authenticationManager(), authModuleConfig))
//                 .sessionManagement()
//                 .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//     }
//
//     /**
//      * defaultWebSecurityExpressionHandler bean.
//      *
//      * @return
//      */
//     @Bean
//     public DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler() {
//         DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
//         expressionHandler.setRoleHierarchy(authModuleConfig.roleHierarchy());
//         return expressionHandler;
//     }
//
// }
