package com.studyolle.modules.settings.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PasswordForm {

	@Length(min = 8, max = 50)
	String newPassword;

	@Length(min = 8, max = 50)
	String newPasswordConfirm;
}
