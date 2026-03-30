<template>
  <div class="upload-page font-display bg-background-light text-slate-900 min-h-screen flex flex-col relative overflow-x-hidden">
    <!-- Background Blobs -->
    <div class="fixed top-[-10%] left-[20%] w-[260px] h-[260px] sm:w-[400px] sm:h-[400px] lg:w-[500px] lg:h-[500px] bg-primary/20 rounded-full blur-[120px] pointer-events-none z-0"></div>
    <div class="fixed bottom-[-10%] right-[-5%] w-[300px] h-[300px] sm:w-[450px] sm:h-[450px] lg:w-[600px] lg:h-[600px] bg-accent-pink/15 rounded-full blur-[100px] pointer-events-none z-0"></div>

    <!-- Main Content -->
    <div class="relative z-10 flex-1 w-full max-w-5xl mx-auto p-6 pb-20">
        
        <!-- Header -->
        <div class="flex flex-col md:flex-row justify-between items-end mb-6 md:mb-8 gap-4">
            <div>
                <h1 class="text-2xl sm:text-3xl font-bold text-slate-900 mb-2">{{ t('picture.addTitle') }}</h1>
                <p class="text-slate-500 text-sm sm:text-base">{{ t('picture.subtitle') }}</p>
            </div>
            <div class="flex flex-col sm:flex-row gap-3 w-full md:w-auto">
                <button class="px-4 py-2.5 rounded-xl bg-white/50 text-slate-600 font-medium hover:bg-white transition-colors border border-transparent hover:border-slate-200 w-full sm:w-auto" @click="goBack">
                    {{ t('picture.cancel') }}
                </button>
                <button class="px-6 py-2.5 rounded-xl bg-primary text-white font-semibold shadow-glow hover:bg-primary-hover transition-all flex items-center justify-center gap-2 w-full sm:w-auto disabled:opacity-60 disabled:cursor-not-allowed" @click="handleSubmit" :disabled="publishDisabled">
                    <RocketOutlined /> {{ t('picture.publish') }}
                </button>
            </div>
        </div>

        <div
            v-if="editLockSupported"
            class="mb-6 rounded-3xl border px-5 py-4 flex flex-col gap-2"
            :class="isLockedByOther ? 'bg-amber-50/90 border-amber-200 text-amber-700' : 'bg-emerald-50/90 border-emerald-200 text-emerald-700'"
        >
            <div class="flex items-center justify-between gap-3">
                <p class="text-sm font-black tracking-wide">{{ t('picture.editLockTitle') }}</p>
                <span class="text-xs font-bold opacity-80">{{ editLockLoading ? t('common.loading') : displayLockStatus }}</span>
            </div>
            <p class="text-sm font-semibold leading-relaxed">
                {{ editLockDescription }}
            </p>
        </div>

        <!-- Grid Layout -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            
            <!-- Left Column: Upload Area (col-span-2) -->
            <div class="lg:col-span-2 flex flex-col gap-6">
                
                <!-- Drag & Drop Zone -->
                <div class="glass-panel rounded-3xl p-1 relative overflow-hidden group">
                     <div class="absolute inset-0 bg-gradient-to-br from-primary/5 to-accent-pink/5 opacity-50 group-hover:opacity-100 transition-opacity"></div>
                     <div class="dashed-border-glass rounded-[20px] p-2 flex flex-col items-center justify-center text-center relative z-10 min-h-[280px] transition-transform duration-300">
                         <a-upload-dragger
                            name="file"
                            :multiple="false"
                            :showUploadList="false"
                            :customRequest="handleUpload"
                            accept="image/*,video/*,audio/*,.glb,.gltf,.pmx,.vrm,.obj,.txt,.md,.markdown,.pdf,.doc,.docx,.rtf,.csv,.json,.xml,.yaml,.yml"
                            class="w-full h-full bg-transparent border-0"
                         >
                            <div class="flex flex-col items-center justify-center py-10">
                                <div class="w-20 h-20 bg-white rounded-full shadow-soft-diffusion flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                                    <CloudUploadOutlined class="text-3xl text-primary" />
                                </div>
                                <h3 class="text-xl font-bold text-slate-800 mb-2">{{ t('picture.dragTitle') }}</h3>
                                <p class="text-slate-500 max-w-sm mx-auto mb-6">{{ t('picture.dragText') }}</p>
                                <button class="px-6 py-2.5 rounded-xl bg-white text-primary font-semibold shadow-sm border border-slate-100 hover:shadow-md transition-all">
                                    {{ t('picture.browse') }}
                                </button>
                            </div>
                         </a-upload-dragger>
                     </div>
                </div>

                <!-- Processing List -->
                <div class="glass-panel rounded-3xl p-6" v-if="previewUrl || uploading">
                    <h3 class="text-sm font-bold text-slate-700 mb-4 uppercase tracking-wider">{{ t('upload.processing') }}</h3>
                    
                    <!-- Loaded Item -->
                    <div class="flex gap-4 mb-6 relative group" v-if="previewUrl">
                         <div class="w-24 h-24 rounded-xl overflow-hidden shadow-sm flex-shrink-0 bg-slate-100 relative">
                             <img v-if="mediaType === 'image'" :src="previewUrl" class="w-full h-full object-cover" />
                             <video v-else-if="mediaType === 'video'" :src="previewUrl" class="w-full h-full object-cover" muted></video>
                             <div v-else-if="mediaType === 'audio'" class="flex items-center justify-center w-full h-full bg-slate-100 text-primary">
                                 <span class="material-symbols-outlined text-4xl animate-pulse">music_note</span>
                             </div>
                             <div v-else-if="mediaType === 'text'" class="flex flex-col items-center justify-center w-full h-full bg-slate-100 text-slate-500 gap-1">
                                 <span class="material-symbols-outlined text-3xl text-slate-400">description</span>
                                 <span class="text-[10px] font-black tracking-widest uppercase opacity-80">TEXT</span>
                             </div>
                             <div v-else class="flex flex-col items-center justify-center w-full h-full bg-slate-800 text-white gap-1">
                                 <span class="material-symbols-outlined text-3xl text-amber-400">deployed_code</span>
                                 <span class="text-[10px] font-black tracking-widest uppercase opacity-80">3D MODEL</span>
                             </div>
                             <div class="absolute top-1 right-1 bg-black/40 text-white text-[10px] px-1.5 py-0.5 rounded backdrop-blur-md font-bold">{{ mediaType.toUpperCase() }}</div>
                         </div>
                         <div class="flex-1 min-w-0 flex flex-col justify-center">
                             <div class="flex justify-between items-start gap-2 mb-1">
                                 <div class="min-w-0 flex-1">
                                     <h4 class="font-semibold text-slate-800 truncate" :title="previewName">{{ previewName || t('common.empty') }}</h4>
                                     <span class="text-xs text-slate-500">{{ t('upload.ready') }}</span>
                                 </div>
                                 <button class="text-slate-400 hover:text-red-500 transition-colors shrink-0" @click="clearPicture">
                                     <DeleteOutlined class="text-[20px]" />
                                 </button>
                             </div>
                             
                             <!-- Palette / Status -->
                             <div class="flex items-center gap-2 mb-3 mt-1" v-if="mediaType === 'image'">
                                 <span class="text-[10px] font-bold text-slate-400 uppercase">{{ t('upload.processing') }}</span>
                                 <span class="text-xs text-emerald-500 font-bold" v-if="!aiLoading">{{ t('upload.ready') }}</span>
                                 <span class="text-xs text-primary font-bold animate-pulse" v-else>{{ t('upload.analyzing') }}</span>
                             </div>

                             <div class="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
                                 <div class="h-full bg-primary w-full shadow-[0_0_10px_rgba(15,118,110,0.35)]"></div>
                             </div>
                         </div>
                    </div>

                    <!-- Uploading Item -->
                    <div class="flex gap-4 relative" v-if="uploading">
                         <div class="w-24 h-24 rounded-xl overflow-hidden shadow-sm flex-shrink-0 bg-slate-100 relative border border-primary/20 flex items-center justify-center">
                             <CloudUploadOutlined class="text-primary/40 text-3xl animate-pulse" />
                         </div>
                         <div class="flex-1 min-w-0 flex flex-col justify-center">
                             <div class="flex justify-between items-start mb-2">
                                 <div class="min-w-0 flex-1">
                                     <h4 class="font-semibold text-slate-800 truncate">{{ t('upload.analyzing') }}</h4>
                                     <span class="text-xs text-slate-500">{{ t('home.hero.loading') }} (Max 100MB)</span>
                                 </div>
                             </div>
                             <div class="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
                                 <div class="h-full bg-primary w-[50%] rounded-full shadow-[0_0_10px_rgba(15,118,110,0.35)] relative overflow-hidden animate-pulse"></div>
                             </div>
                         </div>
                    </div>
                </div>

            </div>

            <!-- Right Column: Details Form (col-span-1) -->
            <div class="lg:col-span-1">
                <div class="glass-panel p-6 rounded-3xl lg:sticky lg:top-6">
                    <div class="flex items-center gap-2 mb-6">
                        <EditOutlined class="text-primary" />
                        <h3 class="font-bold text-slate-800">{{ t('picture.details') }}</h3>
                    </div>
                    
                    <div class="flex flex-col gap-4">
                        <div class="group">
                             <label class="block text-xs font-bold text-slate-500 uppercase mb-1.5 ml-1">{{ t('upload.title') }}</label>
                             <input v-model="pictureForm.name" class="glass-input w-full rounded-xl px-4 py-2.5 text-sm font-medium text-slate-800 placeholder:text-slate-400 focus:ring-0" :placeholder="t('picture.titlePlaceholder')" />
                        </div>
                        
                        <div class="group">
                             <label class="block text-xs font-bold text-slate-500 uppercase mb-1.5 ml-1">{{ t('upload.desc') }}</label>
                             <textarea v-model="pictureForm.introduction" class="glass-input w-full rounded-xl px-4 py-2.5 text-sm font-medium text-slate-800 placeholder:text-slate-400 focus:ring-0 resize-none" rows="4" :placeholder="t('picture.descPlaceholder')"></textarea>
                        </div>
                        
                    </div>
                </div>
            </div>

        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
    RocketOutlined, UserOutlined, CloudUploadOutlined, DeleteOutlined, 
    EditOutlined
} from '@ant-design/icons-vue'
import { 
    acquirePictureEditLockUsingPost,
    editPictureUsingPost, 
    getPictureVoByIdUsingGet, 
    releasePictureEditLockUsingPost,
    renewPictureEditLockUsingPost,
    uploadPictureUsingPost
} from '@/api/asset/assetController'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

// i18n
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const EDIT_LOCK_RENEW_INTERVAL = 60 * 1000

// State
const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const uploading = ref(false)
const aiLoading = ref(false)
const loading = ref(false) // Submit loading
const lastUploadFile = ref<File | null>(null)
const localPreviewUrl = ref('')
const editLockInfo = ref<API.PictureEditLockVO>()
const editLockLoading = ref(false)
const editLockRenewTimer = ref<number | null>(null)

// Computed
const mediaType = computed(() => {
    const format = (
        picture.value?.picFormat ||
        picture.value?.url?.split('.').pop() ||
        lastUploadFile.value?.name?.split('.').pop() ||
        lastUploadFile.value?.type?.split('/').pop() ||
        ''
    ).toLowerCase()
    if (!format) return 'image'
    if (['mp4', 'webm', 'mov', 'avi', 'mkv'].includes(format)) return 'video'
    if (['mp3', 'wav', 'flac', 'm4a', 'ogg', 'aac'].includes(format)) return 'audio'
    if (['glb', 'gltf', 'pmx', 'vrm', 'obj'].includes(format)) return 'model'
    if (['txt', 'md', 'markdown', 'pdf', 'doc', 'docx', 'rtf', 'csv', 'json', 'xml', 'yaml', 'yml'].includes(format)) return 'text'
    return 'image'
})

const previewUrl = computed(() => picture.value?.url || localPreviewUrl.value)
const previewName = computed(() => picture.value?.name || lastUploadFile.value?.name || '')
const isEditingExistingPicture = computed(() => !!route.query.id)
const editLockSupported = computed(() => isEditingExistingPicture.value && !!picture.value?.spaceId && !!editLockInfo.value?.supported)
const isLockedByOther = computed(() => !!editLockInfo.value?.supported && !!editLockInfo.value?.locked && !editLockInfo.value?.lockedByCurrentUser)
const isLockedByMe = computed(() => !!editLockInfo.value?.supported && !!editLockInfo.value?.lockedByCurrentUser)
const publishDisabled = computed(() => loading.value || editLockLoading.value || isLockedByOther.value)
const displayLockStatus = computed(() => {
    if (isLockedByOther.value) return t.value('picture.editLockStatusBusy')
    if (isLockedByMe.value) return t.value('picture.editLockStatusMine')
    return t.value('picture.editLockStatusFree')
})
const editLockDescription = computed(() => {
    if (isLockedByOther.value) {
        return t.value('picture.editLockBusy', { user: editLockInfo.value?.userName || t.value('common.unknown') })
    }
    if (isLockedByMe.value) {
        return t.value('picture.editLockHolding')
    }
    return t.value('picture.editLockIdle')
})

// Actions
const goBack = () => router.back()

const clearPicture = () => {
    if (localPreviewUrl.value) {
        URL.revokeObjectURL(localPreviewUrl.value)
        localPreviewUrl.value = ''
    }
    picture.value = undefined
    lastUploadFile.value = null
}

// Upload Logic
const handleUpload = async ({ file }: any) => {
    uploading.value = true
    if (localPreviewUrl.value) {
        URL.revokeObjectURL(localPreviewUrl.value)
        localPreviewUrl.value = ''
    }
    lastUploadFile.value = file ?? null
    try {
        if (file) {
            localPreviewUrl.value = URL.createObjectURL(file)
        }
        picture.value = undefined
        if (mediaType.value === 'image') {
            analyzeImage()
        }
    } catch (e) {
        message.error(t.value('common.actionFailed'))
    } finally {
        uploading.value = false
    }
}

const buildAutoTags = (colorTag?: string) => {
    const tags: string[] = []
    if (colorTag && colorTag !== '其他') {
        tags.push(colorTag)
    }
    return tags
}

const analyzeImage = async () => {
    if (!previewUrl.value || mediaType.value !== 'image') return
    try {
        aiLoading.value = true
        message.loading({ content: t.value('upload.analyzing'), key: 'ai' })
        const colorTag = await extractDominantColorTag(lastUploadFile.value, previewUrl.value)
        pictureForm.tags = buildAutoTags(colorTag)
        message.success({ content: t.value('picture.aiSuccess'), key: 'ai' })
    } catch (e) {
        applyFallbackTags()
        message.success({ content: t.value('picture.aiSuccess'), key: 'ai' })
    } finally {
        aiLoading.value = false
    }
}

const applyFallbackTags = () => {
    pictureForm.tags = buildAutoTags()
}

const getPictureLastEditTimestamp = (pictureItem?: API.PictureVO) => {
    const raw = pictureItem?.editTime || pictureItem?.updateTime || pictureItem?.createTime
    if (!raw) return undefined
    const parsed = Date.parse(raw)
    return Number.isNaN(parsed) ? undefined : parsed
}

const clearEditLockRenewTimer = () => {
    if (editLockRenewTimer.value !== null) {
        window.clearInterval(editLockRenewTimer.value)
        editLockRenewTimer.value = null
    }
}

const getLockPayload = () => {
    const pictureId = Number(picture.value?.id || route.query.id || 0)
    if (!pictureId) return null
    return { pictureId }
}

const startEditLockRenewTimer = () => {
    clearEditLockRenewTimer()
    if (!isLockedByMe.value) return
    editLockRenewTimer.value = window.setInterval(() => {
        renewEditLock(true)
    }, EDIT_LOCK_RENEW_INTERVAL)
}

const acquireEditLock = async (silent = false) => {
    const payload = getLockPayload()
    if (!payload || !picture.value?.spaceId) return true
    editLockLoading.value = true
    try {
        const res = await acquirePictureEditLockUsingPost(payload)
        if (res.data.code === 0 && res.data.data) {
            editLockInfo.value = res.data.data
            if (res.data.data.lockedByCurrentUser) {
                startEditLockRenewTimer()
                return true
            }
            clearEditLockRenewTimer()
            if (!silent) {
                message.warning(t.value('picture.editLockBusy', { user: res.data.data.userName || t.value('common.unknown') }))
            }
            return false
        }
        if (!silent) {
            message.error(t.value('picture.editLockAcquireFailed'))
        }
        return false
    } catch (error) {
        if (!silent) {
            message.error(t.value('picture.editLockAcquireFailed'))
        }
        return false
    } finally {
        editLockLoading.value = false
    }
}

const renewEditLock = async (silent = true) => {
    const payload = getLockPayload()
    if (!payload || !picture.value?.spaceId) return true
    try {
        const res = await renewPictureEditLockUsingPost(payload)
        if (res.data.code === 0 && res.data.data) {
            editLockInfo.value = res.data.data
            if (res.data.data.lockedByCurrentUser) {
                startEditLockRenewTimer()
                return true
            }
        }
        clearEditLockRenewTimer()
        if (!silent) {
            message.warning(t.value('picture.editLockRenewFailed'))
        }
        return false
    } catch (error) {
        clearEditLockRenewTimer()
        if (!silent) {
            message.warning(t.value('picture.editLockRenewFailed'))
        }
        return false
    }
}

const releaseEditLock = async () => {
    const payload = getLockPayload()
    clearEditLockRenewTimer()
    if (!payload || !picture.value?.spaceId || !isLockedByMe.value) {
        return
    }
    try {
        await releasePictureEditLockUsingPost(payload)
    } catch (error) {
        // 页面退出时静默释放
    } finally {
        editLockInfo.value = {
            pictureId: payload.pictureId,
            supported: true,
            locked: false,
            lockedByCurrentUser: false,
        }
    }
}

const isEditConflictMessage = (msg?: string) => {
    if (!msg) return false
    return msg.includes('资源已被他人更新') || msg.includes('当前资源正在被其他成员编辑')
}

const extractDominantColorTag = async (file?: File | null, url?: string) => {
    const img = await loadImageForAnalysis(file, url)
    const { r, g, b } = getAverageColor(img)
    return classifyColorTag(r, g, b)
}

const loadImageForAnalysis = (file?: File | null, url?: string) => {
    return new Promise<HTMLImageElement>((resolve, reject) => {
        const img = new Image()
        img.crossOrigin = 'anonymous'
        const objectUrl = file ? URL.createObjectURL(file) : ''
        img.onload = () => {
            if (objectUrl) URL.revokeObjectURL(objectUrl)
            resolve(img)
        }
        img.onerror = () => {
            if (objectUrl) URL.revokeObjectURL(objectUrl)
            reject(new Error('image load failed'))
        }
        img.src = objectUrl || url || ''
    })
}

const getAverageColor = (img: HTMLImageElement) => {
    const canvas = document.createElement('canvas')
    const size = 24
    canvas.width = size
    canvas.height = size
    const ctx = canvas.getContext('2d')
    if (!ctx) return { r: 0, g: 0, b: 0 }
    ctx.drawImage(img, 0, 0, size, size)
    const data = ctx.getImageData(0, 0, size, size).data
    let r = 0
    let g = 0
    let b = 0
    let count = 0
    for (let i = 0; i < data.length; i += 4) {
        const alpha = data[i + 3]
        if (alpha < 10) continue
        r += data[i]
        g += data[i + 1]
        b += data[i + 2]
        count += 1
    }
    if (count === 0) return { r: 0, g: 0, b: 0 }
    return {
        r: Math.round(r / count),
        g: Math.round(g / count),
        b: Math.round(b / count)
    }
}

const rgbToHsv = (r: number, g: number, b: number) => {
    const rn = r / 255
    const gn = g / 255
    const bn = b / 255
    const max = Math.max(rn, gn, bn)
    const min = Math.min(rn, gn, bn)
    const delta = max - min
    let h = 0
    if (delta !== 0) {
        if (max === rn) {
            h = ((gn - bn) / delta) % 6
        } else if (max === gn) {
            h = (bn - rn) / delta + 2
        } else {
            h = (rn - gn) / delta + 4
        }
        h *= 60
        if (h < 0) h += 360
    }
    const s = max === 0 ? 0 : delta / max
    const v = max
    return { h, s, v }
}

const classifyColorTag = (r: number, g: number, b: number) => {
    const { h, s, v } = rgbToHsv(r, g, b)
    const saturation = s * 100
    const brightness = v * 100
    if (brightness < 15) return t.value('common.colors.black')
    if (brightness > 90 && saturation < 10) return t.value('common.colors.white')
    if (saturation < 10) return t.value('common.colors.gray')
    if ((h >= 0 && h < 10) || (h >= 345 && h <= 360)) return t.value('common.colors.red')
    if (h >= 10 && h < 35) return t.value('common.colors.orange')
    if (h >= 35 && h < 65) return t.value('common.colors.yellow')
    if (h >= 65 && h < 165) return t.value('common.colors.green')
    if (h >= 165 && h < 190) return t.value('common.colors.cyan')
    if (h >= 190 && h < 260) return t.value('common.colors.blue')
    if (h >= 260 && h < 315) return t.value('common.colors.purple')
    if (h >= 315 && h < 345) return t.value('common.colors.pink')
    return t.value('common.colors.other')
}

// Submit
const handleSubmit = async () => {
     if (!pictureForm.name) {
        message.warning(t.value('picture.titlePlaceholder'))
        return
    }
    if (!picture.value?.id && !lastUploadFile.value) {
        message.warning(t.value('common.chooseBtn'))
        return
    }
    loading.value = true
    try {
        if (picture.value?.spaceId) {
            const lockOk = isLockedByMe.value ? await renewEditLock(false) : await acquireEditLock(false)
            if (!lockOk) {
                return
            }
        }
        const existingPictureId = picture.value?.id
        // 先确保已上传文件，再提交详情发布
        if (existingPictureId && lastUploadFile.value) {
            const uploadRes = await uploadPictureUsingPost({
                id: existingPictureId
            }, { file: lastUploadFile.value })
            if (uploadRes.data.code === 0 && uploadRes.data.data) {
                picture.value = uploadRes.data.data
            } else {
                message.error(t.value('common.actionFailed') + ': ' + uploadRes.data.message)
                return
            }
        }
        if (!picture.value?.id && lastUploadFile.value) {
            const uploadRes = await uploadPictureUsingPost({}, { file: lastUploadFile.value })
            if (uploadRes.data.code === 0 && uploadRes.data.data) {
                picture.value = uploadRes.data.data
            } else {
                message.error(t.value('common.actionFailed') + ': ' + uploadRes.data.message)
                return
            }
        }
        const pictureId = picture.value?.id
        if (!pictureId) {
            message.error(t.value('common.actionFailed'))
            return
        }
        const lastKnownEditTime = getPictureLastEditTimestamp(picture.value)
        const res = await editPictureUsingPost({
            id: pictureId,
            ...pictureForm,
            ...(lastKnownEditTime ? { lastKnownEditTime } : {}),
        })
         if (res.data.code === 0) {
            const targetId = picture.value?.idStr || picture.value?.id || pictureForm.id
            if (!targetId) {
                message.error(t.value('common.actionFailed'))
                return
            }
            message.success(t.value('common.actionSuccess'))
            await releaseEditLock()
            if (localPreviewUrl.value) {
                URL.revokeObjectURL(localPreviewUrl.value)
                localPreviewUrl.value = ''
            }
            router.push(`/asset/${targetId}`)
        } else {
            const errorMsg = res.data.message === '账号已被封禁' ? t.value('common.bannedUser') : res.data.message
            if (isEditConflictMessage(res.data.message)) {
                message.warning(t.value('picture.editConflict'))
            } else {
                message.error(t.value('common.actionFailed') + ': ' + errorMsg)
            }
        }
    } catch (e) {
        message.error(t.value('common.actionFailed'))
    } finally {
        loading.value = false
    }
}

// Init Data
const initData = async () => {
    // 1. Edit Mode
    const id = route.query.id
    if (id) {
        const picRes = await getPictureVoByIdUsingGet({ id: String(id) as any })
        if (picRes.data.data) {
            picture.value = picRes.data.data
            Object.assign(pictureForm, picRes.data.data)
            await acquireEditLock(true)
        }
    }
}

onMounted(() => {
    if (!loginUserStore.loginUser.id) {
        message.warning(t.value('common.loginPrompt'))
        router.push('/user/login?redirect=' + route.fullPath)
        return
    }
    initData()
})

onBeforeUnmount(() => {
    if (localPreviewUrl.value) {
        URL.revokeObjectURL(localPreviewUrl.value)
        localPreviewUrl.value = ''
    }
    releaseEditLock()
})

</script>

<style scoped>
/* Glass Styles from Prototype */
.glass-panel {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
}

.glass-input {
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(229, 231, 235, 0.5);
    transition: all 0.2s ease;
}

.glass-input:focus, .glass-input:focus-within {
    background: rgba(255, 255, 255, 0.9);
    border-color: rgba(15, 118, 110, 0.35);
    box-shadow: 0 0 0 4px rgba(15, 118, 110, 0.12);
}

.dashed-border-glass {
    background-image: url("data:image/svg+xml,%3csvg width='100%25' height='100%25' xmlns='http://www.w3.org/2000/svg'%3e%3crect width='100%25' height='100%25' fill='none' rx='20' ry='20' stroke='%230F766E55' stroke-width='2' stroke-dasharray='12%2c 12' stroke-dashoffset='0' stroke-linecap='square'/%3e%3c/svg%3e");
}

/* Ant Design Overrides to match Glass Input */
:deep(.ant-select-selector), :deep(.ant-select-selection-item) {
    background: transparent !important;
    border: none !important;
    box-shadow: none !important;
}
:deep(.ant-select-selector) {
    height: 100% !important; 
    display: flex; 
    align-items: center;
}
:deep(.glass-select-input) {
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(229, 231, 235, 0.5);
    border-radius: 0.75rem; /* rounded-xl */
    padding: 4px 8px; /* Adjustment for AD option */
    height: 46px; /* Match inputs */
}

:deep(.ant-upload-drag) {
    background: transparent !important;
    border: none !important;
}
:deep(.ant-upload-btn) {
    padding: 0 !important;
}

/* Colors */
.text-primary { color: #0f766e; }
.bg-primary { background-color: #0f766e; }
.bg-primary\/20 { background-color: rgba(15, 118, 110, 0.2); }
.bg-primary\/10 { background-color: rgba(15, 118, 110, 0.1); }
.bg-primary\/5 { background-color: rgba(15, 118, 110, 0.05); }
.hover\:bg-primary-hover:hover { background-color: #115e59; }
.bg-accent-pink\/15 { background-color: rgba(203, 213, 225, 0.22); }
.bg-accent-pink\/5 { background-color: rgba(203, 213, 225, 0.1); }
.bg-background-light { background-color: #f4f7f8; }

.shadow-glow { box-shadow: 0 12px 24px rgba(15, 118, 110, 0.2); }
.shadow-soft-diffusion { box-shadow: 0 22px 40px -18px rgba(15, 23, 42, 0.14), 0 10px 20px -14px rgba(15, 23, 42, 0.08); }
</style>
