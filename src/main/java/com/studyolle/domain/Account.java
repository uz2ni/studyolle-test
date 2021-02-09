package com.studyolle.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id") // id만 사용하는 이유 ? -> 연관관계가 복잡해질 때
											  // 서로간의 엔티티를 참조하면 순환참조가 발생하고, 스택오버플로우 발생할 수 있다.
											  // 그래서 id만 주요 사용한다.
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

	@Id @GeneratedValue
	private Long id;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String nickname;

	private String password;

	private boolean emailVerified;

	private String emailCheckToken;

	private LocalDateTime emailCheckTokenGeneratedAt;

	private LocalDateTime joinedAt;

	/* 프로필 */
	private String bio;

	private String url;

	private String occupation;

	private String location;

	@Lob @Basic(fetch = FetchType.EAGER)
	private String profileImage;

	/* 알림 설정 값 체크 */
	private boolean studyCreatedByEmail;

	@Builder.Default
	private boolean studyCreatedByWeb = true;

	private boolean studyEnrollmentResultByEmail;

	@Builder.Default
	private boolean studyEnrollmentResultByWeb = true;

	private boolean studyUpdatedByEmail;

	@Builder.Default
	private boolean studyUpdatedByWeb = true;

	@ManyToMany
	private Set<Tag> tags = new HashSet<>();

	@ManyToMany
	private Set<Zone> zones = new HashSet<>();

	public void generateEmailCheckToken() {
		this.emailCheckToken = UUID.randomUUID().toString();
		this.emailCheckTokenGeneratedAt = LocalDateTime.now();
	}

	public void completeSignUp() {
		this.emailVerified = true;
		this.joinedAt = LocalDateTime.now();
	}

	public boolean isValidToken(String token) {
		return this.emailCheckToken.equals(token);
	}

	public boolean canSendConfirmEmail() {
		return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
	}

	public boolean isManagerOf(Study study) {
		return study.getManagers().contains(this);
	}
}
