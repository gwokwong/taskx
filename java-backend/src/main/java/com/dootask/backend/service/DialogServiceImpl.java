package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.entity.Dialog;
import com.dootask.backend.entity.DialogMember;
import com.dootask.backend.mapper.DialogMapper;
import com.dootask.backend.mapper.DialogMemberMapper;
import com.dootask.backend.mapper.MessageMapper;
import com.dootask.backend.service.DialogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DialogServiceImpl extends ServiceImpl<DialogMapper, Dialog> implements DialogService {

    private final DialogMemberMapper dialogMemberMapper;
    private final MessageMapper messageMapper;

    @Override
    public List<Map<String, Object>> getUserDialogs(Long userId) {
        return baseMapper.getUserDialogs(userId);
    }

    @Override
    public Dialog createDialog(String name, String type, Long createdBy, List<Long> memberIds) {
        Dialog dialog = new Dialog();
        dialog.setName(name);
        dialog.setType(type);
        dialog.setCreatedBy(createdBy);
        dialog.setCreatedAt(LocalDateTime.now());
        dialog.setUpdatedAt(LocalDateTime.now());

        save(dialog);

        // 添加创建者为管理员
        addMember(dialog.getId(), createdBy, "admin");

        // 添加其他成员
        if (memberIds != null) {
            for (Long memberId : memberIds) {
                if (!memberId.equals(createdBy)) {
                    addMember(dialog.getId(), memberId, "member");
                }
            }
        }

        return dialog;
    }

    @Override
    public void addMember(Long dialogId, Long userId, String role) {
        DialogMember member = new DialogMember();
        member.setDialogId(dialogId);
        member.setUserId(userId);
        member.setRole(role != null ? role : "member");
        member.setJoinedAt(LocalDateTime.now());
        member.setLastReadAt(LocalDateTime.now());

        dialogMemberMapper.insert(member);
    }

    @Override
    public void removeMember(Long dialogId, Long userId) {
        dialogMemberMapper.delete(new QueryWrapper<DialogMember>()
                .eq("dialog_id", dialogId)
                .eq("user_id", userId));
    }

    @Override
    public List<DialogMember> getDialogMembers(Long dialogId) {
        return dialogMemberMapper.selectList(new QueryWrapper<DialogMember>()
                .eq("dialog_id", dialogId));
    }

    @Override
    public Long getUnreadCount(Long dialogId, Long userId) {
        DialogMember member = dialogMemberMapper.selectOne(new QueryWrapper<DialogMember>()
                .eq("dialog_id", dialogId)
                .eq("user_id", userId));

        if (member == null) {
            return 0L;
        }

        return messageMapper.selectCount(new QueryWrapper<com.dootask.backend.entity.Message>()
                .eq("dialog_id", dialogId)
                .gt("created_at", member.getLastReadAt() != null ? member.getLastReadAt() : LocalDateTime.of(1970, 1, 1, 0, 0)));
    }
}