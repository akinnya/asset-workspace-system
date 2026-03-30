<template>
  <div id="globalHeader" class="w-full">
    <nav class="sticky top-0 z-50 px-8 flex items-center w-full h-20 transition-all duration-300 bg-white/88 border-b border-slate-200/80 backdrop-blur-xl">
      <!-- 1. Logo & Title -->
      <div class="flex items-center gap-4 cursor-pointer shrink-0 group" @click="router.push('/')">
        <div class="w-12 h-12 bg-slate-950 rounded-2xl p-2.5 border border-slate-900 shadow-sm flex items-center justify-center transition-transform duration-300 group-hover:-translate-y-0.5">
            <img class="w-full h-full object-contain" src="@/assets/brand-mark.svg" alt="Asset Workspace Logo" />
        </div>
        <span class="text-2xl font-black tracking-tight hidden md:block title-gradient">{{ t('title') }}</span>
      </div>

      <!-- Mobile Menu Trigger (New) -->
      <button class="md:hidden ml-4 w-10 h-10 flex items-center justify-center rounded-xl bg-slate-100 text-slate-700 border border-slate-200" @click="mobileMenuVisible = true">
          <MenuOutlined class="text-xl" />
      </button>
 
      <!-- 1.5 Navigation Tabs (New) -->
      <div class="hidden md:flex items-center justify-center ml-8 lg:ml-16 shrink-0">
        <div class="flex items-center p-1.5 bg-slate-100 rounded-2xl border border-slate-200 shadow-sm">
            <button 
                class="px-6 py-2 rounded-xl transition-all text-sm font-bold"
                :class="route.path === '/' || route.path === '/exhibition' ? 'bg-white shadow-sm text-primary' : 'text-slate-500 hover:text-primary hover:bg-white'"
                @click="router.push('/')"
            >
                {{ t('menu.home') }}
            </button>
            <button 
                 class="px-6 py-2 rounded-xl transition-all text-sm font-bold"
                 :class="route.path === '/asset/search' ? 'bg-white shadow-sm text-primary' : 'text-slate-500 hover:text-primary hover:bg-white'"
                 @click="router.push('/asset/search')"
            >
                {{ t('discover') }}
            </button>
            
            <!-- Upload Dropdown -->
            <a-dropdown placement="bottomRight">
                <button 
                    class="px-6 py-2 rounded-xl text-slate-500 hover:text-primary hover:bg-white transition-all text-sm font-bold flex items-center gap-2"
                >
                    {{ t('upload.self') }} <DownOutlined class="text-[10px]" />
                </button>
                <template #overlay>
                    <a-menu @click="doMenuClick">
                        <a-menu-item key="upload_single">
                            <span class="font-bold text-slate-700">{{ t('upload.singleFile') }}</span>
                        </a-menu-item>
                        <a-menu-item key="upload_batch">
                            <span class="font-bold text-slate-700">{{ t('upload.batchImport') }}</span>
                        </a-menu-item>
                    </a-menu>
                </template>
            </a-dropdown>

            <button 
                class="px-6 py-2 rounded-xl transition-all text-sm font-bold"
                :class="route.path.startsWith('/workspace') ? 'bg-white shadow-sm text-primary' : 'text-slate-500 hover:text-primary hover:bg-white'"
                @click="router.push('/workspace/my')"
            >
                {{ t('space.teamSpace') }}
            </button>
            <button 
                class="px-6 py-2 rounded-xl transition-all text-sm font-bold"
                :class="route.path === '/user/media' ? 'bg-white shadow-sm text-primary' : 'text-slate-500 hover:text-primary hover:bg-white'"
                @click="router.push('/user/media')"
            >
                {{ t('userMedia.library') }}
            </button>

        </div>
      </div>
 
      <!-- 2. Centered Gap (Search removed) -->
      <div class="hidden md:flex flex-1"></div>
 
      <!-- 3. Right Actions (Force Right) -->
      <div class="flex items-center gap-5 ml-auto shrink-0">
        
        <!-- 3.1 Notification (Leftmost of group) -->
          <a-popover
            v-model:open="notificationOpen"
            trigger="click"
            placement="bottomRight"
            :get-popup-container="getPopupContainer"
            :overlay-style="{ zIndex: 10050 }"
          >
                <button
                ref="notificationTriggerRef"
                class="w-10 h-10 flex items-center justify-center rounded-xl bg-white border border-slate-200 text-slate-600 transition-all relative hover:border-slate-300"
                @click.stop="toggleNotification"
                @mousedown.stop
            >
                <BellOutlined class="text-xl" />
                <span v-if="unreadCount > 0" class="absolute top-0 right-0 min-w-[20px] h-[20px] bg-accent-pink rounded-full border-2 border-white text-white text-[10px] font-black flex items-center justify-center shadow-lg">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </button>
           <template #content>
             <div ref="notificationPanelRef" class="w-96 max-w-[90vw] max-h-[30rem] overflow-hidden !bg-white rounded-[1.5rem] shadow-xl border border-slate-200" @click.stop>
               <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100/50">
                 <h3 class="font-black text-slate-800 text-lg">{{ t('notification.title') }}</h3>
                 <button v-if="unreadCount > 0" class="text-xs font-bold text-primary hover:underline uppercase tracking-wide" @click="doMarkAllAsRead">{{ t('notification.markAllRead') }}</button>
               </div>
               <div v-if="notifications.length === 0" class="py-16 text-center text-slate-400">
                 <BellOutlined class="text-5xl mb-4 opacity-20" />
                 <p class="text-sm font-bold">{{ t('notification.empty') }}</p>
               </div>
               <div v-else class="max-h-[22rem] overflow-y-auto no-scrollbar">
                 <div 
                   v-for="notif in notifications" 
                   :key="notif.id" 
                   class="px-6 py-4 hover:bg-primary/5 cursor-pointer border-b border-slate-50/50 transition-colors"
                   :class="{ 'bg-primary/5': notif.isRead === 0 }"
                   @click="handleNotificationClick(notif)"
                 >
                   <div class="flex items-start gap-4">
                     <div class="w-10 h-10 rounded-2xl bg-slate-100 shadow-sm flex items-center justify-center text-primary flex-shrink-0">
                       <HeartFilled v-if="notif.type === 'like'" class="text-red-500" />
                       <MessageFilled v-else-if="notif.type === 'comment'" class="text-primary" />
                       <StarFilled v-else-if="notif.type === 'star' || notif.type === 'favorite'" class="text-amber-500" />
                       <UserAddOutlined v-else-if="notif.type === 'follow'" />
                       <TeamOutlined v-else-if="['space_join', 'space_role', 'space_invite'].includes(notif.type || '')" class="text-primary" />
                       <BellFilled v-else />
                     </div>
                     <div class="flex-1 min-w-0">
                       <p class="text-sm font-bold text-slate-800 line-clamp-2">{{ getNotificationContent(notif) }}</p>
                       <p class="text-[11px] font-black text-slate-400 mt-1 uppercase tracking-wider">{{ formatTime(notif.createTime) }}</p>
                     </div>
                     <span v-if="notif.isRead === 0" class="w-2 h-2 rounded-full bg-primary flex-shrink-0 mt-2 shadow-glow"></span>
                   </div>
                 </div>
               </div>
               <div class="px-6 py-4 border-t border-slate-100/50 text-center">
                 <button class="text-sm font-black text-primary hover:underline" @click="router.push('/notifications'); notificationOpen = false">{{ t('notification.viewAll') }}</button>
               </div>
             </div>
           </template>
         </a-popover>
 
         <!-- 3.2 Gear Icon (Settings) -> Dropdown: Profile & Lang -->
         <a-dropdown placement="bottomRight" :overlay-style="{ paddingTop: '10px' }">
                <button class="w-10 h-10 flex items-center justify-center rounded-xl bg-white border border-slate-200 text-slate-600 transition-all hover:border-slate-300">
                  <SettingOutlined class="text-xl" />
              </button>
              <template #overlay>
                 <a-menu class="!rounded-[2rem] !p-2 !border-none !shadow-2xl glass-effect !bg-white/95 w-48 overflow-hidden">
                     <a-menu-item @click="goToProfile" class="!rounded-xl !py-3 !px-4 hover:!bg-primary/5">
                          <template #icon><UserOutlined class="text-primary" /></template>
                          <span class="font-bold text-slate-700">{{ t('user.profile') }}</span>
                     </a-menu-item>
                     <a-menu-item @click="toggleLang" class="!rounded-xl !py-3 !px-4 hover:!bg-primary/5">
                          <template #icon><GlobalOutlined class="text-primary" /></template>
                          <span class="font-bold text-slate-700">{{ currentLang === 'zh' ? 'English' : '中文' }}</span>
                     </a-menu-item>
                 </a-menu>
              </template>
         </a-dropdown>
 
         <!-- 3.3 User Profile / Login (Rightmost) -->
         <div v-if="loginUserStore.loginUser.id">
             <a-dropdown placement="bottomRight" :overlay-style="{ paddingTop: '10px' }">
                 <button class="flex items-center gap-3 pl-1.5 pr-4 py-1.5 rounded-2xl bg-white border border-slate-200 transition-all cursor-pointer shadow-sm hover:border-slate-300 group">
                     <div class="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center text-primary font-bold text-xs overflow-hidden border border-slate-200 shadow-sm transition-transform group-hover:scale-105">
                         <img :src="headerAvatarUrl" @error="handleHeaderAvatarError" class="w-full h-full object-cover" />
                     </div>
                     <span class="text-sm font-black text-slate-800 max-w-[100px] truncate">{{ loginUserStore.loginUser.userName }}</span>
                 </button>
                 <template #overlay>
                     <a-menu class="!rounded-[2rem] !p-2 !border-none !shadow-2xl glass-effect !bg-white/95 w-60 overflow-hidden">
                          <a-menu-item @click="router.push('/user/media')" class="!rounded-xl !py-3 !px-4 hover:!bg-primary/5">
                              <template #icon><span class="material-symbols-outlined text-[18px] text-primary">perm_media</span></template>
                              <span class="font-bold text-slate-700">{{ t('userMedia.library') }}</span>
                          </a-menu-item>
                          <a-menu-item @click="router.push('/workspace/my')" class="!rounded-xl !py-3 !px-4 hover:!bg-primary/5">
                              <template #icon><TeamOutlined class="text-primary" /></template>
                              <span class="font-bold text-slate-700">{{ t('space.teamSpace') }}</span>
                          </a-menu-item>
                          <!-- Admin Links -->
                          <template v-if="loginUserStore.loginUser.userRole === 'admin'">
                              <a-menu-divider class="!bg-slate-100/50" />
                              <a-menu-item @click="openInNewTab('/admin/user')" class="!rounded-xl !py-3 !px-4 hover:!bg-primary/5">
                                  <template #icon><SettingOutlined class="text-primary" /></template>
                                  <span class="font-bold text-slate-700">{{ t('adminController') }}</span>
                              </a-menu-item>
                          </template>
                         <a-menu-divider class="!bg-slate-100/50" />
                         <a-menu-item @click="doLogout" class="!rounded-xl !py-3 !px-4 hover:!bg-red-50 text-red-500">
                             <template #icon><LogoutOutlined /></template>
                             <span class="font-bold">{{ t('user.logout') }}</span>
                         </a-menu-item>
                     </a-menu>
                 </template>
             </a-dropdown>
         </div>
         <div v-else>
              <button class="flex items-center gap-2 px-8 py-3 rounded-2xl bg-primary text-white hover:bg-primary-hover shadow-glow transition-all font-black text-sm active:scale-95" @click="router.push('/user/login?redirect=' + route.fullPath)">
                 {{ t('user.login') }}
              </button>
         </div>

      </div>
    </nav>

    <!-- Mobile Navigation Drawer (New) -->
    <a-drawer
        v-model:open="mobileMenuVisible"
        placement="left"
        :closable="false"
        width="280px"
        class="mobile-nav-drawer"
    >
        <div class="p-6">
            <div class="flex items-center gap-4 mb-10">
                <div class="w-10 h-10 bg-slate-950 rounded-xl p-2 flex items-center justify-center">
                    <img class="w-full h-full object-contain" src="@/assets/brand-mark.svg" />
                </div>
                <span class="text-xl font-black title-gradient">{{ t('title') }}</span>
            </div>

            <div class="space-y-2">
                <button 
                    class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold transition-all"
                    :class="route.path === '/' ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'"
                    @click="mobileRoute('/')"
                >
                    <HomeOutlined /> {{ t('menu.home') }}
                </button>
                <button 
                    class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold transition-all"
                    :class="route.path === '/asset/search' ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'"
                    @click="mobileRoute('/asset/search')"
                >
                    <SearchOutlined /> {{ t('discover') }}
                </button>
                <div class="h-px bg-slate-100 my-4"></div>
                <template v-if="loginUserStore.loginUser.id">
                    <button class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold text-slate-600 hover:bg-slate-50 transition-all" @click="mobileRoute('/asset/upload/batch')">
                        <PlusOutlined /> {{ t('upload.batchImport') }}
                    </button>
                    <button class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold text-slate-600 hover:bg-slate-50 transition-all" @click="mobileRoute('/asset/upload')">
                        <PlusOutlined /> {{ t('upload.singleFile') }}
                    </button>
                    <button class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold text-slate-600 hover:bg-slate-50 transition-all" @click="mobileRoute('/user/media')">
                        <CloudOutlined /> {{ t('userMedia.library') }}
                    </button>
                    <button class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold text-slate-600 hover:bg-slate-50 transition-all" @click="mobileRoute('/workspace/my')">
                        <TeamOutlined /> {{ t('space.teamSpace') }}
                    </button>
                    <button class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold text-slate-600 hover:bg-slate-50 transition-all" @click="mobileRoute('/user/profile')">
                        <UserOutlined /> {{ t('user.profile') }}
                    </button>
                    <button class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold text-slate-600 hover:bg-slate-50 transition-all" @click="mobileRoute('/user/settings')">
                        <SettingOutlined /> {{ t('user.settings') }}
                    </button>
                    <button v-if="loginUserStore.loginUser.userRole === ACCESS_ENUM.ADMIN" class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold text-amber-600 hover:bg-amber-50 transition-all" @click="mobileRoute('/admin/user')">
                        <CrownOutlined /> {{ t('admin.self') }}
                    </button>
                </template>
                <div class="h-px bg-slate-100 my-4"></div>
                <button 
                  class="w-full flex items-center gap-4 px-4 py-4 rounded-2xl font-bold text-slate-500 hover:bg-slate-50 transition-all"
                  @click="languageStore.toggleLanguage"
                >
                  <TranslationOutlined /> {{ languageStore.currentLang === 'zh' ? 'English' : '简体中文' }}
                </button>
            </div>
        </div>
    </a-drawer>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { 
    HomeOutlined, 
    PlusOutlined, 
    UserOutlined, 
    SettingOutlined, 
    LogoutOutlined, 
    TeamOutlined,
    SearchOutlined,
    BellOutlined,
    GlobalOutlined,
    HeartOutlined,
    HeartFilled,
    MessageOutlined,
    MessageFilled,
    StarFilled,
    BellFilled,
    UserAddOutlined,
    DownOutlined,
    MenuOutlined,
    TranslationOutlined,
    CloudOutlined,
    CrownOutlined
} from '@ant-design/icons-vue'
import ACCESS_ENUM from '@/access/accessEnum'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { userLogoutUsingPost } from '@/api/user/userController'
import { 
    listNotificationsUsingPost, 
    getUnreadCountUsingGet, 
    markAllAsReadUsingPost,
    markAsReadUsingPost
} from '@/api/notification/notificationController'
import defaultAvatar from '@/assets/avatar.png'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

// Router & Store
const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()
const { t, currentLang } = storeToRefs(languageStore)
const { toggleLanguage } = languageStore

// State
const current = ref<string[]>([])
const notificationOpen = ref(false)
const mobileMenuVisible = ref(false)
const showNotifs = ref(false)
const notifications = ref<any[]>([])
const unreadCount = ref(0)
let pollTimer: any = null
const headerAvatarFallback = ref(false)
const headerAvatarRefreshTried = ref(false)
const headerAvatarVersion = ref(0)

const headerAvatarUrl = computed(() => {
    const baseUrl = headerAvatarFallback.value
        ? defaultAvatar
        : (loginUserStore.loginUser.userAvatar || defaultAvatar)
    if (baseUrl === defaultAvatar) {
        return baseUrl
    }
    const separator = baseUrl.includes('?') ? '&' : '?'
    return `${baseUrl}${separator}v=${headerAvatarVersion.value}`
})

const openInNewTab = (url: string) => {
    const target = router.resolve(url)
    window.open(target.href, '_blank')
}

const notificationTriggerRef = ref<HTMLElement | null>(null)
const notificationPanelRef = ref<HTMLElement | null>(null)

const toggleNotification = () => {
    notificationOpen.value = !notificationOpen.value
    if (notificationOpen.value) {
        fetchNotifications()
    }
}

const getPopupContainer = (trigger?: HTMLElement) => {
    return trigger?.parentElement || document.body
}

const handleDocumentClick = (event: MouseEvent) => {
    if (!notificationOpen.value) return
    const target = event.target as Node
    if (notificationTriggerRef.value?.contains(target)) return
    if (notificationPanelRef.value?.contains(target)) return
    notificationOpen.value = false
}

// Methods
const mobileRoute = (path: string) => {
    mobileMenuVisible.value = false
    router.push(path)
}

const toggleLang = () => {
    toggleLanguage()
}

const doMenuClick = ({ key }: { key: string }) => {
  if (key === 'upload_single') {
    router.push('/asset/upload')
  } else if (key === 'upload_batch') {
    router.push('/asset/upload/batch')
  }
}

const goToSettings = () => {
  router.push('/user/settings')
}

const goToUpload = () => {
  router.push('/asset/upload')
}

const goToProfile = () => {
  router.push('/user/profile')
}

const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.logout()
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}

const handleHeaderAvatarError = async () => {
    if (headerAvatarFallback.value) {
        return
    }
    if (!loginUserStore.loginUser.id) {
        headerAvatarFallback.value = true
        return
    }
    if (!headerAvatarRefreshTried.value) {
        const previousAvatar = loginUserStore.loginUser.userAvatar
        headerAvatarRefreshTried.value = true
        try {
            await loginUserStore.fetchLoginUser()
        } catch (e) {
            // ignore
        }
        const refreshedAvatar = loginUserStore.loginUser.userAvatar
        if (!refreshedAvatar || refreshedAvatar === previousAvatar) {
            headerAvatarFallback.value = true
            return
        }
        headerAvatarVersion.value += 1
    } else {
        headerAvatarFallback.value = true
    }
}

// Notification Methods
const fetchUnreadCount = async () => {
    if (!loginUserStore.loginUser.id) return
    try {
        const res = await getUnreadCountUsingGet()
        if (res.data.code === 0) {
            unreadCount.value = Number(res.data.data) || 0
        }
    } catch (e) {
        console.error('Fetch unread count failed', e)
    }
}

const fetchNotifications = async () => {
    if (!loginUserStore.loginUser.id) return
    try {
        const res = await listNotificationsUsingPost({ current: 1, pageSize: 20 })
        if (res.data.code === 0 && res.data.data) {
            notifications.value = res.data.data.records || []
        }
    } catch (e) {
        message.error(t.value('notification.loadFailed'))
    }
}

watch(
    () => notificationOpen.value,
    (open) => {
        if (open) {
            fetchNotifications()
        }
    }
)

watch(
    () => loginUserStore.loginUser.userAvatar,
    () => {
        headerAvatarFallback.value = false
        headerAvatarRefreshTried.value = false
        headerAvatarVersion.value += 1
    }
)

const doMarkAllAsRead = async () => {
    try {
        const res = await markAllAsReadUsingPost()
        if (res.data.code === 0) {
            message.success(t.value('notification.markAllSuccess'))
            unreadCount.value = 0
            notifications.value.forEach(n => n.isRead = 1)
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
            unreadCount.value = Math.max(0, unreadCount.value - 1)
        } catch (e) {}
    }
    
    // Improved navigation based on notification type and targetId
    if (notif.targetId) {
        // Assume targetId is pictureId for now, could be dynamic based on type
        // You might need to adjust this logic if you have different target types
        if (['like', 'comment', 'star', 'favorite'].includes(notif.type || '')) {
             router.push(`/asset/${notif.targetId}`)
        } else if (['space_join', 'space_role', 'space_invite'].includes(notif.type || '')) {
             router.push(`/workspace/detail/${notif.targetId}`)
        } else if (notif.type === 'follow' && notif.fromUserId) {
            // For follow, maybe go to user profile
             // However, backend might need to provide fromUserId. 
             // Without it, maybe just stay or go to notification center
             if (notif.fromUserId) {
                 // router.push(`/user/${notif.fromUserId}`) // If you have public profile page
             }
        }
    }
    notificationOpen.value = false
}

const formatTime = (timeStr?: string) => {
    if (!timeStr) return ''
    return dayjs(timeStr).fromNow()
}

// Hooks
onMounted(() => {
    // 保留生命周期钩子，实际轮询由 watch 控制
    document.addEventListener('click', handleDocumentClick, true)
    window.addEventListener('notification-refresh', fetchUnreadCount)
})

onUnmounted(() => {
    if (pollTimer) clearInterval(pollTimer)
    document.removeEventListener('click', handleDocumentClick, true)
    window.removeEventListener('notification-refresh', fetchUnreadCount)
})

const startNotificationPolling = () => {
    if (pollTimer) clearInterval(pollTimer)
    fetchUnreadCount()
    pollTimer = setInterval(fetchUnreadCount, 60000)
}

const stopNotificationPolling = () => {
    if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
    }
    unreadCount.value = 0
    notifications.value = []
}

watch(
    () => loginUserStore.loginUser.id,
    (userId) => {
        if (userId) {
            startNotificationPolling()
        } else {
            stopNotificationPolling()
        }
    },
    { immediate: true }
)

// Router Listener
router.afterEach((to) => {
  current.value = [to.path]
})
</script>
<style scoped>
.glass-nav {
    background: rgba(255, 255, 255, 0.4);
    backdrop-filter: blur(24px) saturate(180%);
    -webkit-backdrop-filter: blur(24px) saturate(180%);
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.no-scrollbar::-webkit-scrollbar {
    display: none;
}
</style>
