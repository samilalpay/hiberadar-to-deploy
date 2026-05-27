package com.hiberadar.domain.grant.service;

import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.notification.service.NotificationService;
import com.hiberadar.domain.user.entity.enums.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GrantMatchNotificationService {

    private final UserRepository userRepository;
    private final GrantQueryService grantQueryService;
    private final NotificationService notificationService;

    public GrantMatchNotificationService(UserRepository userRepository,
                                         GrantQueryService grantQueryService,
                                         NotificationService notificationService) {
        this.userRepository = userRepository;
        this.grantQueryService = grantQueryService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void notifyMatchingFirms(Grant grant) {
        if (grant == null) {
            return;
        }
        List<AppUser> firms = userRepository.findByRoleAndProfileCompletedTrue(UserRole.FIRMA);
        for (AppUser firm : firms) {
            if (grantQueryService.isMatchedForUser(grant, firm)) {
                notificationService.createGrantMatchedNotification(firm, grant);
            }
        }
    }
}
