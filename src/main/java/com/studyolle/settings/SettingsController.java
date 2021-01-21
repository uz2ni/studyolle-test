package com.studyolle.settings;

import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {

	static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
	static final String SETTINGS_PROFILE_URL = "/settings/profile";

	private final AccountService accountService;

	@GetMapping(SETTINGS_PROFILE_URL)
	public String profileUpdateForm(@CurrentUser Account account, Model model) {
		model.addAttribute(account);
		model.addAttribute(new Profile(account));
		return SETTINGS_PROFILE_VIEW_NAME;
	}

	@PostMapping(SETTINGS_PROFILE_URL)
	public String updateProfile(@CurrentUser Account account, @Valid Profile profile, Errors errors,
	                            Model model, RedirectAttributes attributes) { // 여기서 account는 Principal 객체임. detached 상태이다. 트랜잭션이 끝난지 오래지만 id는 가지고 있음.
		if(errors.hasErrors()) {
			model.addAttribute(account);
			return SETTINGS_PROFILE_VIEW_NAME;
		}
		accountService.updateProfile(account, profile);
		attributes.addFlashAttribute("message", "프로필을 수정했습니다."); // 한번 쓰고 사라질 속성. 리다이렉트된 메서드의 model로 들어간다.
		return "redirect:" + SETTINGS_PROFILE_URL;
	}
}
