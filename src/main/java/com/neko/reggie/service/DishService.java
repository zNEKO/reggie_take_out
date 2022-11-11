package com.neko.reggie.service;

import com.neko.reggie.dto.DishDto;
import com.neko.reggie.entity.Dish;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.*;

/**
* @author z-nek
* @description 针对表【dish(菜品管理)】的数据库操作Service
* @createDate 2022-11-05 15:51:37
*/
public interface DishService extends IService<Dish> {
    // 新增菜品，同时插入对应的菜品口味数据，需要操作两张表 -> dish AND dishFlavor
    public void saveWithFlavor(DishDto dishDto);

    // 根据dishId查询菜品信息和口味信息，操作dish表和dishFlavor
    public DishDto getByIdWithFlavor(Long id);

    // 修改菜品信息
    public void updateWithFlavor(DishDto dishDto);

    // 删除菜品信息
    public void removeWithCategory(List<Long> ids);
}
