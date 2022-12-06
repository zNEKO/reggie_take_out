package com.neko.reggie.dto;

import com.neko.reggie.entity.Dish;
import com.neko.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    // 口味信息
    private List<DishFlavor> flavors = new ArrayList<>();

    // 菜品分类名称
    private String categoryName;

    private Integer copies;
}
