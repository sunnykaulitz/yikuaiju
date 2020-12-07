package com.example.yikuaiju.controller;

import com.example.yikuaiju.bean.Ykj_admin;
import com.example.yikuaiju.bean.resultbean.operation.OperatorGame;
import com.example.yikuaiju.bean.resultbean.operation.OperatorUser;
import com.example.yikuaiju.mapper.Ykj_gameMapper;
import com.example.yikuaiju.mapper.Ykj_userMapper;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IOperatorService;
import com.example.yikuaiju.util.BeanMapConvertUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("hello")
@Transactional
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private ICommonService commonService;

    @Autowired
    private Ykj_gameMapper gameMapper;

    @Autowired
    private Ykj_userMapper userMapper;

    /**
     * 跳转到用户登录页面
     * @return 登录页面
     */
    @RequestMapping("/index")
    public String sayHello(){
        return "login";
    }

    /**
     * 退出登录
     * @return
     */
    @RequestMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().removeAttribute("session_user");
        return "login";
    }

    /**
     * 获取用户名与密码，用户登录
     * @return 登录成功页面
     */
    @RequestMapping(value = {"/adminLogin"})
    public String userLogin(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest request){
        if (!StringUtils.isEmptyOrWhitespace(username) && !StringUtils.isEmptyOrWhitespace(password)) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("username", username);
            params.put("password", password);
            List<Map<String, Object>> admin = commonService.select(
                    "select *from ykj_admin where username=:username and password=:password", params);

            if (admin != null && admin.size()>0) {                                                  //登录成功
                try {
                    request.getSession().setAttribute("session_user", BeanMapConvertUtil.mapToBean(Ykj_admin.class, admin.get(0)));     //将用户信息放入session
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    return "loginError";
                }
                return "redirect:/hello/userList";
            }
        }
        logger.error("用户名密码错误");
        return "loginError";
    }

    @RequestMapping("/usernav")
    public String usernav(HttpServletRequest request) {
        Ykj_admin admin = (Ykj_admin) request.getSession().getAttribute("session_user");
        if(admin != null)
            return "redirect:/hello/userList";
        else
            return "login";
    }

    @RequestMapping("/gamenav")
    public String gamenav(HttpServletRequest request) {
        Ykj_admin admin = (Ykj_admin) request.getSession().getAttribute("session_user");
        if(admin != null)
            return "redirect:/hello/gameList";
        else
            return "login";
    }

    @RequestMapping("/userList")
    public String userList(Model model , @RequestParam(value="pageNum",defaultValue="1")Integer pageNum,
                       @RequestParam(value="pageSize",defaultValue="10")Integer pageSize,
                           @ModelAttribute OperatorUser user, HttpServletRequest request) {
        Ykj_admin admin = (Ykj_admin) request.getSession().getAttribute("session_user");
        if(admin == null)
            return "login";
        if(!StringUtils.isEmptyOrWhitespace(user.getNickName())){
            if(org.apache.commons.lang3.StringUtils.isNumericSpace(user.getNickName()))
                user.setId(Integer.parseInt(user.getNickName()));
            user.setMobile(user.getNickName());
            user.setMark(user.getNickName());
        }

        //1.引入分页插件,pageNum是第几页，pageSize是每页显示多少条,默认查询总数count
        PageHelper.startPage(pageNum,pageSize);

        //2.紧跟的查询就是一个分页查询-必须紧跟.后面的其他查询不会被分页，除非再次调用PageHelper.startPage
        List<OperatorUser> userList = userMapper.selectOperatorUser(user);

        //3.使用PageInfo包装查询后的结果,5是连续显示的条数,结果list类型是Page<E>
        PageInfo pageInfo = new PageInfo(userList,pageSize);

        //4.使用model/map/modelandview等带回前端
        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("user",user);
        return "userList";
    }

    @RequestMapping("/gameList")
    public String gameList(Model model , @RequestParam(value="pageNum",defaultValue="1")Integer pageNum,
                       @RequestParam(value="pageSize",defaultValue="10")Integer pageSize,
                           @ModelAttribute OperatorGame game, HttpServletRequest request) {
        Ykj_admin admin = (Ykj_admin) request.getSession().getAttribute("session_user");
        if(admin == null)
            return "login";
        if(!StringUtils.isEmptyOrWhitespace(game.getName())){
            if(org.apache.commons.lang3.StringUtils.isNumericSpace(game.getName()))
                game.setId(Integer.parseInt(game.getName()));
            game.setGroupname(game.getName());
        }

        //1.引入分页插件,pageNum是第几页，pageSize是每页显示多少条,默认查询总数count
        PageHelper.startPage(pageNum,pageSize);

        //2.紧跟的查询就是一个分页查询-必须紧跟.后面的其他查询不会被分页，除非再次调用PageHelper.startPage
        List<OperatorGame> gameList = gameMapper.selectOperatorGame(game);
//        List<Map<String,Object>> gameList = operatorService.gameList();

        //3.使用PageInfo包装查询后的结果,5是连续显示的条数,结果list类型是Page<E>
        PageInfo pageInfo = new PageInfo(gameList,pageSize);

        //4.使用model/map/modelandview等带回前端
        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("game",game);
        return "gameList";
    }

    @RequestMapping(value = "/updateUserMark",method = RequestMethod.POST)
    public String updateUserMark(@RequestParam(value="mark", required = true)String mark,
                                 @RequestParam(value="userid", required = true)Integer userid, HttpServletRequest request){
        Ykj_admin admin = (Ykj_admin) request.getSession().getAttribute("session_user");
        if(admin == null)
            return "login";
        commonService.execute("update ykj_user set mark='"+mark+"' where id="+userid);
        return "redirect:/hello/userList";
    }
}
