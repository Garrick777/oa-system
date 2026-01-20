package com.oasystem.modules.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.modules.workflow.entity.ExpenseApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报销申请 Mapper
 */
@Mapper
public interface ExpenseApplicationMapper extends BaseMapper<ExpenseApplication> {
}
