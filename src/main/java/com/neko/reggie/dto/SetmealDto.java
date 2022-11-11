package com.neko.reggie.dto;

import com.neko.reggie.entity.Setmeal;
import com.neko.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
