<template>
  <div class="space-y-6">
    <!-- Filters Bar -->
    <div class="flex flex-wrap items-center gap-3 mb-8 sticky top-0 z-20 py-4 -mt-4 bg-background-light/95 backdrop-blur-sm transition-all border-b border-white/20">
        <div class="flex items-center gap-3 bg-white/60 text-primary px-5 py-2.5 rounded-2xl font-bold border border-primary/20 shadow-sm">
            <span class="material-symbols-outlined text-[20px]">gallery_thumbnail</span>
            {{ t('admin.picture.allPictures') }}
        </div>

        <!-- Batch Action Button -->
        <a-popconfirm
            v-if="selectedIds.length > 0"
            :title="`确认删除选中的 ${selectedIds.length} 项吗？`"
            @confirm="doBatchDelete"
        >
            <button class="flex items-center gap-2 bg-rose-500 hover:bg-rose-600 text-white px-5 py-2.5 rounded-2xl font-bold shadow-glow-rose transition-all animate-in fade-in slide-in-from-left-4">
                <span class="material-symbols-outlined text-[20px]">delete_sweep</span>
                {{ t('common.delete') }} ({{ selectedIds.length }})
            </button>
        </a-popconfirm>
        
        <div class="w-full md:hidden relative">
            <input 
                v-model="searchParams.searchText" 
                @keydown.enter="doSearch"
                type="text" 
                :placeholder="t('admin.picture.searchPlaceholder')" 
                class="bg-white/50 border border-white/40 rounded-xl py-2 pl-3 pr-10 text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary/20 w-full transition-all"
            />
            <span class="absolute right-3 top-2 text-slate-400 font-variation-settings-fill-1 material-symbols-outlined">search</span>
        </div>

        <div class="ml-0 md:ml-auto flex items-center gap-3 w-full md:w-auto">
            <div class="flex items-center gap-2 px-2 py-1.5 bg-white/40 rounded-lg border border-white/40 overflow-x-auto no-scrollbar">
                <button
                    v-for="option in fileTypeOptions"
                    :key="option.key"
                    class="px-3 py-1 rounded-full text-xs font-bold transition-all whitespace-nowrap"
                    :class="searchParams.fileType === option.value || (!searchParams.fileType && option.value === undefined) ? 'bg-primary text-white shadow-sm' : 'text-slate-500 hover:text-primary hover:bg-white/60'"
                    @click="setFileType(option.value)"
                >
                    {{ option.label }}
                </button>
            </div>
             <div class="relative hidden md:block">
                <input 
                    v-model="searchParams.searchText" 
                    @keydown.enter="doSearch"
                    type="text" 
                    :placeholder="t('admin.picture.searchPlaceholder')" 
                    class="bg-white/50 border border-white/40 rounded-xl py-2 pl-3 pr-10 text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary/20 w-48 focus:w-64 transition-all"
                />
                <span class="absolute right-3 top-2 text-slate-400 font-variation-settings-fill-1 material-symbols-outlined">search</span>
            </div>
            <div class="flex items-center gap-2 px-3 py-1.5 bg-white/40 rounded-lg border border-white/40">
                <span class="text-xs text-slate-500 font-medium">{{ t('admin.picture.sortLabel') }}</span>
                <select v-model="searchParams.sortOrder" @change="doSearch" class="bg-transparent border-none text-xs rounded-lg text-slate-700 focus:ring-0 cursor-pointer font-semibold py-0 pl-1 pr-6">
                    <option value="descend">{{ t('admin.picture.newest') }}</option>
                    <option value="ascend">{{ t('admin.picture.oldest') }}</option>
                </select>
            </div>
        </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center py-20">
        <a-spin size="large" :tip="t('admin.picture.loading')" />
    </div>

    <!-- Empty State -->
    <div v-else-if="dataList.length === 0" class="flex flex-col items-center justify-center py-32 bg-white/40 rounded-3xl border border-dashed border-slate-300">
        <span class="material-symbols-outlined text-6xl text-slate-200 mb-4">image_not_supported</span>
        <p class="text-slate-500 font-medium">{{ t('admin.picture.empty') }}</p>
    </div>

    <!-- Content Grid -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <div 
            v-for="item in dataList" 
            :key="item.id" 
            class="group relative bg-white/60 backdrop-blur-md rounded-[20px] p-3 border border-white/60 shadow-soft-diffusion hover:shadow-lg transition-all duration-300 hover:-translate-y-1 flex flex-col"
            :class="{ 'ring-2 ring-primary border-primary/40 bg-primary/5': selectedIds.includes(item.id!) }"
            @click="toggleSelect(item.id)"
        >
            <div class="relative aspect-[4/3] rounded-2xl overflow-hidden bg-slate-100 mb-3 group-hover:shadow-md transition-shadow">
                <img :src="item.thumbnailUrl || item.url" v-img-fallback="item.url" class="absolute inset-0 w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" />
                
                <div class="absolute top-2 left-2 bg-black/30 backdrop-blur-md text-white px-2 py-1 rounded-lg border border-white/10 text-[10px] font-bold uppercase">
                    {{ item.picFormat || 'IMG' }}
                </div>

                <div class="absolute inset-0 bg-slate-900/10 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                     <a-button type="primary" class="rounded-xl font-bold text-xs shadow-lg" @click.stop="$router.push(`/asset/${item.idStr || item.id}`)">{{ t('admin.picture.viewDetails') }}</a-button>
                </div>

                <!-- Checkbox Overlay -->
                <div 
                    class="absolute top-2 right-2 w-6 h-6 rounded-lg border-2 flex items-center justify-center transition-all z-10"
                    :class="selectedIds.includes(item.id!) ? 'bg-primary border-primary text-white' : 'bg-white/40 border-white/60 text-transparent opacity-0 group-hover:opacity-100'"
                >
                    <span class="material-symbols-outlined text-[16px] font-bold">check</span>
                </div>
            </div>

                <div class="px-1.5 pb-1 flex-1 flex flex-col">
                <div class="flex justify-between items-start mb-1 gap-2">
                    <h3 class="font-bold text-slate-800 text-sm truncate flex-1">{{ item.name }}</h3>
                </div>
                <p class="text-xs text-slate-500 mb-2 truncate">{{ t('admin.picture.by') }} <span class="text-primary font-medium hover:underline cursor-pointer">@{{ item.user?.userName }}</span></p>
                <div class="mt-auto flex items-center justify-between text-[11px] text-slate-400 font-medium border-t border-slate-200/50 pt-2">
                    <span class="flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px]">sd_card</span>
                        {{ formatSize(item.picSize) }}
                    </span>
                    <span>{{ formatDate(item.createTime) }}</span>
                </div>
            </div>
             <!-- Quick Delete (for Admin) -->
             <button @click.stop="doDelete(item.id)" class="absolute -top-2 -right-2 w-7 h-7 bg-rose-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity shadow-lg flex items-center justify-center hover:scale-110 active:scale-90 z-30">
                <span class="material-symbols-outlined text-sm">delete</span>
             </button>
        </div>
    </div>

    <!-- Pagination -->
    <div v-if="!loading && dataList.length > 0" class="mt-12 flex justify-center">
        <a-pagination 
            v-model:current="searchParams.current" 
            :total="total" 
            :pageSize="searchParams.pageSize" 
            @change="doSearch"
            show-less-items 
        />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listPictureVoByPageUsingPost, deletePictureUsingPost, deletePictureBatchUsingPost } from '@/api/asset/assetController'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const loading = ref(true)
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

const searchParams = reactive({
    current: 1,
    pageSize: 12,
    searchText: '',
    sortField: 'createTime',
    sortOrder: 'descend',
    fileType: undefined as number | undefined
})

const fileTypeOptions = [
    { key: 'all', label: t.value('admin.picture.filterAll'), value: undefined },
    { key: 'image', label: t.value('admin.picture.filterImage'), value: 1 },
    { key: 'video', label: t.value('admin.picture.filterVideo'), value: 2 },
    { key: 'model', label: t.value('admin.picture.filterModel'), value: 3 },
    { key: 'audio', label: t.value('admin.picture.filterAudio'), value: 4 },
    { key: 'text', label: t.value('admin.picture.filterText'), value: 5 }
]


const fetchData = async () => {
    loading.value = true
    try {
        const res = await listPictureVoByPageUsingPost({
            ...searchParams,
            name: searchParams.searchText
        })
        if(res.data.code === 0 && res.data.data) {
            dataList.value = res.data.data.records || []
            total.value = Number(res.data.data.total) || 0
        } else {
            message.error(t.value('admin.picture.fetchFailed') + ': ' + res.data.message)
        }
    } catch(e) {
        message.error(t.value('admin.picture.requestFailed'))
    } finally {
        loading.value = false
    }
}

const toggleSelect = (id?: number) => {
    if (!id) return
    const index = selectedIds.value.indexOf(id)
    if (index > -1) {
        selectedIds.value.splice(index, 1)
    } else {
        selectedIds.value.push(id)
    }
}

const doBatchDelete = async () => {
    if (selectedIds.value.length === 0) return
    try {
        const res = await deletePictureBatchUsingPost({ idList: selectedIds.value as any })
        if (res.data.code === 0) {
            message.success(t.value('admin.picture.deleteSuccess'))
            selectedIds.value = []
            fetchData()
        } else {
            message.error(t.value('admin.picture.deleteFailed'))
        }
    } catch (e) {
        message.error(t.value('admin.picture.requestFailed'))
    }
}


const doSearch = () => {
    searchParams.current = 1
    fetchData()
}

const setFileType = (value?: number) => {
    searchParams.fileType = value
    doSearch()
}


const doDelete = (id: number | undefined) => {
    if (!id) return
    Modal.confirm({
        title: t.value('admin.picture.deleteConfirmTitle'),
        content: t.value('admin.picture.deleteConfirmContent'),
        okText: t.value('admin.picture.deleteText'),
        okType: 'danger',
        onOk: async () => {
             const res = await deletePictureUsingPost({ id })
             if(res.data.code === 0) {
                 message.success(t.value('admin.picture.deleteSuccess'))
                 fetchData()
             } else {
                 message.error(t.value('admin.picture.deleteFailed'))
             }
        }
    })
}

const formatSize = (bytes?: number) => {
    if (!bytes || bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-'
    return dayjs(dateStr).format('YYYY-MM-DD')
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.shadow-glow-rose {
    box-shadow: 0 10px 30px rgba(244, 63, 94, 0.4);
}
/* Grid animations can be added here */
</style>
