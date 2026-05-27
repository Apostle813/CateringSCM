package com.student.scm.controller;


import com.student.scm.dto.ScmSysUserLoginDTO;
import com.student.scm.dto.ChangePasswordDTO;
import com.student.scm.dto.ResetPasswordDTO;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.properties.JwtProperties;
import com.student.scm.result.Result;
import com.student.scm.service.IScmOperationLogService;
import com.student.scm.service.IScmSysUserService;
import com.student.scm.vo.ScmSysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class ScmSysUserController {
    private IScmSysUserService scmsysuserservice;
    private JwtProperties jwtProperties;
    private IScmOperationLogService operationLogService;

    public ScmSysUserController(IScmSysUserService scmsysuserservice, JwtProperties jwtProperties, IScmOperationLogService operationLogService) {
        this.scmsysuserservice = scmsysuserservice;
        this.jwtProperties = jwtProperties;
        this.operationLogService = operationLogService;
    }

    @PostMapping("/login")
    public Result<ScmSysUserVO> login(@RequestBody ScmSysUserLoginDTO userLoginDTO) {
        try {
            log.info("用户传过来的登录信息DTO:{}", userLoginDTO);
            ScmSysUserVO user = scmsysuserservice.login(userLoginDTO);
            // 登录日志（login时BaseContext还没有用户，所以手动传参）
            //operationLogService.saveLog(user.getId(), user.getRealName(), "USER_LOGIN", "用户登录系统", "sys_user", user.getId());
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Result<ScmSysUserVO> getProfile() {
        return Result.success(scmsysuserservice.getProfile());
    }

    @PostMapping()
    public Result<String> save(@RequestBody ScmSysUser scmSysUser) {
        scmSysUser.setPassword(org.springframework.util.DigestUtils.md5DigestAsHex("123456".getBytes()));
        scmsysuserservice.save(scmSysUser);
        operationLogService.saveLog("USER_CREATE", "创建系统用户 " + scmSysUser.getUsername(), "sys_user", scmSysUser.getId());
        return Result.success("员工账号创建成功");
    }

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<ScmSysUser>> page(com.student.scm.dto.ScmSysUserPageQueryDTO queryDTO) {
        return Result.success(scmsysuserservice.queryPageByCondition(queryDTO));
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody ScmSysUser scmSysUser) {
        scmsysuserservice.updateById(scmSysUser);
        operationLogService.saveLog("USER_UPDATE", "修改系统用户 ID:" + scmSysUser.getId(), "sys_user", scmSysUser.getId());
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        scmsysuserservice.removeById(id);
        operationLogService.saveLog("USER_DELETE", "删除系统用户 ID:" + id, "sys_user", id);
        return Result.success("删除成功");
    }

    @PutMapping("/change-password")
    public Result<String> changePassword(@RequestBody ChangePasswordDTO dto) {
        try {
            scmsysuserservice.changePassword(dto);
            operationLogService.saveLog("PASSWORD_CHANGE", "修改密码", "sys_user", dto.getUserId());
            return Result.success(com.student.scm.constant.MessageConstant.PASSWORD_CHANGED);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/exists")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = scmsysuserservice.lambdaQuery()
                .eq(com.student.scm.entity.ScmSysUser::getUsername, username)
                .count() > 0;
        return Result.success(exists);
    }

    /**
     * 忘记密码 — 自助重置密码
     * 验证方式：用户名 + 注册手机号 双重匹配
     */
    @PutMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
        // 查询用户
        ScmSysUser user = scmsysuserservice.lambdaQuery()
                .eq(ScmSysUser::getUsername, dto.getUsername())
                .one();
        if (user == null) {
            return Result.error("账号不存在");
        }
        // 验证手机号
        if (dto.getPhone() == null || !dto.getPhone().equals(user.getPhone())) {
            return Result.error("手机号验证失败，非本人操作");
        }
        // 更新密码
        String newMd5 = org.springframework.util.DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes());
        user.setPassword(newMd5);
        scmsysuserservice.updateById(user);

        operationLogService.saveLog("PASSWORD_RESET", "用户自助重置密码", "sys_user", user.getId());
        return Result.success("密码重置成功，请返回登录");
    }
}
