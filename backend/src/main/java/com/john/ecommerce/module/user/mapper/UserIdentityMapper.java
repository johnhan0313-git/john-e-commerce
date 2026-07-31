package com.john.ecommerce.module.user.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.user.entity.UserIdentity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserIdentityMapper extends BaseMapper<UserIdentity> {

    /** 登录发 token 前可能尚无租户上下文 */
    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT * FROM t_user_identity
            WHERE user_id = #{userId} AND identity_code = #{identityCode} AND delete_flag = 0
            LIMIT 1
            """)
    UserIdentity selectByUserAndCode(@Param("userId") Long userId,
                                     @Param("identityCode") String identityCode);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT identity_code FROM t_user_identity
            WHERE user_id = #{userId} AND status = 1 AND delete_flag = 0
            ORDER BY identity_code
            """)
    List<String> listActiveCodesByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT COUNT(1) FROM t_user_identity
            WHERE user_id = #{userId} AND identity_code = #{identityCode}
              AND status = 1 AND delete_flag = 0
            """)
    long countActive(@Param("userId") Long userId, @Param("identityCode") String identityCode);
}
