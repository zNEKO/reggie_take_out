package com.neko.reggie.service;

import com.neko.reggie.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author z-nek
* @description 针对表【category(菜品及套餐分类)】的数据库操作Service
* @createDate 2022-11-05 15:51:37
*/
public interface CategoryService extends IService<Category> {
    void deleteById(Long id);
}
