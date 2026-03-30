<template>
  <div id="homePage" class="home-container">
    <div class="fixed-bg">
      <div class="bg-grid"></div>
      <div class="bg-spotlight"></div>
      <div class="bg-surface"></div>
    </div>

    <!-- Main Content -->
    <main class="content-area">
      <div class="max-w-container">
        
        <!-- Hero Section -->
        <section class="hero-section relative mb-12 py-16 overflow-hidden">
            <div class="relative z-10 text-center">
                <!-- Tagline -->
                <div class="inline-flex items-center gap-2 px-5 py-2.5 mb-8 rounded-full glass-premium reveal-on-scroll">
                    <span class="relative flex h-3 w-3">
                        <span class="relative inline-flex rounded-full h-3 w-3 bg-primary"></span>
                    </span>
                    <span class="text-sm font-semibold text-shimmer">{{ t('home.heroTagline') }}</span>
                </div>

                <!-- Title -->
                <h1 class="text-5xl md:text-7xl font-black mb-6 text-shimmer tracking-tight reveal-on-scroll">
                    {{ t('home.heroTitle') }}
                </h1>
                
                <!-- Subtitle -->
                <p class="text-lg md:text-xl text-slate-500 max-w-2xl mx-auto mb-10 reveal-on-scroll animation-delay-200">
                    {{ t('home.heroSubtitle') }}
                </p>


                <!-- Search box -->
                <div class="max-w-xl mx-auto relative group reveal-on-scroll animation-delay-600">
                    <div class="absolute inset-x-0 -bottom-2 h-px bg-gradient-to-r from-transparent via-primary/30 to-transparent blur-sm"></div>
                    <div class="flex flex-col sm:flex-row items-stretch gap-3">
                        <div class="relative flex-1">
                            <input
                                v-model="searchText"
                                type="text" 
                                class="w-full bg-white border border-slate-200 p-4 sm:p-5 pl-12 sm:pl-14 rounded-2xl shadow-sm focus:ring-4 focus:ring-primary/10 transition-all outline-none text-slate-700 text-base sm:text-lg"
                                :placeholder="t('home.hero.search_placeholder')"
                                @keyup.enter="doSearch"
                            />
                            <SearchOutlined class="absolute left-4 sm:left-6 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-primary text-lg sm:text-xl transition-colors" />
                        </div>
                        <button
                           class="w-full sm:w-auto px-6 sm:px-8 py-3 bg-primary text-white rounded-xl font-bold shadow-sm hover:bg-primary-hover transition-all flex items-center justify-center gap-2"
                           @click="doSearch"
                        >
                            <SearchOutlined /> {{ t('home.hero.search_button') }}
                        </button>
                    </div>
                </div>
            </div>
        </section>

        <!-- Filter Tabs (Centered) -->
        <div class="filter-scroll flex justify-start md:justify-center items-center gap-2 mb-12 overflow-x-auto px-4 pb-4 scrollbar-hide reveal-on-scroll animation-delay-600">
          <div class="p-1.5 bg-white rounded-2xl border border-slate-200 shadow-sm flex gap-1">
            <button 
              v-for="tabKey in ['featured', 'image', 'video', 'model', 'text', 'audio']"
              :key="tabKey"
              :class="[
                'px-4 sm:px-8 py-2.5 rounded-xl text-xs sm:text-sm transition-all duration-300',
                activeTab === tabKey 
                  ? 'bg-slate-950 text-white font-bold shadow-sm'
                  : 'text-slate-500 hover:text-primary font-medium hover:bg-slate-50'
              ]"
              @click="activeTab = tabKey"
            >
              {{ t(`home.tabs.${tabKey}`) }}
            </button>
          </div>
        </div>

        <!-- Masonry Grid -->
        <a-list
          :grid="{ gutter: 32, xs: 1, sm: 2, md: 3, lg: 4, xl: 4, xxl: 5 }"
          :data-source="dataList"
          :loading="loading && searchParams.current === 1"
        >
          <template #renderItem="{ item, index }">
            <a-list-item class="masonry-item reveal-on-scroll" :style="`transition-delay: ${index % 4 * 100}ms`">
              <div class="bg-white rounded-[2rem] overflow-hidden shadow-soft-diffusion hover:shadow-2xl transition-all duration-500 border border-white group cursor-pointer bouncy-hover" @click="doDetail(item)">
                 <div class="relative aspect-[3/4] overflow-hidden">
                    <img 
                      v-if="getMediaType(item) === 'image'"
                      :src="item.thumbnailUrl || item.url" 
                      v-img-fallback="item.url"
                      :alt="item.name" 
                      @contextmenu.prevent
                      onload="this.classList.add('loaded')"
                      class="w-full h-full object-cover transform scale-105 group-hover:scale-110 transition-all duration-[1.5s] ease-out img-lazy-fade" 
                      loading="lazy"
                    />
                    <div 
                      v-else
                      class="w-full h-full flex flex-col items-center justify-center bg-slate-100 text-slate-500"
                    >
                      <span class="material-symbols-outlined text-5xl text-slate-400">
                        {{ getMediaIcon(getMediaType(item)) }}
                      </span>
                      <span class="mt-2 text-[10px] font-black tracking-widest uppercase text-slate-400">
                        {{ getMediaLabel(item) }}
                      </span>
                    </div>
                    <!-- Hover Overlay -->
                    <div class="absolute inset-0 bg-gradient-to-t from-slate-900/90 via-slate-900/10 to-transparent opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-all duration-500 flex flex-col justify-end p-4 sm:p-8">
                        <div class="md:translate-y-4 md:group-hover:translate-y-0 transition-transform duration-500">
                            <h3 class="text-white font-black text-sm sm:text-xl leading-tight mb-1">{{ item.name }}</h3>
                            <div class="flex items-center gap-2 text-white/70 text-xs sm:text-sm">
                                <div class="w-5 h-5 sm:w-6 sm:h-6 rounded-full bg-white/20 p-0.5" v-if="item.user">
                                   <img :src="item.user.userAvatar || defaultAvatar" class="w-full h-full object-cover rounded-full" />
                                </div>
                                    <span v-if="item.user" class="truncate">{{ item.user.userName }}</span>
                            </div>
                        </div>
                    </div>
                    <!-- Stats Badge -->
                    <div class="absolute top-6 left-6 flex gap-2">
                        <div class="bg-black/20 backdrop-blur-md text-white text-[10px] uppercase font-black px-3 py-1.5 rounded-full border border-white/10 tracking-wider">
                            {{ getMediaLabel(item) }}
                        </div>
                    </div>
                 </div>
              </div>
            </a-list-item>
          </template>
        </a-list>
        
        <!-- Load More Button -->
        <div class="load-more-wrapper flex justify-center mt-12 mb-20 text-center" v-if="dataList.length < total">
             <button 
                class="flex items-center gap-2 px-8 py-3 bg-white border border-slate-200 rounded-full text-slate-600 font-medium hover:bg-slate-50 hover:border-slate-300 transition-all shadow-sm group"
                @click="loadMore"
                :disabled="loading"
             >
                <span class="anticon animate-spin text-primary" v-if="loading"><loading-outlined /></span>
                <span class="anticon text-slate-400 group-hover:text-primary transition-colors" v-else><reload-outlined /></span>
                <span>{{ loading ? t('home.hero.loading') : t('home.hero.loadMore') }}</span>
             </button>
        </div>
      </div>
    </main>


  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watchEffect, watch, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { listPictureVoByPageUsingPost } from '@/api/asset/assetController'
import { useRouter } from 'vue-router'
import defaultAvatar from '@/assets/avatar.png'
import { 
  SearchOutlined, PlusOutlined, LoadingOutlined, ReloadOutlined
} from '@ant-design/icons-vue'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)
const searchText = ref('')

// Magnetic effect logic
const handleMagneticMove = (e: MouseEvent) => {
    const targets = document.querySelectorAll('.magnetic')
    targets.forEach(el => {
        const rect = el.getBoundingClientRect()
        const x = e.clientX - (rect.left + rect.width / 2)
        const y = e.clientY - (rect.top + rect.height / 2)
        const distance = Math.sqrt(x*x + y*y)
        
        if (distance < 100) {
            const multi = 0.2
            ;(el as HTMLElement).style.transform = `translate(${x * multi}px, ${y * multi}px)`
        } else {
            ;(el as HTMLElement).style.transform = `translate(0, 0)`
        }
    })
}

const toggleLanguage = () => {
    languageStore.setLanguage(languageStore.currentLang === 'en' ? 'zh' : 'en')
}

const router = useRouter()
// State
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)
const activeTab = ref('featured')

// Search Params
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
  fileType: undefined,
})

// Fetch Data
const fetchData = async (append = false) => {
  loading.value = true
  try {
    const res = await listPictureVoByPageUsingPost(searchParams)
    if (res.data.code === 0 && res.data.data) {
      if (append) {
          dataList.value = [...dataList.value, ...(res.data.data.records ?? [])]
      } else {
          dataList.value = res.data.data.records ?? []
      }
      total.value = res.data.data.total ?? 0
    } else {
      message.error(t.value('common.loadError') + ': ' + res.data.message)
    }
  } catch (e) {
    message.error(t.value('common.actionFailed'))
  } finally {
    loading.value = false
  }
}

// Map tabs to backend categories
const tabToFileTypeMap: Record<string, number | undefined> = {
    featured: undefined,
    image: 1,
    video: 2,
    model: 3,
    audio: 4,
    text: 5
}

// Watch activeTab to trigger search
watch(activeTab, (newTab) => {
    searchParams.fileType = tabToFileTypeMap[newTab]
    searchParams.current = 1
    fetchData(false)
})

const mediaFormats = {
    image: ['jpg', 'jpeg', 'png', 'webp', 'gif', 'bmp', 'tif', 'tiff', 'svg'],
    video: ['mp4', 'mov', 'avi', 'webm', 'mkv'],
    audio: ['mp3', 'wav', 'flac', 'm4a', 'ogg', 'aac'],
    model: ['pmx', 'vrm', 'obj', 'fbx', 'glb', 'gltf'],
    text: ['txt', 'md', 'markdown', 'pdf', 'doc', 'docx', 'rtf', 'csv', 'json', 'xml', 'yaml', 'yml']
}

const getMediaType = (item: API.PictureVO) => {
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

// Watchers
watchEffect(() => {
    // When activeTab changes, we should reset (logic needs to handle this if using tabs for filtering)
    // For now, only searchParams change triggers this automatically? 
    // Wait, watchEffect is tracking all reactive deps.
    // If we want manual control for Load More, we should separate the watcher.
})

// Initial Load
onMounted(() => {
  if (router.currentRoute.value.query.searchText) {
      searchText.value = router.currentRoute.value.query.searchText as string
      searchParams.searchText = searchText.value
  }
  fetchData()
  window.addEventListener('mousemove', handleMagneticMove)
})

onUnmounted(() => {
    window.removeEventListener('mousemove', handleMagneticMove)
})

const doSearch = () => {
    searchParams.searchText = searchText.value;
    searchParams.current = 1;
    fetchData(false);
}

const loadMore = () => {
    searchParams.current = (searchParams.current || 1) + 1;
    fetchData(true);
}

const doDetail = (picture: API.PictureVO) => {
  router.push(`/asset/${picture.idStr || picture.id}`)
}

// Intersection Observer for Reveal Animation
onMounted(() => {
    fetchData()
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    }
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('is-visible')
            }
        })
    }, observerOptions)
    
    // We need to wait for items to render or watch the dataList
    const watchReveal = () => {
        const items = document.querySelectorAll('.reveal-on-scroll')
        items.forEach(el => observer.observe(el))
    }
    
    watch(dataList, () => {
        setTimeout(watchReveal, 100)
    }, { immediate: true })
})
</script>

<style scoped>
/* Base Layout */
/* Base Layout */
.home-container {
  min-height: 100vh;
  padding-top: 80px;
  background-color: transparent; /* Background handled by fixed-bg */
  position: relative;
  overflow-x: hidden;
}

/* Fixed Background */
.fixed-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(15, 23, 42, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(15, 23, 42, 0.045) 1px, transparent 1px);
  background-size: 34px 34px;
  opacity: 0.7;
}

.bg-spotlight {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 16% 18%, rgba(20, 184, 166, 0.14), transparent 24%),
    radial-gradient(circle at 82% 14%, rgba(15, 118, 110, 0.1), transparent 20%);
}

.bg-surface {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg,
    rgba(248, 250, 252, 0.76) 0%,
    rgba(248, 250, 252, 0.9) 35%,
    rgba(241, 245, 249, 1) 100%);
}

/* Content Area */
.content-area {
  position: relative;
  z-index: 10;
  padding: 0 24px;
}

.max-w-container {
  max-width: 1600px;
  margin: 0 auto;
}

/* Filter Scroll */
.filter-scroll {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none;  /* IE 10+ */
}
.filter-scroll::-webkit-scrollbar {
  display: none; /* Chrome/Safari */
}

.masonry-item {
  margin-bottom: 24px;
  break-inside: avoid;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

</style>
