package com.yorozuya.awesomecs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yorozuya.awesomecs.repository.mapper.ConsultationMessagesMapper;
import com.yorozuya.awesomecs.model.domain.ConsultationMessages;
import com.yorozuya.awesomecs.service.ConsultationMessagesService;
import org.springframework.stereotype.Service;

@Service
public class ConsultationMessagesServiceImpl extends ServiceImpl<ConsultationMessagesMapper, ConsultationMessages>
        implements ConsultationMessagesService {
}
