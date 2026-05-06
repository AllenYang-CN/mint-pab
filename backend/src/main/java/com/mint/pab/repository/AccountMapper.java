package com.mint.pab.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mint.pab.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    @Select("SELECT * FROM account WHERE id = #{id} AND is_deleted = 0 FOR UPDATE")
    Account selectByIdForUpdate(@Param("id") Long id);

}
