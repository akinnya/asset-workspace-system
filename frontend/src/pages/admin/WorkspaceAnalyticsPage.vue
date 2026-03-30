<template>
  <div id="spaceAnalyzePage" class="p-6 md:p-8">
    <div class="flex flex-col sm:flex-row sm:items-center gap-4 mb-8">
        <div class="w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center text-primary">
            <span class="material-symbols-outlined text-2xl">analytics</span>
        </div>
        <div>
            <h1 class="font-display font-bold text-2xl sm:text-3xl text-slate-800 tracking-tight">{{ t('admin.spaceAnalyze.title') || '空间数据分析' }}</h1>
            <p class="text-slate-500 text-xs sm:text-sm">Analyze space usage and resource distribution</p>
        </div>
    </div>
    
    <!-- Top Section: Stats Cards -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 mb-10">
        <!-- Main Charts Area -->
        <div class="lg:col-span-8">
            <div class="grid grid-cols-1 xl:grid-cols-2 gap-8 h-full">
                <!-- 空间使用率 -->
                <div class="glass-panel p-6 rounded-[2.5rem] bg-white/60 backdrop-blur-md border border-white/80 shadow-sm relative overflow-hidden group">
                    <div class="flex items-center justify-between mb-6">
                        <h3 class="font-bold text-slate-800">{{ t('admin.spaceAnalyze.usageTitle') }}</h3>
                        <div class="w-8 h-8 rounded-xl bg-primary/10 flex items-center justify-center text-primary">
                            <span class="material-symbols-outlined text-sm">pie_chart</span>
                        </div>
                    </div>
                    <div ref="usageChart" class="w-full h-[260px] sm:h-[380px]"></div>
                </div>

                <!-- 空间排行 -->
                <div class="glass-panel p-6 rounded-[2.5rem] bg-white/60 backdrop-blur-md border border-white/80 shadow-sm relative overflow-hidden group">
                    <div class="flex items-center justify-between mb-6">
                        <h3 class="font-bold text-slate-800">{{ t('admin.spaceAnalyze.rankTitle') }}</h3>
                        <div class="w-8 h-8 rounded-xl bg-orange-500/10 flex items-center justify-center text-orange-500">
                            <span class="material-symbols-outlined text-sm">bar_chart</span>
                        </div>
                    </div>
                    <div ref="rankChart" class="w-full h-[260px] sm:h-[380px]"></div>
                </div>
            </div>
        </div>

        <!-- Sidebar Insights Area -->
        <div class="lg:col-span-4">
            <div class="glass-panel p-6 sm:p-8 rounded-[2.5rem] h-full bg-white/60 backdrop-blur-md border border-white/80 shadow-sm">
                <h3 class="text-xl font-black text-slate-800 mb-6 flex items-center gap-3">
                    <div class="w-10 h-10 rounded-2xl bg-indigo-500/10 flex items-center justify-center text-indigo-500">
                        <span class="material-symbols-outlined">insights</span>
                    </div>
                    {{ t('admin.spaceAnalyze.sideTitle') }}
                </h3>
                <div class="space-y-6">
                        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div class="bg-indigo-50 p-4 rounded-2xl">
                                <p class="text-[10px] font-black text-indigo-400 uppercase tracking-widest mb-1">Total Spaces</p>
                                <p class="text-2xl font-black text-indigo-600">{{ totalSpaces }}</p>
                            </div>
                            <div class="bg-emerald-50 p-4 rounded-2xl">
                                <p class="text-[10px] font-black text-emerald-400 uppercase tracking-widest mb-1">Active Users</p>
                                <p class="text-2xl font-black text-emerald-600">{{ totalUsers }}</p>
                            </div>
                             <div class="bg-amber-50 p-4 rounded-2xl col-span-2">
                                <p class="text-[10px] font-black text-amber-400 uppercase tracking-widest mb-1">{{ t('admin.spaceAnalyze.usageTitle') }}</p>
                                <div class="flex items-center gap-2 text-amber-600 font-bold">
                                    <span class="w-2 h-2 rounded-full bg-amber-500 animate-pulse"></span> {{ usageText }}
                                </div>
                            </div>
                        </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Bottom Section: Category & Size Distribution -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- 分类分析 -->
        <div class="glass-panel p-6 rounded-[2.5rem] bg-white/60 backdrop-blur-md border border-white/80 shadow-sm">
            <div class="flex items-center justify-between mb-6">
                <h3 class="font-bold text-slate-800">{{ t('admin.spaceAnalyze.categoryTitle') || '分类分布' }}</h3>
            </div>
            <div ref="categoryChart" class="w-full h-[260px] sm:h-[400px]"></div>
        </div>

        <!-- 大小分析 -->
        <div class="glass-panel p-6 rounded-[2.5rem] bg-white/60 backdrop-blur-md border border-white/80 shadow-sm">
            <div class="flex items-center justify-between mb-6">
                <h3 class="font-bold text-slate-800">{{ t('admin.spaceAnalyze.sizeTitle') || '按大小分布' }}</h3>
            </div>
            <div ref="sizeChart" class="w-full h-[260px] sm:h-[400px]"></div>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { 
    getSpaceRankAnalyzeUsingPost, 
    getSpaceUsageAnalyzeUsingPost,
    getSpaceCategoryAnalyzeUsingPost,
    getSpaceSizeAnalyzeUsingPost
} from '@/api/workspace/workspaceAnalyzeController'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const usageChart = ref(null)
const rankChart = ref(null)
const categoryChart = ref(null)
const sizeChart = ref(null)

const initUsageChart = (data: API.SpaceUsageAnalyzeResponse) => {
    if(!usageChart.value) return;
    const chart = echarts.init(usageChart.value)
    
    const option = {
        title: {
            text: '资源占用比例',
            left: 'center'
        },
        tooltip: {
            trigger: 'item'
        },
        legend: {
            orient: 'vertical',
            left: 'left'
        },
        series: [
            {
                name: '存储空间',
                type: 'pie',
                radius: '50%',
                data: [
                    { value: data.usedSize, name: '已使用大小 (Bytes)' },
                    { value: (data.maxSize || 0) - (data.usedSize || 0), name: '剩余空间' }
                ],
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ]
    };
    chart.setOption(option)
}

const initRankChart = (data: API.SpaceRankAnalyzeResponse[]) => {
    if(!rankChart.value) return;
    const chart = echarts.init(rankChart.value)
    
    const option = {
        title: {
             text: '空间使用量 Top 10',
             left: 'center'
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
            type: 'shadow'
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: [
            {
            type: 'category',
            data: data.map(item => item.spaceName),
            axisTick: {
                alignWithLabel: true
            }
            }
        ],
        yAxis: [
            {
            type: 'value'
            }
        ],
        series: [
            {
            name: '已用大小',
            type: 'bar',
            barWidth: '60%',
            data: data.map(item => item.totalSize)
            }
        ]
    };
    chart.setOption(option)
}

const initCategoryChart = (data: API.SpaceCategoryAnalyzeResponse[]) => {
    if(!categoryChart.value) return;
    const chart = echarts.init(categoryChart.value)
    const option = {
        title: { text: '分类占用分析', left: 'center' },
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            data: data.map(item => ({ value: item.totalSize, name: item.category }))
        }]
    }
    chart.setOption(option)
}

const initSizeChart = (data: API.SpaceSizeAnalyzeResponse[]) => {
    if(!sizeChart.value) return;
    const chart = echarts.init(sizeChart.value)
    const option = {
        title: { text: '图片大小分布', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: data.map(item => item.sizeRange) },
        yAxis: { type: 'value' },
        series: [{
            data: data.map(item => item.count),
            type: 'bar',
            itemStyle: { color: '#6366f1' }
        }]
    }
    chart.setOption(option)
}

import { formatSize } from '@/utils'
import { computed } from 'vue'

const totalSpaces = ref<number | string>('--')
const totalUsers = ref<number | string>('--')
const usedSize = ref<number>(0)
const maxSize = ref<number>(0)

const usageText = computed(() => {
    return `${formatSize(usedSize.value)} / ${formatSize(maxSize.value)}`
})

const fetchData = async () => {
    // 1. Fetch Stats (Total Spaces & Users)
    try {
        const { listSpaceByPageUsingPost } = await import('@/api/workspace/workspaceController')
        const { listUserVoByPageUsingPost } = await import('@/api/user/userController')
        
        const spaceRes = await listSpaceByPageUsingPost({ current: 1, pageSize: 1 })
        if (spaceRes.data.code === 0) {
            totalSpaces.value = spaceRes.data.data?.total ?? 0
        }
        
        const userRes = await listUserVoByPageUsingPost({ current: 1, pageSize: 1 })
        if (userRes.data.code === 0) {
            totalUsers.value = userRes.data.data?.total ?? 0
        }
    } catch (e) {
        console.error('Failed to fetch stats', e)
    }

    // 获取使用情况
    try {
        const usageRes = await getSpaceUsageAnalyzeUsingPost({
            queryAll: true
        })
        if(usageRes.data.code === 0 && usageRes.data.data) {
           initUsageChart(usageRes.data.data)
           usedSize.value = usageRes.data.data.usedSize ?? 0
           maxSize.value = usageRes.data.data.maxSize ?? 0
        }
    } catch (e) {
        console.error(e)
    }

    // 获取排行
    try {
        const rankRes = await getSpaceRankAnalyzeUsingPost({
            topN: 10
        })
        if(rankRes.data.code === 0 && rankRes.data.data) {
           initRankChart(rankRes.data.data)
        }
    } catch (e) {
        console.error(e)
    }

    // 获取分类
    try {
        const categoryRes = await getSpaceCategoryAnalyzeUsingPost({ queryAll: true })
        if (categoryRes.data.code === 0 && categoryRes.data.data) {
            initCategoryChart(categoryRes.data.data)
        }
    } catch (e) { console.error(e) }

    // 获取大小分布
    try {
        const sizeRes = await getSpaceSizeAnalyzeUsingPost({ queryAll: true })
        if (sizeRes.data.code === 0 && sizeRes.data.data) {
            initSizeChart(sizeRes.data.data)
        }
    } catch (e) { console.error(e) }
}

onMounted(() => {
    fetchData()
})

</script>

<style scoped>
</style>
