package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Attraction;

import java.util.List;

public interface AttractionRepository extends ContentRepository<Attraction> {

    List<Attraction> findByActiveTrueOrderBySortOrderAsc();
}
