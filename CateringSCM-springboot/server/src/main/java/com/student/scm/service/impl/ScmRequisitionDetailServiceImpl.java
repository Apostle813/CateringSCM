package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.entity.ScmMaterial;
import com.student.scm.entity.ScmRequisitionDetail;
import com.student.scm.mapper.ScmMaterialMapper;
import com.student.scm.mapper.ScmRequisitionDetailMapper;
import com.student.scm.service.IScmRequisitionDetailService;
import com.student.scm.vo.ScmRequisitionDetailVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScmRequisitionDetailServiceImpl extends ServiceImpl<ScmRequisitionDetailMapper, ScmRequisitionDetail> implements IScmRequisitionDetailService {

    private final ScmMaterialMapper materialMapper;

    public ScmRequisitionDetailServiceImpl(ScmMaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    @Override
    public List<ScmRequisitionDetailVO> listByOrderId(Long orderId) {
        LambdaQueryWrapper<ScmRequisitionDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScmRequisitionDetail::getOrderId, orderId);
        List<ScmRequisitionDetail> details = this.list(wrapper);

        List<ScmRequisitionDetailVO> voList = new ArrayList<>();
        for (ScmRequisitionDetail detail : details) {
            ScmMaterial material = materialMapper.selectById(detail.getMaterialId());

            ScmRequisitionDetailVO vo = ScmRequisitionDetailVO.builder()
                    .id(detail.getId())
                    .orderId(detail.getOrderId())
                    .materialId(detail.getMaterialId())
                    .planQty(detail.getPlanQty())
                    .realQty(detail.getRealQty())
                    .build();

            if (material != null) {
                vo.setMaterialName(material.getName());
                vo.setCategory(material.getCategory());
                vo.setUnit(material.getUnit());
            }

            voList.add(vo);
        }
        return voList;
    }
}
