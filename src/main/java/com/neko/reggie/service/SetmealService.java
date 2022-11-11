package com.neko.reggie.service;

import java.util.*;
import com.neko.reggie.dto.SetmealDto;
import com.neko.reggie.entity.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author z-nek
* @description 针对表【setmeal(套餐)】的数据库操作Service
* @createDate 2022-11-05 15:51:37
*/
public interface SetmealService extends IService<Setmeal> {
    // 新增套餐，同时需要保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);
    // 删除套餐，同时要删除套餐中相应的菜品的关联关系
    public void removeWithDish(List<Long> ids);
}
