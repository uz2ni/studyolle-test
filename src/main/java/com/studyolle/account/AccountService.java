package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;
	private final JavaMailSender javaMailSender;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void processNewAccount(SignUpForm signUpForm) {
		Account newAccount = saveNewAccount(signUpForm); // --> transaction 일어난 상태 (save)
		newAccount.generateEmailCheckToken(); // --> 토큰 생성 후는 따로 transaction이 일어나지 않는다. 메서드에 @Transaction 추가 필요
		sendSignUpConfirmMail(newAccount);
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

	private void sendSignUpConfirmMail(Account newAccount) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(newAccount.getEmail());
		mailMessage.setSubject("스터디올래, 회원 가입 인증"); // 메일 제목
		mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail()); // 메일 본문
		javaMailSender.send(mailMessage);
	}
}
