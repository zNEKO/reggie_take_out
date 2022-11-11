package com.neko.reggie.service;

import com.neko.reggie.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestCategoryService {

    @Autowired
    private CategoryService categoryService;

    @Test
    public void test() {
        List<Category> list = categoryService.list();
        list.forEach(System.out::println);
    }


}
