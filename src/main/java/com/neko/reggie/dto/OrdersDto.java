package com.neko.reggie.dto;

import com.neko.reggie.entity.OrderDetail;
import com.neko.reggie.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private int sumNum;

    private List<OrderDetail> orderDetails;
	
}
