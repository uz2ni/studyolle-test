package com.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator { // 커스텀 유효값 검사

	private final AccountRepository accountRepository;

	@Override
	public boolean supports(Class<?> aClass) {
		return aClass.isAssignableFrom(SignUpForm.class); // formType의 인스턴스를 검사
	}

	@Override
	public void validate(Object o, Errors errors) {
		// TODO email, nickname
		SignUpForm signUpForm = (SignUpForm)o;
		if(accountRepository.existsByEmail(signUpForm.getEmail())) {
			errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
		}

		if(accountRepository.existsByNickname(signUpForm.getNickname())) {
			errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임입니다.");
		}
	}
}
