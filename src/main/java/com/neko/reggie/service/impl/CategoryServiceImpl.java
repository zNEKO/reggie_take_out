package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.Exception.CustomException;
import com.neko.reggie.entity.Category;
import com.neko.reggie.entity.Dish;
import com.neko.reggie.entity.Setmeal;
import com.neko.reggie.service.CategoryService;
import com.neko.reggie.mapper.CategoryMapper;
import com.neko.reggie.service.DishFlavorService;
import com.neko.reggie.service.DishService;
import com.neko.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author z-nek
* @description 针对表【category(菜品及套餐分类)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:37
*/
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    public void deleteById(Long id) {
        // 查看当前分类是否关联了菜品，如果已经关联，则抛出一个业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int countDish = dishService.count(dishLambdaQueryWrapper);
        if (countDish > 0) {
            throw new CustomException("此分类关联了菜品，不能删除");
        }

        // 查看当前分类是否关联了套餐，如果已经关联，则抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int countSetmeal = setmealService.count(setmealLambdaQueryWrapper);
        if (countSetmeal > 0) {
            throw new CustomException("此分类关联了套餐，不能删除");
        }

        // 正常删除分类
        if (super.removeById(id)) {
            log.info("成功删除...");
        }
    }
}




