package com.studyolle.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.account.CurrentAccount;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import com.studyolle.domain.Tag;
import com.studyolle.tag.TagForm;
import com.studyolle.study.form.StudyDescriptionForm;
import com.studyolle.tag.TagRepository;
import com.studyolle.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingController {

	private final StudyService studyService;
	private final ModelMapper modelMapper;
	private final TagService tagService;
	private final TagRepository tagRepository;
	//private final ZoneRepository zoneRepository;
	private final ObjectMapper objectMapper;

	@GetMapping("/description")
	public String viewStudySetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
		Study study = studyService.getStudyToUpdate(account, path);
		model.addAttribute(account);
		model.addAttribute(study);
		model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
		return "study/settings/description";
	}

	@PostMapping("/description")
	public String updateStudyInfo(@CurrentAccount Account account, @PathVariable String path,
	                              @Valid StudyDescriptionForm studyDescriptionForm, Errors errors,
	                              Model model, RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdate(account, path);

		if (errors.hasErrors()) {
			// studyDescriptionForm, errors 는 attribute에 기본으로 들어감
			model.addAttribute(account);
			model.addAttribute(study);
			return "study/settings/description";
		}

		studyService.updateStudyDescription(study, studyDescriptionForm);
		attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
		return "redirect:/study/" + getPath(path) + "/settings/description";
	}

	@GetMapping("/banner")
	public String studyImageForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
		Study study = studyService.getStudyToUpdate(account, path);
		model.addAttribute(account);
		model.addAttribute(study);
		return "study/settings/banner";
	}

	@PostMapping("/banner")
	public String studyImageSubmit(@CurrentAccount Account account, @PathVariable String path,
	                               String image, RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdate(account, path);
		studyService.updateStudyImage(study, image);
		attributes.addFlashAttribute("message", "스터디 이미지를 수정했습니다.");
		return "redirect:/study/" + getPath(path) + "/settings/banner";
	}

	private String getPath(String path) {
		return URLEncoder.encode(path, StandardCharsets.UTF_8);
	}

	@PostMapping("/banner/enable")
	public String enableStudyBanner(@CurrentAccount Account account, @PathVariable String path) {
		Study study = studyService.getStudyToUpdate(account, path);
		studyService.enableStudyBanner(study);
		return "redirect:/study/" + getPath(path) + "/settings/banner";
	}

	@PostMapping("/banner/disable")
	public String disableStudyBanner(@CurrentAccount Account account, @PathVariable String path) {
		Study study = studyService.getStudyToUpdate(account, path);
		studyService.disableStudyBanner(study);
		return "redirect:/study/" + getPath(path) + "/settings/banner";
	}

	@GetMapping("/tags")
	public String studyTagForm(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
		Study study = studyService.getStudyToUpdate(account, path);
		model.addAttribute(account);
		model.addAttribute(study);

		// 해당 study에 존재하는 태그 list 가져오기
		model.addAttribute("tags", study.getTags().stream()
				.map(Tag::getTitle).collect(Collectors.toList()));

		// 전체 태그 list 가져오기 (뷰에서 태그 목록 보여줘야 하니까)
		List<String> allTagTitles = tagRepository.findAll().stream()
				.map(Tag::getTitle).collect(Collectors.toList());

		// object to json
		model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));

		return "study/settings/tags";
	}

	@PostMapping("/tags/add")
	@ResponseBody
	public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
		Study study = studyService.getStudyToUpdateTag(account, path);
		Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
		studyService.addTag(study, tag);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/tags/remove")
	@ResponseBody
	public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
		Study study = studyService.getStudyToUpdateTag(account, path);
		Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
		if(tag == null) {
			return ResponseEntity.badRequest().build();
		}

		studyService.removeTag(study, tag);
		return ResponseEntity.ok().build();
	}
}
