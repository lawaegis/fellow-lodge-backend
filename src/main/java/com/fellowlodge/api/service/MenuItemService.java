package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.MenuItem;
import com.fellowlodge.api.repository.MenuItemRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MenuItemService extends ContentCrudService<MenuItem> {

    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository repository) {
        super(repository, "Menu item");
        this.menuItemRepository = repository;
    }

    public List<MenuItem> findByCategoryId(UUID categoryId) {
        return menuItemRepository.findByCategoryId(categoryId);
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "name");
    }
}
