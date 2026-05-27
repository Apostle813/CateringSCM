package com.student.server.service;

import com.student.scm.context.BaseContext;
import com.student.scm.dto.ChangePasswordDTO;
import com.student.scm.dto.ScmSysUserLoginDTO;
import com.student.scm.entity.ScmSysRole;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.mapper.ScmSysRoleMapper;
import com.student.scm.mapper.ScmSysUserMapper;
import com.student.scm.properties.JwtProperties;
import com.student.scm.service.impl.ScmSysUserServiceImpl;
import com.student.scm.vo.ScmSysUserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScmSysUserServiceTest {

    @Mock private JwtProperties jwtProperties;
    @Mock private ScmSysUserMapper userMapper;
    @Mock private ScmSysRoleMapper roleMapper;

    private ScmSysUserServiceImpl userService;

    @BeforeEach
    void setUp() throws Exception {
        userService = new ScmSysUserServiceImpl(jwtProperties, userMapper, roleMapper);
        Class<?> clazz = userService.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field f = clazz.getDeclaredField("baseMapper");
                f.setAccessible(true);
                f.set(userService, userMapper);
                break;
            } catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
    }

    // ===== T1-01: 正确账号密码登录 =====
    @Test
    void testLogin_Success() {
        ScmSysUserLoginDTO dto = new ScmSysUserLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        ScmSysUser user = buildUser(1L, "admin", DigestUtils.md5DigestAsHex("123456".getBytes()), 1L);
        ScmSysRole role = new ScmSysRole();
        role.setId(1L); role.setRoleName("管理员"); role.setRoleCode("ADMIN");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(roleMapper.selectById(1L)).thenReturn(role);
        when(jwtProperties.getAdminSecretKey()).thenReturn("test-secret-key-123456789012345");
        when(jwtProperties.getAdminTtl()).thenReturn(3600000L);

        ScmSysUserVO vo = userService.login(dto);
        assertNotNull(vo.getToken());
        assertEquals("admin", vo.getUsername());
        assertEquals("ADMIN", vo.getRoleCode());
    }

    // ===== T1-06: 错误密码 =====
    @Test
    void testLogin_WrongPassword_ShouldThrow() {
        ScmSysUserLoginDTO dto = new ScmSysUserLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrong");

        ScmSysUser user = buildUser(1L, "admin", DigestUtils.md5DigestAsHex("123456".getBytes()), 1L);
        when(userMapper.selectOne(any())).thenReturn(user);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login(dto));
        assertTrue(ex.getMessage().contains("密码"));
    }

    // ===== T1-07: 不存在的账号 =====
    @Test
    void testLogin_UserNotFound_ShouldThrow() {
        ScmSysUserLoginDTO dto = new ScmSysUserLoginDTO();
        dto.setUsername("ghost");
        dto.setPassword("123456");
        when(userMapper.selectOne(any())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login(dto));
        assertTrue(ex.getMessage().contains("账号不存在"));
    }

    // ===== T1-10: 停用账号 =====
    @Test
    void testLogin_DisabledAccount_ShouldThrow() {
        ScmSysUserLoginDTO dto = new ScmSysUserLoginDTO();
        dto.setUsername("disabled");
        dto.setPassword("123456");

        ScmSysUser user = buildUser(2L, "disabled", DigestUtils.md5DigestAsHex("123456".getBytes()), 2L);
        user.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(user);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login(dto));
        assertTrue(ex.getMessage().contains("停用"));
    }

    // ===== T7-01: 个人修改密码成功 =====
    @Test
    void testChangePassword_Self_Success() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("123456");
        dto.setNewPassword("888888");

        ScmSysUser currentUser = buildUser(3L, "buyer", DigestUtils.md5DigestAsHex("123456".getBytes()), 2L);
        ScmSysRole role = new ScmSysRole();
        role.setId(2L); role.setRoleCode("PURCHASER");

        try (MockedStatic<BaseContext> mocked = mockStatic(BaseContext.class)) {
            mocked.when(BaseContext::getCurrentId).thenReturn(3L);
            when(userMapper.selectById(3L)).thenReturn(currentUser);
            when(roleMapper.selectById(2L)).thenReturn(role);

            userService.changePassword(dto);
            verify(userMapper).updateById(any(ScmSysUser.class));
        }
    }

    // ===== T7-04: 旧密码错误 =====
    @Test
    void testChangePassword_WrongOldPassword_ShouldThrow() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("wrong");
        dto.setNewPassword("888888");

        ScmSysUser currentUser = buildUser(3L, "buyer", DigestUtils.md5DigestAsHex("123456".getBytes()), 2L);
        ScmSysRole role = new ScmSysRole();
        role.setId(2L); role.setRoleCode("PURCHASER");

        try (MockedStatic<BaseContext> mocked = mockStatic(BaseContext.class)) {
            mocked.when(BaseContext::getCurrentId).thenReturn(3L);
            when(userMapper.selectById(3L)).thenReturn(currentUser);
            when(roleMapper.selectById(2L)).thenReturn(role);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.changePassword(dto));
            assertTrue(ex.getMessage().contains("原密码"));
        }
    }

    // ===== T7-03: 管理员修改他人密码 =====
    @Test
    void testChangePassword_AdminChangeOthers_Success() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setUserId(4L);
        dto.setNewPassword("999999");

        ScmSysUser admin = buildUser(1L, "admin", DigestUtils.md5DigestAsHex("123456".getBytes()), 1L);
        ScmSysRole adminRole = new ScmSysRole();
        adminRole.setId(1L); adminRole.setRoleCode("ADMIN");

        try (MockedStatic<BaseContext> mocked = mockStatic(BaseContext.class)) {
            mocked.when(BaseContext::getCurrentId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(admin);
            when(roleMapper.selectById(1L)).thenReturn(adminRole);

            userService.changePassword(dto);
            verify(userMapper).updateById(any(ScmSysUser.class));
        }
    }

    // ===== T7-05: 非管理员修改他人密码 =====
    @Test
    void testChangePassword_NonAdminChangeOthers_ShouldThrow() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setUserId(1L);
        dto.setNewPassword("999999");

        ScmSysUser buyer = buildUser(3L, "buyer", DigestUtils.md5DigestAsHex("123456".getBytes()), 2L);
        ScmSysRole role = new ScmSysRole();
        role.setId(2L); role.setRoleCode("PURCHASER");

        try (MockedStatic<BaseContext> mocked = mockStatic(BaseContext.class)) {
            mocked.when(BaseContext::getCurrentId).thenReturn(3L);
            when(userMapper.selectById(3L)).thenReturn(buyer);
            when(roleMapper.selectById(2L)).thenReturn(role);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.changePassword(dto));
            assertTrue(ex.getMessage().contains("无权限"));
        }
    }

    // Helper
    private ScmSysUser buildUser(Long id, String username, String password, Long roleId) {
        ScmSysUser u = new ScmSysUser();
        u.setId(id); u.setUsername(username); u.setPassword(password);
        u.setRoleId(roleId); u.setStatus(1); u.setPhone("13800000001");
        return u;
    }
}
