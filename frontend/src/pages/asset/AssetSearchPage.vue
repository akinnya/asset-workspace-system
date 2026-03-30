<template>
  <div class="search-picture-page">
    <div class="content-wrapper">
      
      <!-- Search Header -->
      <div class="mb-6 sm:mb-10 md:mb-12 flex flex-col md:flex-row gap-4 sm:gap-6 md:gap-8 items-center justify-between">
        <div class="flex-1 w-full md:w-auto">
             <h2 class="text-2xl sm:text-3xl md:text-4xl font-extrabold bg-gradient-to-r from-slate-900 via-slate-700 to-slate-500 bg-clip-text text-transparent mb-3 tracking-tight">
                <template v-if="searchText">{{ t('search.resultsFor', { q: searchText }) }}</template>
                <template v-else-if="searchColor">{{ t('search.colorResults') }}</template>
                <template v-else-if="searchImageId">{{ t('search.imageResults') || '以图搜图结果' }}</template>
                <template v-else>{{ t('search.discover') }}</template>
             </h2>
             <div class="flex flex-wrap items-center gap-3">
                <p class="text-slate-400 text-base flex items-center gap-2 font-medium">
                   <span class="inline-block w-2.5 h-2.5 rounded-full bg-primary shadow-[0_0_8px_rgba(15,118,110,0.35)] animate-pulse"></span>
                   {{ t('search.found', { total: total }) }}
                </p>
                <button 
                    v-if="searchText || searchColor || searchImageId"
                    @click="resetSearch"
                    class="px-4 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-600 text-sm font-medium transition-colors flex items-center gap-1"
                >
                   <span class="material-symbols-outlined text-[16px]">refresh</span>
                   {{ t('search.reset') || '重置' }}
                </button>
             </div>
        </div>

        <!-- Optimized Search Tabs -->
        <div class="w-full md:w-[500px] bg-white p-2 sm:p-3 rounded-xl sm:rounded-2xl shadow-sm border border-slate-100">
          <a-tabs v-model:activeKey="activeSearchMode" :tabBarStyle="{ marginBottom: '12px', borderBottom: 'none' }" centered class="search-tabs">
            <!-- Keyword Search -->
            <a-tab-pane key="keyword">
              <template #tab>
                <div class="flex items-center gap-2 text-xs sm:text-sm">
                  <search-outlined />
                  <span>{{ t('search.searchByName') || '文字搜索' }}</span>
                </div>
              </template>
              <div class="px-2 pb-2">
                <a-input-search
                  v-model:value="searchInput"
                  :placeholder="t('search.placeholder')"
                  enter-button
                  size="large"
                  @search="onSearch"
                  class="search-input-custom"
                />
                <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <a-select
                    v-model:value="searchParams.category"
                    allow-clear
                    size="large"
                    :placeholder="t('search.categoryFilter')"
                    :options="categoryOptions.map((item) => ({ label: item, value: item }))"
                    @change="onKeywordFilterChange"
                  />
                  <a-select
                    v-model:value="searchParams.tags"
                    mode="multiple"
                    allow-clear
                    size="large"
                    :placeholder="t('search.tagFilter')"
                    :options="tagOptions.map((item) => ({ label: item, value: item }))"
                    @change="onKeywordFilterChange"
                  />
                </div>
              </div>
            </a-tab-pane>

            <!-- Color Search -->
            <a-tab-pane key="color">
              <template #tab>
                <div class="flex items-center gap-2 text-xs sm:text-sm">
                  <bg-colors-outlined />
                  <span>{{ t('search.searchByColor') || '色彩搜索' }}</span>
                </div>
              </template>
              <div class="px-2 pb-2 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 sm:gap-4">
                <div class="flex flex-1 items-center gap-3 bg-slate-50 p-2 rounded-xl border border-slate-100">
                  <input 
                    type="color" 
                    v-model="colorInput" 
                    class="h-10 w-12 p-0 border-0 rounded-lg cursor-pointer shadow-sm overflow-hidden"
                    :title="t('search.colorResults')"
                    @change="onColorSearch"
                  />
                  <span class="text-slate-400 font-mono text-xs uppercase">{{ colorInput }}</span>
                </div>
                <a-button type="primary" size="large" @click="onColorSearch" class="rounded-xl px-6 w-full sm:w-auto">
                   {{ t('search.button') }}
                </a-button>
              </div>
            </a-tab-pane>

            <!-- Image Search -->
            <a-tab-pane key="image">
              <template #tab>
                <div class="flex items-center gap-2 text-xs sm:text-sm">
                  <camera-outlined />
                  <span>{{ t('search.searchByImage') || '以图搜图' }}</span>
                </div>
              </template>
              <div class="px-2 pb-2">
                <!-- Preview of uploaded image -->
                <div v-if="searchImagePreview" class="mb-3 flex items-center gap-3 p-2 bg-slate-50 rounded-xl border border-slate-100">
                   <img :src="searchImagePreview" class="w-16 h-16 object-cover rounded-lg shadow-sm" />
                   <div class="flex-1">
                      <p class="text-sm font-medium text-slate-700">{{ t('search.searchingWith') || '搜索图片' }}</p>
                      <p class="text-xs text-slate-400">{{ t('search.imageSearch') || '以图搜图中...' }}</p>
                   </div>
                   <button @click="clearImageSearch" class="p-1.5 rounded-lg hover:bg-slate-200 text-slate-400 transition-colors">
                      <span class="material-symbols-outlined text-[18px]">close</span>
                   </button>
                </div>
                <a-upload
                  :show-upload-list="false"
                  :before-upload="beforeUpload"
                  :custom-request="handleImageSearch"
                  class="w-full"
                >
                  <a-button size="large" class="w-full h-12 dashed-upload-btn flex items-center justify-center gap-2 rounded-xl">
                    <camera-outlined />
                    <span>{{ t('search.uploadImagePrompt') || '选择或拖拽图片进行搜索' }}</span>
                  </a-button>
                </a-upload>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </div>

      <!-- Results Grid -->
      <div class="masonry-grid" v-if="dataList.length > 0">
         <div 
             v-for="pic in dataList" 
             :key="pic.id" 
             class="group relative mb-6 sm:mb-8 rounded-2xl overflow-hidden shadow-sm hover:shadow-2xl hover:-translate-y-1 transition-all duration-500 break-inside-avoid cursor-pointer bg-white border border-slate-100/50"
             @click="goToDetail(pic.idStr || pic.id)"
         >
             <div class="relative h-[45vh] max-h-[360px] sm:h-auto sm:max-h-none overflow-hidden bg-slate-100">
               <img 
                   :src="pic.url" 
                   :alt="pic.name" 
                   class="w-full h-full sm:h-auto object-cover transition-transform duration-700 group-hover:scale-105"
                   loading="lazy"
               />
             </div>
             
             <!-- Overlay -->
             <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-all duration-300 flex flex-col justify-end p-4 sm:p-6">
                 <h3 class="text-white font-bold text-base sm:text-lg md:text-xl mb-1 md:mb-2 md:translate-y-4 md:group-hover:translate-y-0 transition-transform duration-300">{{ pic.name }}</h3>
                 <div class="flex justify-between items-center md:opacity-0 md:group-hover:opacity-100 md:translate-y-4 md:group-hover:translate-y-0 transition-all duration-300 delay-75">
                     <div class="flex items-center gap-2">
                         <a-avatar :src="pic.user?.userAvatar" :size="24" class="border border-white/20" />
                         <span class="text-white/90 text-sm font-medium">{{ pic.user?.userName }}</span>
                     </div>
                 </div>
             </div>
         </div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && dataList.length === 0" class="py-20 text-center">
          <img src="https://gw.alipayobjects.com/zos/antfincdn/ZHrcdLPrvN/empty.svg" alt="empty" class="h-40 mx-auto mb-4 opacity-50" />
          <p class="text-slate-500">{{ t('search.empty') }}</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading && dataList.length === 0" class="py-20 text-center">
           <a-spin size="large" />
      </div>

       <!-- Pagination -->
      <div class="mt-8 flex justify-center" v-if="total > (searchParams.pageSize || 20)">
           <a-pagination
            v-model:current="searchParams.current"
            v-model:pageSize="searchParams.pageSize"
            :total="total"
            @change="fetchData"
            show-less-items
          />
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
  searchPictureByColorUsingPost,
  searchPictureByPictureUsingPost,
  searchPictureByFileUsingPost,
} from '@/api/asset/assetController'
import { message } from 'ant-design-vue'
import { CameraOutlined, SearchOutlined, BgColorsOutlined } from '@ant-design/icons-vue'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const route = useRoute()
const router = useRouter()

const searchText = ref<string>((route.query.q as string) || '')
const searchColor = ref<string>((route.query.color as string) || '')
const searchImageId = ref<string>((route.query.pictureId as string) || '')

const activeSearchMode = ref('keyword')

const searchInput = ref(searchText.value)
const colorInput = ref(searchColor.value || '#0f766e')

const dataList = ref<API.PictureVO[]>([])
const loading = ref(false)
const total = ref(0)
const searchImagePreview = ref<string>('')
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

const searchParams = reactive<API.PictureQueryRequest>({
  category: undefined,
  current: 1,
  pageSize: 20,
  sortField: 'createTime',
  sortOrder: 'descend',
  tags: [],
})

// Fetch Data
const fetchData = async () => {
  loading.value = true
  try {
      let res;
      if (searchImageId.value) {
          // Image Search
          res = await searchPictureByPictureUsingPost({
              pictureId: searchImageId.value
          })
          if (res.data.code === 0 && res.data.data) {
               dataList.value = (res.data.data ?? []) as API.PictureVO[]
               total.value = dataList.value.length
           }
      } else if (searchColor.value) {
          // Color Search
          res = await searchPictureByColorUsingPost({
              picColor: searchColor.value,
          })
           if (res.data.code === 0 && res.data.data) {
               dataList.value = (res.data.data ?? []) as API.PictureVO[]
               total.value = dataList.value.length
           }
      } else {
          // Keyword Search
          res = await listPictureVoByPageUsingPost({
            ...searchParams,
            category: searchParams.category || undefined,
            searchText: searchText.value,
            tags: searchParams.tags?.length ? searchParams.tags : undefined,
          })
          if (res.data?.data) {
             dataList.value = res.data.data.records ?? []
             total.value = res.data.data.total ?? 0
          }
      }
  } catch (error) {
    console.error(error)
    message.error(t.value('common.loadError'))
  } finally {
    loading.value = false
  }
}

const fetchTagCategory = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      categoryOptions.value = res.data.data.categoryList ?? []
      tagOptions.value = res.data.data.tagList ?? []
    }
  } catch (error) {
    // 获取筛选元数据失败时不阻断页面使用
  }
}

const onSearch = () => {
    router.push({
        path: '/asset/search',
        query: { q: searchInput.value }
    })
}

const onKeywordFilterChange = () => {
    if (activeSearchMode.value !== 'keyword') {
        activeSearchMode.value = 'keyword'
    }
    if (searchColor.value || searchImageId.value) {
        return
    }
    searchParams.current = 1
    fetchData()
}

const onColorSearch = () => {
    router.push({
        path: '/asset/search',
        query: { color: colorInput.value.replace('#', '') }
    })
}

// Image Search Upload
const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  if (!isJpgOrPng) {
    message.error(t.value('search.uploadTypeError'))
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error(t.value('search.uploadSizeError'))
  }
  return isJpgOrPng && isLt5M
}

const handleImageSearch = async (options: any) => {
    const { file } = options
    // Generate preview
    const reader = new FileReader()
    reader.onload = (e) => {
        searchImagePreview.value = e.target?.result as string
    }
    reader.readAsDataURL(file)
    
    try {
        const res = await searchPictureByFileUsingPost(file)
        if (res.data.code === 0 && res.data.data) {
            dataList.value = (res.data.data ?? []) as API.PictureVO[]
            total.value = dataList.value.length
            activeSearchMode.value = 'image'
            searchImageId.value = ''
        } else {
            message.error(t.value('common.actionFailed') + ': ' + res.data.message)
        }
    } catch(e) {
        message.error(t.value('common.actionFailed'))
    }
}

const resetSearch = () => {
    searchText.value = ''
    searchColor.value = ''
    searchImageId.value = ''
    searchInput.value = ''
    searchImagePreview.value = ''
    searchParams.category = undefined
    searchParams.tags = []
    router.push({ path: '/asset/search' })
}

const clearImageSearch = () => {
    searchImagePreview.value = ''
    searchImageId.value = ''
    activeSearchMode.value = 'keyword'
    router.push({ path: '/asset/search' })
}

const goToDetail = (id?: string | number) => {
    if (!id) return
    router.push(`/asset/${id}`)
}

// Watch route changes to refresh data
watch(
  () => route.query,
  (newQuery) => {
      searchText.value = (newQuery.q as string) || ''
      searchColor.value = (newQuery.color as string) ? '#' + newQuery.color : ''
      searchImageId.value = (newQuery.pictureId as string) || ''
      
      searchInput.value = searchText.value
      // reset pagination
      searchParams.current = 1
      fetchData()
  }
)

onMounted(() => {
    // Check if color provided in query needs # prefix
    if (route.query.color && !(route.query.color as string).startsWith('#')) {
         searchColor.value = '#' + route.query.color
         colorInput.value = searchColor.value
         activeSearchMode.value = 'color'
    } else if (route.query.q) {
         activeSearchMode.value = 'keyword'
    } else if (route.query.pictureId) {
         activeSearchMode.value = 'image'
    }
    fetchTagCategory()
    fetchData()
})

</script>

<style scoped>
.search-picture-page {
    min-height: 100vh;
    padding: 0;
    background: transparent;
}

@media (min-width: 768px) {
    .search-picture-page {
        padding: 0;
    }
}

.content-wrapper {
    max-width: 1400px;
    margin: 0 auto;
}

:deep(.search-input-custom .ant-input) {
  border-radius: 12px 0 0 12px;
}
:deep(.search-input-custom .ant-input-search-button) {
  border-radius: 0 12px 12px 0 !important;
}

.dashed-upload-btn {
  border: 2px dashed #e2e8f0;
  color: #64748b;
  background: #f8fafc;
  transition: all 0.3s ease;
}

.dashed-upload-btn:hover {
  border-color: #0f766e;
  color: #0f766e;
  background: #ecfdf5;
}

.masonry-grid {
    column-count: 1;
    column-gap: 16px;
}

.masonry-grid > * {
  width: 100%;
  display: inline-block;
  vertical-align: top;
  box-sizing: border-box;
}
@media (min-width: 640px) { 
  .masonry-grid { 
    column-count: 2; 
    column-gap: 24px;
  } 
}
@media (min-width: 1024px) { 
  .masonry-grid { 
    column-count: 3; 
    column-gap: 28px;
  } 
}
@media (min-width: 1280px) { 
  .masonry-grid { 
    column-count: 4; 
    column-gap: 32px;
  } 
}

/* Fix Ant Design Tab bottom margin */
:deep(.ant-tabs-nav) {
  margin-bottom: 20px !important;
}

:deep(.search-tabs .ant-tabs-nav-wrap) {
  overflow-x: auto;
  scrollbar-width: none;
}

:deep(.search-tabs .ant-tabs-nav-wrap::-webkit-scrollbar) {
  display: none;
}

:deep(.search-tabs .ant-tabs-nav-list) {
  gap: 8px;
}

@media (max-width: 640px) {
  :deep(.search-tabs .ant-tabs-tab) {
    padding: 6px 10px;
  }
}
</style>
