package com.oasystem.modules.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.modules.workflow.entity.ExpenseDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报销明细 Mapper
 */
@Mapper
public interface ExpenseDetailMapper extends BaseMapper<ExpenseDetail> {
}
