package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.entity.Message;
import com.dootask.backend.entity.DialogMember;
import com.dootask.backend.mapper.MessageMapper;
import com.dootask.backend.mapper.DialogMemberMapper;
import com.dootask.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final DialogMemberMapper dialogMemberMapper;

    @Override
    public List<Message> getMessagesByDialog(Long dialogId, Integer page, Integer size) {
        Page<Message> pageInfo = new Page<>(page != null ? page : 1, size != null ? size : 50);

        Page<Message> result = page(pageInfo, new QueryWrapper<Message>()
                .eq("dialog_id", dialogId)
                .orderByAsc("created_at"));

        return result.getRecords();
    }

    @Override
    public Message sendMessage(Long dialogId, Long userId, String content, String messageType) {
        Message message = new Message();
        message.setDialogId(dialogId);
        message.setUserId(userId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        save(message);

        // 发送WebSocket消息
        messagingTemplate.convertAndSend("/topic/dialog/" + dialogId, message);

        return message;
    }

    @Override
    public void markMessagesAsRead(Long dialogId, Long userId) {
        DialogMember member = dialogMemberMapper.selectOne(new QueryWrapper<DialogMember>()
                .eq("dialog_id", dialogId)
                .eq("user_id", userId));

        if (member != null) {
            member.setLastReadAt(LocalDateTime.now());
            dialogMemberMapper.updateById(member);
        }
    }
}