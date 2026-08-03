package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.dto.portal.HotelInfoResponse;
import com.fellowlodge.api.entity.Attraction;
import com.fellowlodge.api.entity.Faq;
import com.fellowlodge.api.entity.LegalDocument;
import com.fellowlodge.api.repository.AttractionRepository;
import com.fellowlodge.api.repository.LegalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Hotel identity and information endpoints consumed by the guest portal
 * (/public/hotel/**). Values come from settings (admin-editable) and the
 * content tables, keeping a single source of truth.
 */
@Service
@RequiredArgsConstructor
public class HotelInfoService {

    private final SettingService settingService;
    private final AttractionRepository attractionRepository;
    private final LegalDocumentRepository legalDocumentRepository;
    private final FaqService faqService;

    public HotelInfoResponse hotelInfo() {
        return new HotelInfoResponse(
                settingService.getValue("hotel.name", "Fellow Lodge"),
                settingService.getValue("hotel.tagline", "Luxury stays, unforgettable events."),
                settingService.getValue("hotel.logo.url", ""),
                settingService.getValue("hotel.hero.image", ""),
                settingService.getValue("hotel.background.image", ""),
                settingService.getValue("hotel.phone", ""),
                settingService.getValue("hotel.email", ""),
                settingService.getValue("hotel.address", ""),
                settingService.getValue("currency.code", "USD"),
                settingService.getValue("currency.symbol", "$"),
                settingService.getValue("booking.checkin.time", "14:00"),
                settingService.getValue("booking.checkout.time", "11:00"),
                settingService.getValue("hotel.about", ""),
                settingService.getValue("hotel.map.url", ""));
    }

    public java.util.Map<String, String> about() {
        return java.util.Map.of(
                "name", settingService.getValue("hotel.name", "Fellow Lodge"),
                "tagline", settingService.getValue("hotel.tagline", ""),
                "about", settingService.getValue("hotel.about", ""),
                "address", settingService.getValue("hotel.address", ""));
    }

    public java.util.Map<String, String> contact() {
        return java.util.Map.of(
                "name", settingService.getValue("hotel.name", "Fellow Lodge"),
                "phone", settingService.getValue("hotel.phone", ""),
                "email", settingService.getValue("hotel.email", ""),
                "address", settingService.getValue("hotel.address", ""),
                "mapUrl", settingService.getValue("hotel.map.url", ""),
                "instagram", settingService.getValue("hotel.social.instagram", ""),
                "facebook", settingService.getValue("hotel.social.facebook", ""));
    }

    public java.util.Map<String, String> backgroundImage() {
        return java.util.Map.of(
                "url", settingService.getValue("hotel.background.image", ""));
    }

    public List<Attraction> attractions() {
        return attractionRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    public List<Faq> faqs() {
        return faqService.findActive();
    }

    public LegalDocument legalBySlug(String slug) {
        return legalDocumentRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Legal document with slug " + slug));
    }
}
