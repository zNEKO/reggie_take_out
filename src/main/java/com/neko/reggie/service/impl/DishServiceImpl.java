package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.Exception.CustomException;
import com.neko.reggie.dto.DishDto;
import com.neko.reggie.entity.Dish;
import com.neko.reggie.entity.DishFlavor;
import com.neko.reggie.service.DishFlavorService;
import com.neko.reggie.service.DishService;
import com.neko.reggie.mapper.DishMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author z-nek
* @description 针对表【dish(菜品管理)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:37
*/
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>
    implements DishService{

    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品基本信息到dish表中
        this.save(dishDto);

        // 获取菜品dishID
        Long dishId = dishDto.getId();

        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        // 为菜品口味的菜品Id赋值
        flavors = flavors.stream().map(item -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        // 将菜品口味数据保存到菜品口味表中 => dishFlavor
        dishFlavorService.saveBatch(flavors);

    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品基本信息，从dish表中查询
        Dish dish = this.getById(id);

        // 拷贝菜品基本信息到DishDao实体类中
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 添加查询DishFlavor表的查询条件
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        // 为DishDto实体类中封装菜品口味信息
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新dish表中的基本信息
        this.updateById(dishDto);

        // 清理当前菜品口味表的信息 -->  dish_flavor表的删除操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        // 添加当前提交的菜品口味的信息 --> dish_flavor表的增加操作
        // 注意：此时添加不能单纯从dishDto中获取flavors信息，因为没有dishId字段的信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(item -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void removeWithCategory(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.eq(Dish::getId, ids);
        int count = this.count(queryWrapper);

        if (count > 0) {
            // 若查询结果大于0，则说明该菜品正在售卖中，不能删除
            throw new CustomException("菜品正在售卖中，不能删除");
        }

        // 若都为停售状态下，则根据id删除Dish表中信息
        this.removeByIds(ids);
    }
}




