package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.Dialog;
import com.dootask.backend.entity.DialogMember;

import java.util.List;
import java.util.Map;

public interface DialogService extends IService<Dialog> {

    List<Map<String, Object>> getUserDialogs(Long userId);

    Dialog createDialog(String name, String type, Long createdBy, List<Long> memberIds);

    void addMember(Long dialogId, Long userId, String role);

    void removeMember(Long dialogId, Long userId);

    List<DialogMember> getDialogMembers(Long dialogId);

    Long getUnreadCount(Long dialogId, Long userId);
}