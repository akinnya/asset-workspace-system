<template>
  <div class="upload-batch-page font-display bg-background-light text-slate-900 min-h-screen flex flex-col relative overflow-x-hidden">
    <div class="fixed top-[-10%] left-[15%] w-[240px] h-[240px] sm:w-[360px] sm:h-[360px] lg:w-[460px] lg:h-[460px] bg-primary/15 rounded-full blur-[120px] pointer-events-none z-0"></div>
    <div class="fixed bottom-[-10%] right-[-5%] w-[260px] h-[260px] sm:w-[380px] sm:h-[380px] lg:w-[520px] lg:h-[520px] bg-accent-pink/10 rounded-full blur-[110px] pointer-events-none z-0"></div>

    <div class="relative z-10 flex-1 w-full max-w-4xl mx-auto p-6 pb-20">
      <div class="flex flex-col md:flex-row justify-between items-end mb-8 gap-4">
        <div>
          <h1 class="text-3xl font-bold text-slate-900 mb-2">{{ t('batch.title') }}</h1>
          <p class="text-slate-500">{{ t('batch.subtitle') }}</p>
        </div>
        <button class="px-4 py-2.5 rounded-xl bg-white/50 text-slate-600 font-medium hover:bg-white transition-colors border border-transparent hover:border-slate-200" @click="router.back()">
          {{ t('picture.cancel') }}
        </button>
      </div>

      <div class="glass-panel rounded-3xl p-8 shadow-soft-diffusion border border-white/60">
        <a-form layout="vertical" :model="formData" @finish="handleSubmit">
          <a-form-item :label="t('batch.keyword')" name="searchText">
            <a-input v-model:value="formData.searchText" :placeholder="t('batch.keywordPlaceholder')" />
          </a-form-item>
          <a-form-item :label="t('batch.count')" name="count">
            <a-input-number
              v-model:value="formData.count"
              :placeholder="t('batch.countPlaceholder')"
              style="min-width: 180px"
              :min="1"
              :max="30"
              allow-clear
            />
          </a-form-item>
          <a-form-item :label="t('batch.namePrefix')" name="namePrefix">
            <a-input v-model:value="formData.namePrefix" :placeholder="t('batch.namePrefixPlaceholder')" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
              {{ t('batch.submit') }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <div v-if="previewItems.length" class="mt-8">
        <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4 mb-4">
          <div>
            <h2 class="text-2xl font-bold text-slate-900">{{ t('batch.previewTitle') }}</h2>
            <p class="text-slate-500 text-sm mt-1">{{ t('batch.previewHint') }}</p>
          </div>
          <div class="flex items-center gap-2">
            <button
              class="px-3 py-2 rounded-xl bg-white/70 text-slate-600 font-medium hover:bg-white transition-colors border border-slate-200"
              @click="toggleSelectAll"
            >
              {{ isAllSelected ? t('batch.selectNone') : t('batch.selectAll') }}
            </button>
            <button
              class="px-3 py-2 rounded-xl bg-white/70 text-slate-600 font-medium hover:bg-white transition-colors border border-slate-200"
              @click="clearSelection"
            >
              {{ t('batch.clearSelection') }}
            </button>
          </div>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          <div
            v-for="item in previewItems"
            :key="item.previewId"
            class="preview-card group rounded-2xl border border-white/60 bg-white/70 overflow-hidden shadow-soft-diffusion transition-all duration-300 cursor-pointer"
            :class="selectedIds.includes(item.previewId || '') ? 'ring-2 ring-primary/60 shadow-lg' : ''"
            @click="toggleSelection(item.previewId)"
          >
            <div class="relative aspect-[4/3] bg-slate-100 overflow-hidden">
              <img
                :src="item.thumbnailUrl || item.url"
                v-img-fallback="item.url"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                :alt="item.picName || 'preview'"
              />
              <div class="absolute top-3 right-3">
                <input
                  type="checkbox"
                  class="w-5 h-5 accent-primary"
                  :checked="selectedIds.includes(item.previewId || '')"
                  @click.stop="toggleSelection(item.previewId)"
                />
              </div>
            </div>
            <div class="p-3">
              <div class="font-semibold text-slate-800 truncate">{{ item.picName || t('batch.unnamed') }}</div>
              <div class="text-xs text-slate-500 mt-1">
                {{ item.picWidth || 0 }} × {{ item.picHeight || 0 }}
              </div>
            </div>
          </div>
        </div>

        <div class="mt-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div class="text-sm text-slate-500">
            {{ t('batch.selectedCount')
              .replace('{selected}', String(selectedIds.length))
              .replace('{total}', String(previewItems.length)) }}
          </div>
          <div class="flex items-center gap-3">
            <button
              class="px-4 py-2.5 rounded-xl bg-white/80 text-slate-600 font-semibold border border-slate-200 hover:bg-white transition-colors"
              :disabled="discarding"
              @click="discardBatch(false)"
            >
              {{ t('batch.discard') }}
            </button>
            <button
              class="px-4 py-2.5 rounded-xl bg-primary text-white font-semibold shadow-glow hover:bg-primary-hover transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="confirming || selectedIds.length === 0"
              @click="confirmBatch"
            >
              {{ confirming ? t('batch.confirming') : t('batch.confirm') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { confirmUploadPictureByBatchUsingPost, discardUploadPictureBatchUsingPost, uploadPictureByBatchUsingPost } from '@/api/asset/assetController'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const formData = reactive<API.PictureUploadByBatchRequest>({
  count: 10,
})
const loading = ref(false)
const confirming = ref(false)
const discarding = ref(false)
const previewItems = ref<API.PictureBatchPreviewItemVO[]>([])
const previewBatchId = ref('')
const selectedIds = ref<string[]>([])

const router = useRouter()
const route = useRoute()

const handleSubmit = async () => {
  loading.value = true
  try {
    if (previewBatchId.value) {
      await discardBatch(true)
    }
    const res = await uploadPictureByBatchUsingPost({
      ...formData,
    })
    if (res.data.code === 0 && res.data.data) {
      previewBatchId.value = res.data.data.batchId || ''
      previewItems.value = res.data.data.items || []
      selectedIds.value = previewItems.value.map((item) => item.previewId || '').filter(Boolean)
      message.success(
        t.value('batch.previewSuccess').replace('{count}', String(previewItems.value.length)),
      )
    } else {
      message.error(t.value('batch.error') + (res.data.message ? `：${res.data.message}` : ''))
    }
  } finally {
    loading.value = false
  }
}

const toggleSelection = (id?: string) => {
  if (!id) return
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const clearSelection = () => {
  selectedIds.value = []
}

const isAllSelected = computed(() => {
  return previewItems.value.length > 0 && selectedIds.value.length === previewItems.value.length
})

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = previewItems.value.map((item) => item.previewId || '').filter(Boolean)
  }
}

const confirmBatch = async () => {
  if (!previewBatchId.value || confirming.value) return
  if (selectedIds.value.length === 0) {
    message.warning(t.value('batch.noSelection'))
    return
  }
  confirming.value = true
  try {
    const res = await confirmUploadPictureByBatchUsingPost({
      batchId: previewBatchId.value,
      previewIds: selectedIds.value,
    })
    if (res.data.code === 0) {
      message.success(
        t.value('batch.confirmSuccess').replace('{count}', String(selectedIds.value.length)),
      )
      resetPreviewState()
    } else {
      message.error(res.data.message || t.value('batch.error'))
    }
  } finally {
    confirming.value = false
  }
}

const discardBatch = async (silent: boolean) => {
  if (!previewBatchId.value || discarding.value) return
  discarding.value = true
  try {
    const res = await discardUploadPictureBatchUsingPost({ batchId: previewBatchId.value })
    if (!silent) {
      if (res.data.code === 0) {
        message.success(t.value('batch.discardSuccess'))
      } else {
        message.error(res.data.message || t.value('batch.error'))
      }
    }
  } finally {
    resetPreviewState()
    discarding.value = false
  }
}

const resetPreviewState = () => {
  previewBatchId.value = ''
  previewItems.value = []
  selectedIds.value = []
}

onMounted(() => {
  if (!loginUserStore.loginUser.id) {
    message.warning(t.value('common.loginPrompt'))
    router.push('/user/login?redirect=' + route.fullPath)
  }
})

onBeforeRouteLeave(() => {
  if (previewBatchId.value) {
    discardBatch(true)
  }
})

onBeforeUnmount(() => {
  if (previewBatchId.value) {
    discardBatch(true)
  }
})
</script>

<style scoped>
.glass-panel {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
}
</style>
