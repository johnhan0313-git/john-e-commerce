package com.john.ecommerce.module.user.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 登录前尚无租户上下文，需绕过租户行拦截。 */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM t_user WHERE lower(email) = lower(#{email}) AND delete_flag = 0 LIMIT 1")
    User selectByEmail(@Param("email") String email);

    /** @deprecated 已切邮箱登录，保留兼容旧调用 */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND delete_flag = 0 LIMIT 1")
    User selectByPhone(@Param("phone") String phone);
}
