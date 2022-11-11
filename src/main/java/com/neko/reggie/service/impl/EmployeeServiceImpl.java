package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.entity.Employee;
import com.neko.reggie.service.EmployeeService;
import com.neko.reggie.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;

/**
* @author z-nek
* @description 针对表【employee(员工信息)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:37
*/
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
    implements EmployeeService{

}




