package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.MenuItem;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends ContentRepository<MenuItem> {

    List<MenuItem> findByCategoryId(UUID categoryId);
}
