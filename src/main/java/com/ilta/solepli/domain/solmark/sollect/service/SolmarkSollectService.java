package com.ilta.solepli.domain.solmark.sollect.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.sollect.entity.Sollect;
import com.ilta.solepli.domain.sollect.repository.SollectRepository;
import com.ilta.solepli.domain.sollect.repository.SollectRepositoryCustom;
import com.ilta.solepli.domain.solmark.sollect.dto.response.SolmarkSollectResponse;
import com.ilta.solepli.domain.solmark.sollect.dto.response.SolmarkSollectResponse.PageInfo;
import com.ilta.solepli.domain.solmark.sollect.dto.response.SolmarkSollectResponseContent;
import com.ilta.solepli.domain.solmark.sollect.entity.SolmarkSollect;
import com.ilta.solepli.domain.solmark.sollect.repository.SolmarkSollectRepository;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.global.exception.CustomException;
import com.ilta.solepli.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class SolmarkSollectService {

  private final SolmarkSollectRepository solmarkSollectRepository;
  private final SollectRepository sollectRepository;
  private final SollectRepositoryCustom sollectRepositoryCustom;

  @Transactional
  public void addSolmarkSollect(User user, Long id) {
    Sollect sollect =
        sollectRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLLECT_NOT_FOUND));

    SolmarkSollect solmarkSollect = SolmarkSollect.builder().sollect(sollect).user(user).build();

    solmarkSollectRepository.save(solmarkSollect);
  }

  @Transactional(readOnly = true)
  public SolmarkSollectResponse getSolmarkSollects(User user, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Direction.DESC, "createdAt"));

    List<Long> sollectIds = solmarkSollectRepository.findSollectIdsByUser(user);

    Page<SolmarkSollectResponseContent> sollects =
        sollectRepositoryCustom.searchBySolmarkSollect(pageable, sollectIds);

    PageInfo info =
        PageInfo.builder()
            .page(sollects.getNumber())
            .size(sollects.getSize())
            .totalPages(sollects.getTotalPages())
            .totalElements(sollects.getTotalElements())
            .isLast(sollects.isLast())
            .build();

    List<SolmarkSollectResponse.SollectSearchContent> convertedContents =
        toResponseContent(sollects.getContent());

    return SolmarkSollectResponse.builder().contents(convertedContents).pageInfo(info).build();
  }

  private List<SolmarkSollectResponse.SollectSearchContent> toResponseContent(
      List<SolmarkSollectResponseContent> contents) {
    return contents.stream()
        .map(
            content ->
                SolmarkSollectResponse.SollectSearchContent.builder()
                    .sollectId(content.sollectId())
                    .thumbnailImage(content.thumbnailImage())
                    .title(content.title())
                    .district(content.district())
                    .neighborhood(content.neighborhood())
                    .build())
        .toList();
  }
}
