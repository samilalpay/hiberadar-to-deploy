package com.hiberadar.domain.application.repository;

import com.hiberadar.domain.application.entity.Application;
import com.hiberadar.domain.application.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByFirmUser_IdAndGrant_Id(Long firmUserId, Long grantId);

    boolean existsByGrant_Id(Long grantId);

    Page<Application> findByFirmUser_Username(String username, Pageable pageable);

    @Query("""
            select a
            from Application a
            join fetch a.grant g
            join fetch a.firmUser fu
            where a.id = :id
            """)
    Optional<Application> findWithRelationsById(@Param("id") Long id);

    @Query("""
            select a
            from Application a
            join a.grant g
            join a.firmUser fu
            where (:status is null or a.status = :status)
              and (:grantId is null or g.id = :grantId)
                                                        and (:firmUsername is null or cast(fu.username as string) like concat('%', cast(:firmUsername as string), '%'))
            order by a.submittedAt desc
            """)
    Page<Application> adminList(@Param("status") ApplicationStatus status,
            @Param("grantId") Long grantId,
            @Param("firmUsername") String firmUsername,
            Pageable pageable);

    boolean existsByConfirmedMeetingAtBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByConfirmedMeetingAtBetweenAndIdNot(LocalDateTime start, LocalDateTime end, Long id);

    boolean existsByRequestedMeetingAtBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByRequestedMeetingAtBetweenAndIdNot(LocalDateTime start, LocalDateTime end, Long id);

    @Query("""
            select a
            from Application a
            join fetch a.grant g
            join fetch a.firmUser fu
            where fu.username = :username
              and (a.requestedMeetingAt is not null or a.confirmedMeetingAt is not null)
            order by coalesce(a.confirmedMeetingAt, a.requestedMeetingAt) asc
            """)
    List<Application> findMeetingCalendarByFirmUsername(@Param("username") String username);

    @Query("""
            select a
            from Application a
            join fetch a.grant g
            join fetch a.firmUser fu
            where fu.username = :username
            order by a.submittedAt desc
            """)
    List<Application> findWithRelationsByFirmUsername(@Param("username") String username);

    @Query("""
            select a
            from Application a
            join fetch a.grant g
            join fetch a.firmUser fu
            where a.requestedMeetingAt is not null or a.confirmedMeetingAt is not null
            order by coalesce(a.confirmedMeetingAt, a.requestedMeetingAt) asc
            """)
    List<Application> findAllMeetingCalendarItems();

    @Query("""
            select a.confirmedMeetingAt
            from Application a
            where a.confirmedMeetingAt is not null
            order by a.confirmedMeetingAt asc
            """)
    List<LocalDateTime> findAllConfirmedMeetingTimes();

    @Query("""
            select count(a)
            from Application a
            where a.firmUser.username = :username
              and a.requestedMeetingAt is not null
              and a.confirmedMeetingAt is null
            """)
    long countPendingMeetingRequestsByFirmUsername(@Param("username") String username);

    @Query("""
            select count(a)
            from Application a
            where a.firmUser.username = :username
              and a.requestedMeetingAt is not null
              and a.confirmedMeetingAt is null
              and a.requestedMeetingAt >= :since
            """)
    long countRecentPendingMeetingRequestsByFirmUsername(
            @Param("username") String username,
            @Param("since") LocalDateTime since);

    @Query("""
            select a
            from Application a
            join fetch a.firmUser fu
            join fetch a.grant g
            where a.id <> :excludingApplicationId
              and a.confirmedMeetingAt is null
              and a.requestedMeetingAt between :start and :end
            order by a.requestedMeetingAt asc
            """)
    List<Application> findPendingMeetingRequestsInWindow(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludingApplicationId") Long excludingApplicationId);
}
