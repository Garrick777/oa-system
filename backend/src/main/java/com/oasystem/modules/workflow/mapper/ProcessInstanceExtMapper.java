package com.oasystem.modules.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.modules.workflow.entity.ProcessInstanceExt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程实例扩展 Mapper
 */
@Mapper
public interface ProcessInstanceExtMapper extends BaseMapper<ProcessInstanceExt> {
}
