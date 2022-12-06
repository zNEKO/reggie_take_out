package com.neko.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neko.reggie.common.R;
import com.neko.reggie.entity.Category;
import com.neko.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@ResponseBody
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    /**
     * 新增菜品分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category = {}", category.toString());

        categoryService.save(category);

        log.info("成功插入数据...");

        return R.success("新增分类成功");
    }

    /**
     * 获取菜品分页数据
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        log.info("page = {}, pageSize = {}", page, pageSize);

        //创建分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        // 根据实体类中Sort属性升序排序
        queryWrapper.orderByAsc(Category::getSort);

        // 进行分页查询
        categoryService.page(pageInfo);

        return R.success(pageInfo);
    }

    /**
     * 删除菜品分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        log.info("根据 ids = {} 删除菜品分类", ids);

        categoryService.deleteById(ids);

        return R.success("删除菜品成功");
    }

    /**
     * 修改菜品分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("菜品分类信息=>category = {}", category.toString());

        categoryService.updateById(category);

        log.info("菜品分类修改成功...");

        return R.success("菜品分类信息修改成功");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        log.info("category = {}", category.toString());

        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        // 添加条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());

        // 添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //查询结果
        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);
    }



}
