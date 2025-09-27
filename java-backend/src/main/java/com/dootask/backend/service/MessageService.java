package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {

    List<Message> getMessagesByDialog(Long dialogId, Integer page, Integer size);

    Message sendMessage(Long dialogId, Long userId, String content, String messageType);

    void markMessagesAsRead(Long dialogId, Long userId);
}