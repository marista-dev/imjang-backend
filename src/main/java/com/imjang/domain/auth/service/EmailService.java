package com.imjang.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  @Async
  public void sendVerificationEmail(String email, String code) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(email);
      message.setSubject("[🏡임장기록장] 인증 코드");
      message.setText(String.format(
              "안녕하세요.\n\n" +
                      "이메일 인증번호: %s\n\n" +
                      "30분 이내에 인증을 완료해주세요.\n\n" +
                      "감사합니다.",
              code
      ));
      mailSender.send(message);
      log.info("Verification email sent to: {}", email);
    } catch (Exception e) {
      log.error("Failed to send verification email to: {}", email, e);
    }
  }
}
