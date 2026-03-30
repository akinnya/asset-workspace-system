<template>
  <div id="recycle-bin-page">
    <div class="header">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h2><delete-outlined /> {{ t('recycleBin.title') }}</h2>
            <div class="desc">{{ t('recycleBin.subtitle') }}</div>
          </div>
          <a-button v-if="dataList.length > 0" type="primary" danger class="w-full sm:w-auto" @click="doEmptyTrash" :loading="clearing">
             {{ t('common.clearAll') }}
          </a-button>
      </div>
    </div>

    <!-- 列表展示 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-6 gap-6">
      <div v-for="item in dataList" :key="item.id" class="glass-panel p-4 rounded-3xl hover:-translate-y-1 transition-all">
        <div class="aspect-square rounded-2xl overflow-hidden mb-4 relative group">
           <img :src="item.thumbnailUrl || item.url" v-img-fallback="item.url" class="w-full h-full object-cover" />
           <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-3">
              <a-button type="primary" shape="circle" @click="doRecover(item)">
                <template #icon><undo-outlined /></template>
              </a-button>
              <a-popconfirm :title="t('recycleBin.confirmDelete')" @confirm="doDelete(item)">
                <a-button danger shape="circle">
                    <template #icon><delete-outlined /></template>
                </a-button>
              </a-popconfirm>
           </div>
        </div>
        <div class="text-sm font-bold text-slate-800 truncate mb-1">{{ item.name }}</div>
        <div class="text-[10px] text-slate-400 font-medium uppercase tracking-wider">{{ formatDate(item.editTime) }}</div>
      </div>
    </div>
    <div v-if="dataList.length === 0 && !loading" class="py-20 text-center">
        <a-empty :description="t('common.empty')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { listDeletedPicturesUsingGet, recoverPictureUsingPost, deletePictureUsingPost, clearRecycleBinUsingPost } from '@/api/asset/assetController';
import { message } from 'ant-design-vue';
import { DeleteOutlined, UndoOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const dataList = ref<API.PictureVO[]>([]);
const loading = ref(false);

const loadData = async () => {
  loading.value = true;
  try {
    const res = await listDeletedPicturesUsingGet();
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data;
    } else {
      message.error(t.value('recycleBin.loadFailed') + ': ' + res.data.message);
    }
  } catch (e: any) {
    message.error(t.value('recycleBin.loadFailed') + ': ' + e.message);
  } finally {
    loading.value = false;
  }
};

const doRecover = async (item: API.PictureVO) => {
    try {
        const res = await recoverPictureUsingPost({ id: item.id });
        if (res.data.code === 0) {
            message.success(t.value('recycleBin.recoverSuccess'));
            loadData(); // 刷新
        } else {
            message.error(t.value('recycleBin.recoverFailed') + ': ' + res.data.message);
        }
    } catch (e: any) {
        message.error(t.value('recycleBin.recoverFailed') + ': ' + e.message);
    }
};

const doDelete = async (record: API.PictureVO) => {
  if (!record.id) return
  const res = await deletePictureUsingPost({ id: record.id })
  if (res.data.code === 0) {
    message.success(t.value('recycleBin.deleteSuccess'))
    loadData()
  } else {
    message.error(t.value('recycleBin.deleteFailed'))
  }
}

const clearing = ref(false)
const doEmptyTrash = async () => {
    if (dataList.value.length === 0) return
    clearing.value = true
    try {
        // Collect all IDs
        const ids = dataList.value.map(item => item.id).filter(id => id !== undefined) as number[]
        if (ids.length === 0) return

        const res = await clearRecycleBinUsingPost({ idList: ids })
        if (res.data.code === 0) {
            message.success(t.value('recycleBin.emptySuccess'))
            loadData()
        } else {
            message.error(t.value('recycleBin.emptyFailed') + ': ' + res.data.message)
        }
    } catch (e: any) {
        message.error(t.value('recycleBin.emptyFailed') + ': ' + e.message)
    } finally {
        clearing.value = false
    }
}

const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-'
    return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

const formatTime = (time: string | undefined) => {
    return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '未知时间';
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
#recycle-bin-page {
  padding: 16px;
}

@media (min-width: 768px) {
  #recycle-bin-page {
    padding: 24px;
  }
}

.header {
  margin-bottom: 24px;
}

.header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.desc {
    color: #888;
}

.recycle-card {
    border-radius: 8px;
    overflow: hidden;
}
</style>
