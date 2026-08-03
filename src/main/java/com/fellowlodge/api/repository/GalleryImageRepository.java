package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.GalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GalleryImageRepository extends JpaRepository<GalleryImage, UUID> {
}
