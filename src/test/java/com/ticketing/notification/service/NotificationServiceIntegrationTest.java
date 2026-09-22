package com.ticketing.notification.service;

import com.ticketing.global.exception.BaseException;
import com.ticketing.notification.domain.Notification;
import com.ticketing.notification.domain.NotificationReferenceType;
import com.ticketing.notification.domain.NotificationType;
import com.ticketing.notification.repository.NotificationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static com.ticketing.global.baseresponse.BaseResponseStatus.NOTIFICATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("다른 회원의 알림은 읽음 처리할 수 없다")
    void cannotReadAnotherMembersNotification() {
        Notification notification = notificationRepository.saveAndFlush(
                createNotification(1L, "회원 1의 알림")
        );

        assertThatThrownBy(() ->
                notificationService.markAsRead(2L, notification.getId())
        )
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;

                    assertThat(baseException.getBaseResponseStatus())
                            .isEqualTo(NOTIFICATION_NOT_FOUND);
                });

        entityManager.clear();

        Notification unchanged = notificationRepository
                .findById(notification.getId())
                .orElseThrow();

        assertThat(unchanged.getReadAt()).isNull();
    }

    @Test
    @DisplayName("전체 읽음 처리는 로그인 회원의 미읽음 알림만 변경한다")
    void markAllAsReadOnlyUpdatesAuthenticatedMembersNotifications() {
        Notification memberOneFirst = notificationRepository.save(
                createNotification(1L, "회원 1의 첫 번째 알림")
        );

        Notification memberOneSecond = notificationRepository.save(
                createNotification(1L, "회원 1의 두 번째 알림")
        );

        Notification memberTwoNotification = notificationRepository.save(
                createNotification(2L, "회원 2의 알림")
        );

        notificationRepository.flush();
        entityManager.clear();

        int updatedCount = notificationService.markAllAsRead(1L);

        entityManager.clear();

        Notification updatedFirst = notificationRepository
                .findById(memberOneFirst.getId())
                .orElseThrow();

        Notification updatedSecond = notificationRepository
                .findById(memberOneSecond.getId())
                .orElseThrow();

        Notification unchangedOtherMember = notificationRepository
                .findById(memberTwoNotification.getId())
                .orElseThrow();

        assertThat(updatedCount).isEqualTo(2);
        assertThat(updatedFirst.getReadAt()).isNotNull();
        assertThat(updatedSecond.getReadAt()).isNotNull();
        assertThat(unchangedOtherMember.getReadAt()).isNull();
    }

    private Notification createNotification(Long memberId, String content) {
        return Notification.create(
                memberId,
                NotificationType.PAYMENT_COMPLETED,
                "결제가 완료되었습니다.",
                content,
                NotificationReferenceType.RESERVATION,
                100L
        );
    }
}
