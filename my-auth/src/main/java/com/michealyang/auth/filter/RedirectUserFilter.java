package com.michealyang.auth.filter;

import com.michealyang.auth.domain.User;
import com.michealyang.auth.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by michealyang on 16/3/20.
 */
@Component
public class RedirectUserFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RedirectUserFilter.class);

    private static final String LOGIN_URI = "/auth/user/r/login";
    private static final String SIGNUP_URI = "/auth/user/r/signup";
    private static final String LOGOUT_URI = "/auth/user/r/logout";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String uri = request.getRequestURI();
        logger.info("[doFilterInternal] uri=#{}", uri);

        UserUtil.unbindUser();

        //1. 对于静态资源，要通过
        //2. 对于API接口，要通过
        if(uri.startsWith("/static/")
                || uri.startsWith("/api/")
                || uri.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        if("/favicon.ico".equals(uri)){
            filterChain.doFilter(request, response);
            return;
        }

        //3. 对于Ajax请求，要通过
        String requestType = request.getHeader("X-Requested-With");
        if("XMLHttpRequest".equals(requestType)){
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if(session != null) {
            logger.info("sessionId=#{}", session.getId());
        }
        User user = (User)WebUtils.getSessionAttribute(request, "user");
        logger.info("[doFilterInternal] user=#{}", user);
        if ((!uri.equals(LOGIN_URI) && !uri.equals(SIGNUP_URI) && !uri.equals(LOGOUT_URI))
                && user == null) {
            response.sendRedirect(LOGIN_URI);
            return;
        }

        if(uri.equals(LOGIN_URI)){
            request.setAttribute("signupUrl", StringUtils.isBlank(request.getQueryString()) ? SIGNUP_URI : SIGNUP_URI + "?" + request.getQueryString());
        }
        if(uri.equals(SIGNUP_URI)){
            request.setAttribute("loginUrl", StringUtils.isBlank(request.getQueryString()) ? LOGIN_URI : LOGIN_URI + "?" + request.getQueryString());
        }

        UserUtil.bind(user);

        filterChain.doFilter(request, response);
    }
}
