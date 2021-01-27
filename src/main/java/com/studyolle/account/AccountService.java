package com.studyolle.account;

import com.studyolle.account.form.SignUpForm;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.settings.form.Notifications;
import com.studyolle.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

	private final AccountRepository accountRepository;
	private final JavaMailSender javaMailSender;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;


	public Account processNewAccount(SignUpForm signUpForm) {
		Account newAccount = saveNewAccount(signUpForm); // --> transaction 일어난 상태 (save)
		//newAccount.generateEmailCheckToken(); // --> 토큰 생성 후는 따로 transaction이 일어나지 않는다. 메서드에 @Transaction 추가 필요
		sendSignUpConfirmMail(newAccount);
		return newAccount;
	}

	private Account saveNewAccount(@Valid SignUpForm signUpForm) {
		Account account = Account.builder()
				.email(signUpForm.getEmail())
				.nickname(signUpForm.getNickname())
				.password(passwordEncoder.encode(signUpForm.getPassword()))
				.studyCreatedByWeb(true)
				.studyEnrollmentResultByWeb(true)
				.studyUpdatedByWeb(true)
				// byEamil 설정값은 기본 false
				.build();

		return accountRepository.save(account); // -->  transaction 일어남. persist 상태
	}

	public void sendSignUpConfirmMail(Account newAccount) {
		newAccount.generateEmailCheckToken(); // 토큰 생성 위치 변경 (이유: 메일 재전송 시 토큰을 갱신하기 위함)
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(newAccount.getEmail());
		mailMessage.setSubject("스터디올래, 회원 가입 인증"); // 메일 제목
		mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail()); // 메일 본문
		javaMailSender.send(mailMessage);
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
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(account.getEmail());
		mailMessage.setSubject("스터디올래, 로그인 링크");
		mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() +
				"&email=" + account.getEmail());
		javaMailSender.send(mailMessage);
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
}
