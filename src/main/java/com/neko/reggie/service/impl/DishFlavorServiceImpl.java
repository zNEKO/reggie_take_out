package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.entity.DishFlavor;
import com.neko.reggie.service.DishFlavorService;
import com.neko.reggie.mapper.DishFlavorMapper;
import org.springframework.stereotype.Service;

/**
* @author z-nek
* @description 针对表【dish_flavor(菜品口味关系表)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:37
*/
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor>
    implements DishFlavorService{

}




