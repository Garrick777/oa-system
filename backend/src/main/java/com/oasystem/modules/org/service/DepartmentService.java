package com.oasystem.modules.org.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.modules.org.entity.Department;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 获取部门树
     */
    List<Department> getDeptTree();

    /**
     * 获取所有部门列表
     */
    List<Department> getAllDepts();

    /**
     * 创建部门
     */
    Long createDept(Department dept);

    /**
     * 更新部门
     */
    void updateDept(Department dept);

    /**
     * 删除部门
     */
    void deleteDept(Long id);
}
