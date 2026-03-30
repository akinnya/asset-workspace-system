<template>
  <div id="addSpacePage" class="min-h-screen bg-slate-50 pb-20">
    <div class="max-w-4xl mx-auto px-6 pt-12">
        <!-- Header -->
        <div class="flex items-center gap-4 mb-10">
            <div class="p-3 bg-primary/10 rounded-2xl text-primary">
                <AppstoreOutlined class="text-2xl" />
            </div>
            <div>
                <h1 class="text-3xl font-black text-slate-900 tracking-tight">
                    {{ route.query?.id ? (t('space.editSpace') || '编辑工作区') : (t('space.createSpace') || '创建工作区') }}
                </h1>
                <p class="text-slate-500 font-medium">{{ t('space.addSubtitle') || '配置工作区信息与封面，邀请成员共同维护素材。' }}</p>
            </div>
        </div>

        <a-form layout="vertical" :model="spaceForm" @finish="handleSubmit" class="space-y-8">
            <!-- Cover Upload Hero -->
            <div class="relative group rounded-[40px] overflow-hidden bg-white border-4 border-white shadow-soft transition-all hover:shadow-2xl">
                <div class="aspect-[21/9] relative bg-slate-100 flex items-center justify-center overflow-hidden">
                    <img v-if="coverPreviewUrl" :src="coverPreviewUrl" class="w-full h-full object-cover" />
                    <div v-else class="flex flex-col items-center gap-3 text-slate-300">
                        <PictureOutlined class="text-5xl" />
                        <span class="font-bold text-sm tracking-widest uppercase">{{ t('space.uploadCover') || '上传工作区封面' }}</span>
                    </div>
                    
                    <!-- Overlay for upload -->
                    <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center backdrop-blur-sm">
                         <a-upload
                            :show-upload-list="false"
                            :custom-request="handleCoverUpload"
                            class="cover-uploader"
                        >
                            <button type="button" class="px-8 py-3 bg-white rounded-2xl text-slate-900 font-bold shadow-xl hover:scale-110 active:scale-95 transition-all">
                                {{ spaceForm.coverUrl ? (t('space.changeCover') || '更换封面') : (t('space.selectPicture') || '选择封面图片') }}
                            </button>
                        </a-upload>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                <!-- Left: Basic Info -->
                <div class="bg-white p-8 rounded-[32px] shadow-sm border border-slate-100">
                    <h3 class="text-sm font-black text-slate-400 uppercase tracking-widest mb-6 border-b border-slate-50 pb-4">{{ t('space.basicInfo') || '基础信息' }}</h3>
                    
                    <a-form-item :label="t('space.spaceName')" name="spaceName" class="custom-form-item">
                        <a-input v-model:value="spaceForm.spaceName" :placeholder="t('space.enterSpaceName')" size="large" class="!rounded-xl" />
                    </a-form-item>

                </div>

                <!-- Right: Worldview Introduction -->
                <div class="bg-white p-8 rounded-[32px] shadow-sm border border-slate-100">
                    <h3 class="text-sm font-black text-slate-400 uppercase tracking-widest mb-6 border-b border-slate-50 pb-4">{{ t('space.worldIntro') || '工作区简介' }}</h3>
                    
                    <a-form-item :label="t('space.introduction')" name="introduction" class="custom-form-item h-full">
                        <a-textarea 
                            v-model:value="spaceForm.introduction" 
                            :placeholder="t('space.enterWorldIntro') || '介绍工作区用途、协作范围或素材规范...'" 
                            :rows="6" 
                            class="!rounded-2xl !bg-slate-50 !border-transparent focus:!bg-white focus:!border-primary transition-all"
                        />
                    </a-form-item>
                </div>
            </div>

            <div class="flex justify-end pt-4">
                <a-button 
                    type="primary" 
                    html-type="submit" 
                    class="!rounded-2xl !h-14 px-12 !font-bold !text-lg shadow-glow hover:scale-105 transition-all" 
                    :loading="loading"
                >
                    {{ route.query?.id ? (t('common.update') || '保存修改') : (t('common.create') || '创建工作区') }}
                </a-button>
            </div>
        </a-form>
    </div>

    <!-- Success Share Modal -->
    <a-modal
      v-model:open="shareVisible"
      :title="t('space.shareTitle')"
      :footer="null"
      class="custom-modal"
    >
      <div class="py-6 flex flex-col items-center text-center">
        <div class="w-16 h-16 bg-emerald-100 rounded-full flex items-center justify-center text-emerald-500 mb-4 animate-bounce">
            <CheckOutlined class="text-3xl" />
        </div>
        <h3 class="text-xl font-black text-slate-800 mb-2">{{ t('common.actionSuccess') }}</h3>
        <p class="text-slate-500 mb-6">{{ t('space.shareTip') || '工作区已创建，邀请成员一起加入协作。' }}</p>
        
        <div class="w-full space-y-4">
            <div class="bg-slate-50 p-4 rounded-2xl border border-slate-100 text-left">
                <span class="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-1">{{ t('space.idLabel') }}</span>
                <div class="flex items-center justify-between">
                    <span class="font-mono font-bold text-slate-700">{{ createdSpaceId }}</span>
                    <button @click="copyText(String(createdSpaceId))" class="text-primary hover:text-primary-hover font-bold text-xs uppercase tracking-wider">{{ t('space.copyId') }}</button>
                </div>
            </div>
            <div class="bg-slate-50 p-4 rounded-2xl border border-slate-100 text-left">
                <span class="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-1">邀请链接</span>
                <div class="flex items-center justify-between">
                    <span class="text-xs font-medium text-slate-500 truncate mr-4">{{ shareUrl }}</span>
                    <button @click="copyText(shareUrl)" class="text-primary hover:text-primary-hover font-bold text-xs uppercase tracking-wider">{{ t('space.copyLink') }}</button>
                </div>
            </div>
        </div>
        
        <button class="mt-8 w-full py-3 bg-slate-900 text-white rounded-2xl font-bold hover:bg-slate-800 transition-all" @click="goMySpaces">
            {{ t('space.viewAll') || '前往我的工作区' }}
        </button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  addSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost
} from '@/api/workspace/workspaceController'
import { uploadFileUsingPost } from '@/api/file/fileController'
import { message } from 'ant-design-vue'
import { 
    AppstoreOutlined, PictureOutlined, LoadingOutlined,
    CloudUploadOutlined, InfoCircleOutlined, CheckOutlined
} from '@ant-design/icons-vue'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const coverPreviewUrl = ref('')
const localCoverObjectUrl = ref<string | null>(null)
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const spaceForm = reactive<API.SpaceAddRequest | any>({
    spaceName: '',
    spaceLevel: 0,
    introduction: '',
    coverUrl: ''
})
const spaceLevelOptions = ref<any[]>([])
const shareVisible = ref(false)
const createdSpaceId = ref<number | string>('')
const shareUrl = ref('')

const copyText = (text: string) => {
    navigator.clipboard.writeText(text)
    message.success(t.value('common.copySuccess') || '已复制到剪贴板')
}

const goMySpaces = () => {
    shareVisible.value = false
    router.push('/workspace/my')
}

const resetLocalCoverPreview = () => {
    if (localCoverObjectUrl.value) {
        URL.revokeObjectURL(localCoverObjectUrl.value)
        localCoverObjectUrl.value = null
    }
}

// 获取空间级别
const fetchSpaceLevel = async () => {
    try {
        const res = await listSpaceLevelUsingGet();
        if (res.data.code === 0 && res.data.data && res.data.data.length > 0) {
            spaceLevelOptions.value = res.data.data.map((level: any) => ({
                label: level.text,
                value: level.value
            }))
        }
    } catch (e) {
        // silent fail
    }
}

// 获取老数据
const getOldSpace = async () => {
  const id = route.query?.id
  if (id) {
    const res = await getSpaceVoByIdUsingGet({
      id: id as any,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      Object.assign(spaceForm, {
          spaceName: data.spaceName,
          spaceLevel: data.spaceLevel,
          introduction: data.introduction,
          coverUrl: data.coverUrl
      })
      resetLocalCoverPreview()
      coverPreviewUrl.value = data.coverUrl || ''
    }
  }
}

const handleCoverUpload = async ({ file }: any) => {
    const uploadFile = file?.originFileObj ?? file
    const isJpgOrPng = uploadFile?.type === 'image/jpeg'
        || uploadFile?.type === 'image/png'
        || uploadFile?.type === 'image/webp'
    if (!isJpgOrPng) {
        message.error(t.value('common.invalidFormat'))
        return
    }
    const isLt5M = uploadFile?.size / 1024 / 1024 < 5
    if (!isLt5M) {
        message.error(t.value('common.imageSizeLimit', { size: '5MB' }) || '封面不能超过 5MB')
        return
    }

    try {
        const res = await uploadFileUsingPost({ biz: 'space-cover' }, uploadFile)
        if (res.data.code === 0 && res.data.data) {
            spaceForm.coverUrl = res.data.data
            // 预览优先使用本地对象 URL，避免私有 OSS 地址在创建阶段未签名导致裂图
            if (uploadFile instanceof File) {
                resetLocalCoverPreview()
                localCoverObjectUrl.value = URL.createObjectURL(uploadFile)
                coverPreviewUrl.value = localCoverObjectUrl.value
            } else {
                coverPreviewUrl.value = res.data.data
            }
            message.success(t.value('common.uploadSuccess'))
        } else {
            message.error(t.value('common.uploadFailed'))
        }
    } catch (e) {
        message.error(t.value('common.uploadFailed'))
    }
}

onMounted(() => {
  fetchSpaceLevel()
  getOldSpace()
})

onBeforeUnmount(() => {
    resetLocalCoverPreview()
})

const handleSubmit = async () => {
  if (!spaceForm.spaceName) {
      message.warning(t.value('space.enterSpaceName'))
      return
  }
  
  loading.value = true
  const id = route.query?.id
  try {
    let res
    const params = { ...spaceForm }
    if (id) {
      res = await updateSpaceUsingPost({
        id: id as any,
        ...params,
      })
    } else {
      res = await addSpaceUsingPost(params)
    }

    if (res.data.code === 0) {
      if (id) {
          message.success(t.value('common.actionSuccess'))
          router.push('/workspace/my')
      } else {
          // Success creation: show share modal
          const newSpace = res.data.data as any
          createdSpaceId.value = (newSpace && typeof newSpace === 'object') ? (newSpace.idStr || newSpace.id) : newSpace || ''
          shareUrl.value = createdSpaceId.value ? `${window.location.origin}/workspace/detail/${createdSpaceId.value}` : ''
          if (createdSpaceId.value) {
            copyText(String(createdSpaceId.value))
          }
          shareVisible.value = true
      }
    } else {
      message.error(res.data.message || t.value('common.actionFailed'))
    }
  } catch (e: any) {
    message.error(t.value('common.actionFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.shadow-soft {
    box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.05);
}
.shadow-glow {
    box-shadow: 0 8px 30px rgba(109, 174, 239, 0.4);
}
:deep(.ant-input), :deep(.ant-input-affix-wrapper), :deep(.ant-textarea) {
    border-color: #f1f5f9;
}
:deep(.ant-input:hover), :deep(.ant-input:focus), :deep(.ant-textarea:focus) {
    border-color: var(--primary-color);
}
:deep(.ant-form-item-label label) {
    font-weight: 700;
    color: #475569;
    font-size: 13px;
}
</style>
