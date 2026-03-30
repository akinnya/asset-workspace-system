<template>
  <div>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-500 flex items-center justify-center">
          <span class="material-symbols-outlined">workspaces</span>
        </div>
        <div>
          <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.teamSpace.totalSpaces') }}</p>
          <p class="text-2xl font-bold text-slate-800">{{ total }}</p>
        </div>
      </div>
      <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-500 flex items-center justify-center">
          <span class="material-symbols-outlined">stacked_bar_chart</span>
        </div>
        <div>
          <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.teamSpace.pageSpaces') }}</p>
          <p class="text-2xl font-bold text-slate-800">{{ spaces.length }}</p>
        </div>
      </div>
    </div>

    <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion min-h-[360px] sm:min-h-[500px] flex flex-col">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
        <div class="flex items-center gap-3 w-full md:w-auto">
          <div class="relative w-full md:w-auto">
            <input
              v-model="searchText"
              @keydown.enter="doSearch"
              type="text"
              :placeholder="t('admin.teamSpace.searchPlaceholder')"
              class="bg-white/50 border border-white/60 rounded-xl py-2 pl-3 pr-10 text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary/20 w-full md:w-48 transition-all md:focus:w-64"
            />
            <button class="absolute right-2 top-2 text-slate-400 hover:text-primary" @click="doSearch">
              <span class="material-symbols-outlined text-[20px]">search</span>
            </button>
          </div>
          <select
            v-model="levelFilter"
            class="bg-white/50 border border-white/60 rounded-xl py-2 px-3 text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary/20"
            @change="doSearch"
          >
            <option :value="''">{{ t('admin.teamSpace.levelAll') }}</option>
            <option :value="0">{{ t('admin.teamSpace.levelCommon') }}</option>
            <option :value="1">{{ t('admin.teamSpace.levelPro') }}</option>
            <option :value="2">{{ t('admin.teamSpace.levelFlagship') }}</option>
          </select>
        </div>
        <button class="px-4 py-2 rounded-xl bg-white/60 border border-white/60 text-slate-600 hover:text-primary transition-colors" @click="fetchSpaces">
          {{ t('admin.teamSpace.refresh') }}
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="text-slate-400 text-xs uppercase tracking-wider border-b border-slate-200/50">
              <th class="pb-4 pl-4 font-semibold">{{ t('admin.teamSpace.name') }}</th>
              <th class="pb-4 font-semibold">{{ t('admin.teamSpace.owner') }}</th>
              <th class="pb-4 font-semibold">{{ t('admin.teamSpace.level') }}</th>
              <th class="pb-4 font-semibold">{{ t('admin.teamSpace.countUsage') }}</th>
              <th class="pb-4 font-semibold">{{ t('admin.teamSpace.sizeUsage') }}</th>
              <th class="pb-4 font-semibold">{{ t('admin.teamSpace.createdAt') }}</th>
              <th class="pb-4 pr-4 text-right font-semibold">{{ t('admin.teamSpace.actions') }}</th>
            </tr>
          </thead>
          <tbody class="text-sm">
            <tr v-if="loading" class="text-center py-10">
              <td colspan="7" class="py-10 text-slate-400">{{ t('admin.teamSpace.loading') }}</td>
            </tr>
            <tr v-else-if="spaces.length === 0" class="text-center py-10">
              <td colspan="7" class="py-10 text-slate-400">{{ t('admin.teamSpace.empty') }}</td>
            </tr>
            <tr v-else v-for="space in spaces" :key="space.id" class="group border-b border-slate-100/50 hover:bg-white/40 transition-colors">
              <td class="py-4 pl-4 align-middle">
                <div>
                  <p class="font-bold text-slate-800 text-sm">{{ space.spaceName || '-' }}</p>
                  <p class="text-xs text-slate-500">#{{ space.id }}</p>
                </div>
              </td>
              <td class="py-4 align-middle">
                <div>
                  <p class="font-semibold text-slate-700">{{ space.user?.userName || '-' }}</p>
                  <p class="text-xs text-slate-500">@{{ space.user?.userAccount || '-' }}</p>
                </div>
              </td>
              <td class="py-4 align-middle">{{ formatLevel(space.spaceLevel) }}</td>
              <td class="py-4 align-middle">{{ formatCount(space.totalCount, space.maxCount) }}</td>
              <td class="py-4 align-middle">{{ formatSize(space.totalSize) }} / {{ formatSize(space.maxSize) }}</td>
              <td class="py-4 align-middle text-slate-600">{{ formatDate(space.createTime) }}</td>
              <td class="py-4 pr-4 text-right align-middle">
                <div class="flex items-center justify-end gap-2">
                  <button
                    class="w-8 h-8 rounded-lg bg-white/60 text-rose-500 hover:bg-rose-500 hover:text-white border border-rose-200/60 transition-all flex items-center justify-center shadow-sm"
                    :title="t('admin.teamSpace.delete')"
                    @click="doDelete(space)"
                  >
                    <span class="material-symbols-outlined text-[18px]">delete</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="flex items-center justify-between mt-6 px-2">
        <p class="text-sm text-slate-500">
          {{ t('admin.teamSpace.pageSummary', { start: spaces.length > 0 ? (current - 1) * pageSize + 1 : 0, end: Math.min(current * pageSize, total), total }) }}
        </p>
        <div class="flex items-center gap-2">
          <button
            class="w-8 h-8 flex items-center justify-center rounded-lg bg-white/50 border border-white/60 text-slate-400 hover:text-primary transition-colors disabled:opacity-50"
            :disabled="current <= 1"
            @click="handlePageChange(current - 1)"
          >
            <span class="material-symbols-outlined text-sm">chevron_left</span>
          </button>
          <button class="w-8 h-8 flex items-center justify-center rounded-lg bg-primary text-white shadow-glow">{{ current }}</button>
          <button
            class="w-8 h-8 flex items-center justify-center rounded-lg bg-white/50 border border-white/60 text-slate-600 hover:text-primary transition-colors disabled:opacity-50"
            :disabled="current * pageSize >= total"
            @click="handlePageChange(current + 1)"
          >
            <span class="material-symbols-outlined text-sm">chevron_right</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listTeamSpaceVoByPageUsingPost, deleteSpaceUsingPost } from '@/api/workspace/workspaceController'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const spaces = ref<API.SpaceVO[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const searchText = ref('')
const levelFilter = ref<number | ''>('')

const fetchSpaces = async () => {
  loading.value = true
  try {
    const res = await listTeamSpaceVoByPageUsingPost({
      current: current.value,
      pageSize: pageSize.value,
      spaceName: searchText.value || undefined,
      spaceLevel: levelFilter.value === '' ? undefined : Number(levelFilter.value),
    })
    if (res.data.code === 0) {
      spaces.value = res.data.data?.records || []
      total.value = Number(res.data.data?.total || 0)
    } else {
      message.error(t.value('admin.teamSpace.requestFailed') + ': ' + res.data.message)
    }
  } catch (e) {
    message.error(t.value('admin.teamSpace.requestFailed'))
  } finally {
    loading.value = false
  }
}

const doSearch = () => {
  current.value = 1
  fetchSpaces()
}

const handlePageChange = (page: number) => {
  current.value = page
  fetchSpaces()
}

const doDelete = (space: API.SpaceVO) => {
  if (!space.id) return
  Modal.confirm({
    title: t.value('admin.teamSpace.deleteConfirmTitle'),
    content: t.value('admin.teamSpace.deleteConfirmContent', { name: space.spaceName || '-' }),
    okText: t.value('admin.teamSpace.deleteText'),
    okType: 'danger',
    onOk: async () => {
      const res = await deleteSpaceUsingPost({ id: space.id })
      if (res.data.code === 0) {
        message.success(t.value('admin.teamSpace.deleteSuccess'))
        fetchSpaces()
      } else {
        message.error(t.value('admin.teamSpace.deleteFailed'))
      }
    },
  })
}

const formatLevel = (level?: number) => {
  if (level === 0) return t.value('admin.teamSpace.levelCommon')
  if (level === 1) return t.value('admin.teamSpace.levelPro')
  if (level === 2) return t.value('admin.teamSpace.levelFlagship')
  return '-'
}

const formatSize = (bytes?: number) => {
  if (!bytes || bytes <= 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatCount = (totalCount?: number, maxCount?: number) => {
  const totalValue = totalCount ?? 0
  const maxValue = maxCount ?? 0
  return maxValue ? `${totalValue}/${maxValue}` : `${totalValue}/-`
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD')
}

onMounted(() => {
  fetchSpaces()
})
</script>
