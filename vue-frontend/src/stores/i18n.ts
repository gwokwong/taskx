import { defineStore } from 'pinia'
import { ref } from 'vue'

export type Language = 'zh-CN' | 'en-US' | 'ja-JP' | 'ko-KR'

interface Messages {
  [key: string]: string | Messages
}

interface LanguageMessages {
  [lang in Language]: Messages
}

export const useI18nStore = defineStore('i18n', () => {
  const currentLanguage = ref<Language>('zh-CN')
  const messages = ref<LanguageMessages>({
    'zh-CN': {
      common: {
        save: '保存',
        cancel: '取消',
        delete: '删除',
        edit: '编辑',
        create: '创建',
        search: '搜索',
        loading: '加载中...',
        success: '操作成功',
        error: '操作失败',
        confirm: '确认',
        yes: '是',
        no: '否',
        ok: '确定',
        back: '返回',
        next: '下一步',
        previous: '上一步',
        submit: '提交',
        reset: '重置',
        export: '导出',
        import: '导入',
        view: '查看',
        copy: '复制',
        share: '分享',
        close: '关闭'
      },
      nav: {
        dashboard: '仪表板',
        projects: '项目',
        tasks: '任务',
        calendar: '日历',
        messages: '消息',
        files: '文件',
        settings: '设置',
        admin: '管理',
        users: '用户管理',
        departments: '部门管理',
        logs: '系统日志',
        workflows: '工作流',
        templates: '模板',
        reports: '报表'
      },
      user: {
        login: '登录',
        logout: '退出',
        register: '注册',
        profile: '个人资料',
        email: '邮箱',
        password: '密码',
        nickname: '昵称',
        department: '部门',
        role: '角色',
        status: '状态',
        active: '正常',
        inactive: '禁用',
        lastLogin: '最后登录',
        createdAt: '创建时间'
      },
      project: {
        name: '项目名称',
        description: '项目描述',
        owner: '项目负责人',
        members: '项目成员',
        progress: '进度',
        status: '状态',
        created: '已创建',
        inProgress: '进行中',
        completed: '已完成',
        archived: '已归档'
      },
      task: {
        // 基础字段
        title: '任务标题',
        name: '任务名称',
        description: '任务描述',
        content: '任务内容',
        assignee: '负责人',
        assignees: '分配人员',
        priority: '优先级',
        status: '状态',
        dueDate: '截止日期',
        deadline: '截止时间',
        startTime: '开始时间',
        tags: '标签',
        progress: '进度',

        // 优先级
        priority: {
          low: '低',
          medium: '中',
          high: '高',
          urgent: '紧急'
        },

        // 状态
        status: {
          pending: '待办',
          inProgress: '进行中',
          review: '评审中',
          testing: '测试中',
          completed: '已完成',
          archived: '已归档',
          overdue: '已逾期'
        },

        // 管理操作
        management: '任务管理',
        createNew: '创建新任务',
        create: '创建任务',
        edit: '编辑任务',
        update: '更新任务',
        delete: '删除任务',
        complete: '完成任务',
        markComplete: '标记完成',
        reopen: '重新打开',
        archive: '归档任务',
        duplicate: '复制任务',
        move: '移动任务',
        assign: '分配任务',

        // 批量操作
        bulkAssign: '批量分配',
        bulkComplete: '批量完成',
        bulkArchive: '批量归档',
        bulkDelete: '批量删除',
        bulkAssignDescription: '为选中的 {count} 个任务批量分配人员',

        // 子任务
        subtasks: '子任务',
        addSubtask: '添加子任务',
        createSubtask: '创建子任务',
        subtaskProgress: '子任务进度',

        // 评论和附件
        comments: '评论',
        addComment: '添加评论',
        addCommentPlaceholder: '请输入评论内容...',
        attachments: '附件',
        addAttachment: '添加附件',

        // 表单和输入
        namePlaceholder: '请输入任务名称',
        descriptionPlaceholder: '请输入任务描述',
        contentPlaceholder: '请输入详细内容',
        selectProject: '选择项目',
        selectColumn: '选择任务列',
        selectAssignee: '选择分配人员',
        selectTemplate: '选择模板',
        selectDeadline: '选择截止时间',
        selectStartTime: '选择开始时间',
        selectParentTask: '选择父任务',
        addTag: '添加标签',
        addDescription: '添加描述',

        // 统计信息
        total: '总计',
        completed: '已完成',
        pending: '待处理',
        overdue: '已逾期',
        today: '今日到期',
        tomorrow: '明日到期',

        // 视图模式
        listView: '列表视图',
        kanbanView: '看板视图',
        calendarView: '日历视图',

        // 排序和筛选
        sortBy: '排序方式',
        sort: {
          created: '创建时间',
          updated: '更新时间',
          deadline: '截止时间',
          priority: '优先级',
          name: '名称'
        },
        filter: '筛选',
        filterBy: '筛选条件',

        // 看板列
        column: '任务列',
        addToColumn: '添加到此列',
        columnTodo: '待办',
        columnInProgress: '进行中',
        columnReview: '评审',
        columnDone: '已完成',

        // 时间相关
        createdAt: '创建时间',
        updatedAt: '更新时间',
        completedAt: '完成时间',
        estimatedTime: '预估时间',
        hours: '小时',
        daysLeft: '{days} 天后到期',
        overdue: '逾期 {days} 天',
        today: '今天',
        tomorrow: '明天',

        // 模板
        useTemplate: '使用模板',
        noTemplate: '不使用模板',
        templateApplied: '模板已应用',
        templateAppliedDesc: '已应用模板 {name}',

        // 高级选项
        advancedOptions: '高级选项',
        parentTask: '父任务',
        dependencies: '依赖任务',

        // 操作结果
        createSuccess: '任务创建成功',
        updateSuccess: '任务更新成功',
        deleteSuccess: '任务删除成功',
        completeSuccess: '任务完成成功',
        archiveSuccess: '任务归档成功',
        assignSuccess: '任务分配成功',
        moveSuccess: '任务移动成功',
        exportSuccess: '任务导出成功',

        // 批量操作结果
        bulkCompleteSuccess: '批量完成成功',
        bulkArchiveSuccess: '批量归档成功',
        bulkDeleteSuccess: '批量删除成功',
        bulkAssignSuccess: '批量分配成功：{action} 了 {tasks} 个任务给 {users} 个用户',

        // 错误信息
        createFailed: '任务创建失败',
        updateFailed: '任务更新失败',
        deleteFailed: '任务删除失败',
        completeFailed: '任务完成失败',
        archiveFailed: '任务归档失败',
        assignFailed: '任务分配失败',
        moveFailed: '任务移动失败',
        exportFailed: '任务导出失败',

        // 其他
        info: '任务信息',
        copy: '副本',
        selectedCount: '已选择 {count} 个任务',
        affectedTasks: '影响的任务',
        usersToAssign: '要分配的用户',
        usersToRemove: '要移除的用户',
        currentAssignments: '当前分配情况',
        previewChanges: '预览变更',
        assignAction: '分配操作',
        replaceAssignees: '替换分配人员',
        replaceAssigneesDesc: '移除现有分配人员并添加新的分配人员',
        addAssignees: '添加分配人员',
        addAssigneesDesc: '在现有分配人员基础上添加新的分配人员',
        removeAssignees: '移除分配人员',
        removeAssigneesDesc: '从现有分配人员中移除选中的人员',
        selectUsersToAssign: '选择要分配的用户',
        selectUsersToRemove: '选择要移除的用户',
        tagExists: '标签已存在',
        tagAdded: '标签已添加',
        tagRemoved: '标签已移除',
        subtaskAdded: '子任务已添加',
        commentAdded: '评论已添加',
        filesUploaded: '文件上传成功',
        assigneeAdded: '分配人员已添加',
        assigneeRemoved: '分配人员已移除',
        completed: '任务已完成',
        reopened: '任务已重新打开',
        archived: '任务已归档',
        deleted: '任务已删除',
        assigning: '分配中...'
      },
      calendar: {
        today: '今天',
        month: '月',
        week: '周',
        day: '日',
        event: '事件',
        reminder: '提醒',
        meeting: '会议',
        birthday: '生日',
        allDay: '全天',
        startTime: '开始时间',
        endTime: '结束时间',
        location: '地点',
        repeat: '重复'
      },
      message: {
        send: '发送',
        reply: '回复',
        forward: '转发',
        subject: '主题',
        content: '内容',
        attachment: '附件',
        read: '已读',
        unread: '未读',
        draft: '草稿'
      },
      file: {
        upload: '上传',
        download: '下载',
        name: '文件名',
        size: '大小',
        type: '类型',
        uploadTime: '上传时间',
        folder: '文件夹',
        rename: '重命名',
        move: '移动'
      },
      workflow: {
        name: '工作流名称',
        description: '工作流描述',
        status: '状态',
        trigger: '触发器',
        steps: '步骤',
        start: '启动',
        stop: '停止',
        pause: '暂停',
        resume: '恢复',
        draft: '草稿',
        active: '活跃',
        inactive: '非活跃'
      },
      validation: {
        required: '此字段为必填项',
        email: '请输入有效的邮箱地址',
        minLength: '最少需要 {min} 个字符',
        maxLength: '最多允许 {max} 个字符',
        passwordMismatch: '两次输入的密码不一致'
      }
    },
    'en-US': {
      common: {
        save: 'Save',
        cancel: 'Cancel',
        delete: 'Delete',
        edit: 'Edit',
        create: 'Create',
        search: 'Search',
        loading: 'Loading...',
        success: 'Success',
        error: 'Error',
        confirm: 'Confirm',
        yes: 'Yes',
        no: 'No',
        ok: 'OK',
        back: 'Back',
        next: 'Next',
        previous: 'Previous',
        submit: 'Submit',
        reset: 'Reset',
        export: 'Export',
        import: 'Import',
        view: 'View',
        copy: 'Copy',
        share: 'Share',
        close: 'Close'
      },
      nav: {
        dashboard: 'Dashboard',
        projects: 'Projects',
        tasks: 'Tasks',
        calendar: 'Calendar',
        messages: 'Messages',
        files: 'Files',
        settings: 'Settings',
        admin: 'Admin',
        users: 'Users',
        departments: 'Departments',
        logs: 'Logs',
        workflows: 'Workflows',
        templates: 'Templates',
        reports: 'Reports'
      },
      user: {
        login: 'Login',
        logout: 'Logout',
        register: 'Register',
        profile: 'Profile',
        email: 'Email',
        password: 'Password',
        nickname: 'Nickname',
        department: 'Department',
        role: 'Role',
        status: 'Status',
        active: 'Active',
        inactive: 'Inactive',
        lastLogin: 'Last Login',
        createdAt: 'Created At'
      },
      project: {
        name: 'Project Name',
        description: 'Description',
        owner: 'Owner',
        members: 'Members',
        progress: 'Progress',
        status: 'Status',
        created: 'Created',
        inProgress: 'In Progress',
        completed: 'Completed',
        archived: 'Archived'
      },
      task: {
        title: 'Task Title',
        description: 'Description',
        assignee: 'Assignee',
        priority: 'Priority',
        status: 'Status',
        dueDate: 'Due Date',
        low: 'Low',
        medium: 'Medium',
        high: 'High',
        urgent: 'Urgent',
        todo: 'To Do',
        doing: 'In Progress',
        done: 'Done'
      },
      calendar: {
        today: 'Today',
        month: 'Month',
        week: 'Week',
        day: 'Day',
        event: 'Event',
        reminder: 'Reminder',
        meeting: 'Meeting',
        birthday: 'Birthday',
        allDay: 'All Day',
        startTime: 'Start Time',
        endTime: 'End Time',
        location: 'Location',
        repeat: 'Repeat'
      },
      message: {
        send: 'Send',
        reply: 'Reply',
        forward: 'Forward',
        subject: 'Subject',
        content: 'Content',
        attachment: 'Attachment',
        read: 'Read',
        unread: 'Unread',
        draft: 'Draft'
      },
      file: {
        upload: 'Upload',
        download: 'Download',
        name: 'File Name',
        size: 'Size',
        type: 'Type',
        uploadTime: 'Upload Time',
        folder: 'Folder',
        rename: 'Rename',
        move: 'Move'
      },
      workflow: {
        name: 'Workflow Name',
        description: 'Description',
        status: 'Status',
        trigger: 'Trigger',
        steps: 'Steps',
        start: 'Start',
        stop: 'Stop',
        pause: 'Pause',
        resume: 'Resume',
        draft: 'Draft',
        active: 'Active',
        inactive: 'Inactive'
      },
      validation: {
        required: 'This field is required',
        email: 'Please enter a valid email address',
        minLength: 'Minimum {min} characters required',
        maxLength: 'Maximum {max} characters allowed',
        passwordMismatch: 'Passwords do not match'
      }
    },
    'ja-JP': {
      common: {
        save: '保存',
        cancel: 'キャンセル',
        delete: '削除',
        edit: '編集',
        create: '作成',
        search: '検索',
        loading: '読み込み中...',
        success: '成功',
        error: 'エラー',
        confirm: '確認',
        yes: 'はい',
        no: 'いいえ',
        ok: 'OK',
        back: '戻る',
        next: '次へ',
        previous: '前へ',
        submit: '送信',
        reset: 'リセット',
        export: 'エクスポート',
        import: 'インポート',
        view: '表示',
        copy: 'コピー',
        share: '共有',
        close: '閉じる'
      },
      // 其他日语翻译...
    },
    'ko-KR': {
      common: {
        save: '저장',
        cancel: '취소',
        delete: '삭제',
        edit: '편집',
        create: '생성',
        search: '검색',
        loading: '로딩 중...',
        success: '성공',
        error: '오류',
        confirm: '확인',
        yes: '예',
        no: '아니요',
        ok: '확인',
        back: '뒤로',
        next: '다음',
        previous: '이전',
        submit: '제출',
        reset: '재설정',
        export: '내보내기',
        import: '가져오기',
        view: '보기',
        copy: '복사',
        share: '공유',
        close: '닫기'
      },
      // 其他韩语翻译...
    }
  })

  const setLanguage = (lang: Language) => {
    currentLanguage.value = lang
    localStorage.setItem('dootask-language', lang)
  }

  const t = (key: string, params?: Record<string, any>): string => {
    const keys = key.split('.')
    let value: any = messages.value[currentLanguage.value]

    for (const k of keys) {
      if (value && typeof value === 'object') {
        value = value[k]
      } else {
        return key // 如果找不到翻译，返回原始key
      }
    }

    if (typeof value === 'string') {
      // 处理参数替换
      if (params) {
        return value.replace(/\{(\w+)\}/g, (match, paramKey) => {
          return params[paramKey]?.toString() || match
        })
      }
      return value
    }

    return key
  }

  const loadLanguage = () => {
    const savedLang = localStorage.getItem('dootask-language') as Language
    if (savedLang && ['zh-CN', 'en-US', 'ja-JP', 'ko-KR'].includes(savedLang)) {
      currentLanguage.value = savedLang
    }
  }

  // 初始化时加载保存的语言设置
  loadLanguage()

  return {
    currentLanguage,
    messages,
    setLanguage,
    t,
    loadLanguage
  }
})

// 全局函数
export const useI18n = () => {
  const i18nStore = useI18nStore()
  return {
    t: i18nStore.t,
    currentLanguage: i18nStore.currentLanguage,
    setLanguage: i18nStore.setLanguage
  }
}