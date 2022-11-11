package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.entity.OrderDetail;
import com.neko.reggie.service.OrderDetailService;
import com.neko.reggie.mapper.OrderDetailMapper;
import org.springframework.stereotype.Service;

/**
* @author z-nek
* @description 针对表【order_detail(订单明细表)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:37
*/
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail>
    implements OrderDetailService{

}




