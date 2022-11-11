package com.neko.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    /**
     * 添加菜品套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("setmealDto = {}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        log.info("添加套餐成功...");

        return R.success("添加套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        // 构建分页构造器
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(name != null, Setmeal::getName, name);
        setmealService.page(setmealPage);

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
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids = {}", ids);

        setmealService.removeWithDish(ids);

        log.info("菜品套餐删除成功...");

        return R.success("菜品套餐删除成功");
    }

}
