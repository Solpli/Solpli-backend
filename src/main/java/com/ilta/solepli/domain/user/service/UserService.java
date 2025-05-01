package com.ilta.solepli.domain.user.service;

import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ilta.solepli.domain.user.entity.Role;
import com.ilta.solepli.domain.user.entity.User;
import com.ilta.solepli.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  private final String[] ADJECTIVES = {"고요한", "잔잔한", "따뜻한", "포근한", "쓸쓸한", "그리운", "낯선", "익숙한"};

  private final String[] NOUNS = {"골목길", "노을", "카페", "창가", "바람", "달빛", "책방", "기억", "의자", "향기"};

  @Value("${DEFAULT_PROFILE_URL}")
  private String defaultImageUrl;

  @Transactional(readOnly = true)
  public String generateAdminNickname() {
    String prefix = "관리자";

    Optional<User> latestAdmin =
        userRepository.findTopByNicknameStartingWithOrderByNicknameDesc(prefix);

    int nextNumber = 1;

    if (latestAdmin.isPresent()) {
      String latestNickname = latestAdmin.get().getNickname(); // 관리자27
      String numberPart = latestNickname.replace(prefix, ""); // 27

      try {
        nextNumber = Integer.parseInt(numberPart) + 1;
      } catch (NumberFormatException e) {
        // 만약 형식이 다르면 1로 시작
        nextNumber = 1;
      }
    }

    return prefix + nextNumber; // ex) 관리자28
  }

  @Transactional
  public User findOrSignUpUser(String loginId) {

    Random random = new Random();
    String nickname;

    do {
      String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
      String noun = NOUNS[random.nextInt(NOUNS.length)];
      int number = 100 + random.nextInt(900);
      nickname = adjective + noun + number;
    } while (checkNickname(nickname));

    final String checkedNickname = nickname;

    return userRepository
        .findByLoginId(loginId)
        .orElseGet(
            () ->
                userRepository.save(
                    User.builder()
                        .role(Role.USER)
                        .loginId(loginId)
                        .profileImageUrl(defaultImageUrl)
                        .nickname(checkedNickname)
                        .build()));
  }

  @Transactional(readOnly = true)
  public boolean checkNickname(String nickname) {
    return userRepository.existsByNickname(nickname);
  }
}
