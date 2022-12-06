package com.neko.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.neko.reggie.common.R;
import com.neko.reggie.entity.ShoppingCart;
import com.neko.reggie.service.ShoppingCartService;
import com.neko.reggie.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @Transactional
    @PostMapping("/add")
    public R<ShoppingCart> save(@RequestBody ShoppingCart shoppingCart) {
        log.info("save shoppingCart = {}", shoppingCart);

        // 设置用户ID，指定当前是哪个用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        if (shoppingCart.getDishId() != null) {
            // 添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            // 添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        // 查询当前菜品或套餐是否在购物车中
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);

        if (cart != null) {
            // 如果已经存在该菜品，则在原来的数量上+1
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartService.updateById(cart);
        } else {
            // 如果不存在，则添加到购物车，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }

        return R.success(cart);
    }

    /**
     * 查看购物车数据
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车...");

        Long currentId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        return R.success(shoppingCartService.list(queryWrapper));
    }

    @DeleteMapping("/clean")
    public R<String> clean() {
        log.info("清空购物车...");

        Long currentId = BaseContext.getCurrentId();
        shoppingCartService.removeById(currentId);

        return R.success("购物车清空成功");
    }

    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart cart) {
        log.info("购物车清除数据 setmealId => {}, dishId => {}", cart.getSetmealId(), cart.getDishId());

        LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(cart.getSetmealId() != null, ShoppingCart::getSetmealId, cart.getSetmealId());
        updateWrapper.eq(cart.getDishId() != null, ShoppingCart::getDishId, cart.getDishId());
        shoppingCartService.remove(updateWrapper);

        return R.success("购物车删除数据成功");
    }
}

