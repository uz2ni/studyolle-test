package com.studyolle.settings.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class NicknameForm {

	@NotBlank                   // 비어있으면 안됨
	@Length(min = 3, max = 20)  // 길이 제한
	@Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$") // 닉네임 문자 범위
	private String nickname;

}
