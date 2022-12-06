package com.neko.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neko.reggie.common.R;
import com.neko.reggie.dto.OrdersDto;
import com.neko.reggie.entity.OrderDetail;
import com.neko.reggie.entity.Orders;
import com.neko.reggie.entity.User;
import com.neko.reggie.service.OrderDetailService;
import com.neko.reggie.service.OrdersService;
import com.neko.reggie.service.UserService;
import com.neko.reggie.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDetailService orderDetailService;

    private int sumNum;

    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders) {
        log.info("updateStatus orders => {}", orders.toString());

        // 修改该用户订单状态值
        int status = orders.getStatus();
        if (status < 5) {
            status += 1;
        } else if(status == 4) {
            return R.error("该用户订单已完成配送!");
        } else if(status == 5) {
            return R.error("该用户订单已取消!");
        }

        // 设置修改条件
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orders.getId());
        updateWrapper.set(Orders::getStatus, status);
        ordersService.update(updateWrapper);

        return R.success("修改成功");
    }

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);

        ordersService.submit(orders);

        return R.success("下单成功");
    }

    @Transactional
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize) {
        log.info("userPage page = {}, pageSize = {}", page, pageSize);

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> dtoPageInfo = new Page<>(page, pageSize);

        // 获取当前用户id
        Long userId = BaseContext.getCurrentId();

        // 设置订单表信息
        LambdaQueryWrapper<Orders> orderQueryWrapper = new LambdaQueryWrapper<>();
        // 根据订单时间排序
        orderQueryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, orderQueryWrapper);

        BeanUtils.copyProperties(pageInfo, dtoPageInfo, "records");

        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> list = records.stream().map((item) -> {

            OrdersDto ordersDto = new OrdersDto();

            // 设置订单详情表信息
            BeanUtils.copyProperties(item, ordersDto);
            Long id = item.getId();

            // 根据id查询分类对象
            Orders orders = ordersService.getById(id);
            // 获取订单号
            String number = orders.getNumber();
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderDetail::getOrderId, number);
            List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);
            ordersDto.setOrderDetails(orderDetails);

            for (OrderDetail orderDetail : orderDetails) {
                sumNum += orderDetail.getNumber().intValue();
            }

            ordersDto.setSumNum(sumNum);

            return ordersDto;

        }).collect(Collectors.toList());

        dtoPageInfo.setRecords(list);
        dtoPageInfo.setTotal(pageInfo.getTotal());

        return R.success(dtoPageInfo);
    }

    @GetMapping("/page")
    public R<Page<OrdersDto>> page(@RequestParam("page") int page,
                                @RequestParam("pageSize") int pageSize,
                                @RequestParam(value = "number", required = false) String number,
                                @RequestParam(value = "beginTime", required = false) LocalDateTime beginTime,
                                @RequestParam(value = "endTime", required = false) LocalDateTime endTime) {
        log.info("OrderController page = {}, pageSize = {}, number = {}, beginTime = {}, endTime = {}",
                page, pageSize, number, beginTime, endTime);

        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(number != null, Orders::getNumber, number);
        queryWrapper.ge(beginTime != null, Orders::getOrderTime, beginTime);
        queryWrapper.le(endTime != null, Orders::getCheckoutTime, endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, queryWrapper);

        Page<OrdersDto> dtoPageInfo = new Page<>(page, pageSize);
        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> list = records.stream().map(item -> {

            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);

            Long userId = item.getUserId();
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.eq(User::getStatus, 1);
            userQueryWrapper.eq(User::getId, userId);
            User user = userService.getOne(userQueryWrapper);
            if (user != null) {
                ordersDto.setUserName(user.getName());
            }

            return ordersDto;

        }).collect(Collectors.toList());

        dtoPageInfo.setRecords(list);
        dtoPageInfo.setTotal(pageInfo.getTotal());

        return R.success(dtoPageInfo);
    }

}
