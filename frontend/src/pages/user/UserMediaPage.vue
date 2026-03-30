<template>
  <div class="flex flex-col h-screen overflow-hidden bg-white/50 relative">
    <!-- Main Layout -->
    <div class="flex flex-1 overflow-hidden relative z-10">
        <!-- Sidebar (Desktop) -->
        <aside class="hidden lg:flex flex-col w-64 glass-sidebar h-full overflow-y-auto custom-scrollbar pb-6 border-r border-white/60 bg-white/40 backdrop-blur-xl">
             <div class="p-4 space-y-6">
                <div>
                    <h3 class="px-3 text-xs font-bold text-slate-400 uppercase tracking-wider mb-4">{{ t('userMedia.categories') }}</h3>
                    <nav class="space-y-2">
                        <button 
                            v-for="item in mediaTypes" 
                            :key="item.key"
                            :class="['nav-item w-full flex items-center gap-4 px-4 py-3 rounded-2xl text-sm transition-all group font-bold font-display', (item.value === undefined ? !searchParams.fileType : searchParams.fileType === item.value) ? 'bg-primary text-white shadow-glow' : 'text-slate-500 hover:text-primary hover:bg-white/70']"
                            @click="onMediaTypeChange(item.value)"
                        >
                            <span class="material-symbols-outlined text-[22px] group-hover:rotate-6 transition-transform">{{ item.icon }}</span>
                            {{ item.label }}
                        </button>
                    </nav>
                </div>
             </div>
        </aside>

        <!-- Sidebar (Mobile Drawer) -->
        <a-drawer
            v-model:visible="mobileSidebarVisible"
            placement="left"
            :closable="false"
            width="280"
            class="mobile-sidebar-drawer"
            :body-style="{ padding: 0 }"
        >
            <div class="h-full flex flex-col bg-slate-50/80 backdrop-blur-xl">
                <div class="p-6 border-b border-slate-100 flex items-center justify-between">
                    <span class="text-lg font-bold text-slate-800">{{ t('userMedia.library') }}</span>
                    <button @click="mobileSidebarVisible = false" class="w-8 h-8 rounded-full flex items-center justify-center bg-white shadow-sm">
                        <span class="material-symbols-outlined text-slate-400">close</span>
                    </button>
                </div>
                <div class="flex-1 p-4">
                    <nav class="space-y-2">
                        <button 
                            v-for="item in mediaTypes" 
                            :key="item.key"
                            :class="['w-full flex items-center gap-4 px-4 py-4 rounded-2xl text-sm transition-all font-bold font-display', (item.value === undefined ? !searchParams.fileType : searchParams.fileType === item.value) ? 'bg-primary text-white' : 'text-slate-500 hover:bg-white']"
                            @click="onMediaTypeChange(item.value); mobileSidebarVisible = false"
                        >
                            <span class="material-symbols-outlined text-[22px]">{{ item.icon }}</span>
                            {{ item.label }}
                        </button>
                    </nav>
                </div>
            </div>
        </a-drawer>

        <!-- Main Content -->
        <main class="flex-1 overflow-y-auto relative custom-scrollbar p-4 md:p-8 scroll-smooth">
            <div class="max-w-7xl mx-auto pb-24">
                 <!-- Page Header -->
                <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-6 md:mb-8">
                    <div>
                         <div class="flex items-center gap-2 text-slate-500 mb-2 cursor-pointer hover:text-primary transition-colors" @click="doBackToProfile">
                              <span class="material-symbols-outlined text-sm">arrow_back</span>
                              <span class="text-sm font-semibold">{{ t('userMedia.back') }}</span>
                         </div>
                         <div class="flex items-center gap-3">
                            <button class="lg:hidden w-10 h-10 rounded-xl bg-white shadow-sm flex items-center justify-center text-slate-400 active:scale-95 transition-all" @click="mobileSidebarVisible = true">
                                <span class="material-symbols-outlined">menu_open</span>
                            </button>
                            <h1 class="text-2xl sm:text-3xl font-bold text-slate-800 mb-1 tracking-tight">{{ t('userMedia.title') }}</h1>
                         </div>
                         <p class="text-slate-500 text-sm font-medium">{{ t('userMedia.subtitle') }}</p>
                    </div>
                    <div class="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full md:w-auto">
                        <div class="flex items-center gap-3">
                            <button class="w-10 h-10 rounded-2xl bg-white border border-slate-200 text-slate-500 hover:text-primary hover:border-primary transition-all flex items-center justify-center font-bold shadow-sm" @click="languageStore.setLanguage(languageStore.currentLang === 'en' ? 'zh' : 'en')">
                                {{ languageStore.currentLang === 'en' ? '中' : 'En' }}
                            </button>
                            <div class="relative group flex-1 sm:flex-none w-full">
                                <input 
                                    v-model="searchParams.searchText"
                                    type="text" 
                                    :placeholder="t('userMedia.searchPlaceholder')" 
                                    @keyup.enter="doSearch"
                                    class="w-full sm:w-64 pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-2xl focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none text-sm transition-all"
                                />
                                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-primary transition-colors text-[20px]">search</span>
                            </div>
                        </div>
                        <button class="flex items-center justify-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white font-medium rounded-2xl shadow-lg shadow-primary/25 transition-all transform hover:scale-105 active:scale-95 text-sm w-full sm:w-auto" @click="router.push('/asset/upload')">
                            <span class="material-symbols-outlined text-[20px]">add</span>
                            <span>{{ t('userMedia.newEntry') }}</span>
                        </button>
                    </div>
                </header>

                <!-- Mobile Media Type Tabs -->
                <div class="lg:hidden mb-6">
                    <div class="flex gap-2 overflow-x-auto no-scrollbar">
                        <button
                            v-for="item in mediaTypes"
                            :key="item.key"
                            :class="[
                                'px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all',
                                (item.value === undefined ? !searchParams.fileType : searchParams.fileType === item.value)
                                  ? 'bg-primary text-white shadow-glow'
                                  : 'bg-white/70 text-slate-600 hover:text-primary'
                            ]"
                            @click="onMediaTypeChange(item.value)"
                        >
                            {{ item.label }}
                        </button>
                    </div>
                </div>

                <!-- Filters Bar -->
                <div class="sticky top-0 z-30 pt-2 pb-6 -mx-4 px-4 md:-mx-8 md:px-8 bg-gradient-to-b from-[#F7F8FA] via-[#F7F8FA] to-transparent">
                     <div class="glass-panel rounded-2xl p-1.5 flex flex-col md:flex-row justify-between items-center gap-4 shadow-sm bg-white/60 backdrop-blur-md border border-white/80">
                        <div class="flex w-full md:w-auto bg-slate-100/50 p-1 rounded-xl overflow-x-auto no-scrollbar">
                            <button 
                            v-for="cat in categoryList" 
                            :key="cat"
                            @click="onCategoryChange(cat)"
                                :class="[
                                    'tab-btn flex-1 md:flex-none px-5 py-2 rounded-lg text-sm transition-all font-semibold whitespace-nowrap',
                                    (cat === 'All' ? !searchParams.category : searchParams.category === cat) 
                                        ? 'bg-primary text-white shadow-glow' 
                                        : 'text-slate-500 hover:text-slate-800 hover:bg-white/50'
                                ]"
                            >
                                {{ cat }}
                            </button>
                        </div>
                        <div class="flex items-center gap-2 w-full md:w-auto justify-end px-2">
                             <div class="h-4 w-px bg-slate-300 mx-1 hidden md:block"></div>
                             <div class="flex items-center gap-2">
                                <span class="text-xs text-slate-400 font-medium">{{ t('userMedia.batchMode') }}:</span>
                                <a-switch v-model:checked="isBatchMode" size="small" />
                             </div>
                        </div>
                     </div>
                </div>

                <!-- Masonry Grid -->
                <div class="columns-1 sm:columns-2 lg:columns-3 xl:columns-4 gap-6 space-y-6">
                    <div v-if="loading && dataList.length === 0" class="col-span-full py-20 text-center">
                        <a-spin size="large" />
                    </div>
                    <div v-else-if="!loading && dataList.length === 0" class="col-span-full py-20 text-center flex flex-col items-center gap-4">
                        <div class="w-20 h-20 rounded-full bg-slate-100 flex items-center justify-center">
                            <span class="material-symbols-outlined text-slate-300 text-4xl">folder_open</span>
                        </div>
                        <p class="text-slate-400 font-medium">{{ t('userMedia.emptyDesc') }}</p>
                    </div>
                    
                    <div v-for="item in dataList" :key="item.id" 
                        :class="[
                            'break-inside-avoid group relative rounded-3xl overflow-hidden bg-white shadow-sm border transition-all duration-300 mb-6 ring-primary/40',
                            selectedIds.includes(item.id!) ? 'ring-2 border-primary shadow-lg shadow-primary/5' : 'border-slate-100 hover:shadow-xl hover:shadow-primary/10 hover:-translate-y-1'
                        ]"
                    >
                        <!-- Batch Checkbox -->
                        <div 
                            v-if="isBatchMode"
                            class="absolute top-4 left-4 z-20"
                            @click.stop
                        >
                            <a-checkbox 
                                :checked="selectedIds.includes(item.id!)" 
                                @change="onSelectChange(item.id!)"
                            />
                        </div>

                        <div class="absolute top-4 right-4 z-20 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-opacity flex gap-2">
                            <button class="w-9 h-9 rounded-full bg-white/90 backdrop-blur-md text-slate-600 hover:text-primary shadow-md flex items-center justify-center transition-colors" @click.stop="doEdit(item.id)">
                                <span class="material-symbols-outlined text-[18px]">edit</span>
                            </button>
                             <button class="w-9 h-9 rounded-full bg-white/90 backdrop-blur-md text-slate-600 hover:text-red-500 shadow-md flex items-center justify-center transition-colors" @click.stop="doDelete(item.id)">
                                <span class="material-symbols-outlined text-[18px]">delete</span>
                            </button>
                        </div>
                        <div class="cursor-pointer" @click="router.push(`/asset/${item.idStr || item.id}`)">
                            <div class="w-full relative overflow-hidden bg-slate-50 min-h-[100px]">
                                <img 
                                    v-if="getMediaType(item) === 'image'" 
                                    :src="item.thumbnailUrl || item.url" 
                                    v-img-fallback="item.url"
                                    class="w-full h-auto object-cover transition-transform duration-700 group-hover:scale-105" 
                                    loading="lazy" 
                                />
                                <div v-else class="w-full h-48 flex flex-col items-center justify-center text-slate-400">
                                    <span class="material-symbols-outlined text-5xl">
                                        {{ getMediaIcon(getMediaType(item)) }}
                                    </span>
                                    <span class="mt-2 text-[10px] font-black tracking-widest uppercase">
                                        {{ getMediaLabel(item) }}
                                    </span>
                                </div>
                            </div>
                            <div class="p-5">
                                <div class="flex justify-between items-start mb-2">
                                    <h3 class="font-bold text-slate-800 text-sm truncate pr-2 group-hover:text-primary transition-colors">{{ item.name }}</h3>
                                </div>
                                <div class="flex items-center justify-between">
                                    <span class="text-xs font-medium text-slate-400">
                                        {{ (item.picSize ? (item.picSize / 1024 / 1024).toFixed(1) + ' MB' : '') }}
                                        <span v-if="getFormatLabel(item)"> • {{ getFormatLabel(item) }}</span>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Load More Button -->
                <div class="flex justify-center mt-10 pb-10" v-if="dataList.length < total">
                    <button 
                        class="px-8 py-3 bg-white border border-slate-200 rounded-xl text-slate-600 font-bold hover:bg-primary hover:text-white hover:border-primary transition-all shadow-sm flex items-center gap-2"
                        @click="() => { searchParams.current = (searchParams.current || 1) + 1; fetchData() }"
                        :disabled="loading"
                    >
                        <span v-if="loading" class="material-symbols-outlined animate-spin text-sm">progress_activity</span>
                        <span>{{ loading ? t('userMedia.loading') : t('userMedia.loadMore') }}</span>
                    </button>
                </div>
            </div>
        </main>
    </div>

    <!-- Floating Selection Bar -->
    <Transition name="slide-up">
        <div v-if="isBatchMode && selectedIds.length > 0" class="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 w-full max-w-lg px-4 pointer-events-none">
            <div class="bg-white/90 backdrop-blur-xl rounded-2xl px-6 py-4 shadow-2xl shadow-primary/20 border border-white/60 flex items-center justify-between pointer-events-auto">
                 <div class="flex items-center gap-4">
                    <div class="flex items-center justify-center px-3 py-1 rounded-full bg-primary text-white font-bold text-sm shadow-md">
                        {{ selectedIds.length }} {{ t('userMedia.selected') }}
                    </div>
                    <button class="text-xs font-semibold text-slate-400 hover:text-primary transition-colors" @click="selectedIds = []">{{ t('userMedia.clear') }}</button>
                </div>
                <div class="h-6 w-px bg-slate-200 mx-2"></div>
                <div class="flex items-center gap-2">
                     <button 
                        class="px-4 py-2 bg-red-50 text-red-500 hover:bg-red-500 hover:text-white rounded-xl text-sm font-bold transition-all flex items-center gap-2 group"
                        @click="doBatchDelete"
                    >
                        <span class="material-symbols-outlined text-[18px]">delete</span>
                        <span>{{ t('userMedia.deleteBatch') }}</span>
                    </button>
                    <button 
                        class="p-2 rounded-xl hover:bg-slate-100 text-slate-400 hover:text-primary transition-colors tooltip-trigger" 
                        :title="t('userMedia.searchSimilar')"
                        v-if="selectedIds.length === 1"
                        @click="router.push(`/asset/search?pictureId=${selectedIds[0]}`)"
                    >
                        <span class="material-symbols-outlined text-[20px]">image_search</span>
                    </button>
                </div>
            </div>
        </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listPictureVoByPageUsingPost, deletePictureUsingPost, deletePictureBatchUsingPost, listPictureTagCategoryUsingGet } from '@/api/asset/assetController'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'
import { message, Modal } from 'ant-design-vue'

const mobileSidebarVisible = ref(false)
const router = useRouter()
const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const dataList = ref<API.PictureVO[]>([])
const loading = ref(false)
const total = ref(0)
const categoryList = ref<string[]>([])
const isBatchMode = ref(false)
const selectedIds = ref<number[]>([])

const mediaTypes = computed(() => [
    { key: 'all', label: t.value('home.category.all'), icon: 'grid_view', value: undefined },
    { key: 'image', label: t.value('home.tabs.image'), icon: 'image', value: 1 },
    { key: 'video', label: t.value('home.tabs.video'), icon: 'movie', value: 2 },
    { key: 'model', label: t.value('home.tabs.model'), icon: 'view_in_ar', value: 3 },
    { key: 'audio', label: t.value('home.tabs.audio'), icon: 'music_note', value: 4 },
    { key: 'text', label: t.value('home.tabs.text'), icon: 'description', value: 5 },
])

const mediaFormats = {
    image: ['jpg', 'jpeg', 'png', 'webp', 'gif', 'bmp', 'tif', 'tiff', 'svg'],
    video: ['mp4', 'mov', 'avi', 'webm', 'mkv'],
    audio: ['mp3', 'wav', 'flac', 'm4a', 'ogg', 'aac'],
    model: ['pmx', 'vrm', 'obj', 'fbx', 'glb', 'gltf'],
    text: ['txt', 'md', 'markdown', 'pdf', 'doc', 'docx', 'rtf', 'csv', 'json', 'xml', 'yaml', 'yml']
}
const knownFormats = new Set(Object.values(mediaFormats).flat())

const normalizeFormat = (value?: string) => {
    if (!value) return ''
    let format = value.toString().trim().toLowerCase()
    if (format.includes('/')) {
        format = format.split('/').pop() || ''
    }
    if (format.includes(';')) {
        format = format.split(';')[0]
    }
    if (format.includes('+')) {
        format = format.split('+')[0]
    }
    return format.replace(/^\./, '')
}

const getMediaType = (item: API.PictureVO): keyof typeof mediaFormats => {
    const urlFormat = item.url?.split('?')?.[0]?.split('.')?.pop()
    const format = (item.picFormat || urlFormat || '').toLowerCase()
    if (mediaFormats.video.includes(format)) return 'video'
    if (mediaFormats.audio.includes(format)) return 'audio'
    if (mediaFormats.model.includes(format)) return 'model'
    if (mediaFormats.text.includes(format)) return 'text'
    return 'image'
}

const getMediaLabel = (item: API.PictureVO) => {
    if (item.category) return item.category
    const typeKey = getMediaType(item)
    return t.value(`home.tabs.${typeKey}`) || t.value('home.tabs.image')
}

const getFormatLabel = (item: API.PictureVO) => {
    const raw = normalizeFormat(item.picFormat)
    const urlFormat = normalizeFormat(item.url?.split('?')?.[0]?.split('.')?.pop())
    let format = ''
    if (raw && knownFormats.has(raw)) {
        format = raw
    } else if (urlFormat && knownFormats.has(urlFormat)) {
        format = urlFormat
    }
    if (!format) {
        const mediaType = getMediaType(item)
        const fallback = mediaFormats[mediaType]?.[0]
        return fallback ? fallback.toUpperCase() : ''
    }
    return format.toUpperCase()
}

const getMediaIcon = (mediaType: string) => {
    switch (mediaType) {
        case 'video':
            return 'movie'
        case 'audio':
            return 'music_note'
        case 'text':
            return 'description'
        case 'model':
            return 'view_in_ar'
        default:
            return 'image'
    }
}

const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 24,
  userId: loginUserStore.loginUser.id,
  sortField: 'createTime',
  sortOrder: 'descend',
  searchText: '',
  category: undefined,
  fileType: undefined
})

const ensureLoginUser = async () => {
    if (!loginUserStore.loginUser.id) {
        await loginUserStore.fetchLoginUser()
    }
}

const doBackToProfile = async () => {
    await ensureLoginUser()
    router.push('/user/profile')
}

const fetchCategoryList = async () => {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
        const raw = res.data.data.categoryList || []
        // Filter out unwanted categories per user feedback (Q18, Emoji, Poster)
        categoryList.value = raw.filter((c: string) => !['电商', '模板', 'Template', 'E-commerce', '素材', '表情包', '海报'].includes(c))
    }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listPictureVoByPageUsingPost({
        ...searchParams.value,
        userId: loginUserStore.loginUser.id 
    })
    if (res.data.code === 0 && res.data.data) {
      // UserMediaPage: Logic for load more
      if ((searchParams.value.current || 1) > 1) {
          dataList.value = [...dataList.value, ...(res.data.data.records ?? [])]
      } else {
          dataList.value = res.data.data.records || []
      }
      total.value = res.data.data.total || 0
    } else {
      message.error(t.value('userMedia.loadFailed') + ': ' + res.data.message)
    }
  } catch (e) {
    message.error(t.value('common.requestFailed'))
  } finally {
    loading.value = false
  }
}

const doSearch = () => {
    searchParams.value.current = 1
    fetchData()
}

const onCategoryChange = (cat: string | undefined) => {
    if (!cat || searchParams.value.category === cat) {
        searchParams.value.category = undefined
    } else {
        searchParams.value.category = cat
    }
    searchParams.value.current = 1
    fetchData()
}

const onMediaTypeChange = (type: number | undefined) => {
    searchParams.value.fileType = type
    searchParams.value.current = 1
    fetchData()
}

const onSelectChange = (id: number) => {
    const index = selectedIds.value.indexOf(id)
    if (index > -1) {
        selectedIds.value.splice(index, 1)
    } else {
        selectedIds.value.push(id)
    }
}

const doDelete = (id: number | undefined) => {
    if (!id) return
    Modal.confirm({
        title: t.value('userMedia.deleteConfirm'),
        content: t.value('userMedia.deleteContent'),
        okText: t.value('userMedia.deleteConfirm'),
        okType: 'danger',
        onOk: async () => {
            const res = await deletePictureUsingPost({ id })
            if (res.data.code === 0) {
                message.success(t.value('userMedia.deleteSuccess'))
                fetchData()
            } else {
                message.error(t.value('userMedia.deleteFailed'))
            }
        }
    })
}

const doBatchDelete = () => {
    if (selectedIds.value.length === 0) return
    Modal.confirm({
        title: t.value('userMedia.deleteBatchConfirm').replace('{count}', selectedIds.value.length.toString()),
        content: t.value('userMedia.deleteContent'),
        okText: t.value('userMedia.deleteBatchConfirm').split('{')[0], // Simplified okText
        okType: 'danger',
        onOk: async () => {
            const res = await deletePictureBatchUsingPost({ idList: selectedIds.value })
            if (res.data.code === 0) {
                message.success(t.value('userMedia.deleteBatchSuccess'))
                selectedIds.value = []
                fetchData()
            } else {
                message.error(t.value('userMedia.deleteFailed'))
            }
        }
    })
}

const doEdit = (id: number | undefined) => {
    if (id) router.push(`/asset/upload?id=${id}`)
}

watch(isBatchMode, (val) => {
    if (!val) selectedIds.value = []
})

onMounted(async () => {
  await ensureLoginUser()
  if (loginUserStore.loginUser.id) {
    fetchCategoryList()
    fetchData()
  }
})
</script>

<style scoped>
.masonry-grid {
    column-count: 1;
}
@media (min-width: 640px) {
    .masonry-grid { column-count: 2; }
}
@media (min-width: 1024px) {
    .masonry-grid { column-count: 3; }
}
@media (min-width: 1280px) {
    .masonry-grid { column-count: 4; }
}
.no-scrollbar::-webkit-scrollbar {
    display: none;
}
.no-scrollbar {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  transform: translate(-50%, 100px);
  opacity: 0;
}
.glass-sidebar {
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
}
</style>
