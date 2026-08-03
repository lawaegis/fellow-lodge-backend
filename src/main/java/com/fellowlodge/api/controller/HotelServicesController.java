package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.HotelService;
import com.fellowlodge.api.service.HotelServicesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/hotel-services")
@RequiredArgsConstructor
public class HotelServicesController {

    private final HotelServicesService hotelServicesService;

    @GetMapping
    @PreAuthorize("hasAuthority('HOTEL_SERVICES:READ')")
    public ApiResponse<List<HotelService>> findAll(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(required = false) String category,
                                                   @RequestParam(required = false) Boolean active) {
        Page<HotelService> result = hotelServicesService.findAll(page, size, sort, category, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('HOTEL_SERVICES:READ')")
    public ApiResponse<List<HotelService>> findAll() {
        return ApiResponse.ok(hotelServicesService.findAll());
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('HOTEL_SERVICES:READ')")
    public ApiResponse<List<HotelService>> byCategory(@PathVariable String category) {
        return ApiResponse.ok(hotelServicesService.findByCategory(category));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HOTEL_SERVICES:READ')")
    public ApiResponse<HotelService> findById(@PathVariable UUID id) {
        return ApiResponse.ok(hotelServicesService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HOTEL_SERVICES:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HotelService> create(@Valid @RequestBody HotelService service) {
        return ApiResponse.created("Hotel service created", hotelServicesService.create(service));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HOTEL_SERVICES:WRITE')")
    public ApiResponse<HotelService> update(@PathVariable UUID id, @Valid @RequestBody HotelService service) {
        return ApiResponse.ok("Hotel service updated", hotelServicesService.update(id, service));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('HOTEL_SERVICES:WRITE')")
    public ApiResponse<HotelService> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Hotel service updated", hotelServicesService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HOTEL_SERVICES:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        hotelServicesService.delete(id);
        return ApiResponse.deleted("Hotel service deleted");
    }
}
