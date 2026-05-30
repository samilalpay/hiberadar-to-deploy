package com.hiberadar.domain.user.controller;

import com.hiberadar.domain.user.dto.AdminFirmDetailResponse;
import com.hiberadar.domain.user.dto.AdminFirmListItem;
import com.hiberadar.domain.user.service.AdminFirmService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/firms")
@PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
public class AdminFirmController {

    private final AdminFirmService adminFirmService;

    public AdminFirmController(AdminFirmService adminFirmService) {
        this.adminFirmService = adminFirmService;
    }

    @GetMapping
    public Page<AdminFirmListItem> list(@RequestParam(required = false) String q,
                                        @RequestParam(required = false) Boolean active,
                                        Pageable pageable) {
        return adminFirmService.list(q, active, pageable);
    }

    @GetMapping("/{id}")
    public AdminFirmDetailResponse get(@PathVariable Long id) {
        return adminFirmService.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminFirmService.delete(id);
    }

    @PatchMapping("/{id}/active")
    public void setActive(@PathVariable Long id, @RequestParam boolean active) {
        adminFirmService.setActive(id, active);
    }
}
