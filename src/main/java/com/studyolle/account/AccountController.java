package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

	private final SignUpFormValidator signUpFormValidator;
	private final AccountService accountService;
	private final AccountRepository accountRepository;

	@InitBinder("signUpForm")
	public void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.addValidators(signUpFormValidator);
	}

	@GetMapping("/sign-up")
	public String signUpForm(Model model) {
		model.addAttribute(new SignUpForm()); // "signUpForm" (class명과 첫번째 파라미터가 카멜표기법으로 같으면 첫 파라미터 생략 가능)
		return "account/sign-up"; // TimeLeaf가 resources/templates 하위의 해당 문자열의 위치를 view로 찾음
	}

	@PostMapping("/sign-up")
	public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
		if(errors.hasErrors()) {
			return "account/sign-up";
		}

		Account account = accountService.processNewAccount(signUpForm);
		accountService.login(account); // login은 가능하지만 토큰인증이 되지 않은 상태

		// TODO 회원 가입 처리
		return "redirect:/";
	}

	@GetMapping("/check-email-token")
	public String checkEmailToken(String token, String email, Model model) {
		Account account = accountRepository.findByEmail(email);
		String view = "account/checked-email";
		// 에러 처리
		if(account == null) {
			model.addAttribute("error","wrong.email");
			return view;
		}

		if(!account.isValidToken(token)) {
			model.addAttribute("error","wrong.token");
			return view;
		}

		// 정상 처리
		account.completeSignUp();
		accountService.login(account);

		model.addAttribute("numberOfUser",accountRepository.count());
		model.addAttribute("nickname",account.getNickname());
		return view;
	}


}
