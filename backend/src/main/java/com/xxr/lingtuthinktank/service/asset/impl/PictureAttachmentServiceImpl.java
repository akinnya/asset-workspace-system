package com.xxr.lingtuthinktank.service.asset.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.mapper.asset.PictureAttachmentMapper;
import com.xxr.lingtuthinktank.model.entity.asset.PictureAttachment;
import com.xxr.lingtuthinktank.service.asset.PictureAttachmentService;
import org.springframework.stereotype.Service;

@Service
public class PictureAttachmentServiceImpl extends ServiceImpl<PictureAttachmentMapper, PictureAttachment>
        implements PictureAttachmentService {
}
