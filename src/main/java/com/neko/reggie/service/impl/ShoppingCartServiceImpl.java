package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.entity.ShoppingCart;
import com.neko.reggie.service.ShoppingCartService;
import com.neko.reggie.mapper.ShoppingCartMapper;
import org.springframework.stereotype.Service;

/**
* @author z-nek
* @description 针对表【shopping_cart(购物车)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:38
*/
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
    implements ShoppingCartService{

}




