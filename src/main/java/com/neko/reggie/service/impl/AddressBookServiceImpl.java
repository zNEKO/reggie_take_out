package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.entity.AddressBook;
import com.neko.reggie.service.AddressBookService;
import com.neko.reggie.mapper.AddressBookMapper;
import org.springframework.stereotype.Service;

/**
* @author z-nek
* @description 针对表【address_book(地址管理)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:37
*/
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook>
    implements AddressBookService{

}




