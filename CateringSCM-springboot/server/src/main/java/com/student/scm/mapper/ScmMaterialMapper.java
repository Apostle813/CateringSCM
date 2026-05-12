package com.student.scm.mapper;

import com.student.scm.entity.ScmMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface ScmMaterialMapper extends BaseMapper<ScmMaterial> {
    @Select("SELECT COUNT(*) FROM scm_material")
    Long countAll();
}
