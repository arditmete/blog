package com.blog.security;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@PropertySource("classpath:data.properties")
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private CustomLoginSuccessHandler sucessHandler;

	@Autowired
	private DataSource dataSource;

	@Value("${usersQuery}")
	private String usersQuery;

	@Value("${rolesQuery}")
	private String rolesQuery;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().usersByUsernameQuery(usersQuery).
		authoritiesByUsernameQuery(rolesQuery)
				.dataSource(dataSource).passwordEncoder(bCryptPasswordEncoder);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeRequests()
		.antMatchers("/").permitAll()
		.antMatchers("/login").permitAll()
		.antMatchers("/forgotPassword").permitAll()
		.antMatchers("/confirm-reset").permitAll()
		.antMatchers("/reset").permitAll()
		.antMatchers("/resetPassword").permitAll()
		.antMatchers("/addNewCategory").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/openCategory/{Id}").hasAnyAuthority("SITE_USER")
		.antMatchers("/openAuthorCategory/{Id}").hasAnyAuthority("SITE_AUTOR")
		.antMatchers("/deleteCategory").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/edit/{categoryId}").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/addComment").hasAnyAuthority("SITE_USER")
		.antMatchers("/pendingComments").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/verifiedComments/{commentId}").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/cancelComments/{commentId}").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/addNewPost").hasAnyAuthority("SITE_AUTOR")
		.antMatchers("/openAuthorPost/{postId}").hasAnyAuthority("SITE_AUTOR")
		.antMatchers("/openUserPost/{postId}").hasAnyAuthority("SITE_USER")
		.antMatchers("/savePostText").hasAnyAuthority("SITE_AUTOR")
		.antMatchers("/pendingPosts").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/pendingAuthorPosts").hasAnyAuthority("SITE_AUTOR")
		.antMatchers("//verifiedPosts/{postId}").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/cancelPosts/{postId}").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/superHighPost/{postId}").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/highPost/{postId}").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/mediumPost/{postId}").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/forgotPassword").permitAll()
		.antMatchers("/users").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/controlUser").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/editProfile").hasAnyAuthority("SITE_USER")
		.antMatchers("/changePassword").hasAnyAuthority("SITE_USER")
		.antMatchers("/updatePassword").hasAnyAuthority("SITE_USER")
		.antMatchers("/home/**").hasAnyAuthority("SITE_USER")
		.antMatchers("/adminHome/**").hasAnyAuthority("SITE_ADMIN")
		.antMatchers("/autorHome/**").hasAnyAuthority("SITE_AUTOR")
		.anyRequest().authenticated()
		.and()
				// form login
				.csrf().disable().formLogin()
				.loginPage("/login")
				.failureUrl("/login?error=true")
				.successHandler(sucessHandler)
				.usernameParameter("email")
				.passwordParameter("password")
				.and()
				// logout
				.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.logoutSuccessUrl("/login")
				.and()
				.exceptionHandling()
				.accessDeniedPage("/access-denied");
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
	}

}
