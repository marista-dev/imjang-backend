package com.imjang.global.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 트랜잭션 커밋 후 이벤트 발행을 보장하는 공유 퍼블리셔.
 * 활성 트랜잭션이 있으면 커밋 후 발행, 없으면 즉시 발행.
 */
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

  private final ApplicationEventPublisher publisher;

  public void publishAfterCommit(Object event) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          publisher.publishEvent(event);
        }
      });
    } else {
      publisher.publishEvent(event);
    }
  }
}
