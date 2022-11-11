package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.entity.Orders;
import com.neko.reggie.service.OrdersService;
import com.neko.reggie.mapper.OrdersMapper;
import org.springframework.stereotype.Service;

/**
* @author z-nek
* @description 针对表【orders(订单表)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:37
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

}




