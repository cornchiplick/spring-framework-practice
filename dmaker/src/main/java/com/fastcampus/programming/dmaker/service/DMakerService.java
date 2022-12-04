package com.fastcampus.programming.dmaker.service;

import static com.fastcampus.programming.dmaker.exception.DMakerErrorCode.DUPLICATED_MEMBER_ID;
import static com.fastcampus.programming.dmaker.exception.DMakerErrorCode.LEVEL_EXPERIENCE_YEARS_NOT_MATCHED;
import static com.fastcampus.programming.dmaker.exception.DMakerErrorCode.NO_DEVELOPER;

import com.fastcampus.programming.dmaker.dto.CreateDeveloper;
import com.fastcampus.programming.dmaker.dto.DeveloperDetailDto;
import com.fastcampus.programming.dmaker.dto.DeveloperDto;
import com.fastcampus.programming.dmaker.entity.Developer;
import com.fastcampus.programming.dmaker.exception.DMakerException;
import com.fastcampus.programming.dmaker.repository.DeveloperRepository;
import com.fastcampus.programming.dmaker.type.DeveloperLevel;
import com.fastcampus.programming.dmaker.type.DeveloperSkillType;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DMakerService {
  private final DeveloperRepository developerRepository;
  private final EntityManager em;

  @Transactional
  public CreateDeveloper.Response createDeveloper(CreateDeveloper.Request request) {
    validateCreateDeveloperRequest(request);

    // business logic start
    Developer developer = Developer.builder()
      .developerLevel(request.getDeveloperLevel())
      .developerSkillType(request.getDeveloperSkillType())
      .experienceYears(request.getExperienceYears())
      .memberId(request.getMemberId())
      .name(request.getName())
      .age(request.getAge())
      .build();
    developerRepository.save(developer);
    return CreateDeveloper.Response.fromEntity(developer);
  }

  private void validateCreateDeveloperRequest(CreateDeveloper.Request request) {
    // business validation
    DeveloperLevel developerLevel = request.getDeveloperLevel();
    Integer experienceYears = request.getExperienceYears();

    if (developerLevel == DeveloperLevel.SENIOR
          && experienceYears < 10) {
      throw new DMakerException(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED);
    }
    if (developerLevel == DeveloperLevel.JUNGNIOR
        && (experienceYears < 4 || experienceYears > 10)) {
      throw new DMakerException(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED);
    }
    if (developerLevel == developerLevel.JUNIOR && experienceYears > 4) {
      throw new DMakerException(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED);
    }

    developerRepository.findByMemberId(request.getMemberId())
        .ifPresent((developer -> {
          throw new DMakerException(DUPLICATED_MEMBER_ID);
        }));
  }

  public List<DeveloperDto> getAllDevelopers() {
    return developerRepository.findAll()
        .stream().map(DeveloperDto::fromEntity)
        .collect(Collectors.toList());

  }

  public DeveloperDetailDto getDeveloperDetail(String memberId) {
    return developerRepository.findByMemberId(memberId)
        .map(DeveloperDetailDto::fromEntity)
        .orElseThrow(() -> new DMakerException(NO_DEVELOPER));
  }
}
