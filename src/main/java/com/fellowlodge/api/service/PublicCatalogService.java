package com.fellowlodge.api.service;

import com.fellowlodge.api.dto.publiccatalog.PublicMenuResponse;
import com.fellowlodge.api.dto.publiccatalog.PublicRoomResponse;
import com.fellowlodge.api.dto.publiccatalog.PublicRoomTypeResponse;
import com.fellowlodge.api.entity.Announcement;
import com.fellowlodge.api.entity.Banner;
import com.fellowlodge.api.entity.ConferenceHall;
import com.fellowlodge.api.entity.ConferencePackage;
import com.fellowlodge.api.entity.Event;
import com.fellowlodge.api.entity.EventPackage;
import com.fellowlodge.api.entity.Faq;
import com.fellowlodge.api.entity.GalleryImage;
import com.fellowlodge.api.entity.HotelService;
import com.fellowlodge.api.entity.MenuItem;
import com.fellowlodge.api.entity.Policy;
import com.fellowlodge.api.entity.Promotion;
import com.fellowlodge.api.entity.Review;
import com.fellowlodge.api.entity.Room;
import com.fellowlodge.api.entity.RoomImage;
import com.fellowlodge.api.entity.RoomType;
import com.fellowlodge.api.enums.ReviewStatus;
import com.fellowlodge.api.repository.RoomImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-only catalog for the public guest portal (no auth required). Every
 * method reads the live database, so any Administrator change is reflected on
 * the portal immediately with no cache or mock data.
 */
@Service
@RequiredArgsConstructor
public class PublicCatalogService {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final AmenityService amenityService;
    private final HotelServicesService hotelServicesService;
    private final EventService eventService;
    private final ConferenceHallService conferenceHallService;
    private final PromotionService promotionService;
    private final GalleryImageService galleryImageService;
    private final ReviewService reviewService;
    private final MenuCategoryService menuCategoryService;
    private final MenuItemService menuItemService;
    private final EventPackageService eventPackageService;
    private final ConferencePackageService conferencePackageService;
    private final BannerService bannerService;
    private final AnnouncementService announcementService;
    private final PolicyService policyService;
    private final FaqService faqService;
    private final RoomImageRepository roomImageRepository;

    public List<RoomType> activeRoomTypes() {
        return roomTypeService.findActive();
    }

    public List<com.fellowlodge.api.entity.Amenity> amenities() {
        return amenityService.findAll().stream()
                .sorted(Comparator.comparing(com.fellowlodge.api.entity.Amenity::getName))
                .toList();
    }

    public List<PublicRoomResponse> availableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = (checkIn == null || checkOut == null)
                ? roomService.findAvailable()
                : roomService.findAvailableForDates(checkIn, checkOut);
        if (rooms.isEmpty()) {
            return List.of();
        }
        List<UUID> roomIds = rooms.stream().map(Room::getId).toList();
        Map<UUID, List<RoomImage>> imagesByRoom = roomImageRepository.findByRoomIdIn(roomIds).stream()
                .collect(Collectors.groupingBy(RoomImage::getRoomId));
        Set<UUID> typeIds = rooms.stream()
                .map(Room::getRoomTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, RoomType> typesById = roomTypeService.findByIdIn(typeIds).stream()
                .collect(Collectors.toMap(RoomType::getId, type -> type));
        return rooms.stream()
                .map(room -> toPublicRoom(room,
                        imagesByRoom.getOrDefault(room.getId(), List.of()),
                        room.getRoomTypeId() == null ? null : typesById.get(room.getRoomTypeId())))
                .toList();
    }

    public PublicRoomResponse roomById(UUID roomId) {
        Room room = roomService.findById(roomId);
        return toPublicRoom(room);
    }

    private PublicRoomResponse toPublicRoom(Room room) {
        List<RoomImage> images = roomImageRepository.findByRoomId(room.getId());
        RoomType type = room.getRoomTypeId() == null ? null : roomTypeService.findById(room.getRoomTypeId());
        return toPublicRoom(room, images, type);
    }

    private PublicRoomResponse toPublicRoom(Room room, List<RoomImage> images, RoomType type) {
        List<String> imageUrls = images.stream()
                .sorted(Comparator.comparing(RoomImage::isPrimary).reversed()
                        .thenComparing(RoomImage::getSortOrder))
                .map(RoomImage::getUrl)
                .toList();
        PublicRoomTypeResponse roomType = type == null ? null : toPublicRoomType(type);
        return new PublicRoomResponse(room.getId(), room.getRoomNumber(), room.getFloor(),
                room.getStatus().name(), room.getPricePerNight(), room.getExtraCharges(),
                room.getDescription(), room.isHasBalcony(), room.isHasView(),
                room.isSmoking(), room.isAccessible(), room.getImageUrl(), imageUrls, roomType);
    }

    private PublicRoomTypeResponse toPublicRoomType(RoomType type) {
        List<String> amenities = new ArrayList<>();
        if (type.getAmenitySet() != null && !type.getAmenitySet().isEmpty()) {
            type.getAmenitySet().stream()
                    .sorted(Comparator.comparing(com.fellowlodge.api.entity.Amenity::getName))
                    .forEach(amenity -> amenities.add(amenity.getName()));
        } else if (type.getAmenities() != null) {
            for (String part : type.getAmenities().split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    amenities.add(trimmed);
                }
            }
        }
        return new PublicRoomTypeResponse(type.getId(), type.getName(), type.getDescription(),
                type.getBasePrice(), type.getMaxGuests(), type.getBedType(), type.getSizeSqm(),
                amenities, type.getImageUrl());
    }

    public List<HotelService> activeServices() {
        return hotelServicesService.findActive();
    }

    public List<Event> upcomingEvents() {
        return eventService.findUpcoming();
    }

    public List<ConferenceHall> conferenceHalls() {
        return conferenceHallService.findActive();
    }

    public List<Promotion> activePromotions() {
        return promotionService.findActive();
    }

    public List<GalleryImage> gallery() {
        return galleryImageService.findActive();
    }

    public List<Review> approvedReviews() {
        return reviewService.findByStatus(ReviewStatus.Approved);
    }

    public double averageRating() {
        return reviewService.averageRating();
    }

    public PublicMenuResponse menu() {
        List<com.fellowlodge.api.entity.MenuCategory> categories = menuCategoryService.findActive();
        List<MenuItem> items = menuItemService.findActive().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparing(MenuItem::getName))
                .toList();
        return new PublicMenuResponse(categories, items);
    }

    public List<MenuItem> menuItems() {
        return menuItemService.findActive().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparing(MenuItem::getName))
                .toList();
    }

    public List<EventPackage> eventPackages() {
        return eventPackageService.findActive();
    }

    public List<ConferencePackage> conferencePackages() {
        return conferencePackageService.findActive();
    }

    public List<Banner> banners() {
        return bannerService.findActive();
    }

    public List<Announcement> announcements() {
        return announcementService.findActive();
    }

    public List<Policy> policies() {
        return policyService.findActive();
    }

    public List<Faq> faqs() {
        return faqService.findActive();
    }
}
