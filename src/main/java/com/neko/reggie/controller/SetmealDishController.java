package com.neko.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neko.reggie.common.R;
import com.neko.reggie.dto.SetmealDto;
import com.neko.reggie.entity.Category;
import com.neko.reggie.entity.Setmeal;
import com.neko.reggie.service.CategoryService;
import com.neko.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealDishController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{id}")
    public R<Setmeal> getSetmeal(@PathVariable Long id) {
        log.info("id = {}", id);

        Setmeal setmeal = setmealService.getById(id);

        return R.success(setmeal);
    }

    /**
     * 添加菜品套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("setmealDto = {}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        log.info("添加套餐成功...");

        return R.success("添加套餐成功");
    }

    @Transactional
    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") int page,
                        @RequestParam("pageSize") int pageSize,
                        @RequestParam(value = "name", required = false) String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        // 构建分页构造器
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        setmealService.page(setmealPage, queryWrapper);

        List<Setmeal> pageRecords = setmealPage.getRecords();

        BeanUtils.copyProperties(setmealPage, setmealDtoPage,"records");

        List<SetmealDto> setmealDtoList = pageRecords.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            // 把Setmeal表中的属性赋值给SetmealDto
            BeanUtils.copyProperties(item, setmealDto);
            // 根据分类id查询菜品分类数据
            Category category = categoryService.getById(item.getCategoryId());

            if (category != null) {
                // 获取菜品分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);

        return R.success(setmealDtoPage);
    }

    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids = {}", ids);

        setmealService.removeWithDish(ids);

        log.info("菜品套餐删除成功...");

        return R.success("菜品套餐删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        log.info("list setMeal = {}", setmeal);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());

        return R.success(setmealService.list(queryWrapper));
    }

    /**
     * 修改套餐状态值
     * @param status
     * @param ids
     * @return
     */

    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> update(@PathVariable int status, @RequestParam List<Long> ids) {
        log.info("update = {}, ids = {}", status, ids);

        // 设置该套餐的状态信息
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        for (Long id : ids) {
            updateWrapper.eq(Setmeal::getId, id);
            updateWrapper.set(Setmeal::getStatus, status);
        }

        // 执行修改
        setmealService.update(updateWrapper);

        return R.success("停售该套餐成功");
    }
}
