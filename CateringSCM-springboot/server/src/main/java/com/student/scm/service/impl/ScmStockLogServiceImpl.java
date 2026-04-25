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
        Page<ScmStockLog> Page = new Page<>(dto.getPage(), dto.getPageSize());
        this.page(Page, new LambdaQueryWrapper<ScmStockLog>().orderByDesc(ScmStockLog::getCreateTime));

        List<ScmStockLogVO> voList = Page.getRecords().stream().map(entity -> {
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

        return new PageResult(Page.getTotal(), voList);
    }
}
