<template>
  <div id="mySpacePage" class="min-h-screen bg-slate-50 relative pb-20 font-display">
    <!-- Hero / Welcome Section -->
    <div class="bg-white border-b border-slate-200 pt-8 sm:pt-10 pb-10 sm:pb-16 px-4 sm:px-6 md:px-12 relative overflow-hidden">
        <div class="absolute top-0 right-0 w-56 h-56 sm:w-72 sm:h-72 lg:w-96 lg:h-96 bg-primary/5 rounded-full blur-[100px] pointer-events-none"></div>
        <div class="max-w-7xl mx-auto relative z-10 flex flex-col md:flex-row justify-between items-end gap-6">
            <div>
                <h1 class="text-2xl sm:text-3xl lg:text-4xl font-bold text-slate-900 mb-3 tracking-tight">
                    {{ t('space.welcome', { name: loginUserStore.loginUser.userName }) }}
                </h1>
                <p class="text-slate-500 text-base sm:text-lg max-w-2xl">
                    {{ t('space.subtitle') }}
                </p>
            </div>
            <div class="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
                 <button 
                    class="w-full sm:w-auto px-6 py-3 rounded-2xl bg-slate-900 text-white font-bold shadow-lg hover:bg-slate-800 hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center justify-center gap-2 shrink-0" 
                    @click="doAddSpace"
                >
                    <PlusOutlined /> {{ t('space.createSpace') }}
                </button>
                <button 
                    class="w-full sm:w-auto px-6 py-3 rounded-2xl bg-white border border-slate-200 text-slate-700 font-bold shadow-sm hover:border-primary hover:text-primary transition-all flex items-center justify-center gap-2 shrink-0" 
                    @click="showJoinModal"
                >
                    <TeamOutlined /> {{ t('space.joinTeam') }}
                </button>
            </div>
        </div>
    </div>

    <!-- Storage Quota Summary Removed as requested -->
    <div class="mt-6"></div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 md:px-12 mt-8 relative z-20">
        
        <div class="flex items-center justify-between mb-8">
            <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2">
                <AppstoreOutlined class="text-primary" /> {{ t('space.mySpaces') }}
            </h2>
            <div class="flex items-center gap-2 bg-slate-100 p-1 rounded-xl">
                <button 
                    v-for="mode in ['all', 'owner', 'member']" 
                    :key="mode"
                    @click="filterMode = mode"
                    :class="filterMode === mode ? 'bg-white text-primary shadow-sm' : 'text-slate-500 hover:text-slate-700'"
                    class="px-3 py-1.5 rounded-lg text-xs font-bold transition-all"
                >
                    {{ t(`space.filter.${mode}`) || mode }}
                </button>
            </div>
        </div>

        <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
             <div v-for="i in 4" :key="i" class="h-72 rounded-[32px] bg-white border border-slate-100 shadow-sm animate-pulse"></div>
        </div>

        <div v-else-if="filteredSpaces.length === 0" class="flex flex-col items-center justify-center py-24 bg-white rounded-[40px] border border-dashed border-slate-200">
            <div class="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mb-6">
                <AppstoreOutlined class="text-3xl text-slate-300" />
            </div>
            <p class="text-slate-400 font-medium mb-6">{{ t('space.emptySpaces') }}</p>
            <button class="px-8 py-3 rounded-2xl bg-primary text-white font-bold shadow-glow hover:scale-105 transition-all" @click="doAddSpace">
                {{ t('space.createFirst') }}
            </button>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
            <div 
                v-for="item in filteredSpaces" 
                :key="item.id" 
                class="group relative bg-white rounded-[32px] border border-white/60 shadow-soft hover:shadow-2xl hover:-translate-y-2 transition-all duration-500 overflow-hidden flex flex-col h-80"
                :class="item.isLocked ? 'cursor-not-allowed opacity-95' : 'cursor-pointer'"
                @click="handleSpaceClick(item)"
            >
                <!-- Card Cover -->
                <div class="h-40 relative overflow-hidden">
                    <img 
                        v-if="item.coverUrl" 
                        :src="item.coverUrl" 
                        class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" 
                    />
                    <div v-else class="w-full h-full bg-gradient-to-br from-primary to-purple-500 opacity-80"></div>
                    
                    <!-- Identity Tag -->
                    <div class="absolute top-4 right-4 bg-white/90 backdrop-blur px-3 py-1.5 rounded-xl text-[10px] font-black uppercase tracking-widest border border-white/50 shadow-sm flex items-center gap-1.5">
                        <span :class="item.isOwner ? 'bg-primary' : (item.isMember ? 'bg-emerald-400' : 'bg-slate-400')" class="w-1.5 h-1.5 rounded-full animate-pulse"></span>
                        <span :class="item.isOwner ? 'text-primary' : (item.isMember ? 'text-emerald-500' : 'text-slate-500')">
                            {{ item.isOwner ? t('space.owner') : (item.isMember ? t('space.member') : t('space.unjoined')) }}
                        </span>
                    </div>

                    <button
                        v-if="item.isOwner"
                        class="absolute top-4 left-4 w-9 h-9 rounded-xl bg-white/90 backdrop-blur text-rose-500 flex items-center justify-center shadow-sm hover:bg-rose-500 hover:text-white transition-all"
                        @click.stop="confirmDeleteSpace(item)"
                    >
                        <DeleteOutlined />
                    </button>
                </div>

                <!-- Card Content -->
                <div class="px-6 py-5 flex-1 flex flex-col relative bg-white">
                    <div class="flex items-start justify-between mb-2">
                        <h3 class="text-lg font-black text-slate-800 line-clamp-1 group-hover:text-primary transition-colors">
                            {{ item.spaceName }}
                        </h3>
                    </div>
                    
                    <p class="text-xs text-slate-400 line-clamp-2 mb-4 h-8">
                        {{ item.introduction || t('space.noIntro') }}
                    </p>

                    <div class="mt-auto flex items-center justify-between pt-4 border-t border-slate-50">
                        <div class="flex -space-x-2">
                            <div class="w-7 h-7 rounded-full border-2 border-white bg-slate-100 flex items-center justify-center overflow-hidden">
                                <img v-if="item.user?.userAvatar" :src="item.user.userAvatar" class="w-full h-full object-cover" />
                                <span v-else class="text-[8px] font-bold text-slate-400">{{ item.user?.userName?.charAt(0) }}</span>
                            </div>
                        </div>
                        <div class="flex items-center gap-3">
                            <span class="text-[10px] font-bold text-slate-300 uppercase tracking-tighter">
                                {{ item.isLocked ? t('space.requestJoin') : t('space.enter') }}
                            </span>
                            <div class="w-8 h-8 rounded-xl bg-slate-50 flex items-center justify-center text-slate-400 transition-all duration-500"
                                 :class="item.isLocked ? '' : 'group-hover:bg-primary group-hover:text-white group-hover:rotate-45'">
                                <ArrowRightOutlined />
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="item.isLocked" class="absolute inset-0 bg-white/40 backdrop-blur-[2px] flex items-center justify-center">
                    <button class="px-4 py-2 rounded-xl bg-primary text-white font-bold shadow-glow" @click.stop="requestJoinSpace(item)">
                        {{ t('space.requestJoin') }}
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Join Space Modal -->
    <a-modal
      v-model:open="joinVisible"
      :title="t('space.joinModalTitle')"
      @ok="doJoinSpace"
      :confirmLoading="loading"
      :okText="t('space.joinNow')"
      :cancelText="t('common.cancel')"
      class="custom-modal"
    >
      <div class="py-4">
        <p class="text-slate-500 mb-4">{{ t('space.enterSpaceIdTip') }}</p>
        <a-input
          v-model:value="joinForm.spaceId"
          :placeholder="t('space.enterSpaceId')"
          size="large"
          class="!w-full !rounded-xl"
        >
          <template #prefix>
            <KeyOutlined class="text-slate-300" />
          </template>
        </a-input>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { deleteSpaceUsingPost, listSpaceVoByPageUsingPost } from '@/api/workspace/workspaceController'
import { listMySpaceUserUsingGet, joinSpaceUsingPost } from '@/api/workspace/workspaceUserController'
import { message, Modal } from 'ant-design-vue'
import { 
  PlusOutlined, AppstoreOutlined, TeamOutlined, 
  ArrowRightOutlined, CloudOutlined, KeyOutlined, DeleteOutlined
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'
import { formatSize } from '@/utils'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const spaceList = ref<API.SpaceVO[]>([])
const teamSpaceList = ref<API.SpaceUserVO[]>([])
const allSpaceList = ref<API.SpaceVO[]>([])
const loading = ref(false)
const total = ref(0)
const activeTab = ref('private')
const viewMode = ref('grid')

const totalUsage = ref(0)
const joinVisible = ref(false)
const joinForm = reactive<API.SpaceUserJoinRequest>({
    spaceId: ''
})
const maxQuota = ref(10 * 1024 * 1024 * 1024) // 10GB default
const usagePercent = computed(() => {
    if (maxQuota.value === 0) return 0
    return Math.min(100, (totalUsage.value / maxQuota.value) * 100)
})

const filterMode = ref('all')

const filteredSpaces = computed(() => {
    const ownerIds = new Set(spaceList.value.map(s => String(s.id)))
    const memberIds = new Set(teamSpaceList.value.map(item => item.space?.id).filter(Boolean).map(id => String(id)))
    const mergedMap = new Map<string, any>()

    allSpaceList.value.forEach((space) => {
        const id = String(space.id)
        mergedMap.set(id, {
            ...space,
            isOwner: ownerIds.has(id),
            isMember: memberIds.has(id),
            isLocked: !ownerIds.has(id) && !memberIds.has(id),
        })
    })

    spaceList.value.forEach((space) => {
        const id = String(space.id)
        if (!mergedMap.has(id)) {
            mergedMap.set(id, { ...space, isOwner: true, isMember: false, isLocked: false })
        }
    })

    teamSpaceList.value.forEach((item) => {
        const space = item.space || {}
        const id = String(space.id)
        if (!mergedMap.has(id)) {
            mergedMap.set(id, { ...space, isOwner: false, isMember: true, isLocked: false, memberRole: item.spaceRole })
        }
    })

    const combined = Array.from(mergedMap.values())
    combined.sort((a, b) => {
        return new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime()
    })

    if (filterMode.value === 'owner') return combined.filter(s => s.isOwner)
    if (filterMode.value === 'member') return combined.filter(s => s.isMember)
    return combined
})


const fetchData = async () => {
  loading.value = true
  try {
    const loginUser = loginUserStore.loginUser
    if (!loginUser.id) {
        return;
    }
    // 获取我创建的空间
    const spaceRes = await listSpaceVoByPageUsingPost({
      userId: loginUser.id,
      current: 1,
      pageSize: 100,
      sortField: 'createTime',
      sortOrder: 'descend'
    })
    if (spaceRes.data.code === 0 && spaceRes.data.data) {
      spaceList.value = spaceRes.data.data.records ?? []
      total.value = spaceRes.data.data.total ?? 0
      // Calculate total usage for display
      totalUsage.value = spaceList.value.reduce((acc, curr) => acc + (curr.totalSize || 0), 0)
    }

    // 获取我加入的空间
    const teamRes = await listMySpaceUserUsingGet()
    if (teamRes.data.code === 0) {
      teamSpaceList.value = teamRes.data.data ?? []
    }

    // 获取全站空间预览
    const allRes = await listSpaceVoByPageUsingPost({
      current: 1,
      pageSize: 20,
      sortField: 'createTime',
      sortOrder: 'descend'
    })
    if (allRes.data.code === 0 && allRes.data.data) {
      allSpaceList.value = allRes.data.data.records ?? []
    }
  } catch (e: any) {
    message.error(t.value('common.loadError') + ': ' + e.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!loginUserStore.loginUser.id) {
    message.warning(t.value('common.loginPrompt'))
    router.push('/user/login?redirect=' + route.fullPath)
    return
  }
  fetchData()
})

const doAddSpace = () => {
  router.push('/workspace/add')
}

const handleSpaceClick = (space: any) => {
    if (!space?.id) return
    if (space.isLocked) {
        requestJoinSpace(space)
        return
    }
    router.push(`/workspace/detail/${space.id}`)
}

const confirmDeleteSpace = (space: any) => {
    if (!space?.id) return
    Modal.confirm({
        title: t.value('space.detail.deleteConfirmTitle'),
        content: t.value('space.detail.deleteConfirmContent').replace('{name}', space.spaceName || ''),
        okText: t.value('space.detail.deleteText'),
        okType: 'danger',
        onOk: async () => {
            const res = await deleteSpaceUsingPost({ id: space.id })
            if (res.data.code === 0) {
                message.success(t.value('space.detail.deleteSuccess'))
                fetchData()
            } else {
                message.error(t.value('space.detail.deleteFailed'))
            }
        }
    })
}

const requestJoinSpace = (space: any) => {
    Modal.confirm({
        title: t.value('space.requestJoinTitle'),
        content: t.value('space.requestJoinContent', { name: space.spaceName || '' }),
        okText: t.value('space.requestJoin'),
        cancelText: t.value('common.cancel'),
        onOk: () => doJoinSpaceById(space.id)
    })
}

const normalizeSpaceId = (raw: string | number | undefined | null) => {
  if (raw === undefined || raw === null) return ''
  const text = String(raw).trim()
  if (!text) return ''
  const matches = text.match(/\d+/g)
  if (!matches || matches.length === 0) return ''
  return matches[matches.length - 1]
}

const doJoinSpaceById = async (spaceId: number | string) => {
  const normalizedId = normalizeSpaceId(spaceId)
  if (!normalizedId) {
    message.warning(t.value('space.enterSpaceIdRequired'))
    return
  }
  loading.value = true
  try {
    const res = await joinSpaceUsingPost({ spaceId: normalizedId })
    if (res.data.code === 0) {
      message.success(t.value('space.requestJoinSuccess'))
      fetchData()
    } else {
      message.error(t.value('space.requestJoinFailed') + ': ' + res.data.message)
    }
  } catch (e: any) {
    message.error(t.value('space.requestJoinFailed') + ': ' + e.message)
  } finally {
    loading.value = false
  }
}

const showJoinModal = () => {
    joinForm.spaceId = undefined
    joinVisible.value = true
}

const doJoinSpace = async () => {
  const normalizedId = normalizeSpaceId(joinForm.spaceId)
  if (!normalizedId) {
    message.warning(t.value('space.enterSpaceIdRequired'))
    return
  }
  loading.value = true
  try {
    const res = await joinSpaceUsingPost({ spaceId: normalizedId })
    if (res.data.code === 0) {
      message.success(t.value('space.requestJoinSuccess'))
      joinVisible.value = false
      fetchData()
    } else {
      message.error(t.value('space.requestJoinFailed') + ': ' + res.data.message)
    }
  } catch (e: any) {
    message.error(t.value('space.requestJoinFailed') + ': ' + e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.space-tabs :deep(.ant-tabs-nav::before) {
    border-bottom: 2px solid #f1f5f9;
}
.shadow-soft {
    box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.04), 0 5px 15px -5px rgba(0, 0, 0, 0.02);
}
.group:hover .shadow-soft {
    box-shadow: 0 25px 50px -12px rgba(109, 174, 239, 0.25);
}
</style>
