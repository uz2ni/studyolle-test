package com.studyolle.settings.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

// DTO Class
@Data
public class Profile {

	@Length(max = 35)
	private String bio;

	@Length(max = 50)
	private String url;

	@Length(max = 50)
	private String occupation;

	@Length(max = 50)
	private String location;

	private String profileImage;
}
