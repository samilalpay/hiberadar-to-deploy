package com.hiberadar.domain.source.mapper;

import com.hiberadar.domain.source.dto.CreateSourceRequest;
import com.hiberadar.domain.source.dto.SourceResponse;
import com.hiberadar.domain.source.dto.UpdateSourceRequest;
import com.hiberadar.domain.source.entity.Source;

public final class SourceMapper {

    private SourceMapper() {}

    public static Source toEntity(CreateSourceRequest req) {
        Source s = new Source();
        apply(s, req.name(), req.category(), req.scope(), req.countryCode(), req.officialUrl(), req.notes(), req.active());
        return s;
    }

    public static void apply(Source s, UpdateSourceRequest req) {
        apply(s, req.name(), req.category(), req.scope(), req.countryCode(), req.officialUrl(), req.notes(), req.active());
    }

    private static void apply(
            Source s,
            String name,
            Object category,
            Object scope,
            String countryCode,
            String officialUrl,
            String notes,
            Boolean active
    ) {
        s.setName(name);
        s.setCategory((com.hiberadar.domain.source.entity.enums.SourceCategory) category);
        s.setScope((com.hiberadar.domain.source.entity.enums.SourceScope) scope);
        s.setCountryCode(countryCode);
        s.setOfficialUrl(officialUrl);
        s.setNotes(notes);
        if (active != null) s.setActive(active);
    }

    public static SourceResponse toResponse(Source s) {
        return new SourceResponse(
                s.getId(),
                s.getName(),
                s.getCategory(),
                s.getScope(),
                s.getCountryCode(),
                s.getOfficialUrl(),
                s.getNotes(),
                s.isActive()
        );
    }
}
