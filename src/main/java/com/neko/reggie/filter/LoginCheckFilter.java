package com.neko.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.neko.reggie.utils.BaseContext;
import com.neko.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    // 路劲匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求{}", requestURI);

        // 定义不需要处理的请求路径
        // 不拦截前后台静态资源的访问，只拦截相关动态数据
        String[] urls = new String[]{
                //登录请求
                "/employee/login",
                // 退出请求
                "/employee/logout",
                // 后台静态资源
                "/backend/**",
                // 前台静态资源
                "/front/**",
                // 前台用户登录
                "/user/login",
                "/common/**",
                // 发送验证码
                "/user/sendMsg"
        };

        // 判断本次请求是否需要处理
        Boolean check = check(urls, requestURI);

        // 如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 判断登录状态，如果已经登录，则直接放行【员工或管理员】
        if (request.getSession().getAttribute("employee") != null) {
            Long empId = (Long) request.getSession().getAttribute("employee");
            log.info("用户已登录,用户ID为{}", empId);
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request, response);
            return;
        }

        // 判断登录状态，如果已经登录，则直接放行【用户】
        if (request.getSession().getAttribute("user") != null) {
            Long userId = (Long) request.getSession().getAttribute("user");
            log.info("用户已登录,用户ID为{}", userId);
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, response);
            return;
        }

        // 如果未登录，则返回未登录结果，通过输出流的方式向客户端响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOT LOGIN")));
    }

    private Boolean check(String[] urls, String requestURI) {
        for (String uri : urls) {
            if (PATH_MATCHER.match(uri, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
