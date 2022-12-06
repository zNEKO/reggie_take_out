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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        log.info("user = {}", user);
        // 获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            // 生成四位随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);

            // 需要将生成的验证码保存到Session中
            //session.setAttribute(phone, code);

            // 将生成的验证码缓存到Redis中，并且设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("手机验证码短信发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpServletRequest request) {
        log.info("loginMap = {}", map.toString());

        // 获取Session域
        HttpSession httpSession = request.getSession();

        // 获取手机号
        String phone = map.get("phone").toString();

        // 获取手机验证码
        String code = map.get("code").toString();

        // 从Session中获取验证码
//        String codeInSession = (String) httpSession.getAttribute(phone);

        // 从Redis中获取缓存的验证码
        String codeInRedis = redisTemplate.opsForValue().get(phone).toString();

        // 进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if (codeInRedis != null && codeInRedis.equals(code)) {
            // 如果code相等则说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            if (user == null) {
                // 如果user为null，则说明为新用户，需为该用户注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }

            httpSession.setAttribute("user", user.getId());

            // 如果用户登录成功，则删除Redis中缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);

        }

        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        log.info("退出当前用户成功......");
        return R.success("退出当前用户成功");
    }

}
