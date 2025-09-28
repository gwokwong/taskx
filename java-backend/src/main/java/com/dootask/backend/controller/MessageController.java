package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.Dialog;
import com.dootask.backend.entity.Message;
import com.dootask.backend.service.DialogService;
import com.dootask.backend.service.MessageService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Tag(name = "消息管理", description = "实时消息相关接口")
@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final DialogService dialogService;
    private final UserService userService;

    @MessageMapping("/dialog/{dialogId}")
    public void sendMessage(@DestinationVariable Long dialogId,
                          @Payload Map<String, Object> messageData,
                          SimpMessageHeaderAccessor headerAccessor) {

        String token = (String) headerAccessor.getSessionAttributes().get("token");
        if (token != null) {
            var user = userService.getUserByToken(token);
            Long userId = user != null ? user.getUserid() : null;
            if (userId != null) {
                String content = (String) messageData.get("content");
                String messageType = (String) messageData.get("messageType");

                messageService.sendMessage(dialogId, userId, content, messageType);
            }
        }
    }

    @SubscribeMapping("/dialog/{dialogId}")
    public void subscribeToDialog(@DestinationVariable Long dialogId,
                                 SimpMessageHeaderAccessor headerAccessor) {
        String token = (String) headerAccessor.getSessionAttributes().get("token");
        if (token != null) {
            var user = userService.getUserByToken(token);
            Long userId = user != null ? user.getUserid() : null;
            if (userId != null) {
                // 标记消息为已读
                messageService.markMessagesAsRead(dialogId, userId);
            }
        }
    }
}

@Tag(name = "对话管理", description = "对话相关接口")
@RestController
@RequestMapping("/api/dialogs")
@RequiredArgsConstructor
class DialogController {

    private final DialogService dialogService;
    private final MessageService messageService;
    private final UserService userService;

    @Operation(summary = "获取用户对话列表")
    @GetMapping
    public Result<List<Map<String, Object>>> getUserDialogs(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Map<String, Object>> dialogs = dialogService.getUserDialogs(userId);
        return Result.success(dialogs);
    }

    @Operation(summary = "创建对话")
    @PostMapping
    public Result<Dialog> createDialog(@RequestBody Map<String, Object> data, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String name = (String) data.get("name");
        String type = (String) data.get("type");
        List<Long> memberIds = (List<Long>) data.get("memberIds");

        Dialog dialog = dialogService.createDialog(name, type, userId, memberIds);
        return Result.success(dialog);
    }

    @Operation(summary = "获取对话消息")
    @GetMapping("/{dialogId}/messages")
    public Result<List<Message>> getDialogMessages(@PathVariable Long dialogId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "50") Integer size,
                                                  HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        // TODO: 验证用户是否有权限访问该对话

        List<Message> messages = messageService.getMessagesByDialog(dialogId, page, size);
        return Result.success(messages);
    }

    @Operation(summary = "发送消息")
    @PostMapping("/{dialogId}/messages")
    public Result<Message> sendMessage(@PathVariable Long dialogId,
                                     @RequestBody Map<String, Object> data,
                                     HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String content = (String) data.get("content");
        String messageType = (String) data.get("messageType");

        Message message = messageService.sendMessage(dialogId, userId, content, messageType);
        return Result.success(message);
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/{dialogId}/read")
    public Result<String> markAsRead(@PathVariable Long dialogId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        messageService.markMessagesAsRead(dialogId, userId);
        return Result.success("标记成功");
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            var user = userService.getUserByToken(token);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }
}