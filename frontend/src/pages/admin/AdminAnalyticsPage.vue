<template>
  <div class="space-y-6">
    <div v-if="loading" class="flex justify-center items-center py-20">
      <a-spin size="large" :tip="t('admin.analytics.loadingRealtime')" />
    </div>

    <template v-else>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="group bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion">
          <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-2 mb-2">
            <h3 class="text-slate-500 text-xs font-semibold uppercase tracking-wider">{{ t('admin.analytics.totalUsers') }}</h3>
            <span class="bg-indigo-100 text-indigo-600 px-2 py-0.5 rounded-full text-[10px] font-bold self-start sm:self-auto">
              {{ t('admin.analytics.todayLabel', { count: stats.todayNewUsers }) }}
            </span>
          </div>
          <div class="flex items-end justify-between">
            <p class="text-3xl font-bold text-slate-800 tracking-tight">{{ stats.totalUsers }}</p>
            <div class="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-500 flex items-center justify-center">
              <span class="material-symbols-outlined">group</span>
            </div>
          </div>
          <p class="mt-3 text-xs text-slate-400">
            {{ t('admin.analytics.last7Days') }} {{ stats.weekNewUsers || 0 }} · {{ t('admin.analytics.last30Days') }} {{ stats.monthNewUsers || 0 }}
          </p>
        </div>

        <div class="group bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion">
          <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-2 mb-2">
            <h3 class="text-slate-500 text-xs font-semibold uppercase tracking-wider">{{ t('admin.analytics.totalPictures') }}</h3>
            <span class="bg-emerald-100 text-emerald-600 px-2 py-0.5 rounded-full text-[10px] font-bold self-start sm:self-auto">
              {{ t('admin.analytics.todayLabel', { count: stats.todayNewPictures }) }}
            </span>
          </div>
          <div class="flex items-end justify-between">
            <p class="text-3xl font-bold text-slate-800 tracking-tight">{{ stats.totalPictures }}</p>
            <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-500 flex items-center justify-center">
              <span class="material-symbols-outlined">image</span>
            </div>
          </div>
          <p class="mt-3 text-xs text-slate-400">{{ t('admin.analytics.trendUploads') }}</p>
        </div>

        <div class="group bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion">
          <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-2 mb-2">
            <h3 class="text-slate-500 text-xs font-semibold uppercase tracking-wider">{{ t('admin.analytics.storageUsed') }}</h3>
            <span class="bg-slate-100 text-slate-600 px-2 py-0.5 rounded-full text-[10px] font-bold self-start sm:self-auto">
              {{ t('admin.analytics.cloudLabel') }}
            </span>
          </div>
          <div class="flex items-end justify-between">
            <p class="text-3xl font-bold text-slate-800 tracking-tight">{{ formatSize(stats.totalStorageUsed) }}</p>
            <div class="w-10 h-10 rounded-xl bg-slate-50 text-slate-400 flex items-center justify-center">
              <span class="material-symbols-outlined">cloud_done</span>
            </div>
          </div>
          <p class="mt-3 text-xs text-slate-400">{{ t('admin.analytics.fileTypeDistribution') }}</p>
        </div>

        <div class="group bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion">
          <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-2 mb-2">
            <h3 class="text-slate-500 text-xs font-semibold uppercase tracking-wider">{{ t('admin.analytics.totalLikes') }}</h3>
            <span class="bg-rose-100 text-rose-600 px-2 py-0.5 rounded-full text-[10px] font-bold self-start sm:self-auto">
              {{ t('admin.analytics.engagementLabel') }}
            </span>
          </div>
          <div class="flex items-end justify-between">
            <p class="text-3xl font-bold text-slate-800 tracking-tight">{{ stats.totalLikes }}</p>
            <div class="ml-4 w-10 h-10 rounded-xl bg-rose-50 text-rose-500 flex items-center justify-center">
              <span class="material-symbols-outlined">favorite</span>
            </div>
          </div>
          <p class="mt-3 text-xs text-slate-400">{{ t('admin.analytics.totalComments') }} {{ stats.totalComments || 0 }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <section class="bg-white/60 backdrop-blur-md rounded-[24px] p-6 sm:p-8 border border-white/60 shadow-soft-diffusion">
          <div class="flex items-center justify-between gap-4 mb-6">
            <div>
              <h3 class="font-bold text-slate-800 text-base sm:text-lg">{{ t('admin.analytics.trendUsers') }}</h3>
              <p class="text-xs text-slate-400 mt-1">{{ t('admin.analytics.platformOverview') }}</p>
            </div>
            <div class="flex items-center gap-2">
              <button
                class="px-3 py-1.5 rounded-full text-xs font-bold transition-all"
                :class="userTrendRange === 7 ? 'bg-primary text-white shadow-glow' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
                @click="userTrendRange = 7"
              >
                {{ t('admin.analytics.last7Days') }}
              </button>
              <button
                class="px-3 py-1.5 rounded-full text-xs font-bold transition-all"
                :class="userTrendRange === 30 ? 'bg-primary text-white shadow-glow' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
                @click="userTrendRange = 30"
              >
                {{ t('admin.analytics.last30Days') }}
              </button>
            </div>
          </div>
          <div ref="userTrendChartRef" class="w-full h-[320px]"></div>
        </section>

        <section class="bg-white/60 backdrop-blur-md rounded-[24px] p-6 sm:p-8 border border-white/60 shadow-soft-diffusion">
          <div class="flex items-center justify-between gap-4 mb-6">
            <div>
              <h3 class="font-bold text-slate-800 text-base sm:text-lg">{{ t('admin.analytics.trendUploads') }}</h3>
              <p class="text-xs text-slate-400 mt-1">{{ t('admin.analytics.realtimeNote') }}</p>
            </div>
            <div class="flex items-center gap-2">
              <button
                class="px-3 py-1.5 rounded-full text-xs font-bold transition-all"
                :class="pictureTrendRange === 7 ? 'bg-primary text-white shadow-glow' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
                @click="pictureTrendRange = 7"
              >
                {{ t('admin.analytics.last7Days') }}
              </button>
              <button
                class="px-3 py-1.5 rounded-full text-xs font-bold transition-all"
                :class="pictureTrendRange === 30 ? 'bg-primary text-white shadow-glow' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
                @click="pictureTrendRange = 30"
              >
                {{ t('admin.analytics.last30Days') }}
              </button>
            </div>
          </div>
          <div ref="pictureTrendChartRef" class="w-full h-[320px]"></div>
        </section>

        <section class="bg-white/60 backdrop-blur-md rounded-[24px] p-6 sm:p-8 border border-white/60 shadow-soft-diffusion">
          <div class="flex items-center justify-between gap-4 mb-6">
            <div>
              <h3 class="font-bold text-slate-800 text-base sm:text-lg">{{ t('admin.analytics.fileTypeDistribution') }}</h3>
              <p class="text-xs text-slate-400 mt-1">{{ t('admin.analytics.storageHealth') }}</p>
            </div>
          </div>
          <div ref="fileTypeChartRef" class="w-full h-[320px]"></div>
        </section>

        <section class="bg-white/60 backdrop-blur-md rounded-[24px] p-6 sm:p-8 border border-white/60 shadow-soft-diffusion">
          <div class="flex items-center justify-between gap-4 mb-6">
            <div>
              <h3 class="font-bold text-slate-800 text-base sm:text-lg">{{ t('admin.analytics.spaceActivityRank') }}</h3>
              <p class="text-xs text-slate-400 mt-1">{{ t('admin.analytics.last30Days') }}</p>
            </div>
          </div>
          <div ref="spaceActivityChartRef" class="w-full h-[320px]"></div>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { getAdminStatsUsingGet } from '@/api/admin/adminController'
import { message } from 'ant-design-vue'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const loading = ref(true)
const userTrendRange = ref<7 | 30>(7)
const pictureTrendRange = ref<7 | 30>(7)

const stats = ref<API.AdminStatsVO>({
  totalUsers: 0,
  todayNewUsers: 0,
  weekNewUsers: 0,
  monthNewUsers: 0,
  totalPictures: 0,
  todayNewPictures: 0,
  totalStorageUsed: 0,
  totalComments: 0,
  totalLikes: 0,
  userTrend7d: [],
  userTrend30d: [],
  pictureTrend7d: [],
  pictureTrend30d: [],
  fileTypeDistribution: [],
  spaceActivityRank: [],
})

const userTrendChartRef = ref<HTMLDivElement | null>(null)
const pictureTrendChartRef = ref<HTMLDivElement | null>(null)
const fileTypeChartRef = ref<HTMLDivElement | null>(null)
const spaceActivityChartRef = ref<HTMLDivElement | null>(null)

let userTrendChart: echarts.EChartsType | null = null
let pictureTrendChart: echarts.EChartsType | null = null
let fileTypeChart: echarts.EChartsType | null = null
let spaceActivityChart: echarts.EChartsType | null = null

const currentUserTrend = computed(() => {
  return userTrendRange.value === 7 ? stats.value.userTrend7d ?? [] : stats.value.userTrend30d ?? []
})

const currentPictureTrend = computed(() => {
  return pictureTrendRange.value === 7 ? stats.value.pictureTrend7d ?? [] : stats.value.pictureTrend30d ?? []
})

const formatSize = (bytes?: number) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const getOrCreateChart = (el: HTMLDivElement | null, current: echarts.EChartsType | null) => {
  if (!el) return null
  return current ?? echarts.init(el)
}

const formatFileTypeLabel = (type?: string) => {
  switch (type) {
    case 'image':
      return t.value('space.detail.filterImage')
    case 'video':
      return t.value('space.detail.filterVideo')
    case 'model':
      return t.value('space.detail.filterModel')
    case 'audio':
      return t.value('space.detail.filterAudio')
    case 'text':
      return t.value('space.detail.filterText')
    default:
      return t.value('common.unknown')
  }
}

const renderUserTrendChart = () => {
  userTrendChart = getOrCreateChart(userTrendChartRef.value, userTrendChart)
  if (!userTrendChart) return
  const seriesData = currentUserTrend.value
  userTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 24, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: seriesData.map(item => item.label || ''),
      axisLabel: { color: '#64748b', rotate: 25 },
      axisLine: { lineStyle: { color: '#cbd5e1' } },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#64748b' },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.18)' } },
    },
    series: [{
      type: 'line',
      smooth: true,
      data: seriesData.map(item => item.value || 0),
      symbolSize: 8,
      itemStyle: { color: '#6366f1' },
      lineStyle: { width: 3, color: '#6366f1' },
      areaStyle: { color: 'rgba(99, 102, 241, 0.18)' },
    }],
  }, true)
}

const renderPictureTrendChart = () => {
  pictureTrendChart = getOrCreateChart(pictureTrendChartRef.value, pictureTrendChart)
  if (!pictureTrendChart) return
  const seriesData = currentPictureTrend.value
  pictureTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 24, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: seriesData.map(item => item.label || ''),
      axisLabel: { color: '#64748b', rotate: 25 },
      axisLine: { lineStyle: { color: '#cbd5e1' } },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#64748b' },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.18)' } },
    },
    series: [{
      type: 'line',
      smooth: true,
      data: seriesData.map(item => item.value || 0),
      symbolSize: 8,
      itemStyle: { color: '#10b981' },
      lineStyle: { width: 3, color: '#10b981' },
      areaStyle: { color: 'rgba(16, 185, 129, 0.18)' },
    }],
  }, true)
}

const renderFileTypeChart = () => {
  fileTypeChart = getOrCreateChart(fileTypeChartRef.value, fileTypeChart)
  if (!fileTypeChart) return
  const seriesData = (stats.value.fileTypeDistribution ?? []).map(item => ({
    value: item.count || 0,
    name: formatFileTypeLabel(item.type),
  }))
  fileTypeChart.setOption({
    tooltip: { trigger: 'item' },
    legend: {
      bottom: 0,
      textStyle: { color: '#64748b' },
    },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['50%', '46%'],
      itemStyle: {
        borderRadius: 12,
        borderColor: '#fff',
        borderWidth: 2,
      },
      label: {
        color: '#475569',
        formatter: '{b}\n{c}',
      },
      data: seriesData.length ? seriesData : [{ value: 1, name: t.value('admin.analytics.noData') }],
    }],
  }, true)
}

const renderSpaceActivityChart = () => {
  spaceActivityChart = getOrCreateChart(spaceActivityChartRef.value, spaceActivityChart)
  if (!spaceActivityChart) return
  const data = stats.value.spaceActivityRank ?? []
  spaceActivityChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const item = data[params?.[0]?.dataIndex ?? 0]
        if (!item) return ''
        return [
          item.spaceName || t.value('common.unknown'),
          `${t.value('admin.analytics.activityScore')}: ${item.activityScore || 0}`,
          `${t.value('admin.analytics.uploadCount')}: ${item.uploadCount || 0}`,
          `${t.value('admin.analytics.editCount')}: ${item.editCount || 0}`,
        ].join('<br/>')
      },
    },
    grid: { left: 80, right: 24, top: 20, bottom: 24 },
    xAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#64748b' },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.18)' } },
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.spaceName || t.value('common.unknown')),
      axisLabel: { color: '#475569' },
      axisLine: { lineStyle: { color: '#cbd5e1' } },
    },
    series: [{
      type: 'bar',
      data: data.map(item => item.activityScore || 0),
      barWidth: 18,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#38bdf8' },
          { offset: 1, color: '#818cf8' },
        ]),
        borderRadius: [12, 12, 12, 12],
      },
    }],
  }, true)
}

const renderAllCharts = () => {
  renderUserTrendChart()
  renderPictureTrendChart()
  renderFileTypeChart()
  renderSpaceActivityChart()
}

const resizeCharts = () => {
  userTrendChart?.resize()
  pictureTrendChart?.resize()
  fileTypeChart?.resize()
  spaceActivityChart?.resize()
}

const fetchStats = async () => {
  loading.value = true
  try {
    const res = await getAdminStatsUsingGet()
    if (res.data.code === 0 && res.data.data) {
      stats.value = {
        ...stats.value,
        ...res.data.data,
      }
      await nextTick()
      renderAllCharts()
    } else {
      message.error(t.value('admin.analytics.fetchFailed'))
    }
  } catch (e) {
    message.error(t.value('admin.analytics.fetchFailed'))
  } finally {
    loading.value = false
  }
}

watch([userTrendRange, currentUserTrend], () => {
  renderUserTrendChart()
}, { deep: true })

watch([pictureTrendRange, currentPictureTrend], () => {
  renderPictureTrendChart()
}, { deep: true })

watch(() => stats.value.fileTypeDistribution, () => {
  renderFileTypeChart()
}, { deep: true })

watch(() => stats.value.spaceActivityRank, () => {
  renderSpaceActivityChart()
}, { deep: true })

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  userTrendChart?.dispose()
  pictureTrendChart?.dispose()
  fileTypeChart?.dispose()
  spaceActivityChart?.dispose()
})
</script>

<style scoped>
</style>
