package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.MenuCategory;
import com.fellowlodge.api.repository.MenuCategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class MenuCategoryService extends ContentCrudService<MenuCategory> {

    public MenuCategoryService(MenuCategoryRepository repository) {
        super(repository, "Menu category");
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "sortOrder");
    }
}
