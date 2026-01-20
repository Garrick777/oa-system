package com.oasystem.modules.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.modules.org.entity.Department;
import com.oasystem.modules.org.mapper.DepartmentMapper;
import com.oasystem.modules.org.service.DepartmentService;
import com.oasystem.modules.system.entity.User;
import com.oasystem.modules.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final UserMapper userMapper;

    @Override
    public List<Department> getDeptTree() {
        List<Department> allDepts = list(new LambdaQueryWrapper<Department>()
                .eq(Department::getStatus, 1)
                .orderByAsc(Department::getSort));
        
        // 填充负责人姓名
        fillLeaderName(allDepts);
        
        return buildTree(allDepts, 0L);
    }

    @Override
    public List<Department> getAllDepts() {
        List<Department> depts = list(new LambdaQueryWrapper<Department>()
                .orderByAsc(Department::getSort));
        fillLeaderName(depts);
        return depts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDept(Department dept) {
        // 检查部门编码是否存在
        if (dept.getDeptCode() != null) {
            Department exist = getOne(new LambdaQueryWrapper<Department>()
                    .eq(Department::getDeptCode, dept.getDeptCode()));
            if (exist != null) {
                throw new BusinessException("部门编码已存在");
            }
        }

        // 检查父部门
        if (dept.getParentId() != null && dept.getParentId() > 0) {
            Department parent = getById(dept.getParentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
        } else {
            dept.setParentId(0L);
        }

        dept.setStatus(dept.getStatus() != null ? dept.getStatus() : 1);
        save(dept);
        
        log.info("创建部门成功: {}", dept.getDeptName());
        return dept.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(Department dept) {
        Department exist = getById(dept.getId());
        if (exist == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查部门编码
        if (dept.getDeptCode() != null) {
            Department codeExist = getOne(new LambdaQueryWrapper<Department>()
                    .eq(Department::getDeptCode, dept.getDeptCode())
                    .ne(Department::getId, dept.getId()));
            if (codeExist != null) {
                throw new BusinessException("部门编码已存在");
            }
        }

        // 检查父部门
        if (dept.getParentId() != null && dept.getParentId().equals(dept.getId())) {
            throw new BusinessException("父部门不能是自己");
        }

        updateById(dept);
        log.info("更新部门成功: {}", dept.getDeptName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        Department dept = getById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查是否有子部门
        Long childCount = count(new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }

        // 检查是否有员工
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getDeptId, id));
        if (userCount > 0) {
            throw new BusinessException("部门下存在员工，无法删除");
        }

        removeById(id);
        log.info("删除部门成功: {}", dept.getDeptName());
    }

    /**
     * 构建部门树
     */
    private List<Department> buildTree(List<Department> depts, Long parentId) {
        return depts.stream()
                .filter(dept -> parentId.equals(dept.getParentId()))
                .map(dept -> {
                    dept.setChildren(buildTree(depts, dept.getId()));
                    return dept;
                })
                .collect(Collectors.toList());
    }

    /**
     * 填充负责人姓名
     */
    private void fillLeaderName(List<Department> depts) {
        for (Department dept : depts) {
            if (dept.getLeaderId() != null) {
                User leader = userMapper.selectById(dept.getLeaderId());
                if (leader != null) {
                    dept.setLeaderName(leader.getRealName());
                }
            }
        }
    }
}
