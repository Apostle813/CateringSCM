package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.entity.ScmMaterial;
import com.student.scm.entity.ScmPurchaseDetail;
import com.student.scm.mapper.ScmMaterialMapper;
import com.student.scm.mapper.ScmPurchaseDetailMapper;
import com.student.scm.service.IScmPurchaseDetailService;
import com.student.scm.vo.ScmPurchaseDetailVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScmPurchaseDetailServiceImpl extends ServiceImpl<ScmPurchaseDetailMapper, ScmPurchaseDetail> implements IScmPurchaseDetailService {

    private final ScmMaterialMapper materialMapper;

    public ScmPurchaseDetailServiceImpl(ScmMaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    @Override
    public List<ScmPurchaseDetailVO> listByOrderId(Long orderId) {
        LambdaQueryWrapper<ScmPurchaseDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScmPurchaseDetail::getOrderId, orderId);
        List<ScmPurchaseDetail> details = this.list(wrapper);

        List<ScmPurchaseDetailVO> voList = new ArrayList<>();
        for (ScmPurchaseDetail detail : details) {
            ScmMaterial material = materialMapper.selectById(detail.getMaterialId());

            BigDecimal lineAmount = detail.getPrice() != null && detail.getPlanQty() != null
                    ? detail.getPrice().multiply(new BigDecimal(detail.getPlanQty()))
                    : BigDecimal.ZERO;

            ScmPurchaseDetailVO vo = ScmPurchaseDetailVO.builder()
                    .id(detail.getId())
                    .orderId(detail.getOrderId())
                    .materialId(detail.getMaterialId())
                    .planQty(detail.getPlanQty())
                    .realQty(detail.getRealQty())
                    .price(detail.getPrice())
                    .lineAmount(lineAmount)
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
