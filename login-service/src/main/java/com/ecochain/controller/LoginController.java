package com.ecochain.controller;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.ecochain.config.CoreMessageSource;
import com.ecochain.util.ValidationCodeUtil;
import com.ecochain.util.ValidationCodeWrap;

@RestController
public class LoginController{
    
    @Autowired
    private CoreMessageSource messageSource;
    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login() throws SQLException{
        
        return new ModelAndView("login");
        
    }
    
    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    public String getToken(HttpServletRequest request) throws SQLException{
        //TODO 将ValidationCodeWrap.image 已流的形式直接输出给前台
        ValidationCodeWrap codeWrap = ValidationCodeUtil.getSesionCode();
        request.getSession().setAttribute("_validationCode", codeWrap.getVc());
        return codeWrap.getVc().getCode();
        
    }
    
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public ModelAndView getHello() throws SQLException{
        
        return new ModelAndView("hello");
    }
    
//    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView home() throws SQLException{
        
        return new ModelAndView("home");
        
    }
    
    @RequestMapping("/safetyOfficers")
    public ModelAndView safetyOfficers() throws SQLException{
        
        return new ModelAndView("home");
        
    }
    
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView index() throws SQLException{
        
        return new ModelAndView("index");
    }
    
    @RequestMapping(value = "/loginexpired", method = RequestMethod.GET)
    public String loginexpired() throws SQLException{
        
        return "被踢出了";
    }
    
    /**  
     * 切换国际化语言<p>
     * lang=zh_CN 为中文<br>
     * lang=en_US 为英文
     * <p>
     * zh_CN和en_US 为国际化配置文件后缀
     * @Title changeLang  
     * @param lang
     * @return String   
     */  
    @GetMapping("/changeLang")
    public String changeLang(@RequestParam(required = false) String lang){
        return messageSource.getMessage("test.addSuccess");
    }

}
