<template>
  <div class="relative w-full max-w-7xl mx-auto p-4 md:p-8">
    <!-- Profile Header -->
    <div v-if="!profileLoading" class="glass-panel rounded-[2rem] p-6 md:p-10 shadow-soft-diffusion mb-10 relative overflow-hidden">
        <div class="absolute left-6 top-6 w-24 h-24 sm:w-28 sm:h-28 lg:w-32 lg:h-32 bg-primary/20 blur-[50px] rounded-full pointer-events-none"></div>
        <div class="flex flex-col lg:flex-row gap-8 lg:gap-12 items-start relative z-10">
            <!-- Avatar Section -->
            <div class="relative shrink-0 mx-auto lg:mx-0">
                <div class="w-32 h-32 md:w-40 md:h-40 rounded-full p-1 bg-white shadow-glow">
                    <img :src="userInfo.userAvatar || defaultAvatar" v-img-fallback="defaultAvatar" class="w-full h-full object-cover rounded-full bg-primary/5" />
                </div>
                <button v-if="isOwner" class="absolute bottom-1 right-1 bg-white text-slate-600 p-2.5 rounded-full shadow-md border border-slate-100 hover:text-primary hover:scale-110 transition-all z-20" @click="router.push('/user/settings')">
                    <span class="material-symbols-outlined text-[20px]">edit</span>
                </button>
            </div>

            <!-- Info Section -->
            <div class="flex-1 w-full flex flex-col gap-5">
                <div class="flex flex-col md:flex-row justify-between items-center md:items-start gap-4">
                    <div class="text-center md:text-left">
                        <div class="flex items-center gap-3 justify-center md:justify-start">
                            <h1 class="font-display font-bold text-3xl md:text-4xl text-slate-800">{{ userInfo.userName }}</h1>
                        </div>
                        <span class="text-slate-500 font-medium block mt-1">@{{ userInfo.userAccount || t('common.unknown') }}</span>
                    </div>
                    <!-- Badges -->
                    <div class="flex flex-wrap justify-center md:justify-end gap-2">
                        <span v-if="userInfo.userRole === 'admin'" class="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-gradient-to-r from-primary to-emerald-400 text-white text-xs font-bold shadow-sm shadow-primary/30">
                            <span class="material-symbols-outlined text-[14px]">diamond</span> {{ t('common.admin') }}
                        </span>
                        <span class="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-white/60 border border-white text-slate-600 text-xs font-semibold">
                             {{ t('common.member') }}
                        </span>
                    </div>
                </div>

                 <!-- Storage Usage (Owner Only) -->
                 <div v-if="isOwner" class="bg-white/40 backdrop-blur-sm rounded-xl p-3 px-4 border border-white/50 shadow-sm mt-1">
                    <div class="flex justify-between items-end mb-2">
                        <div class="flex items-center gap-2">
                           <span class="material-symbols-outlined text-primary text-[20px]">database</span>
                           <span class="text-xs font-bold text-slate-600 uppercase tracking-wide">{{ t('userProfile.storage') }}</span>
                        </div>
                        <div class="text-xs font-medium text-slate-500">
                           <span class="text-slate-800 font-bold">{{ formatSize(userStats.storageUsed) }}</span> 
                           <span class="text-slate-400 mx-1">/</span>
                           {{ formatSize(userStats.storageTotal) }}
                        </div>
                    </div>
                    <div class="relative w-full h-3 bg-slate-200/50 rounded-full overflow-hidden">
                         <div 
                            class="absolute top-0 left-0 h-full bg-gradient-to-r from-primary to-emerald-300 rounded-full shadow-[0_0_12px_rgba(15,118,110,0.35)] transition-all duration-1000 ease-out"
                            :style="{ width: `${storagePercent}%` }"
                        >
                             <div class="absolute inset-0 bg-white/20 w-full h-full"></div>
                         </div>
                    </div>
                 </div>

                <p class="text-slate-600 leading-relaxed text-center md:text-left">
                     {{ userInfo.userProfile || t('detail.artistBioPlaceholder') }}
                </p>

                <!-- Stats -->
                <div class="flex flex-col md:flex-row items-center justify-between gap-6 pt-2">
                    <div class="flex items-center gap-8 md:gap-12 w-full md:w-auto justify-center md:justify-start">
                          <div class="text-center md:text-left">
                            <span class="block font-display font-bold text-2xl text-slate-800">{{ userStats.pictureCount || 0 }}</span>
                            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">{{ t('userProfile.tabs.posts') }}</span>
                         </div>
                         <div class="text-center md:text-left">
                            <span class="block font-display font-bold text-2xl text-slate-800">{{ userStats.likeCount || 0 }}</span>
                            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">{{ t('common.likes') }}</span>
                         </div>
                    </div>
                    
                    <!-- Check-in Button -->
                    <div v-if="isOwner" class="shrink-0">
                        <button 
                            @click="handleCheckIn" 
                            :disabled="isCheckedIn || checkingIn"
                            :class="`group flex items-center gap-3 px-5 py-2.5 rounded-2xl transition-all duration-500 ${isCheckedIn ? 'bg-emerald-50 text-emerald-500 cursor-default opacity-80' : 'bg-primary text-white shadow-glow hover:scale-105 active:scale-95'}`"
                        >
                            <img :src="logo" class="w-8 h-8 rounded-full bg-white/80 p-1" />
                            <div class="flex flex-col items-start leading-tight">
                                <span class="text-[10px] font-black uppercase tracking-widest">{{ t('userProfile.checkIn.title') }}</span>
                                <span class="text-xs font-semibold">{{ t('userProfile.checkIn.subtitle') }}</span>
                            </div>
                            <span class="ml-2 text-[10px] font-black uppercase tracking-widest bg-white/20 px-2 py-1 rounded-full">
                                {{ checkInStatus }}
                            </span>
                        </button>
                    </div>

                </div>
            </div>
        </div>
    </div>

    <div v-else class="glass-panel rounded-[2rem] p-6 md:p-10 shadow-soft-diffusion mb-10 relative overflow-hidden animate-pulse">
        <div class="flex flex-col lg:flex-row gap-8 lg:gap-12 items-start">
            <div class="w-32 h-32 md:w-40 md:h-40 rounded-full bg-slate-200/70"></div>
            <div class="flex-1 space-y-4 w-full">
                <div class="h-8 w-1/3 bg-slate-200/70 rounded-lg"></div>
                <div class="h-4 w-1/4 bg-slate-200/70 rounded-lg"></div>
                <div class="h-20 w-full bg-slate-200/70 rounded-xl"></div>
            </div>
        </div>
    </div>

    <!-- Content Tabs -->
    <div class="flex items-center justify-between mb-8 overflow-x-auto pb-2 scrollbar-hide">
        <div class="flex items-center gap-1">
            <button v-for="tab in tabs" :key="tab.key" 
                :class="`px-6 py-2.5 rounded-full transition-all flex items-center gap-2 whitespace-nowrap ${currentTab === tab.key ? 'bg-primary text-white font-semibold shadow-glow shadow-primary/30' : 'bg-white/40 hover:bg-white/80 text-slate-600 font-medium border border-transparent hover:border-white/50'}`"
                @click="changeTab(tab.key)">
                <span class="material-symbols-outlined text-[20px]">{{ tab.icon }}</span> {{ tab.label }}
            </button>
        </div>
        <div class="flex items-center gap-2">
            <button v-if="isOwner" class="hidden md:flex items-center gap-2 px-4 py-2 rounded-full bg-white/60 hover:bg-white text-slate-600 hover:text-primary font-medium transition-all shadow-sm border border-transparent hover:border-slate-200" @click="router.push('/asset/upload')">
                  <span class="material-symbols-outlined text-[20px]">add_photo_alternate</span>
                 <span>{{ t('user.upload') }}</span>
            </button>
        </div>
    </div>

    <!-- Gallery Grid with Skeleton Support -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 min-h-[400px]">
        <!-- New Upload Button (Only in OCs/Posts tab) -->
        <div v-if="!loadingData && isOwner && currentTab !== 'likes'" class="group cursor-pointer border-2 border-dashed border-primary/30 hover:border-primary/60 rounded-[1.5rem] flex flex-col items-center justify-center min-h-[200px] sm:min-h-[340px] bg-white/20 hover:bg-white/40 transition-all duration-300" @click="router.push('/asset/upload')">
             <div class="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                <span class="material-symbols-outlined text-3xl text-primary">add</span>
             </div>
              <span class="font-bold text-slate-600">{{ t('user.upload') }}</span>
        </div>

        <!-- Skeletons -->
        <template v-if="loadingData">
            <div v-for="i in 8" :key="i" class="glass-card rounded-[1.5rem] p-3 animate-pulse h-[340px] flex flex-col">
                <div class="w-full aspect-[3/4] bg-slate-100 rounded-2xl mb-3"></div>
                <div class="space-y-3 px-1">
                    <div class="h-4 bg-slate-100 rounded w-3/4"></div>
                    <div class="h-3 bg-slate-100 rounded w-full"></div>
                    <div class="h-3 bg-slate-100 rounded w-1/2"></div>
                </div>
            </div>
        </template>

        <!-- Cards -->
        <template v-else>
             <div v-for="item in dataList" :key="item.id" class="glass-card rounded-[1.5rem] p-3 flex flex-col h-full shadow-soft-diffusion cursor-pointer" @click="router.push(`/asset/${item.idStr || item.id}`)">
                <div class="relative aspect-[3/4] rounded-2xl overflow-hidden mb-3 bg-slate-100 group">
                    <img class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" :src="item.thumbnailUrl || item.url" v-img-fallback="item.url" loading="lazy" />
                     <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-end p-4">
                        <button class="w-full py-2 bg-white/20 backdrop-blur-md rounded-lg text-white text-sm font-semibold border border-white/40 hover:bg-white/30">{{ t('common.viewDetails') }}</button>
                    </div>
                </div>
                <div class="px-1 pb-1">
                    <div class="flex justify-between items-start mb-1">
                        <h3 class="font-display font-bold text-slate-800 text-lg truncate pr-2">{{ item.name }}</h3>
                        <div class="flex gap-1">
                             <span v-if="item.isLiked" class="material-symbols-outlined text-[20px] text-red-500 fill-current">favorite</span>
                             <span v-else class="material-symbols-outlined text-[20px] text-slate-400">favorite</span>
                        </div>
                    </div>
                     <p class="text-xs text-slate-500 mb-3 line-clamp-2">{{ item.introduction || t('detail.artistBioPlaceholder') }}</p>
                     <div class="flex gap-2 flex-wrap">
                        <span v-for="tag in item.tags" :key="tag" class="px-2 py-0.5 rounded-md bg-primary/10 text-primary text-[10px] font-bold uppercase tracking-wide">#{{ tag }}</span>
                     </div>
                </div>
             </div>
        </template>
         
        <div v-if="!loadingData && dataList.length === 0" class="col-span-full text-center py-20 text-slate-400">
             <span class="material-symbols-outlined text-6xl mb-4 text-slate-200">image_not_supported</span>
              <p>{{ t('common.empty') }}</p>
        </div>
    </div>
    
     <!-- Pagination -->
    <div style="text-align: center; margin-top: 32px" v-if="total > searchParams.pageSize">
        <a-pagination 
            v-model:current="searchParams.current" 
            :total="total" 
            :pageSize="searchParams.pageSize" 
            @change="fetchListData" 
            size="small"
        />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import defaultAvatar from '@/assets/avatar.png'
import logo from '@/assets/brand-mark.svg'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { getUserVoByIdUsingGet, getUserStatsUsingGet } from '@/api/user/userController'
import { listPictureVoByPageUsingPost } from '@/api/asset/assetController'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const userInfo = ref<API.UserVO>({})
const userStats = ref<API.UserStatsVO>({})
const currentTab = ref('posts')
const dataList = ref<API.PictureVO[]>([])
const loadingData = ref(false)
const total = ref(0)
const targetUserId = ref('')
const profileLoading = ref(true)
const checkingIn = ref(false)
const isCheckedIn = ref(false)
const checkInStatus = computed(() => {
    if (checkingIn.value) return t.value('userProfile.checkIn.status.checking')
    if (isCheckedIn.value) return t.value('userProfile.checkIn.status.done')
    return t.value('userProfile.checkIn.status.ready')
})

const handleCheckIn = async () => {
    if (!isOwner.value) return
    if (isCheckedIn.value || checkingIn.value) return
    checkingIn.value = true
    setTimeout(() => {
        isCheckedIn.value = true
        checkingIn.value = false
        message.success(t.value('common.checkInSuccess'))
    }, 1200)
}

const tabs = computed(() => [
    { key: 'posts', label: t.value('userProfile.tabs.posts'), icon: 'grid_view' },
    { key: 'likes', label: t.value('userProfile.tabs.likes'), icon: 'favorite' }
])

const isOwner = computed(() => {
    const loginUserId = loginUserStore.loginUser?.idStr || (loginUserStore.loginUser?.id ? String(loginUserStore.loginUser.id) : '')
    const profileUserId = userInfo.value.idStr || (userInfo.value.id ? String(userInfo.value.id) : '')
    return !!loginUserId && loginUserId === profileUserId
})

const storagePercent = computed(() => {
    const used = userStats.value.storageUsed || 0
    const total = userStats.value.storageTotal || 1
    return Math.min(100, Math.max(0, (used / total) * 100))
})

const formatSize = (bytes?: number) => {
    if (!bytes || bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const searchParams = reactive({
    current: 1,
    pageSize: 12,
    sortField: 'createTime',
    sortOrder: 'descend'
})

const changeTab = (key: string) => {
    currentTab.value = key
    searchParams.current = 1
    fetchListData()
}

const getCurrentLoginUserId = () => {
    return loginUserStore.loginUser.idStr || (loginUserStore.loginUser.id ? String(loginUserStore.loginUser.id) : '')
}

const resolveTargetUserId = async () => {
    const routeId = route.params.id ? String(route.params.id) : ''
    if (routeId) {
        return routeId
    }
    let currentLoginUserId = getCurrentLoginUserId()
    if (currentLoginUserId) {
        return currentLoginUserId
    }
    await loginUserStore.fetchLoginUser()
    currentLoginUserId = getCurrentLoginUserId()
    return currentLoginUserId
}

const fetchStats = async () => {
    if (!targetUserId.value) return
    try {
        const res = await getUserStatsUsingGet({ userId: targetUserId.value as any })
        if (res.data.code === 0 && res.data.data) {
            userStats.value = res.data.data
        }
    } catch (e) {
        console.error(e)
    }
}

const fetchListData = async () => {
    if (!targetUserId.value) return
    loadingData.value = true
    try {
        const params: API.PictureQueryRequest = {
            ...searchParams,
            userId: targetUserId.value as any
        }
        
        if (currentTab.value === 'likes') {
             // 点赞列表需要使用当前登录用户态
             params.userId = undefined
             params.likedOnly = true
        }

        const res = await listPictureVoByPageUsingPost(params)
        if (res.data.code === 0 && res.data.data) {
            dataList.value = res.data.data.records || []
            total.value = res.data.data.total || 0
        }
    } catch (e) {
        console.error(e)
    } finally {
        loadingData.value = false
    }
}

const initData = async () => {
    const id = await resolveTargetUserId()
    if (!id) {
        userInfo.value = {}
        userStats.value = {}
        dataList.value = []
        total.value = 0
        profileLoading.value = false
        return
    }
    targetUserId.value = id
    profileLoading.value = true
    try {
        const userRes = await getUserVoByIdUsingGet({ id: id as any })
        if (userRes.data.code === 0 && userRes.data.data) {
            userInfo.value = userRes.data.data
        } else if (!route.params.id && loginUserStore.loginUser.id) {
            userInfo.value = { ...loginUserStore.loginUser }
        }
    } finally {
        profileLoading.value = false
    }
    
    // Get Stats
    fetchStats()
    
    // Get List
    fetchListData()
}

watch(() => route.params.id, () => {
   void initData()
})

watch(() => loginUserStore.loginUser.idStr, (nextId, previousId) => {
    if (!route.params.id && nextId && nextId !== previousId) {
        void initData()
    }
})

onMounted(() => {
    void initData()
})
</script>

<style scoped>
.glass-panel {
    background: rgba(255, 255, 255, 0.65);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.6);
}
.glass-card {
    background: rgba(255, 255, 255, 0.4);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.glass-card:hover {
    background: rgba(255, 255, 255, 0.7);
    transform: translateY(-4px);
    border-color: rgba(255, 255, 255, 0.8);
}
.animate-float {
    animation: float 3s ease-in-out infinite;
}
@keyframes float {
    0% { transform: translateY(0px); }
    50% { transform: translateY(-5px); }
    100% { transform: translateY(0px); }
}
</style>
