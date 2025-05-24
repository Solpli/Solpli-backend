package com.ilta.solepli.domain.solmark.sollect.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.sollect.entity.Sollect;
import com.ilta.solepli.domain.sollect.repository.SollectRepository;
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

  @Transactional
  public void addSolmarkSollect(User user, Long id) {
    Sollect sollect =
        sollectRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SOLLECT_NOT_FOUND));

    SolmarkSollect solmarkSollect = SolmarkSollect.builder().sollect(sollect).user(user).build();

    solmarkSollectRepository.save(solmarkSollect);
  }
}
