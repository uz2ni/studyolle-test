package com.studyolle.event;

import com.studyolle.modules.account.Account;
import com.studyolle.modules.study.Study;
import com.studyolle.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

	private final EventRepository eventRepository;
	private final ModelMapper modelMapper;
	private final EnrollmentRepository enrollmentRepository;

	public Event createEvent(Event event, Study study, Account account) {
		event.setCreatedBy(account);
		event.setCreatedDateTime(LocalDateTime.now());
		event.setStudy(study);
		return eventRepository.save(event);
	}

	public void updateEvent(Event event, EventForm eventForm) {
		modelMapper.map(eventForm, event);
		// TODO 모집 인원을 늘린 선착순 모임의 경우에, 자동으로 추가 인원의 참가 신청을 확정 상태로 변경해야 한다. (나중에 할 일)
	}

	public void deleteEvent(Event event) {
		eventRepository.delete(event);
	}

	public void newEnrollment(Event event, Account account) {
		if(!enrollmentRepository.existsByEventAndAccount(event, account)) { // 해당 모임에 현재 계정이 참가 신청하지 않은 상태 (=참가 신청 가능)
			Enrollment enrollment = new Enrollment();
			enrollment.setEnrolledAt(LocalDateTime.now());
			enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment()); // 선착순 모집이고 자리 남은 경우 true
			enrollment.setAccount(account);
			event.addEnrollment(enrollment);
			enrollmentRepository.save(enrollment);
		}
	}

	public void cancelEnrollment(Event event, Account account) {
		Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
		if (!enrollment.isAttended()) {
			event.removeEnrollment(enrollment);
			enrollmentRepository.delete(enrollment);
			event.acceptNextWaitingEnrollment();
		}
	}

	public void acceptEnrollment(Event event, Enrollment enrollment) {
		event.accept(enrollment);
	}

	public void rejectEnrollment(Event event, Enrollment enrollment) {
		event.reject(enrollment);
	}

	public void checkInEnrollment(Enrollment enrollment) {
		enrollment.setAttended(true);
	}

	public void cancelCheckInEnrollment(Enrollment enrollment) {
		enrollment.setAttended(false);
	}
}