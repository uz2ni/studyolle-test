package com.studyolle.account;

import com.studyolle.account.form.SignUpForm;
import com.studyolle.config.AppProperties;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.mail.EmailMessage;
import com.studyolle.mail.EmailService;
import com.studyolle.settings.form.Notifications;
import com.studyolle.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

	private final AccountRepository accountRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	private final TemplateEngine templateEngine;
	private final AppProperties appProperties;


	public Account processNewAccount(SignUpForm signUpForm) { // 이 메서드가 트랜젝션 단위이므로, 내부 코드에서 에러가 일어나면 메서드 전체가 롤백됨!, newAccount 생성 안됨
		Account newAccount = saveNewAccount(signUpForm); // --> transaction 일어난 상태 (save)
		//newAccount.generateEmailCheckToken(); // --> 토큰 생성 후는 따로 transaction이 일어나지 않는다. 메서드에 @Transaction 추가 필요
		sendSignUpConfirmMail(newAccount);
		return newAccount;
	}

	private Account saveNewAccount(@Valid SignUpForm signUpForm) {
		signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
		Account account = modelMapper.map(signUpForm, Account.class);
		//account.generateEmailCheckToken();
		return accountRepository.save(account); // -->  transaction 일어남. persist 상태
	}

	public void sendSignUpConfirmMail(Account newAccount) {
		newAccount.generateEmailCheckToken(); // 토큰 생성 위치 변경 (이유: 메일 재전송 시 토큰을 갱신하기 위함)

		Context context = new Context();
		context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());
		context.setVariable("nickname", newAccount.getNickname());
		context.setVariable("linkName", "이메일 인증하기");
		context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
		context.setVariable("host", appProperties.getHost());
		String message = templateEngine.process("mail/simple-link", context);

		EmailMessage emailMessage = EmailMessage.builder()
				.to(newAccount.getEmail())
				.subject("스터디올래, 회원 가입 인증")
				.message(message)
				.build();

		emailService.sendEmail(emailMessage);
	}

	public void login(Account account) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken( // 변형된 방법
				new UserAccount(account), // after. Principal 객체 변경
				account.getPassword(), // 인코딩된 password
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(token);

		/* 정석적인 방법
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password); // 사용자가 입력한 password
		Authentication authentication = authenticationManager.authenticate(token); // 매니저를 통해 인증을 거친 토큰을 넣어준다.
		SecurityContextHolder.getContext().setAuthentication(authentication);
		*/
	}

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException { // login 시 실행됨
		Account account = accountRepository.findByEmail(emailOrNickname);
		if(account == null) {
			account = accountRepository.findByNickname(emailOrNickname);
		}

		if(account == null) {
			throw new UsernameNotFoundException(emailOrNickname);
		}
		System.out.println("account:");
		System.out.println(account.isEmailVerified());
		return new UserAccount(account); // Principal 객체 생성
	}

	public void completeSignUp(Account account) {
		account.completeSignUp(); // --> 영속성 컨텍스트에 존재하는 범위가 아니다. (Entity의 값만 변경되었음) --> Service 안으로 코드 변경
		login(account);
	}

	public void updateProfile(Account account, Profile profile) {
		modelMapper.map(profile, account);
		accountRepository.save(account); // id가 있는지 없는지 판단하여 있으면 merge(update) 시킨다.
		// TODO 문제가 하나 더 남았습니다.
	}

	public void updatePassword(Account account, String newPassword) {
		account.setPassword(passwordEncoder.encode(newPassword));
		accountRepository.save(account); // merge
	}

	public void updateNotifications(Account account, Notifications notifications) {
		modelMapper.map(notifications, account);
		accountRepository.save(account);
	}

	public void updateNickname(Account account, String nickname) {
		account.setNickname(nickname);
		accountRepository.save(account);
		login(account);
	}

	public void sendLoginLink(Account account) {
		account.generateEmailCheckToken();

		Context context = new Context();
		context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
				"&email=" + account.getEmail());
		context.setVariable("nickname", account.getNickname());
		context.setVariable("linkName", "스터디올래 로그인하기");
		context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
		context.setVariable("host", appProperties.getHost());
		String message = templateEngine.process("mail/simple-link", context);

		EmailMessage emailMessage = EmailMessage.builder()
				.to(account.getEmail())
				.subject("스터디올래, 로그인 링크")
				.message(message)
				.build();

		emailService.sendEmail(emailMessage);
	}

	public void addTag(Account account, Tag tag) {
		Optional<Account> byId = accountRepository.findById(account.getId());
		byId.ifPresent(a -> a.getTags().add(tag));
	}

	public Set<Tag> getTags(Account account) {
		Optional<Account> byId = accountRepository.findById(account.getId());
		return byId.orElseThrow().getTags();
	}

	public void removeTag(Account account, Tag tag) {
		Optional<Account> byId = accountRepository.findById(account.getId());
		byId.ifPresent(a -> a.getTags().remove(tag));
	}


	public Set<Zone> getZones(Account account) {
		Optional<Account> byId = accountRepository.findById(account.getId());
		return byId.orElseThrow().getZones();
	}

	public void addZone(Account account, Zone zone) {
		Optional<Account> byId = accountRepository.findById(account.getId());
		byId.ifPresent(a -> a.getZones().add(zone));
	}

	public void removeZone(Account account, Zone zone) {
		Optional<Account> byId = accountRepository.findById(account.getId());
		byId.ifPresent(a -> a.getZones().remove(zone));
	}

	public Account getAccount(String nickname) {
		Account account = accountRepository.findByNickname(nickname);
		if (account == null) {
			throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
		}
		return account;
	}
}
