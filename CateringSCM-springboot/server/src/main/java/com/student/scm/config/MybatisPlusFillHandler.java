package com.student.scm.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.student.scm.context.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充配置类
 */
@Component
public class MybatisPlusFillHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // 1. 填充创建、修改时间，根据实体中有字段且有注解。
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 2. 从 ThreadLocal 获取当前登录用户 ID，填充创建人 & 修改人
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
        }
    }
}
