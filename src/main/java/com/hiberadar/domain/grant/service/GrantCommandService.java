package com.hiberadar.domain.grant.service;

import com.hiberadar.domain.grant.dto.CreateGrantRequest;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.repository.GrantRepository;
import com.hiberadar.domain.source.entity.Source;
import com.hiberadar.domain.source.service.SourceService;
import org.springframework.stereotype.Service;

@Service
public class GrantCommandService {

    private final GrantRepository grantRepository;
    private final SourceService sourceService;

    public GrantCommandService(GrantRepository grantRepository, SourceService sourceService) {
        this.grantRepository = grantRepository;
        this.sourceService = sourceService;
    }

    // ✅ Controller "create" çağırıyorsa hata çıkmasın
    public Grant create(CreateGrantRequest req) {
        return upsert(req);
    }

    public Grant upsert(CreateGrantRequest req) {
        Source source = sourceService.getByIdOrThrow(req.sourceId());

        Grant g = new Grant();
        g.setSource(source);
        g.setTitle(req.title());
        g.setStatus(req.status());
        g.setScope(req.scope());
        g.setCountryCode(req.countryCode());
        g.setOfficialUrl(req.officialUrl());
        g.setProviderName(req.providerName());
        g.setProgramName(req.programName());
        g.setReferenceCode(req.referenceCode());
        g.setSummaryShort(req.summaryShort());
        g.setPublishedAt(req.publishedAt());
        g.setDeadlineAt(req.deadlineAt());
        g.setCurrency(req.currency());
        g.setFundingMin(req.fundingMin());
        g.setFundingMax(req.fundingMax());

        return grantRepository.save(g);
    }
}
