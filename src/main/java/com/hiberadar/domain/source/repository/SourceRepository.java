package com.hiberadar.domain.source.repository;

import com.hiberadar.domain.source.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, Long> {

    Optional<Source> findByNameIgnoreCase(String name);

    Optional<Source> findByOfficialUrlIgnoreCase(String officialUrl);
}
