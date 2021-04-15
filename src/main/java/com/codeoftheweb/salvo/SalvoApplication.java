package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository,
                                      GameRepository gameRepository,
                                      GamePlayerRepository gPRepository,
                                      ShipRepository shipRepository,
                                      SalvoRepository salvoRepository,
                                      ScoreRepository scoreRepository) {
		return (args) -> {
			Player player1 = playerRepository.save(new Player("Jack@hotmail.com", passwordEncoder().encode("24")));
			Player player2 = playerRepository.save(new Player("Chloe@hotmail.com", passwordEncoder().encode("42")));
			Player player3 = playerRepository.save(new Player("Kim@hotmail.com", passwordEncoder().encode("kb")));
			Player player4 = playerRepository.save(new Player("David@hotmail.com", passwordEncoder().encode("mole")));
			Player player5 = playerRepository.save(new Player("KimChi@hotmail.com", passwordEncoder().encode("20")));
			Player player6 = playerRepository.save(new Player("MickeyMouse@hotmail.com", passwordEncoder().encode("40")));

			Game game1 = gameRepository.save(new Game(LocalDateTime.now()));
			Game game2 = gameRepository.save(new Game(LocalDateTime.now().plusHours(2)));
			Game game3 = gameRepository.save(new Game(LocalDateTime.now().plusHours(3)));

			GamePlayer gm1 = gPRepository.save(new GamePlayer(game1, player1));
			GamePlayer gm2 = gPRepository.save(new GamePlayer(game1, player2));

			GamePlayer gm3 = gPRepository.save(new GamePlayer(game2, player3));
			GamePlayer gm4 = gPRepository.save(new GamePlayer(game2, player4));

			GamePlayer gm5 = gPRepository.save(new GamePlayer(game3, player5));
			GamePlayer gm6 = gPRepository.save(new GamePlayer(game3, player6));

			List<String> ubication;
			Ship ship1 = shipRepository.save(new Ship("destroyer", gm1, ubication = Arrays.asList("H2", "H3", "H4")));
			Ship ship2 = shipRepository.save(new Ship("submarine", gm1, ubication = Arrays.asList("E1", "F1", "G1")));
			Ship ship3 = shipRepository.save(new Ship("patrolboat", gm1, ubication = Arrays.asList("F2", "F3")));

			Ship ship5 = shipRepository.save(new Ship("destroyer", gm2, ubication = Arrays.asList("B5", "C5", "D5")));
			Ship ship4 = shipRepository.save(new Ship("submarine", gm2, ubication = Arrays.asList("A10", "A9", "A8")));
			Ship ship6 = shipRepository.save(new Ship("patrolboat", gm2, ubication = Arrays.asList("J7", "J8")));

			Ship ship7 = shipRepository.save(new Ship("destroyer", gm3, ubication = Arrays.asList("C1", "C2", "C3")));
			Ship ship8 = shipRepository.save(new Ship("submarine", gm3, ubication = Arrays.asList("I8", "I9", "I10")));
			Ship ship9 = shipRepository.save(new Ship("patrolboat", gm3, ubication = Arrays.asList("E10", "F10")));

			Ship ship10 = shipRepository.save(new Ship("destroyer", gm4, ubication = Arrays.asList("H1", "H2", "H3")));
			Ship ship11 = shipRepository.save(new Ship("submarine", gm4, ubication = Arrays.asList("J5", "J6", "J7")));
			Ship ship12 = shipRepository.save(new Ship("patrolboat", gm4, ubication = Arrays.asList("A1", "A2")));

			Ship ship13 = shipRepository.save(new Ship("destroyer", gm5, ubication = Arrays.asList("D1", "D2", "D3")));
			Ship ship14 = shipRepository.save(new Ship("submarine", gm5, ubication = Arrays.asList("B8", "B9", "B10")));
			Ship ship15 = shipRepository.save(new Ship("patrolboat", gm5, ubication = Arrays.asList("F10", "F9")));

			Ship ship16 = shipRepository.save(new Ship("destroyer", gm6, ubication = Arrays.asList("C7", "C8", "C9")));
			Ship ship17 = shipRepository.save(new Ship("submarine", gm6, ubication = Arrays.asList("J1", "J2", "J3")));
			Ship ship18 = shipRepository.save(new Ship("patrolboat", gm6, ubication = Arrays.asList("H1", "H2")));

			Salvo salvo1 = salvoRepository.save(new Salvo(1, ubication = Arrays.asList("A1", "E7"), gm1));
			Salvo salvo3 = salvoRepository.save(new Salvo(2, ubication = Arrays.asList("F7"), gm1));

			Salvo salvo2 = salvoRepository.save(new Salvo(1, ubication = Arrays.asList("C3", "F1"), gm2));
			Salvo salvo4 = salvoRepository.save(new Salvo(2, ubication = Arrays.asList("G6"), gm2));

			Salvo salvo5 = salvoRepository.save(new Salvo(1, ubication = Arrays.asList("G9", "B3"), gm3));
			Salvo salvo6 = salvoRepository.save(new Salvo(2, ubication = Arrays.asList("C5"), gm3));

			Salvo salvo7 = salvoRepository.save(new Salvo(1, ubication = Arrays.asList("E10", "F10"), gm4));
			Salvo salvo8 = salvoRepository.save(new Salvo(2, ubication = Arrays.asList("H2"), gm4));

			Salvo salvo9 = salvoRepository.save(new Salvo(1, ubication = Arrays.asList("H1", "H2"), gm5));
			Salvo salvo10 = salvoRepository.save(new Salvo(2, ubication = Arrays.asList("A2"), gm5));

			Salvo salvo11 = salvoRepository.save(new Salvo(1, ubication = Arrays.asList("F10", "C3"), gm6));
			Salvo salvo12 = salvoRepository.save(new Salvo(2, ubication = Arrays.asList("E7"), gm6));

			Score score1 = scoreRepository.save(new Score(game1, player1, LocalDateTime.now(), 0.5));
			Score score2 = scoreRepository.save(new Score(game1, player2, LocalDateTime.now(), 1.0));

			Score score3 = scoreRepository.save(new Score(game2, player3, LocalDateTime.now(), 0.0));
			Score score4 = scoreRepository.save(new Score(game2, player4, LocalDateTime.now(), 1.0));

			Score score5 = scoreRepository.save(new Score(game3, player5, LocalDateTime.now(), 1.0));
			Score score6 = scoreRepository.save(new Score(game3, player6, LocalDateTime.now(), 1.0));
		};
	}
}
	@Configuration
	class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

		@Autowired
		PlayerRepository playerRepository;

		@Override
		public void init(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(inputName -> {
				Player player = playerRepository.findByUserName(inputName);
				if (player != null) {
					return new User(player.getUserName(), player.getPassword(),
							AuthorityUtils.createAuthorityList("USER"));
				} else {
					throw new UsernameNotFoundException("Unknown user: " + inputName);
				}
			});
		}
	}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/web/**","/api/login","/api/logout","/api/players").permitAll()
				.antMatchers("/api/game_view/**").hasAuthority("USER")
				.antMatchers("/h2-console/**").permitAll()
				.antMatchers("/api/games").permitAll()
				.anyRequest().authenticated()
				.and().csrf().ignoringAntMatchers("/h2-console/**")
				.and().headers().frameOptions().sameOrigin();
		http.formLogin()
				.usernameParameter("name")
				.passwordParameter("pwd")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}

