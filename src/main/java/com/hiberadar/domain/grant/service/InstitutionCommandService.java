package com.hiberadar.domain.grant.service;

import com.hiberadar.domain.grant.dto.CreateInstitutionRequest;
import com.hiberadar.domain.grant.entity.Institution;
import com.hiberadar.domain.grant.repository.InstitutionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class InstitutionCommandService {

    private final InstitutionRepository institutionRepository;

    public InstitutionCommandService(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    public Institution create(CreateInstitutionRequest req) {
        // Duplicate check
        if (institutionRepository.findByNameIgnoreCase(req.name()).isPresent()) {
            throw new IllegalArgumentException("Bu kurumu adıyla kurum zaten kayıtlı: " + req.name());
        }

        Institution institution = new Institution(
                req.name(),
                req.shortCode(),
                req.logoUrl(),
                req.scope());
        return institutionRepository.save(institution);
    }

    public Institution update(Long id, CreateInstitutionRequest req) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kurum bulunamadı: " + id));

        // Check duplicate name if it changed
        if (!institution.getName().equalsIgnoreCase(req.name())) {
            if (institutionRepository.findByNameIgnoreCase(req.name()).isPresent()) {
                throw new IllegalArgumentException("Bu isimle başka kurum zaten kayıtlı: " + req.name());
            }
        }

        institution.setName(req.name());
        institution.setShortCode(req.shortCode());
        institution.setLogoUrl(req.logoUrl());
        institution.setScope(req.scope());
        institution.setUpdatedAt(LocalDateTime.now());

        return institutionRepository.save(institution);
    }

    public void delete(Long id) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kurum bulunamadı: " + id));
        institutionRepository.delete(institution);
    }

    public Institution updateLogo(Long id, String logoUrl) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kurum bulunamadı: " + id));

        institution.setLogoUrl(logoUrl);
        institution.setUpdatedAt(LocalDateTime.now());
        return institutionRepository.save(institution);
    }
}
