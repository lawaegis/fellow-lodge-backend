package com.fellowlodge.api.dto.publiccatalog;

import com.fellowlodge.api.entity.MenuCategory;
import com.fellowlodge.api.entity.MenuItem;

import java.util.List;

/**
 * Public restaurant menu for the guest portal: active categories plus their
 * active items, exactly as configured by the Administrator.
 */
public record PublicMenuResponse(List<MenuCategory> categories, List<MenuItem> items) {
}
