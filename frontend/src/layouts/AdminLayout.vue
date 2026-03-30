<template>
  <div class="flex h-screen w-full relative overflow-hidden bg-slate-100 font-display text-slate-700">
    <div class="fixed inset-0 pointer-events-none z-0 opacity-60" style="background-image:linear-gradient(rgba(15,23,42,0.04) 1px,transparent 1px),linear-gradient(90deg,rgba(15,23,42,0.04) 1px,transparent 1px);background-size:32px 32px;"></div>

    <!-- Sidebar -->
    <aside class="w-72 flex flex-col bg-white/92 backdrop-blur-xl border-r border-slate-200 z-20 relative shadow-[8px_0_30px_-18px_rgba(15,23,42,0.18)] transition-all duration-300">
      <div class="p-8 pb-4 flex items-center gap-3">
        <div class="w-12 h-12 rounded-2xl bg-slate-950 flex items-center justify-center p-2 shadow-sm border border-slate-900">
           <img alt="Asset Workspace Logo" class="w-full h-full object-contain" src="@/assets/brand-mark.svg"/>
        </div>
        <div>
          <span class="block font-bold text-lg text-slate-800 tracking-tight leading-tight">Workspace Admin</span>
          <span class="block text-xs font-medium text-slate-500">Operations Console</span>
        </div>
      </div>

      <nav class="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
        <router-link to="/admin" exact-active-class="bg-primary/10 text-primary border-primary/10 shadow-sm" class="group flex items-center gap-3 px-4 py-3 text-slate-500 hover:bg-slate-50 hover:text-slate-800 rounded-xl border border-transparent transition-all duration-200">
          <span class="material-symbols-outlined group-hover:text-primary transition-colors" :class="{ 'fill-1': $route.path === '/admin' }">dashboard</span>
          <span class="font-medium">{{ t('admin.menu.dashboard') }}</span>
        </router-link>
        
        <router-link to="/admin/assets" active-class="bg-primary/10 text-primary border-primary/10 shadow-sm" class="group flex items-center gap-3 px-4 py-3 text-slate-500 hover:bg-slate-50 hover:text-slate-800 rounded-xl border border-transparent transition-all duration-200">
          <span class="material-symbols-outlined group-hover:text-primary transition-colors">image</span>
          <span class="font-medium">{{ t('menu.pictureManage') }}</span>
        </router-link>
        
        <router-link to="/admin/user" active-class="bg-primary/10 text-primary border-primary/10 shadow-sm" class="group flex items-center gap-3 px-4 py-3 text-slate-500 hover:bg-slate-50 hover:text-slate-800 rounded-xl border border-transparent transition-all duration-200">
          <span class="material-symbols-outlined group-hover:text-primary transition-colors">group</span>
          <span class="font-medium">{{ t('menu.userManage') }}</span>
        </router-link>
        
        <router-link to="/admin/analytics" active-class="bg-primary/10 text-primary border-primary/10 shadow-sm" class="group flex items-center gap-3 px-4 py-3 text-slate-500 hover:bg-slate-50 hover:text-slate-800 rounded-xl border border-transparent transition-all duration-200">
          <span class="material-symbols-outlined group-hover:text-primary transition-colors">bar_chart</span>
          <span class="font-medium">{{ t('admin.menu.analytics') }}</span>
        </router-link>
 
        <router-link to="/admin/workspace-analytics" active-class="bg-primary/10 text-primary border-primary/10 shadow-sm" class="group flex items-center gap-3 px-4 py-3 text-slate-500 hover:bg-slate-50 hover:text-slate-800 rounded-xl border border-transparent transition-all duration-200">
          <span class="material-symbols-outlined group-hover:text-primary transition-colors">database</span>
          <span class="font-medium">{{ t('admin.menu.spaceAnalyze') }}</span>
        </router-link>

        <router-link to="/admin/workspaces" active-class="bg-primary/10 text-primary border-primary/10 shadow-sm" class="group flex items-center gap-3 px-4 py-3 text-slate-500 hover:bg-slate-50 hover:text-slate-800 rounded-xl border border-transparent transition-all duration-200">
          <span class="material-symbols-outlined group-hover:text-primary transition-colors">group_work</span>
          <span class="font-medium">{{ t('admin.menu.teamSpace') }}</span>
        </router-link>
 
        <router-link to="/admin/recycle" active-class="bg-primary/10 text-primary border-primary/10 shadow-sm" class="group flex items-center gap-3 px-4 py-3 text-slate-500 hover:bg-slate-50 hover:text-slate-800 rounded-xl border border-transparent transition-all duration-200">
          <span class="material-symbols-outlined group-hover:text-primary transition-colors">delete</span>
          <span class="font-medium">{{ t('admin.menu.recycle') }}</span>
        </router-link>

        <router-link to="/admin/logs" active-class="bg-primary/10 text-primary border-primary/10 shadow-sm" class="group flex items-center gap-3 px-4 py-3 text-slate-500 hover:bg-slate-50 hover:text-slate-800 rounded-xl border border-transparent transition-all duration-200">
          <span class="material-symbols-outlined group-hover:text-primary transition-colors">fact_check</span>
          <span class="font-medium">{{ t('admin.menu.logs') }}</span>
        </router-link>
      </nav>

      <div class="p-4 border-t border-white/30">

        <div class="flex items-center gap-3 p-3 rounded-2xl bg-slate-50 border border-slate-200 shadow-sm mt-2">
            <div class="w-10 h-10 rounded-full bg-slate-200 p-[1px]">
                <div class="w-full h-full rounded-full bg-white flex items-center justify-center overflow-hidden">
                    <img v-if="loginUser.userAvatar || adminAvatarFallback" :src="adminAvatarUrl" @error="handleAdminAvatarError" class="w-full h-full object-cover" />
                    <span v-else class="font-bold text-xs text-slate-700">{{ loginUser.userName?.substring(0, 2).toUpperCase() || 'AD' }}</span>
                </div>
            </div>
            <div class="flex-1 overflow-hidden">
                <p class="text-sm font-bold text-slate-800 truncate">{{ loginUser.userName || 'Admin' }}</p>
                <p class="text-xs text-slate-500 truncate">{{ loginUser.userAccount || 'admin@asset-workspace.local' }}</p>
            </div>
            <button class="text-slate-400 hover:text-primary transition-colors" @click="handleLogout">
                <span class="material-symbols-outlined text-[20px]">logout</span>
            </button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col h-full overflow-hidden relative z-10">
        <!-- Header -->
        <header class="h-20 flex items-center justify-between px-8 py-4 shrink-0">
            <div>
                <h1 class="text-2xl font-bold text-slate-800 tracking-tight">{{ pageTitle }}</h1>
                <p class="text-slate-500 text-sm">{{ pageSubtitle }}</p>
            </div>
            <div class="flex items-center gap-4">
                <button class="w-10 h-10 rounded-full bg-white hover:bg-slate-50 border border-slate-200 flex items-center justify-center text-slate-600 transition-all relative shadow-sm" @click="toggleLanguage" title="Switch Language">
                    <span class="material-symbols-outlined">language</span>
                    <span class="absolute -top-1 -right-1 bg-primary text-white text-[9px] px-1 rounded-full font-bold">{{ currentLang.toUpperCase() }}</span>
                </button>
                <router-link to="/" class="w-10 h-10 rounded-full bg-white hover:bg-slate-50 border border-slate-200 flex items-center justify-center text-slate-600 transition-all relative shadow-sm" title="Back to Site">
                     <span class="material-symbols-outlined">home</span>
                </router-link>
            </div>
        </header>

        <!-- Content Area -->
        <div class="flex-1 overflow-y-auto p-8 pt-2 pb-12 space-y-6">
            <router-view />
        </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'
import { userLogoutUsingPost } from '@/api/user/userController'
import { message } from 'ant-design-vue'
import defaultAvatar from '@/assets/avatar.png'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const loginUser = computed(() => loginUserStore.loginUser)
const adminAvatarFallback = ref(false)
const adminAvatarRefreshTried = ref(false)
const adminAvatarVersion = ref(0)
const adminAvatarUrl = computed(() => {
    const baseUrl = adminAvatarFallback.value
        ? defaultAvatar
        : (loginUser.value.userAvatar || defaultAvatar)
    if (baseUrl === defaultAvatar) {
        return baseUrl
    }
    const separator = baseUrl.includes('?') ? '&' : '?'
    return `${baseUrl}${separator}v=${adminAvatarVersion.value}`
})

const languageStore = useLanguageStore()
const { t, currentLang } = storeToRefs(languageStore)
const { toggleLanguage } = languageStore

const pageTitle = computed(() => {
    switch(route.path) {
        case '/admin/assets': return t.value('menu.pictureManage')
        case '/admin/user': return t.value('menu.userManage')
        case '/admin/analytics': return t.value('admin.menu.analytics')
        case '/admin/workspace-analytics': return t.value('admin.menu.spaceAnalyze')
        case '/admin/workspaces': return t.value('admin.menu.teamSpace')
        case '/admin/recycle': return t.value('admin.menu.recycle')
        case '/admin/logs': return t.value('admin.menu.logs')
        default: return t.value('admin.menu.dashboard')
    }
})

const pageSubtitle = computed(() => {
    switch(route.path) {
        case '/admin/assets': return t.value('admin.subtitles.picture')
        case '/admin/user': return t.value('admin.subtitles.user')
        case '/admin/analytics': return t.value('admin.subtitles.analytics')
        case '/admin/workspace-analytics': return t.value('admin.subtitles.spaceAnalyze')
        case '/admin/workspaces': return t.value('admin.subtitles.teamSpace')
        case '/admin/recycle': return t.value('admin.subtitles.recycle')
        case '/admin/logs': return t.value('admin.subtitles.logs')
        default: return 'Workspace operations overview.'
    }
})

const handleAdminAvatarError = async () => {
    if (adminAvatarFallback.value) {
        return
    }
    if (!loginUser.value.id) {
        adminAvatarFallback.value = true
        return
    }
    if (!adminAvatarRefreshTried.value) {
        const previousAvatar = loginUser.value.userAvatar
        adminAvatarRefreshTried.value = true
        try {
            await loginUserStore.fetchLoginUser()
        } catch (e) {
            // ignore
        }
        const refreshedAvatar = loginUser.value.userAvatar
        if (!refreshedAvatar || refreshedAvatar === previousAvatar) {
            adminAvatarFallback.value = true
            return
        }
        adminAvatarVersion.value += 1
    } else {
        adminAvatarFallback.value = true
    }
}

watch(
    () => loginUser.value.userAvatar,
    () => {
        adminAvatarFallback.value = false
        adminAvatarRefreshTried.value = false
        adminAvatarVersion.value += 1
    }
)

const handleLogout = async () => {
    await userLogoutUsingPost()
    loginUserStore.logout()
    router.push('/user/login')
    message.success('Logged out successfully')
}
</script>

<style scoped>
/* Ensure Material Symbols are available */
@import url("https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap");

/* Custom Scrollbar for Admin Panel */
::-webkit-scrollbar {
    width: 6px;
    height: 6px;
}
::-webkit-scrollbar-track {
    background: transparent;
}
::-webkit-scrollbar-thumb {
    background: rgba(203, 213, 225, 0.4);
    border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
    background: rgba(148, 163, 184, 0.6);
}

.fill-1 {
  font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}
</style>
