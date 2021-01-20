package com.studyolle.settings;

import com.studyolle.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO Class
@Data
@NoArgsConstructor // Controller에서 @ModelAttrubute로 모델 객체를 받을 때 빈생성자를 생성해야하기 때문에 필요함.
public class Profile {

	private String bio;

	private String url;

	private String occupation;

	private String location;

	public Profile(Account account) {
		this.bio = account.getBio();
		this.url = account.getUrl();
		this.occupation = account.getOccupation();
		this.location = account.getLocation();
	}
}
