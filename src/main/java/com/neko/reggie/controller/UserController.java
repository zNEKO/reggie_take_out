package com.neko.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neko.reggie.common.R;
import com.neko.reggie.entity.AddressBook;
import com.neko.reggie.entity.User;
import com.neko.reggie.service.UserService;
import com.neko.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        log.info("user = {}", user);
        // 获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            // 生成四位随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);

            session.setAttribute(phone, code);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("手机验证码短信发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession httpSession) {
        log.info("loginMap = {}", map.toString());

        // 获取手机号
        String phone = map.get("phone").toString();

        // 获取手机验证码
        String code = map.get("code").toString();

        String codeInSession = (String) httpSession.getAttribute(phone);

        // 进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if (codeInSession != null && codeInSession.equals(code)) {
            // 如果code相等则说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            if (user == null) {
                // 如果user为null，则说明为新用户，需为该用户注册
                User newUser = new User();
                newUser.setPhone(phone);
                newUser.setStatus(1);
                userService.save(newUser);
            }

            httpSession.setAttribute("user", user.getId());

            return R.success(user);
        }

        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request) {

        request.getSession().removeAttribute("user");

        return R.success("退出当前用户成功");
    }

}
