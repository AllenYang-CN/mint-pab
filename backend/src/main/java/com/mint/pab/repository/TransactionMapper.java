package com.mint.pab.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mint.pab.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    /**
     * 按用户ID和月份统计支出金额（按分类分组）
     *
     * @param userId 用户ID
     * @param month  月份，格式：yyyy-MM
     * @return 分类支出统计列表
     */
    @Select("SELECT c.id as category_id, c.parent_name as category_parent_name, c.name as category_name, SUM(t.amount) as total_amount " +
            "FROM transaction t " +
            "LEFT JOIN category c ON t.category_id = c.id " +
            "WHERE t.user_id = #{userId} " +
            "AND t.type = 2 " +
            "AND DATE_FORMAT(t.transaction_time, '%Y-%m') = #{month} " +
            "AND t.is_deleted = 0 " +
            "AND c.is_deleted = 0 " +
            "GROUP BY c.id, c.parent_name, c.name")
    List<Map<String, Object>> selectExpenseByCategoryAndMonth(@Param("userId") Long userId, @Param("month") String month);

}
