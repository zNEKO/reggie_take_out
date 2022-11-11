package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.Exception.CustomException;
import com.neko.reggie.dto.SetmealDto;
import com.neko.reggie.entity.Setmeal;
import com.neko.reggie.entity.SetmealDish;
import com.neko.reggie.service.SetmealDishService;
import com.neko.reggie.service.SetmealService;
import com.neko.reggie.mapper.SetmealMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author z-nek
* @description 针对表【setmeal(套餐)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:37
*/
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
    implements SetmealService{

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息，操作setmeal表，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，确定套餐是否售卖中
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(setmealLambdaQueryWrapper);

        // 如果所有套餐售卖中，不能删除，则抛出一个自定义异常
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        // 如果可以删除，则先把Setmeal表中的字段先删除
        this.removeByIds(ids);

        // 删除关系表中的数据 --> Setmeal_Dish
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);

    }
}




