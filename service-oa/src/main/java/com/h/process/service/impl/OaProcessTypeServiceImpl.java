package com.h.process.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.h.model.process.ProcessType;
import com.h.process.mapper.ProcessTypeMapper;
import com.h.process.service.ProcessTypeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author Lin
 * @since 2023-03-12
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<ProcessTypeMapper, ProcessType> implements ProcessTypeService {

}
