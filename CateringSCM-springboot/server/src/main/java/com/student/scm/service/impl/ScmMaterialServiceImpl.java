package com.student.scm.service.impl;

import com.student.scm.entity.ScmMaterial;
import com.student.scm.mapper.ScmMaterialMapper;
import com.student.scm.service.IScmMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ScmMaterialServiceImpl extends ServiceImpl<ScmMaterialMapper, ScmMaterial> implements IScmMaterialService {

}
