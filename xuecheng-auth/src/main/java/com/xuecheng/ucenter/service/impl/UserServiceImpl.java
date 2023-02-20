package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求：{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }
        String authType = authParamsDto.getAuthType();

        AuthService authService = applicationContext.getBean(authType + "_authService", AuthService.class);
        XcUserExt user = authService.execute(authParamsDto);

        return getUserPrincipal(user);
    }

    private UserDetails getUserPrincipal(XcUserExt user) {
        //用户权限,如果不加报 Cannot pass a null GrantedAuthority collection
        String[] authorities = {"p1"};
        String password = user.getPassword();
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将 user 对象转 json
        String userString = JSON.toJSONString(user);
        //创建 UserDetails 对象
        UserDetails userDetails = User.withUsername(userString).password("").authorities(authorities).build();
        return userDetails;

    }
}
