package com.xxr.lingtuthinktank.service.asset.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.mapper.asset.PictureEditLogMapper;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.asset.PictureEditLog;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.asset.PictureEditLogVO;
import com.xxr.lingtuthinktank.service.asset.PictureEditLogService;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图片协作编辑日志服务实现
 */
@Service
public class PictureEditLogServiceImpl extends ServiceImpl<PictureEditLogMapper, PictureEditLog>
        implements PictureEditLogService {

    @Resource
    private UserService userService;

    @Override
    public void saveEditLog(Picture picture, User operator, String changeSummary, String beforeSummary, String afterSummary) {
        if (picture == null || picture.getId() == null || picture.getSpaceId() == null || operator == null) {
            return;
        }
        PictureEditLog editLog = new PictureEditLog();
        editLog.setPictureId(picture.getId());
        editLog.setSpaceId(picture.getSpaceId());
        editLog.setOperatorUserId(operator.getId());
        editLog.setActionType("edit");
        editLog.setChangeSummary(changeSummary);
        editLog.setBeforeSummary(beforeSummary);
        editLog.setAfterSummary(afterSummary);
        this.save(editLog);
    }

    @Override
    public List<PictureEditLogVO> listPictureEditLogVOList(Long pictureId) {
        if (pictureId == null || pictureId <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper<PictureEditLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pictureId", pictureId);
        queryWrapper.orderByDesc("createTime");
        queryWrapper.last("limit 20");
        List<PictureEditLog> editLogList = this.list(queryWrapper);
        if (editLogList.isEmpty()) {
            return new ArrayList<>();
        }
        List<PictureEditLogVO> voList = editLogList.stream()
                .map(PictureEditLogVO::objToVo)
                .collect(Collectors.toList());
        Set<Long> userIdSet = editLogList.stream()
                .map(PictureEditLog::getOperatorUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        voList.forEach(vo -> {
            List<User> users = userMap.get(vo.getOperatorUserId());
            User user = (users == null || users.isEmpty()) ? null : users.get(0);
            vo.setOperatorUser(userService.getUserVO(user));
        });
        return voList;
    }
}
