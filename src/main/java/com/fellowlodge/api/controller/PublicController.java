package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.dto.publiccatalog.PublicAmenityResponse;
import com.fellowlodge.api.dto.publiccatalog.PublicMenuResponse;
import com.fellowlodge.api.dto.publiccatalog.PublicPolicyResponse;
import com.fellowlodge.api.dto.publiccatalog.PublicRoomResponse;
import com.fellowlodge.api.dto.portal.HotelInfoResponse;
import com.fellowlodge.api.entity.Announcement;
import com.fellowlodge.api.entity.Attraction;
import com.fellowlodge.api.entity.Banner;
import com.fellowlodge.api.entity.ConferenceHall;
import com.fellowlodge.api.entity.ConferencePackage;
import com.fellowlodge.api.entity.Event;
import com.fellowlodge.api.entity.EventPackage;
import com.fellowlodge.api.entity.Faq;
import com.fellowlodge.api.entity.GalleryImage;
import com.fellowlodge.api.entity.HotelService;
import com.fellowlodge.api.entity.LegalDocument;
import com.fellowlodge.api.entity.Policy;
import com.fellowlodge.api.entity.Promotion;
import com.fellowlodge.api.entity.Review;
import com.fellowlodge.api.entity.RoomType;
import com.fellowlodge.api.service.HotelInfoService;
import com.fellowlodge.api.service.PublicCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Public catalog consumed by the React guest portal. Every response is read
 * live from PostgreSQL (the single source of truth), so Administrator CRUD
 * operations in the desktop application appear here immediately.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicCatalogService catalogService;
    private final HotelInfoService hotelInfoService;

    @GetMapping("/room-types")
    public ApiResponse<List<RoomType>> roomTypes() {
        return ApiResponse.ok(catalogService.activeRoomTypes());
    }

    @GetMapping("/amenities")
    public ApiResponse<List<PublicAmenityResponse>> amenities() {
        return ApiResponse.ok(catalogService.amenities());
    }

    @GetMapping("/rooms")
    public ApiResponse<List<PublicRoomResponse>> availableRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ApiResponse.ok(catalogService.availableRooms(checkIn, checkOut));
    }

    @GetMapping("/rooms/{id}")
    public ApiResponse<PublicRoomResponse> roomById(@PathVariable UUID id) {
        return ApiResponse.ok(catalogService.roomById(id));
    }

    @GetMapping("/services")
    public ApiResponse<List<HotelService>> services() {
        return ApiResponse.ok(catalogService.activeServices());
    }

    @GetMapping("/events")
    public ApiResponse<List<Event>> events() {
        return ApiResponse.ok(catalogService.upcomingEvents());
    }

    @GetMapping("/conference-halls")
    public ApiResponse<List<ConferenceHall>> conferenceHalls() {
        return ApiResponse.ok(catalogService.conferenceHalls());
    }

    @GetMapping("/promotions")
    public ApiResponse<List<Promotion>> promotions() {
        return ApiResponse.ok(catalogService.activePromotions());
    }

    @GetMapping("/gallery")
    public ApiResponse<List<GalleryImage>> gallery() {
        return ApiResponse.ok(catalogService.gallery());
    }

    @GetMapping("/reviews")
    public ApiResponse<Map<String, Object>> reviews() {
        return ApiResponse.ok(Map.of(
                "averageRating", catalogService.averageRating(),
                "reviews", catalogService.approvedReviews()));
    }

    @GetMapping("/menu")
    public ApiResponse<PublicMenuResponse> menu() {
        return ApiResponse.ok(catalogService.menu());
    }

    @GetMapping("/event-packages")
    public ApiResponse<List<EventPackage>> eventPackages() {
        return ApiResponse.ok(catalogService.eventPackages());
    }

    @GetMapping("/conference-packages")
    public ApiResponse<List<ConferencePackage>> conferencePackages() {
        return ApiResponse.ok(catalogService.conferencePackages());
    }

    @GetMapping("/banners")
    public ApiResponse<List<Banner>> banners() {
        return ApiResponse.ok(catalogService.banners());
    }

    @GetMapping("/announcements")
    public ApiResponse<List<Announcement>> announcements() {
        return ApiResponse.ok(catalogService.announcements());
    }

    @GetMapping("/policies")
    public ApiResponse<List<Policy>> policies() {
        return ApiResponse.ok(catalogService.policies());
    }

    @GetMapping("/policies/{slug}")
    public ApiResponse<PublicPolicyResponse> policyBySlug(@PathVariable String slug) {
        return ApiResponse.ok(catalogService.policyBySlug(slug));
    }

    @GetMapping("/faqs")
    public ApiResponse<List<Faq>> faqs() {
        return ApiResponse.ok(catalogService.faqs());
    }

    @GetMapping("/hotel")
    public ApiResponse<HotelInfoResponse> hotel() {
        return ApiResponse.ok(hotelInfoService.hotelInfo());
    }

    @GetMapping("/hotel/about")
    public ApiResponse<Map<String, String>> hotelAbout() {
        return ApiResponse.ok(hotelInfoService.about());
    }

    @GetMapping("/hotel/contact")
    public ApiResponse<Map<String, String>> hotelContact() {
        return ApiResponse.ok(hotelInfoService.contact());
    }

    @GetMapping("/hotel/faqs")
    public ApiResponse<List<Faq>> hotelFaqs() {
        return ApiResponse.ok(hotelInfoService.faqs());
    }

    @GetMapping("/hotel/background")
    public ApiResponse<Map<String, String>> hotelBackground() {
        return ApiResponse.ok(hotelInfoService.backgroundImage());
    }

    @GetMapping("/hotel/hero-banners")
    public ApiResponse<List<Banner>> hotelHeroBanners() {
        return ApiResponse.ok(catalogService.banners());
    }

    @GetMapping("/hotel/attractions")
    public ApiResponse<List<Attraction>> hotelAttractions() {
        return ApiResponse.ok(hotelInfoService.attractions());
    }

    @GetMapping("/hotel/legal/{slug}")
    public ApiResponse<LegalDocument> hotelLegal(@PathVariable String slug) {
        return ApiResponse.ok(hotelInfoService.legalBySlug(slug));
    }
}
