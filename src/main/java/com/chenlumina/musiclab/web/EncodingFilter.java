package com.chenlumina.musiclab.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * 统一编码过滤器：拦截 /*，在请求进入任何 Servlet/JSP 之前设置 UTF-8。
 * 重构前每个 doGet/doPost 都要重复写编码设置，现在集中到这一处。
 * 在 web.xml 中排在 AuthFilter 之前，保证登录检查与业务读到的参数已是正确编码。
 */
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}