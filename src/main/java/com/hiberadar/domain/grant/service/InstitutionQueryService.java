package com.hiberadar.domain.grant.service;

import com.hiberadar.domain.grant.dto.InstitutionResponse;
import com.hiberadar.domain.grant.entity.Institution;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.mapper.InstitutionMapper;
import com.hiberadar.domain.grant.repository.InstitutionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstitutionQueryService {

    private final InstitutionRepository institutionRepository;

    public InstitutionQueryService(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    public List<InstitutionResponse> getAll() {
        return institutionRepository.findAllByOrderByName()
                .stream()
                .map(InstitutionMapper::toResponse)
                .toList();
    }

    public List<InstitutionResponse> getByScope(InstitutionScope scope) {
        return institutionRepository.findByScope(scope)
                .stream()
                .map(InstitutionMapper::toResponse)
                .toList();
    }

    public InstitutionResponse getById(Long id) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kurum bulunamadı: " + id));
        return InstitutionMapper.toResponse(institution);
    }

    public Institution getEntityById(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kurum bulunamadı: " + id));
    }
}
