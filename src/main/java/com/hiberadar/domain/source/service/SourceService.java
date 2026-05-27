package com.hiberadar.domain.source.service;

import com.hiberadar.domain.source.dto.CreateSourceRequest;
import com.hiberadar.domain.source.dto.SourceResponse;
import com.hiberadar.domain.source.dto.UpdateSourceRequest;
import com.hiberadar.domain.source.entity.Source;
import com.hiberadar.domain.source.entity.enums.SourceCategory;
import com.hiberadar.domain.source.entity.enums.SourceScope;
import com.hiberadar.domain.source.mapper.SourceMapper;
import com.hiberadar.domain.source.repository.SourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SourceService {

    private final SourceRepository sourceRepository;

    public SourceService(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    public SourceResponse create(CreateSourceRequest req) {
        Source saved = sourceRepository.save(SourceMapper.toEntity(req));
        return SourceMapper.toResponse(saved);
    }

    public List<SourceResponse> listAll() {
        return sourceRepository.findAll()
                .stream()
                .map(SourceMapper::toResponse)
                .toList();
    }

    public SourceResponse get(Long id) {
        return SourceMapper.toResponse(getByIdOrThrow(id));
    }

    public SourceResponse update(Long id, UpdateSourceRequest req) {
        Source s = getByIdOrThrow(id);
        SourceMapper.apply(s, req);
        Source saved = sourceRepository.save(s);
        return SourceMapper.toResponse(saved);
    }

    public SourceResponse setActive(Long id, boolean active) {
        Source s = getByIdOrThrow(id);
        s.setActive(active);
        Source saved = sourceRepository.save(s);
        return SourceMapper.toResponse(saved);
    }

    public Source getByIdOrThrow(Long id) {
        return sourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + id));
    }

    public Source getOrCreateManualSource() {
        return sourceRepository.findByNameIgnoreCase("MANUAL")
                .orElseGet(() -> {
                    Source source = new Source();
                    source.setName("MANUAL");
                    source.setCategory(SourceCategory.AGGREGATOR_DB);
                    source.setScope(SourceScope.MIXED);
                    source.setNotes("Manual grants entry");
                    source.setActive(true);
                    return sourceRepository.save(source);
                });
    }

}
