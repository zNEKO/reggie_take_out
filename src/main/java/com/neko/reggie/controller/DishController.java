package com.neko.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neko.reggie.common.R;
import com.neko.reggie.dto.DishDto;
import com.neko.reggie.entity.Category;
import com.neko.reggie.entity.Dish;
import com.neko.reggie.entity.DishFlavor;
import com.neko.reggie.service.CategoryService;
import com.neko.reggie.service.DishFlavorService;
import com.neko.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 菜品分页信息查询
     * @param page
     * @param pageSize
     * @return
     */
    @Cacheable(value = "dishCache",
            key = "#page + '_' + #pageSize + '_' + #name")
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        // 构建分页构造器对象
        Page<Dish> dishPageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishFlavorPageInfo = new Page<>(page, pageSize);

        // 创建条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.like(name != null, Dish::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        // 执行分页查询
        dishService.page(dishPageInfo, queryWrapper);

        // 对象拷贝
        List<Dish> records = dishPageInfo.getRecords();

        List<DishDto> list = records.stream().map(item -> {
            DishDto dishDto = new DishDto();

            // 将item中属性的值复制到dishDto中
            BeanUtils.copyProperties(item, dishDto);

            // 获取分类ID
            Long categoryId = item.getCategoryId();

            // 根据分类ID查询对象
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }

            return dishDto;
        }).collect(Collectors.toList());

        // 往DishDto实体类中设置菜品信息和菜品分类信息
        dishFlavorPageInfo.setRecords(list);
        dishFlavorPageInfo.setTotal(dishPageInfo.getTotal());

        return R.success(dishFlavorPageInfo);
    }

    /**
     * 添加菜品信息
     * @param dishDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("dishDto = {}", dishDto);

        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);

        dishService.saveWithFlavor(dishDto);

        log.info("添加菜品信息成功...");

        return R.success("添加菜品成功");
    }

    /**
     * 根据dishId查询菜品基本信息和菜品口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Cacheable(value = "dishCache", key = "#id")
    public R<DishDto> get(@PathVariable Long id) {
        log.info("dishId = {}", id);

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        log.info("查询菜品信息和菜品口味信息成功...");

        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("dishDto = {}", dishDto.toString());

        // 清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        // 清理某个分类下的菜品缓存数据
//        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
//        redisTemplate.delete(key);

        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品信息成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
//    @Cacheable(value = "dishCache", key = "#dish.categoryId + '_' + #dish.update")
    public R<List<DishDto>> list(Dish dish) {
        log.info("dish = {}", dish);

        // 动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        List<DishDto> dishDtoList = null;

        // 先从Redis获取缓存数据
        List<DishDto> dataList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dataList != null) {
            // 如果存在，直接返回，无需查询数据库
            return R.success(dataList);
        } else {
            // 如果不存在，需要查询数据库，将查询的菜品数据缓存到Redis中

            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

            // 添加查询条件
            queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
            // 查询起售状态（update = 1）的菜品
            queryWrapper.eq(Dish::getStatus, 1);

            // 添加排序条件
            queryWrapper.orderByDesc(Dish::getUpdateTime).orderByAsc(Dish::getSort);

            //执行查询
            List<Dish> dishList = dishService.list(queryWrapper);

            dishDtoList = dishList.stream().map((item) -> {
                // 此item为Dish实体类
                DishDto dishDto = new DishDto();
                BeanUtils.copyProperties(item, dishDto);

                Category category = categoryService.getById(item.getCategoryId());
                if (category != null) {
                    // dishDto设置菜品分类名称
                    dishDto.setCategoryName(category.getName());
                }
                // 设置dishDto中口味信息字段
                LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
                dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
                dishDto.setFlavors(dishFlavorService.list(dishFlavorLambdaQueryWrapper));

                // 返回赋值
                return dishDto;
            }).collect(Collectors.toList());

            redisTemplate.opsForValue().set(key, dishDtoList, 1, TimeUnit.HOURS);

            return R.success(dishDtoList);
        }
    }

    /**
     * 删除菜品信息
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids = {}", ids);

        dishService.removeWithCategory(ids);

        log.info("删除菜品信息成功...");

        return R.success("删除菜品信息成功");
    }

    @PostMapping("/status/{status}")
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> haltSales(@PathVariable int status, @RequestParam List<Long> ids) {
        log.info("update = {}, ids = {}", status, ids);

        for (Long id : ids) {
            // ========================== 方式一 ==================================
            // 设置修改菜品状态信息
            LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Dish::getId, id);
            updateWrapper.set(Dish::getStatus, status);
            // 执行修改
            dishService.update(updateWrapper);
        }


        return R.success("停售该菜品成功");
    }
}
