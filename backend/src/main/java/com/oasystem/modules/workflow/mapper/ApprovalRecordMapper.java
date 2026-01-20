package com.oasystem.modules.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.modules.workflow.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批记录 Mapper
 */
@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {
}
