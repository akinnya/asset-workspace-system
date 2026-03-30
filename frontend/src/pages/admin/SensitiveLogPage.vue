<template>
  <div>
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-sky-50 text-sky-500 flex items-center justify-center">
          <span class="material-symbols-outlined">fact_check</span>
        </div>
        <div>
          <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.logs.totalLogs') }}</p>
          <p class="text-2xl font-bold text-slate-800">{{ total }}</p>
        </div>
      </div>
      <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-500 flex items-center justify-center">
          <span class="material-symbols-outlined">verified</span>
        </div>
        <div>
          <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.logs.pageSuccess') }}</p>
          <p class="text-2xl font-bold text-slate-800">{{ pageSuccessCount }}</p>
        </div>
      </div>
      <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-rose-50 text-rose-500 flex items-center justify-center">
          <span class="material-symbols-outlined">warning</span>
        </div>
        <div>
          <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.logs.pageFailed') }}</p>
          <p class="text-2xl font-bold text-slate-800">{{ pageFailCount }}</p>
        </div>
      </div>
      <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-amber-50 text-amber-500 flex items-center justify-center">
          <span class="material-symbols-outlined">timer</span>
        </div>
        <div>
          <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.logs.avgDuration') }}</p>
          <p class="text-2xl font-bold text-slate-800">{{ averageDurationLabel }}</p>
        </div>
      </div>
    </div>

    <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion min-h-[360px] sm:min-h-[500px] flex flex-col">
      <div class="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-4 mb-6">
        <div class="flex flex-wrap items-center gap-3 w-full lg:w-auto">
          <div class="relative w-full sm:w-72">
            <input
              v-model="searchText"
              @keydown.enter="fetchLogs"
              type="text"
              :placeholder="t('admin.logs.searchPlaceholder')"
              class="bg-white/50 border border-white/60 rounded-xl py-2 pl-3 pr-10 text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary/20 w-full transition-all"
            />
            <button class="absolute right-2 top-2 text-slate-400 hover:text-primary" @click="fetchLogs">
              <span class="material-symbols-outlined text-[20px]">search</span>
            </button>
          </div>
          <a-select v-model:value="statusFilter" class="w-full sm:w-40" @change="handleFilterChange">
            <a-select-option value="">{{ t('admin.logs.allStatus') }}</a-select-option>
            <a-select-option value="SUCCESS">{{ t('admin.logs.success') }}</a-select-option>
            <a-select-option value="FAIL">{{ t('admin.logs.fail') }}</a-select-option>
          </a-select>
        </div>
        <button
          class="px-4 py-2 rounded-xl text-sm font-semibold bg-white/70 text-primary shadow-sm border border-primary/10 hover:bg-white transition-colors"
          @click="fetchLogs"
        >
          {{ t('admin.logs.refresh') }}
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="text-slate-400 text-xs uppercase tracking-wider border-b border-slate-200/50">
              <th class="pb-4 pl-4 font-semibold min-w-[160px]">{{ t('admin.logs.time') }}</th>
              <th class="pb-4 font-semibold min-w-[140px]">{{ t('admin.logs.operator') }}</th>
              <th class="pb-4 font-semibold min-w-[160px]">{{ t('admin.logs.action') }}</th>
              <th class="pb-4 font-semibold min-w-[220px]">{{ t('admin.logs.path') }}</th>
              <th class="pb-4 font-semibold min-w-[100px]">{{ t('admin.logs.status') }}</th>
              <th class="pb-4 font-semibold min-w-[100px]">{{ t('admin.logs.duration') }}</th>
              <th class="pb-4 pr-4 text-right font-semibold min-w-[100px]">{{ t('admin.logs.details') }}</th>
            </tr>
          </thead>
          <tbody class="text-sm">
            <tr v-if="loading" class="text-center">
              <td colspan="7" class="py-10 text-slate-400">{{ t('admin.logs.loading') }}</td>
            </tr>
            <tr v-else-if="logs.length === 0" class="text-center">
              <td colspan="7" class="py-10 text-slate-400">{{ t('admin.logs.empty') }}</td>
            </tr>
            <tr v-else v-for="logItem in logs" :key="logItem.id" class="group border-b border-slate-100/50 hover:bg-white/40 transition-colors">
              <td class="py-4 pl-4 align-middle text-slate-600">{{ formatDate(logItem.createTime) }}</td>
              <td class="py-4 align-middle">
                <div>
                  <p class="font-bold text-slate-800 text-sm">{{ formatOperator(logItem) }}</p>
                  <p class="text-xs text-slate-500">{{ logItem.ip || '-' }}</p>
                </div>
              </td>
              <td class="py-4 align-middle">
                <div>
                  <p class="font-semibold text-slate-800">{{ logItem.description || '-' }}</p>
                  <p class="text-xs text-slate-500 truncate max-w-[200px]">{{ logItem.methodName || '-' }}</p>
                </div>
              </td>
              <td class="py-4 align-middle text-slate-600">
                <div class="max-w-[260px]">
                  <p class="truncate">{{ logItem.requestPath || '-' }}</p>
                  <p class="text-xs text-slate-400 mt-1">{{ logItem.httpMethod || '-' }}</p>
                </div>
              </td>
              <td class="py-4 align-middle">
                <span :class="statusClass(logItem.status)" class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border">
                  <span :class="statusDotClass(logItem.status)" class="w-1.5 h-1.5 rounded-full"></span>
                  {{ statusLabel(logItem.status) }}
                </span>
              </td>
              <td class="py-4 align-middle text-slate-600">{{ durationLabel(logItem.durationMs) }}</td>
              <td class="py-4 pr-4 text-right align-middle">
                <button
                  class="w-8 h-8 rounded-lg bg-white/60 text-slate-500 hover:bg-primary hover:text-white border border-slate-200/60 transition-all flex items-center justify-center shadow-sm ml-auto"
                  :title="t('admin.logs.viewDetails')"
                  @click="openDetail(logItem)"
                >
                  <span class="material-symbols-outlined text-[18px]">visibility</span>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="flex items-center justify-between mt-6 px-2">
        <p class="text-sm text-slate-500">
          {{ t('admin.logs.pageSummary', { start: logs.length > 0 ? (current - 1) * pageSize + 1 : 0, end: Math.min(current * pageSize, total), total }) }}
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

    <a-modal
      v-model:visible="detailVisible"
      :title="t('admin.logs.detailTitle')"
      :footer="null"
      width="720px"
      class="glass-modal"
    >
      <div v-if="currentDetail" class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="rounded-2xl bg-slate-50/80 border border-slate-100 p-4">
            <p class="text-xs text-slate-400 mb-1">{{ t('admin.logs.operator') }}</p>
            <p class="font-semibold text-slate-800">{{ formatOperator(currentDetail) }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50/80 border border-slate-100 p-4">
            <p class="text-xs text-slate-400 mb-1">{{ t('admin.logs.status') }}</p>
            <p class="font-semibold text-slate-800">{{ statusLabel(currentDetail.status) }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50/80 border border-slate-100 p-4">
            <p class="text-xs text-slate-400 mb-1">{{ t('admin.logs.path') }}</p>
            <p class="font-semibold text-slate-800 break-all">{{ currentDetail.requestPath || '-' }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50/80 border border-slate-100 p-4">
            <p class="text-xs text-slate-400 mb-1">{{ t('admin.logs.duration') }}</p>
            <p class="font-semibold text-slate-800">{{ durationLabel(currentDetail.durationMs) }}</p>
          </div>
        </div>

        <div class="rounded-2xl bg-slate-50/80 border border-slate-100 p-4">
          <p class="text-xs text-slate-400 mb-2">{{ t('admin.logs.requestParams') }}</p>
          <pre class="text-sm text-slate-700 whitespace-pre-wrap break-all font-mono leading-6">{{ currentDetail.requestParams || '[]' }}</pre>
        </div>

        <div class="rounded-2xl bg-slate-50/80 border border-slate-100 p-4">
          <p class="text-xs text-slate-400 mb-2">{{ t('admin.logs.errorMessage') }}</p>
          <pre class="text-sm text-slate-700 whitespace-pre-wrap break-all font-mono leading-6">{{ currentDetail.errorMessage || t('admin.logs.noError') }}</pre>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listSensitiveOperationLogsUsingPost } from '@/api/admin/adminController'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const logs = ref<API.SensitiveOperationLog[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const searchText = ref('')
const statusFilter = ref('')
const detailVisible = ref(false)
const currentDetail = ref<API.SensitiveOperationLog>()

const pageSuccessCount = computed(() => logs.value.filter(item => item.status === 'SUCCESS').length)
const pageFailCount = computed(() => logs.value.filter(item => item.status === 'FAIL').length)
const averageDuration = computed(() => {
  if (logs.value.length === 0) {
    return 0
  }
  const totalDuration = logs.value.reduce((sum, item) => sum + Number(item.durationMs || 0), 0)
  return Math.round(totalDuration / logs.value.length)
})
const averageDurationLabel = computed(() => durationLabel(averageDuration.value))

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await listSensitiveOperationLogsUsingPost({
      current: current.value,
      pageSize: pageSize.value,
      searchText: searchText.value,
      status: statusFilter.value,
      sortField: 'createTime',
      sortOrder: 'descend',
    })
    if (res.data.code === 0 && res.data.data) {
      logs.value = res.data.data.records || []
      total.value = Number(res.data.data.total) || 0
    } else {
      message.error(`${t.value('admin.logs.loadFailed')}: ${res.data.message}`)
    }
  } catch (e) {
    message.error(t.value('common.requestFailed'))
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  current.value = 1
  fetchLogs()
}

const handlePageChange = (page: number) => {
  current.value = page
  fetchLogs()
}

const openDetail = (logItem: API.SensitiveOperationLog) => {
  currentDetail.value = logItem
  detailVisible.value = true
}

const formatDate = (value?: string) => {
  if (!value) {
    return '-'
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

const formatOperator = (logItem: API.SensitiveOperationLog) => {
  if (logItem.userName) {
    return logItem.userId ? `${logItem.userName} (#${logItem.userId})` : logItem.userName
  }
  if (logItem.userId) {
    return `#${logItem.userId}`
  }
  return t.value('admin.logs.anonymous')
}

const statusLabel = (status?: string) => {
  return status === 'FAIL' ? t.value('admin.logs.fail') : t.value('admin.logs.success')
}

const statusClass = (status?: string) => {
  return status === 'FAIL'
    ? 'bg-rose-50 text-rose-600 border-rose-100'
    : 'bg-emerald-50 text-emerald-600 border-emerald-100'
}

const statusDotClass = (status?: string) => {
  return status === 'FAIL' ? 'bg-rose-500' : 'bg-emerald-500'
}

const durationLabel = (duration?: number) => {
  return t.value('admin.logs.milliseconds', { count: Number(duration || 0) })
}

onMounted(() => {
  fetchLogs()
})
</script>
