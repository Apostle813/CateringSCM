package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.dto.StockLogPageQueryDTO;
import com.student.scm.entity.ScmMaterial;
import com.student.scm.entity.ScmStockLog;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.entity.ScmWarehouse;
import com.student.scm.mapper.ScmMaterialMapper;
import com.student.scm.mapper.ScmStockLogMapper;
import com.student.scm.mapper.ScmSysUserMapper;
import com.student.scm.mapper.ScmWarehouseMapper;
import com.student.scm.result.PageResult;
import com.student.scm.service.IScmStockLogService;
import com.student.scm.vo.ScmStockLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScmStockLogServiceImpl extends ServiceImpl<ScmStockLogMapper, ScmStockLog> implements IScmStockLogService {
        private ScmWarehouseMapper warehouseMapper;
        private ScmMaterialMapper materialMapper;
        private ScmSysUserMapper sysUserMapper;

        public ScmStockLogServiceImpl(ScmWarehouseMapper warehouseMapper, ScmMaterialMapper materialMapper, ScmSysUserMapper sysUserMapper) {
            this.warehouseMapper = warehouseMapper;
            this.materialMapper = materialMapper;
            this.sysUserMapper = sysUserMapper;
        }

    @Override
    public PageResult queryPageByCondition(StockLogPageQueryDTO dto) {
        // 1. 查出原始 Entity 分页
        Page<ScmStockLog> page = new Page<>(dto.getPage(), dto.getPageSize());
        
        LambdaQueryWrapper<ScmStockLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按参考单号模糊查询
        if (StringUtils.hasText(dto.getReferenceNo())) {
            queryWrapper.like(ScmStockLog::getReferenceNo, dto.getReferenceNo());
        }
        // 按类型筛选
        if (dto.getType() != null) {
            queryWrapper.eq(ScmStockLog::getType, dto.getType());
        }
        // 按仓库筛选
        if (dto.getWarehouseId() != null) {
            queryWrapper.eq(ScmStockLog::getWarehouseId, dto.getWarehouseId());
        }
        // 按物料筛选
        if (dto.getMaterialId() != null) {
            queryWrapper.eq(ScmStockLog::getMaterialId, dto.getMaterialId());
        }
        // 按开始日期筛选
        if (StringUtils.hasText(dto.getStartDate())) {
            queryWrapper.ge(ScmStockLog::getCreateTime, dto.getStartDate() + " 00:00:00");
        }
        // 按结束日期筛选
        if (StringUtils.hasText(dto.getEndDate())) {
            queryWrapper.le(ScmStockLog::getCreateTime, dto.getEndDate() + " 23:59:59");
        }
        // 按物料名称筛选（需要先查询符合条件的物料ID）
        if (StringUtils.hasText(dto.getMaterialName())) {
            LambdaQueryWrapper<ScmMaterial> materialWrapper = new LambdaQueryWrapper<>();
            materialWrapper.like(ScmMaterial::getName, dto.getMaterialName());
            List<ScmMaterial> materials = materialMapper.selectList(materialWrapper);
            if (!materials.isEmpty()) {
                List<Long> materialIds = materials.stream().map(ScmMaterial::getId).collect(Collectors.toList());
                queryWrapper.in(ScmStockLog::getMaterialId, materialIds);
            } else {
                // 如果没有符合条件的物料，直接返回空结果
                return new PageResult(0L, Collections.emptyList());
            }
        }
        
        queryWrapper.orderByDesc(ScmStockLog::getCreateTime);
        this.page(page, queryWrapper);

        List<ScmStockLogVO> voList = page.getRecords().stream().map(entity -> {
            ScmStockLogVO vo = new ScmStockLogVO();
            BeanUtils.copyProperties(entity, vo);

            ScmWarehouse w = warehouseMapper.selectById(entity.getWarehouseId());
            if (w != null) vo.setWarehouseName(w.getName());

            ScmMaterial m = materialMapper.selectById(entity.getMaterialId());
            if (m != null) vo.setMaterialName(m.getName());

            ScmSysUser u = sysUserMapper.selectById(entity.getOperatorId());
            if (u != null) vo.setOperatorName(u.getUsername());

            if (entity.getType() != null) {
                if (entity.getType() == 1) vo.setTypeName("采购入库");
                else if (entity.getType() == 2) vo.setTypeName("领料出库");
                else if (entity.getType() == 3) vo.setTypeName("盘点调整");
            }

            return vo;
        }).collect(Collectors.toList());

        return new PageResult(page.getTotal(), voList);
    }
}
