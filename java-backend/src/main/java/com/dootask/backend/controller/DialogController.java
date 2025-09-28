package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.WebSocketDialogMsg;
import com.dootask.backend.entity.ProjectTaskDialog;
import com.dootask.backend.service.DialogService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "对话管理", description = "聊天对话相关接口")
@RestController
@RequestMapping("/api/dialog")
@RequiredArgsConstructor
public class DialogController {

    private final DialogService dialogService;
    private final UserService userService;

    @Operation(summary = "获取对话列表")
    @GetMapping
    public Result<List<WebSocketDialogMsg>> getDialogs(@RequestParam(required = false) String type,
                                                       @RequestParam(required = false) Long dialogId,
                                                       HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<WebSocketDialogMsg> dialogs = dialogService.getDialogs(userId, type, dialogId);
        return Result.success(dialogs);
    }

    @Operation(summary = "获取对话详情")
    @GetMapping("/{id}")
    public Result<WebSocketDialogMsg> getDialog(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        WebSocketDialogMsg dialog = dialogService.getDialogById(id, userId);
        return Result.success(dialog);
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public Result<WebSocketDialogMsg> sendMessage(@Valid @RequestBody SendMessageRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        WebSocketDialogMsg message = dialogService.sendMessage(request, userId);
        return Result.success("消息发送成功", message);
    }

    @Operation(summary = "发送文件消息")
    @PostMapping("/send-file")
    public Result<WebSocketDialogMsg> sendFileMessage(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("type") String type,
                                                      @RequestParam("dialogId") Long dialogId,
                                                      HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        WebSocketDialogMsg message = dialogService.sendFileMessage(file, type, dialogId, userId);
        return Result.success("文件发送成功", message);
    }

    @Operation(summary = "撤回消息")
    @PostMapping("/{id}/recall")
    public Result<String> recallMessage(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.recallMessage(id, userId);
        return Result.success("消息撤回成功");
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public Result<String> deleteMessage(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.deleteMessage(id, userId);
        return Result.success("消息删除成功");
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.markAsRead(id, userId);
        return Result.success("标记已读成功");
    }

    @Operation(summary = "标记所有消息已读")
    @PostMapping("/read-all")
    public Result<String> markAllAsRead(@RequestParam String type, @RequestParam Long dialogId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.markAllAsRead(type, dialogId, userId);
        return Result.success("全部标记已读成功");
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Integer> unreadCount = dialogService.getUnreadCount(userId);
        return Result.success(unreadCount);
    }

    @Operation(summary = "搜索消息")
    @GetMapping("/search")
    public Result<Page<WebSocketDialogMsg>> searchMessages(@RequestParam String keyword,
                                                           @RequestParam(required = false) String type,
                                                           @RequestParam(required = false) Long dialogId,
                                                           Pageable pageable,
                                                           HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Page<WebSocketDialogMsg> messages = dialogService.searchMessages(keyword, type, dialogId, pageable, userId);
        return Result.success(messages);
    }

    @Operation(summary = "获取消息历史")
    @GetMapping("/history")
    public Result<Page<WebSocketDialogMsg>> getMessageHistory(@RequestParam String type,
                                                              @RequestParam Long dialogId,
                                                              @RequestParam(required = false) LocalDateTime before,
                                                              Pageable pageable,
                                                              HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Page<WebSocketDialogMsg> messages = dialogService.getMessageHistory(type, dialogId, before, pageable, userId);
        return Result.success(messages);
    }

    @Operation(summary = "转发消息")
    @PostMapping("/{id}/forward")
    public Result<String> forwardMessage(@PathVariable Long id, @RequestBody ForwardMessageRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        dialogService.forwardMessage(id, request.getTargetDialogs(), userId);
        return Result.success("消息转发成功");
    }

    @Operation(summary = "回复消息")
    @PostMapping("/{id}/reply")
    public Result<WebSocketDialogMsg> replyMessage(@PathVariable Long id, @Valid @RequestBody ReplyMessageRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        WebSocketDialogMsg message = dialogService.replyMessage(id, request, userId);
        return Result.success("回复发送成功", message);
    }

    @Operation(summary = "添加表情回应")
    @PostMapping("/{id}/reaction")
    public Result<String> addReaction(@PathVariable Long id, @RequestBody AddReactionRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        dialogService.addReaction(id, request.getEmoji(), userId);
        return Result.success("表情添加成功");
    }

    @Operation(summary = "移除表情回应")
    @DeleteMapping("/{id}/reaction")
    public Result<String> removeReaction(@PathVariable Long id, @RequestParam String emoji, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.removeReaction(id, emoji, userId);
        return Result.success("表情移除成功");
    }

    @Operation(summary = "置顶消息")
    @PostMapping("/{id}/pin")
    public Result<String> pinMessage(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.pinMessage(id, userId);
        return Result.success("消息置顶成功");
    }

    @Operation(summary = "取消置顶消息")
    @DeleteMapping("/{id}/pin")
    public Result<String> unpinMessage(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.unpinMessage(id, userId);
        return Result.success("取消置顶成功");
    }

    @Operation(summary = "获取置顶消息")
    @GetMapping("/pinned")
    public Result<List<WebSocketDialogMsg>> getPinnedMessages(@RequestParam String type, @RequestParam Long dialogId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<WebSocketDialogMsg> messages = dialogService.getPinnedMessages(type, dialogId, userId);
        return Result.success(messages);
    }

    @Operation(summary = "创建群聊")
    @PostMapping("/group/create")
    public Result<Map<String, Object>> createGroup(@Valid @RequestBody CreateGroupRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Map<String, Object> group = dialogService.createGroup(request, userId);
        return Result.success("群聊创建成功", group);
    }

    @Operation(summary = "加入群聊")
    @PostMapping("/group/{groupId}/join")
    public Result<String> joinGroup(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.joinGroup(groupId, userId);
        return Result.success("加入群聊成功");
    }

    @Operation(summary = "退出群聊")
    @PostMapping("/group/{groupId}/leave")
    public Result<String> leaveGroup(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.leaveGroup(groupId, userId);
        return Result.success("退出群聊成功");
    }

    @Operation(summary = "邀请成员")
    @PostMapping("/group/{groupId}/invite")
    public Result<String> inviteToGroup(@PathVariable Long groupId, @RequestBody InviteToGroupRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        dialogService.inviteToGroup(groupId, request.getUserIds(), userId);
        return Result.success("邀请发送成功");
    }

    @Operation(summary = "移除群成员")
    @DeleteMapping("/group/{groupId}/members/{memberId}")
    public Result<String> removeFromGroup(@PathVariable Long groupId, @PathVariable Long memberId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.removeFromGroup(groupId, memberId, userId);
        return Result.success("成员移除成功");
    }

    @Operation(summary = "更新群信息")
    @PutMapping("/group/{groupId}")
    public Result<String> updateGroup(@PathVariable Long groupId, @RequestBody UpdateGroupRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        dialogService.updateGroup(groupId, request, userId);
        return Result.success("群信息更新成功");
    }

    @Operation(summary = "获取群成员列表")
    @GetMapping("/group/{groupId}/members")
    public Result<List<Map<String, Object>>> getGroupMembers(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Map<String, Object>> members = dialogService.getGroupMembers(groupId, userId);
        return Result.success(members);
    }

    @Operation(summary = "设置群管理员")
    @PostMapping("/group/{groupId}/admin/{memberId}")
    public Result<String> setGroupAdmin(@PathVariable Long groupId, @PathVariable Long memberId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.setGroupAdmin(groupId, memberId, userId);
        return Result.success("设置管理员成功");
    }

    @Operation(summary = "取消群管理员")
    @DeleteMapping("/group/{groupId}/admin/{memberId}")
    public Result<String> removeGroupAdmin(@PathVariable Long groupId, @PathVariable Long memberId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.removeGroupAdmin(groupId, memberId, userId);
        return Result.success("取消管理员成功");
    }

    @Operation(summary = "禁言群成员")
    @PostMapping("/group/{groupId}/mute/{memberId}")
    public Result<String> muteGroupMember(@PathVariable Long groupId, @PathVariable Long memberId, @RequestBody MuteRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        dialogService.muteGroupMember(groupId, memberId, request.getDuration(), userId);
        return Result.success("禁言设置成功");
    }

    @Operation(summary = "取消禁言")
    @DeleteMapping("/group/{groupId}/mute/{memberId}")
    public Result<String> unmuteGroupMember(@PathVariable Long groupId, @PathVariable Long memberId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.unmuteGroupMember(groupId, memberId, userId);
        return Result.success("取消禁言成功");
    }

    @Operation(summary = "获取在线状态")
    @GetMapping("/online-status")
    public Result<Map<String, Object>> getOnlineStatus(@RequestParam(required = false) List<Long> userIds, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> status = dialogService.getOnlineStatus(userIds, userId);
        return Result.success(status);
    }

    @Operation(summary = "设置消息免打扰")
    @PostMapping("/mute")
    public Result<String> muteDialog(@RequestBody MuteDialogRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        dialogService.muteDialog(request.getType(), request.getDialogId(), request.isMuted(), userId);
        return Result.success(request.isMuted() ? "设置免打扰成功" : "取消免打扰成功");
    }

    @Operation(summary = "清空聊天记录")
    @DeleteMapping("/clear")
    public Result<String> clearMessages(@RequestParam String type, @RequestParam Long dialogId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        dialogService.clearMessages(type, dialogId, userId);
        return Result.success("聊天记录清空成功");
    }

    @Operation(summary = "导出聊天记录")
    @GetMapping("/export")
    public Result<String> exportMessages(@RequestParam String type,
                                        @RequestParam Long dialogId,
                                        @RequestParam(required = false) LocalDateTime startTime,
                                        @RequestParam(required = false) LocalDateTime endTime,
                                        HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String downloadUrl = dialogService.exportMessages(type, dialogId, startTime, endTime, userId);
        return Result.success("导出任务已创建", downloadUrl);
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

    // Request DTOs
    public static class SendMessageRequest {
        @NotBlank(message = "消息类型不能为空")
        private String type;

        @NotNull(message = "对话ID不能为空")
        private Long dialogId;

        private String msg;
        private String file;
        private String msgType = "text";

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getDialogId() { return dialogId; }
        public void setDialogId(Long dialogId) { this.dialogId = dialogId; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }
        public String getMsgType() { return msgType; }
        public void setMsgType(String msgType) { this.msgType = msgType; }
    }

    public static class ForwardMessageRequest {
        private List<Map<String, Object>> targetDialogs;

        public List<Map<String, Object>> getTargetDialogs() { return targetDialogs; }
        public void setTargetDialogs(List<Map<String, Object>> targetDialogs) { this.targetDialogs = targetDialogs; }
    }

    public static class ReplyMessageRequest {
        @NotBlank(message = "回复内容不能为空")
        private String msg;
        private String msgType = "text";

        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public String getMsgType() { return msgType; }
        public void setMsgType(String msgType) { this.msgType = msgType; }
    }

    public static class AddReactionRequest {
        @NotBlank(message = "表情不能为空")
        private String emoji;

        public String getEmoji() { return emoji; }
        public void setEmoji(String emoji) { this.emoji = emoji; }
    }

    public static class CreateGroupRequest {
        @NotBlank(message = "群名称不能为空")
        private String name;
        private String desc;
        private List<Long> memberIds;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public List<Long> getMemberIds() { return memberIds; }
        public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
    }

    public static class InviteToGroupRequest {
        private List<Long> userIds;

        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    }

    public static class UpdateGroupRequest {
        private String name;
        private String desc;
        private String avatar;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    public static class MuteRequest {
        private Long duration; // 禁言时长(分钟)

        public Long getDuration() { return duration; }
        public void setDuration(Long duration) { this.duration = duration; }
    }

    public static class MuteDialogRequest {
        private String type;
        private Long dialogId;
        private boolean muted;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getDialogId() { return dialogId; }
        public void setDialogId(Long dialogId) { this.dialogId = dialogId; }
        public boolean isMuted() { return muted; }
        public void setMuted(boolean muted) { this.muted = muted; }
    }
}