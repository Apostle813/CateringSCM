package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.constant.MessageConstant;
import com.student.scm.context.BaseContext;
import com.student.scm.dto.ChangePasswordDTO;
import com.student.scm.dto.ScmSysUserLoginDTO;
import com.student.scm.dto.ScmSysUserPageQueryDTO;
import com.student.scm.entity.ScmSysRole;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.mapper.ScmSysRoleMapper;
import com.student.scm.mapper.ScmSysUserMapper;
import com.student.scm.properties.JwtProperties;
import com.student.scm.service.IScmSysUserService;
import com.student.scm.utils.JwtUtil;
import com.student.scm.vo.ScmSysUserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class ScmSysUserServiceImpl extends ServiceImpl<ScmSysUserMapper, ScmSysUser> implements IScmSysUserService {
    private JwtProperties jwtProperties;
    private ScmSysUserMapper userMapper;
    private ScmSysRoleMapper roleMapper;

    public ScmSysUserServiceImpl(JwtProperties jwtProperties, ScmSysUserMapper userMapper, ScmSysRoleMapper roleMapper) {
        this.jwtProperties = jwtProperties;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public Page<ScmSysUser> queryPageByCondition(ScmSysUserPageQueryDTO queryDTO) {
        Page<ScmSysUser> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<ScmSysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(ScmSysUser::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getRealName())) {
            wrapper.like(ScmSysUser::getRealName, queryDTO.getRealName());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(ScmSysUser::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(ScmSysUser::getCreateTime);
        return this.page(page, wrapper);
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
        //账号处于禁用状态
        if (user.getStatus() == 0) {throw new RuntimeException(MessageConstant.ACCOUNT_STOP_USE);}
        //判断密码是否正确
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException(MessageConstant.PASSWORD_ERROR);
        }

        // 查询角色信息
        ScmSysRole role = roleMapper.selectById(user.getRoleId());
        String roleCode = role != null ? role.getRoleCode() : "";

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
                .roleCode(roleCode)
                .build();
    }

    @Override
    public ScmSysUserVO getProfile() {
        // 1. 从 ThreadLocal 中获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        
        // 2. 查询用户信息
        ScmSysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 3. 查询角色信息
        ScmSysRole role = roleMapper.selectById(user.getRoleId());
        String roleCode = role != null ? role.getRoleCode() : "";
        String roleName = role != null ? role.getRoleName() : "";
        
        // 4. 构建并返回 VO
        return ScmSysUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .sex(user.getSex())
                .roleCode(roleCode)
                .roleName(roleName)
                .status(user.getStatus())
                .build();
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        // 1. 获取当前登录用户
        Long currentUserId = BaseContext.getCurrentId();
        ScmSysUser currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 查询当前用户的角色
        ScmSysRole role = roleMapper.selectById(currentUser.getRoleId());
        boolean isAdmin = role != null && "ADMIN".equalsIgnoreCase(role.getRoleCode());

        // 3. 确定目标用户
        Long targetUserId = (dto.getUserId() != null) ? dto.getUserId() : currentUserId;

        // 4. 非管理员：只能改自己的密码，且必须验证旧密码
        if (!isAdmin) {
            if (!targetUserId.equals(currentUserId)) {
                throw new RuntimeException(MessageConstant.NO_PERMISSION_CHANGE_PASSWORD);
            }
            if (dto.getOldPassword() == null || dto.getOldPassword().trim().isEmpty()) {
                throw new RuntimeException("请填写原密码");
            }
            String oldMd5 = DigestUtils.md5DigestAsHex(dto.getOldPassword().getBytes());
            if (!oldMd5.equals(currentUser.getPassword())) {
                throw new RuntimeException(MessageConstant.OLD_PASSWORD_ERROR);
            }
        } else {
            // 管理员：如果修改的是自己的密码，也需要验证旧密码
            if (targetUserId.equals(currentUserId)) {
                if (dto.getOldPassword() == null || dto.getOldPassword().trim().isEmpty()) {
                    throw new RuntimeException("修改自己的密码请填写原密码");
                }
                String oldMd5 = DigestUtils.md5DigestAsHex(dto.getOldPassword().getBytes());
                if (!oldMd5.equals(currentUser.getPassword())) {
                    throw new RuntimeException(MessageConstant.OLD_PASSWORD_ERROR);
                }
            }
        }

        // 5. 更新密码
        if (dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        ScmSysUser targetUser = new ScmSysUser();
        targetUser.setId(targetUserId);
        targetUser.setPassword(DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes()));
        userMapper.updateById(targetUser);
    }
}
