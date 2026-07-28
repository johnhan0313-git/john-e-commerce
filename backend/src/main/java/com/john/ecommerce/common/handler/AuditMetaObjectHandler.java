package com.john.ecommerce.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.john.ecommerce.common.context.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        long now = System.currentTimeMillis();
        Long userId = UserContext.getCurrentUserId();
        this.strictInsertFill(metaObject, "createdAt", Long.class, now);
        this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updatedAt", Long.class, now);
        this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        long now = System.currentTimeMillis();
        Long userId = UserContext.getCurrentUserId();
        this.strictUpdateFill(metaObject, "updatedAt", Long.class, now);
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
    }
}
