package com.example.yikuaiju;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Error500  implements HandlerExceptionResolver {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        ModelAndView mav = new ModelAndView();
        if(e.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")){
            log.debug("304缓存错误,不做处理!");
            mav.setStatus(HttpStatus.NOT_MODIFIED);
            return mav;
        }
        e.printStackTrace();
        mav.setViewName("redirect:/error-500");
        return mav;
    }
}
