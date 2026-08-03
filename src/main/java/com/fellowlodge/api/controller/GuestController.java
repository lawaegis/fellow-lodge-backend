package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Guest;
import com.fellowlodge.api.service.GuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @GetMapping
    @PreAuthorize("hasAuthority('GUESTS:READ')")
    public ApiResponse<List<Guest>> findAll(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) Boolean vip) {
        Page<Guest> result = guestService.findAll(page, size, sort, search, vip);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('GUESTS:READ')")
    public ApiResponse<List<Guest>> findAll() {
        return ApiResponse.ok(guestService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GUESTS:READ')")
    public ApiResponse<Guest> findById(@PathVariable UUID id) {
        return ApiResponse.ok(guestService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GUESTS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Guest> create(@Valid @RequestBody Guest guest) {
        return ApiResponse.created("Guest created", guestService.create(guest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GUESTS:WRITE')")
    public ApiResponse<Guest> update(@PathVariable UUID id, @Valid @RequestBody Guest guest) {
        return ApiResponse.ok("Guest updated", guestService.update(id, guest));
    }

    @PatchMapping("/{id}/vip")
    @PreAuthorize("hasAuthority('GUESTS:WRITE')")
    public ApiResponse<Guest> setVip(@PathVariable UUID id, @RequestParam boolean vip) {
        return ApiResponse.ok("Guest VIP status updated", guestService.setVip(id, vip));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GUESTS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        guestService.delete(id);
        return ApiResponse.deleted("Guest deleted");
    }
}
