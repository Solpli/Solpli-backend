package com.ilta.solepli.domain.solmark.sollect.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.sollect.entity.Sollect;
import com.ilta.solepli.domain.sollect.repository.SollectRepository;
import com.ilta.solepli.domain.sollect.repository.SollectRepositoryCustom;
import com.ilta.solepli.domain.solmark.sollect.dto.response.SolmarkSollectResponse;
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

    if (solmarkSollectRepository.existsBySollectIdAndUser(id, user)) {
      throw new CustomException(ErrorCode.SOLMARK_SOLLECT_EXISTS);
    }

    SolmarkSollect solmarkSollect = SolmarkSollect.builder().sollect(sollect).user(user).build();

    solmarkSollectRepository.save(solmarkSollect);
  }

  @Transactional(readOnly = true)
  public SolmarkSollectResponse getSolmarkSollects(User user, Long cursorId, int size) {
    List<Long> sollectIds = solmarkSollectRepository.findSollectIdsByUser(user);
    List<SolmarkSollectResponseContent> contents =
        sollectRepositoryCustom.searchBySolmarkSollect(cursorId, size, sollectIds);

    boolean hasNext = contents.size() > size;
    if (hasNext) contents.remove(size);
    Long nextCursorId = hasNext ? contents.get(contents.size() - 1).sollectId() : null;

    List<SolmarkSollectResponse.SollectSearchContent> converted = toResponseContent(contents);
    return SolmarkSollectResponse.builder()
        .contents(converted)
        .cursorInfo(
            SolmarkSollectResponse.CursorInfo.builder()
                .nextCursorId(nextCursorId)
                .hasNext(hasNext)
                .build())
        .build();
  }

  @Transactional(readOnly = true)
  public SolmarkSollectResponse getMySollects(User user, Long cursorId, int size) {
    List<Long> sollectIds = sollectRepository.findSollectIdsByUser(user);
    List<SolmarkSollectResponseContent> contents =
        sollectRepositoryCustom.searchBySolmarkSollect(cursorId, size, sollectIds);

    boolean hasNext = contents.size() > size;
    if (hasNext) contents.remove(size);
    Long nextCursorId = hasNext ? contents.get(contents.size() - 1).sollectId() : null;

    List<SolmarkSollectResponse.SollectSearchContent> converted = toResponseContent(contents);
    return SolmarkSollectResponse.builder()
        .contents(converted)
        .cursorInfo(
            SolmarkSollectResponse.CursorInfo.builder()
                .nextCursorId(nextCursorId)
                .hasNext(hasNext)
                .build())
        .build();
  }

  @Transactional
  public void deleteSollect(User user, Long id) {
    Sollect sollect =
        sollectRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLLECT_NOT_FOUND));

    SolmarkSollect solmarkSollect =
        solmarkSollectRepository
            .findBySollectAndUser(sollect, user)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLMARK_SOLLECT_NOT_FOUND));

    solmarkSollectRepository.delete(solmarkSollect);
  }

  @Transactional(readOnly = true)
  public Long getSavedCount(Sollect sollect) {
    return solmarkSollectRepository.countSolmarkSollectsBySollect(sollect);
  }

  @Transactional(readOnly = true)
  public List<Long> getPopularSollectIds(int limit) {
    return solmarkSollectRepository.findPopularSollectIds(PageRequest.of(0, limit));
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
