package com.neko.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.neko.reggie.common.R;
import com.neko.reggie.entity.AddressBook;
import com.neko.reggie.service.AddressBookService;
import com.neko.reggie.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询用户全部的地址
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        log.info("addressBook = {}", addressBook);

        // 为实体类封装当前用户id
        addressBook.setUserId(BaseContext.getCurrentId());

        // 设置查询条件
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        // 执行查询
        List<AddressBook> addressBooks = addressBookService.list(queryWrapper);

        log.info("查询用户收获地址成功...");

        return R.success(addressBooks);
    }

    /**
     * 添加用户收获地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());

        log.info("save addressBook = {}", addressBook);

        addressBookService.save(addressBook);

        return R.success(addressBook);
    }

    // 查询默认地址
    @GetMapping("/default")
    public R<AddressBook> getDefault(@RequestBody Long id) {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, id);
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (addressBook == null) {
            return R.error("没有查到该对象");
        }

        return R.success(addressBook);
    }

    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id) {
        log.info("id = {}", id);

        AddressBook addressBook = addressBookService.getById(id);

        if (addressBook == null) {
            return R.error("没有查到该对象");
        }

        return R.success(addressBook);
    }

    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook = {}", addressBook);

        // 修改当前用户id中所有地址的默认状态都为0，防止isDefault字段出现多个默认状态为1的情况
        LambdaUpdateWrapper<AddressBook> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.set(AddressBook::getIsDefault, 0);
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());

        // 修改当前用户的地址默认状态为1
        addressBookService.update(queryWrapper);

        // 修改当前用户的所选地址的默认状态为1
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return R.success(addressBook);
    }
}
