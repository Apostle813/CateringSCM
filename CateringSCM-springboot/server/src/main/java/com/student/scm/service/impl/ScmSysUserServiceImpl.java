package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.constant.MessageConstant;
import com.student.scm.dto.ScmSysUserLoginDTO;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.mapper.ScmSysUserMapper;
import com.student.scm.properties.JwtProperties;
import com.student.scm.service.IScmSysUserService;
import com.student.scm.utils.JwtUtil;
import com.student.scm.vo.ScmSysUserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class ScmSysUserServiceImpl extends ServiceImpl<ScmSysUserMapper, ScmSysUser> implements IScmSysUserService {
    private JwtProperties jwtProperties;
    private ScmSysUserMapper userMapper;

    public ScmSysUserServiceImpl(JwtProperties jwtProperties,ScmSysUserMapper userMapper) {
        this.jwtProperties = jwtProperties;
        this.userMapper = userMapper;
    }

    @Override
    public ScmSysUserVO login(ScmSysUserLoginDTO loginUser) {
        String username = loginUser.getUsername();
        String password = loginUser.getPassword();
        // 1. 根据前端传来的 username 查询数据库
        LambdaQueryWrapper<ScmSysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScmSysUser::getUsername, username);
        ScmSysUser user = userMapper.selectOne(queryWrapper);

        // 2. 核心业务判断：如果不符合条件，直接抛出 RuntimeException 异常
        //账号不存在
        if (user == null) {throw new RuntimeException(MessageConstant.ACCOUNT_NOT_FOUND);}
        //账号处于删除状态
        if (user.getIsDeleted()==1) {throw new RuntimeException(MessageConstant.ACCOUNT_DELETE);}
        //账号处于禁用状态
        if (user.getStatus() == 0) {throw new RuntimeException(MessageConstant.ACCOUNT_STOP_USE);}
        //判断密码是否正确
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException(MessageConstant.PASSWORD_ERROR);
        }

        // 3. 验证通过，准备生成 JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roleId", user.getRoleId());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        // 4. 封装返回给 Controller 的数据
        user.setPassword(null); // 安全起见，抹除密码
        return ScmSysUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .token(token)
                .build();
    }
}
