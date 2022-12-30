package com.neko.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neko.reggie.common.R;
import com.neko.reggie.entity.Employee;
import com.neko.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 用户登录
     * @param httpServletRequest
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest httpServletRequest,
                             @RequestBody Employee employee){

        // 将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(lambdaQueryWrapper);

        // 如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }

        // 密码比对，如果不一致则返回登录失败的结果
        if (!password.equals(emp.getPassword())) {
            return R.error("登录失败");
        }

        // 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        // 登录成功，将员工id存入Session并返回登录成功结果
        httpServletRequest.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    /**
     * 用户退出
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getSession().getAttribute("employee") == null) {
            return R.error("退出失败");
        }
        // 清楚Session中保存的当前登录员工的ID
        httpServletRequest.getSession().removeAttribute("employee");
        return R.success("登录成功");
    }

    /**
     * 新增用户
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("用户信息为{}", employee.toString());

        // 设置初始密码123456，需要进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        // 获取登录用户ID
//        Long empID = (Long) request.getSession().getAttribute("employee");
//
//        // 设置创建该用户的ID
//        employee.setCreateUser(empID);
//        // 设置修改该用户的ID
//        employee.setUpdateUser(empID);
//
//        // 设置用户创建时间
//        employee.setCreateTime(LocalDateTime.now());
//        // 设置用户修改时间
//        employee.setUpdateTime(LocalDateTime.now());

        employeeService.save(employee);
        log.info("添加员工成功...");

        return R.success("新增员工成功");
    }

    /**
     * 获取员工分页信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> Page(int page, int pageSize, String name) {

        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);
        // 构造分页构造器
        Page pageInfo = new Page<>(page, pageSize);

        // 添加过滤条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        // 模糊匹配用户名<条件搜索>
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        // 按照修改时间倒序排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行分页查询
        employeeService.page(pageInfo, queryWrapper);
        log.info("获取分页数据...");

        // 返回分页查询结果，前端通过ElementUI展示数据
        return R.success(pageInfo);
    }

    /**
     * 修改员工信息
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("id={}, update={}", employee.getId(), employee.getStatus());

//        // 设置修改时间
//        employee.setUpdateTime(LocalDateTime.now());

//        // 获取修改者的ID信息，此处注意js中的long类型会丢失精度，
//        // 需要配置消息转换器把long类型的Java对象转换成String再响应成json数据发送给前端页面展示
//        Long employeeId = (Long) request.getSession().getAttribute("employee");

//        // 设置修改者ID
//        employee.setUpdateUser(employeeId);

        // 修改员工信息状态
        employee.setStatus(employee.getStatus());
        // 执行修改
        employeeService.updateById(employee);
        // 返回修改成功的信息
        return R.success("修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询...");
        Employee emp = employeeService.getById(id);
        if (emp != null) {
            return R.success(emp);
        }


        return R.error("没有查询到对应的员工信息");

    }
}
