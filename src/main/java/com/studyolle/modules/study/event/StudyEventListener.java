package com.studyolle.modules.study.event;

import com.studyolle.modules.account.AccountRepository;
import com.studyolle.modules.study.Study;
import com.studyolle.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

	private final StudyRepository studyRepository;
	private final AccountRepository accountRepository;

	@EventListener
	public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
		Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
		study.getZones();
		study.getTags();
	}
}