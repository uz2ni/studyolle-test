package com.studyolle.modules.account.validate;

import com.studyolle.modules.account.AccountRepository;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.account.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

	private final AccountRepository accountRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		return NicknameForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		NicknameForm nicknameForm =(NicknameForm)target;  // form객체 먼저 검사 -> 닉네임 중복 검사(아래 코드)
		Account byNickname = accountRepository.findByNickname(nicknameForm.getNickname());
		if(byNickname != null) {
			errors.rejectValue("nickname","wrong.value","입력하신 닉네임을 사용할 수 없습니다.");
		}
	}
}
