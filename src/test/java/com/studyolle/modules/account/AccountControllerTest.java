package com.studyolle.modules.account;

import com.studyolle.infra.mail.EmailMessage;
import com.studyolle.infra.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

	@Autowired private MockMvc mockMvc;

	@Autowired
	AccountRepository accountRepository;

	@MockBean
//	JavaMailSender javaMailSender;
	EmailService emailService;

	@DisplayName("인증 메일 확인 - 입력값 오류")
	@Test
	void checkEmailToken_with_wrong_input() throws Exception {
		mockMvc.perform(get("/check-email-token")
				.param("token","dkjfsljflsdj")
				.param("email","email@email.com"))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("error"))
				.andExpect(view().name("account/checked-email"))
				.andExpect(unauthenticated());
	}

	@DisplayName("인증 메일 확인 - 입력값 정상")
	@Test
	void checkEmailToken() throws Exception {
		Account account = Account.builder()
				.email("test@email.com")
				.password("12345678")
				.nickname("yujin")
				.build();
		Account newAccount = accountRepository.save(account);
		newAccount.generateEmailCheckToken();

		mockMvc.perform(get("/check-email-token")
				.param("token",newAccount.getEmailCheckToken())
				.param("email",newAccount.getEmail()))
				.andExpect(status().isOk())
				.andExpect(model().attributeDoesNotExist("error"))
				.andExpect(model().attributeExists("nickname"))
				.andExpect(model().attributeExists("numberOfUser"))
				.andExpect(view().name("account/checked-email"))
				.andExpect(authenticated().withUsername("yujin"));
	}

	@DisplayName("회원 가입 화면 보이는지 테스트")
	@Test
	void signUpForm() throws Exception {
		mockMvc.perform(get("/sign-up"))
				.andDo(print()) // view 실제로 보여줌 (콘솔)
				.andExpect(status().isOk())
				.andExpect(view().name("account/sign-up"))
				.andExpect(model().attributeExists("signUpForm"));
	}

	@DisplayName("회원 가입 처리 - 입력값 오류")
	@Test
	void signUpSubmit_with_wrong_input() throws Exception {
		mockMvc.perform(post("/sign-up")
				.param("nickname","yuja")
				.param("email","email..")
				.param("password","12345")
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("account/sign-up"))
				.andExpect(unauthenticated());

	}

	@DisplayName("회원 가입 처리 - 입력값 정상")
	@Test
	void signUpSubmit_with_correct_input() throws Exception {
		mockMvc.perform(post("/sign-up")
				.param("nickname","yuja")
				.param("email","yuja@naver.com")
				.param("password","12345678")
				.with(csrf()))
				.andExpect(status().is3xxRedirection()) //리다이렉션
				.andExpect(view().name("redirect:/"))
				.andExpect(authenticated());

		Account account = accountRepository.findByEmail("yuja@naver.com");
		assertNotNull(account);
		assertNotEquals(account.getPassword(), "12345678"); //입력과 저장값이 다른지 확인
		System.out.println("encoding password: " + account.getPassword());

		assertTrue(accountRepository.existsByEmail("yuja@naver.com"));
		then(emailService).should().sendEmail(any(EmailMessage.class)); // 아무 메일을 보냈다.
	}

}
