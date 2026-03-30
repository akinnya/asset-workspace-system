<template>
  <div id="notificationPage" class="min-h-screen bg-slate-50 font-display pb-20">
    <div class="bg-white border-b border-slate-200 py-8 px-4 sm:px-6 md:px-12">
      <div class="max-w-6xl mx-auto flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 class="text-2xl sm:text-3xl font-black text-slate-800">
            {{ t('notification.title') }}
          </h1>
          <p class="text-slate-500 text-sm sm:text-base">
            {{ t('notification.viewAll') }}
          </p>
        </div>
        <button
          class="px-5 py-2.5 rounded-2xl bg-primary text-white font-bold shadow-glow hover:bg-primary-hover transition-all"
          @click="doMarkAllAsRead"
        >
          {{ t('notification.markAllRead') }}
        </button>
      </div>
    </div>

    <div class="max-w-6xl mx-auto px-4 sm:px-6 md:px-12 mt-10">
      <div v-if="loading && notifications.length === 0" class="text-center text-slate-400 py-16">
        {{ t('common.loading') }}
      </div>

      <div v-else-if="notifications.length === 0" class="text-center text-slate-400 py-20 bg-white rounded-[32px] border border-dashed border-slate-200">
        <BellOutlined class="text-5xl mb-4 opacity-30" />
        <div class="text-sm font-bold">{{ t('notification.empty') }}</div>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="notif in notifications"
          :key="notif.id"
          class="bg-white rounded-[24px] border border-slate-100 shadow-sm px-5 py-4 flex items-start gap-4 cursor-pointer hover:shadow-lg transition-all"
          :class="notif.isRead === 0 ? 'ring-1 ring-primary/20' : ''"
          @click="handleNotificationClick(notif)"
        >
          <div class="w-12 h-12 rounded-2xl bg-slate-50 flex items-center justify-center text-primary">
            <HeartFilled v-if="notif.type === 'like'" class="text-red-500" />
            <MessageFilled v-else-if="notif.type === 'comment'" class="text-primary" />
            <StarFilled v-else-if="notif.type === 'star' || notif.type === 'favorite'" class="text-amber-500" />
            <UserAddOutlined v-else-if="notif.type === 'follow'" />
            <TeamOutlined v-else-if="['space_join', 'space_role', 'space_invite'].includes(notif.type || '')" class="text-primary" />
            <BellFilled v-else />
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-sm sm:text-base font-bold text-slate-800 mb-1">
              {{ getNotificationContent(notif) }}
            </div>
            <div class="text-xs text-slate-400 font-semibold uppercase tracking-wide">
              {{ formatTime(notif.createTime) }}
            </div>
          </div>
          <div v-if="notif.isRead === 0" class="w-2 h-2 rounded-full bg-primary mt-2"></div>
        </div>
      </div>

      <div v-if="notifications.length > 0" class="flex justify-center mt-10">
        <button
          class="px-6 py-2.5 rounded-full border border-slate-200 text-slate-500 font-bold hover:text-primary hover:border-primary transition-all"
          :disabled="loading || !hasMore"
          @click="loadMore"
        >
          {{ hasMore ? t('common.loadMore') : t('common.noMore') || t('common.loadMore') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import { storeToRefs } from 'pinia'
import {
  BellOutlined,
  BellFilled,
  HeartFilled,
  MessageFilled,
  StarFilled,
  UserAddOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import {
  listNotificationsUsingPost,
  markAllAsReadUsingPost,
  markAsReadUsingPost,
} from '@/api/notification/notificationController'

dayjs.extend(relativeTime)

const router = useRouter()
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const notifications = ref<API.Notification[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
})

const hasMore = computed(() => notifications.value.length < pagination.value.total)

const fetchNotifications = async (reset = false) => {
  if (loading.value) return
  loading.value = true
  try {
    if (reset) {
      pagination.value.current = 1
    }
    const res = await listNotificationsUsingPost({
      current: pagination.value.current,
      pageSize: pagination.value.pageSize,
    })
    if (res.data.code === 0 && res.data.data) {
      const records = res.data.data.records || []
      if (reset) {
        notifications.value = records
      } else {
        notifications.value = [...notifications.value, ...records]
      }
      pagination.value.total = res.data.data.total || notifications.value.length
    } else {
      message.error(t.value('notification.loadFailed'))
    }
  } catch (e) {
    message.error(t.value('notification.loadFailed'))
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  pagination.value.current += 1
  await fetchNotifications(false)
}

const doMarkAllAsRead = async () => {
  try {
    const res = await markAllAsReadUsingPost()
    if (res.data.code === 0) {
      notifications.value = notifications.value.map((item) => ({
        ...item,
        isRead: 1,
      }))
      window.dispatchEvent(new Event('notification-refresh'))
      message.success(t.value('notification.markAllSuccess'))
    } else {
      message.error(t.value('notification.markAllFailed'))
    }
  } catch (e) {
    message.error(t.value('notification.markAllFailed'))
  }
}

const getNotificationContent = (notif: API.Notification) => {
  const raw = notif.content || ''
  if (!raw) return ''
  if (!raw.trim().startsWith('{')) {
    return raw
  }
  try {
    const parsed = JSON.parse(raw)
    if (parsed && typeof parsed === 'object' && typeof parsed.key === 'string') {
      const params = parsed.params && typeof parsed.params === 'object' ? parsed.params : undefined
      return t.value(parsed.key, params)
    }
  } catch (e) {
    return raw
  }
  return raw
}

const handleNotificationClick = async (notif: API.Notification) => {
  if (notif.isRead === 0) {
    try {
      await markAsReadUsingPost([notif.id!])
      notif.isRead = 1
      window.dispatchEvent(new Event('notification-refresh'))
    } catch (e) {}
  }

  if (notif.targetId) {
    if (['like', 'comment', 'star', 'favorite'].includes(notif.type || '')) {
      router.push(`/asset/${notif.targetId}`)
    } else if (['space_join', 'space_role', 'space_invite'].includes(notif.type || '')) {
      router.push(`/workspace/detail/${notif.targetId}`)
    }
  }
}

const formatTime = (timeStr?: string) => {
  if (!timeStr) return ''
  return dayjs(timeStr).fromNow()
}

onMounted(() => {
  fetchNotifications(true)
})
</script>

<style scoped>
#notificationPage :deep(.anticon) {
  line-height: 1;
}
</style>
