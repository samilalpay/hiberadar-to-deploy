package com.hiberadar.domain.grant.repository;

import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GrantRepository extends JpaRepository<Grant, Long> {

  // --- UPSERT için lookup'lar ---
  Optional<Grant> findBySourceIdAndReferenceCodeIgnoreCase(Long sourceId, String referenceCode);

  Optional<Grant> findBySourceIdAndOfficialUrlIgnoreCase(Long sourceId, String officialUrl);

  // --- Duplicate check (CREATE için) ---
  boolean existsBySource_IdAndReferenceCodeIgnoreCase(Long sourceId, String referenceCode);

  boolean existsBySource_IdAndOfficialUrlIgnoreCase(Long sourceId, String officialUrl);

  // --- Duplicate check (UPDATE için - kendi kaydını hariç tut) ✅ ---
  boolean existsBySource_IdAndReferenceCodeIgnoreCaseAndIdNot(Long sourceId, String referenceCode, Long id);

  boolean existsBySource_IdAndOfficialUrlIgnoreCaseAndIdNot(Long sourceId, String officialUrl, Long id);

  // (Opsiyonel fallback) link/ref yoksa duplicate riskini azaltmak için:
  Optional<Grant> findFirstBySourceIdAndTitleIgnoreCase(Long sourceId, String title);

  // (Elinde zaten vardı - istersen bunları ileride sadeleştiririz)
  Optional<Grant> findBySource_IdAndReferenceCodeIgnoreCase(Long sourceId, String referenceCode);

  Optional<Grant> findBySource_IdAndOfficialUrlIgnoreCase(Long sourceId, String officialUrl);

  List<Grant> findByStatus(GrantStatus status);

  // --- SEARCH (FTS) ---
  @Query(value = """
      SELECT *
      FROM grants g
      WHERE
        (:status IS NULL OR g.status = :status)
        AND (:sourceId IS NULL OR g.source_id = :sourceId)
        AND (:nace IS NULL OR :nace = '' OR upper(g.nace_code) = upper(:nace))
        AND (:scope IS NULL OR g.scope = :scope)
        AND (:countryCode IS NULL OR :countryCode = ''
             OR upper(g.country_code) = upper(:countryCode))
        AND (:currency IS NULL OR :currency = ''
             OR upper(g.currency) = upper(:currency))
        AND (:deadlineFrom IS NULL OR g.deadline_at >= :deadlineFrom)
        AND (:deadlineTo IS NULL OR g.deadline_at <= :deadlineTo)
        AND (:minFunding IS NULL OR g.funding_max IS NULL OR g.funding_max >= :minFunding)
        AND (:maxFunding IS NULL OR g.funding_min IS NULL OR g.funding_min <= :maxFunding)
        AND (
          :q IS NULL OR :q = ''
          OR lower(translate(coalesce(g.title, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
          OR lower(translate(coalesce(g.program_name, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
          OR lower(translate(coalesce(g.provider_name, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
          OR lower(translate(coalesce(g.summary_short, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
          OR lower(translate(coalesce(g.reference_code, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
        )
      """, countQuery = """
      SELECT count(*)
      FROM grants g
      WHERE
        (:status IS NULL OR g.status = :status)
        AND (:sourceId IS NULL OR g.source_id = :sourceId)
        AND (:nace IS NULL OR :nace = '' OR upper(g.nace_code) = upper(:nace))
        AND (:scope IS NULL OR g.scope = :scope)
        AND (:countryCode IS NULL OR :countryCode = ''
             OR upper(g.country_code) = upper(:countryCode))
        AND (:currency IS NULL OR :currency = ''
             OR upper(g.currency) = upper(:currency))
        AND (:deadlineFrom IS NULL OR g.deadline_at >= :deadlineFrom)
        AND (:deadlineTo IS NULL OR g.deadline_at <= :deadlineTo)
        AND (:minFunding IS NULL OR g.funding_max IS NULL OR g.funding_max >= :minFunding)
        AND (:maxFunding IS NULL OR g.funding_min IS NULL OR g.funding_min <= :maxFunding)
        AND (
          :q IS NULL OR :q = ''
          OR lower(translate(coalesce(g.title, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
          OR lower(translate(coalesce(g.program_name, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
          OR lower(translate(coalesce(g.provider_name, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
          OR lower(translate(coalesce(g.summary_short, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
          OR lower(translate(coalesce(g.reference_code, ''), 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) LIKE '%' || lower(translate(:q, 'ÇĞIİÖŞÜçğıiöşü', 'CGIIOSUcgiiosu')) || '%'
        )
      """, nativeQuery = true)
  Page<Grant> search(
      @Param("status") String status,
      @Param("sourceId") Long sourceId,
      @Param("nace") String nace,
      @Param("scope") String scope,
      @Param("countryCode") String countryCode,
      @Param("currency") String currency,
      @Param("q") String q,
      @Param("deadlineFrom") LocalDate deadlineFrom,
      @Param("deadlineTo") LocalDate deadlineTo,
      @Param("minFunding") BigDecimal minFunding,
      @Param("maxFunding") BigDecimal maxFunding,
      Pageable pageable);
}
