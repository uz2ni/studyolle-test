package com.studyolle.settings;

import com.studyolle.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

// DTO Class
@Data
@NoArgsConstructor // Controller에서 @ModelAttrubute로 모델 객체를 받을 때 빈생성자를 생성해야하기 때문에 필요함.
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

	public Profile(Account account) {
		this.bio = account.getBio();
		this.url = account.getUrl();
		this.occupation = account.getOccupation();
		this.location = account.getLocation();
		this.profileImage = account.getProfileImage();
	}
}
