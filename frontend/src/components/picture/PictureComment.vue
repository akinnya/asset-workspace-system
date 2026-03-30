<template>
  <div class="picture-comment">
    <h3 class="mb-6 font-bold text-lg text-slate-700 flex items-center gap-2">
      <span class="material-symbols-outlined">forum</span>
      {{ t('comment.title', { total }) }}
    </h3>
    
    <!-- Comment Input -->
    <div class="flex gap-4 mb-8">
        <a-avatar :size="48" :src="loginUserStore.loginUser.userAvatar" v-if="loginUserStore.loginUser.id" class="flex-shrink-0 shadow-sm border border-white" />
        <a-avatar :size="48" icon="user" v-else class="flex-shrink-0 bg-slate-200" />
        <div class="flex-1 relative group">
            <a-textarea 
                v-model:value="commentText" 
                :placeholder="t('comment.placeholder')" 
                :rows="3" 
                :maxLength="200"
                show-count
                class="!bg-slate-50 !border-slate-200 !rounded-xlFocus focus:!bg-white focus:!shadow-soft-diffusion transition-all !text-slate-700"
            />
            <div class="mt-3 flex justify-end">
                <a-button type="primary" class="!px-6 !rounded-full !h-9 !font-bold !bg-primary hover:!bg-primary/90 !shadow-lg shadow-primary/20" :disabled="!commentText.trim()" :loading="submitting" @click="doSubmit">
                    {{ t('comment.post') }}
                </a-button>
            </div>
        </div>
    </div>

    <!-- Comment List -->
    <div class="space-y-6">
        <div v-for="item in commentList" :key="item.id" class="group/root">
            <!-- Parent Comment -->
            <div class="flex gap-4">
                 <a-avatar :size="40" :src="item.user?.userAvatar" class="flex-shrink-0 border border-slate-100 shadow-sm cursor-pointer hover:opacity-80 transition-opacity" />
                 <div class="flex-1">
                     <!-- Header -->
                     <div class="flex items-center gap-2 mb-1">
                         <span class="font-bold text-slate-700 text-sm hover:text-primary cursor-pointer transition-colors">{{ item.user?.userName }}</span>
                         <span v-if="item.userId === props.pictureUserId" class="px-1.5 py-0.5 rounded bg-pink-50 text-pink-500 text-[10px] font-bold border border-pink-100 uppercase">{{ t('comment.author') }}</span>
                         <span class="text-xs text-slate-400 font-medium ml-auto">{{ formatTime(item.createTime) }}</span>
                     </div>
                     
                     <!-- Content -->
                     <p class="text-slate-600 text-sm leading-relaxed mb-2">{{ item.content }}</p>

                     <!-- Actions -->
                     <div class="flex items-center gap-4 text-xs font-medium text-slate-400">
                         <button class="flex items-center gap-1 hover:text-red-500 transition-colors group/like" @click="handleLike(item)">
                             <span class="material-symbols-outlined text-base group-active/like:scale-75 transition-transform" :class="{ 'text-red-500 fill-current': item.isLiked }">favorite</span>
                             <span>{{ item.likeCount ? item.likeCount : t('comment.like') }}</span>
                         </button>
                         <button class="flex items-center gap-1 hover:text-primary transition-colors" @click="handleReply(item)">
                             <span class="material-symbols-outlined text-base">chat_bubble</span>
                             <span>{{ t('comment.reply') }}</span>
                         </button>
                         <button v-if="canDelete(item)" class="flex items-center gap-1 hover:text-red-500 transition-colors ml-auto opacity-0 group-hover/root:opacity-100" @click="doDelete(item)">
                             <span class="material-symbols-outlined text-base">delete</span>
                         </button>
                     </div>

                     <!-- Reply Input (If Replying to Parent) -->
                     <div v-if="replyingTo === item.id" class="mt-4 flex gap-3 animate-fade-in">
                        <a-textarea v-model:value="replyText" :placeholder="t('comment.replyTo', { name: item.user?.userName || '' })" :rows="2" class="!bg-slate-50 !rounded-lg !text-xs" />
                        <div class="flex flex-col gap-2">
                             <a-button type="primary" size="small" class="!bg-primary" :loading="replySubmitting" @click="doSubmitReply(item.id)">{{ t('comment.reply') }}</a-button>
                             <a-button type="text" size="small" @click="replyingTo = null">{{ t('common.cancel') }}</a-button>
                        </div>
                     </div>

                     <!-- Children Comments -->
                     <div v-if="item.children && item.children.length > 0" class="mt-3 pl-4 border-l-2 border-slate-100 space-y-3">
                         <div v-for="child in item.children" :key="child.id" class="bg-slate-50/50 p-3 rounded-xl flex gap-3 group/child hover:bg-slate-50 transition-colors">
                             <a-avatar :size="24" :src="child.user?.userAvatar" class="flex-shrink-0" />
                             <div class="flex-1 min-w-0">
                                 <div class="flex items-center gap-2 mb-0.5">
                                     <span class="font-bold text-slate-700 text-xs">{{ child.user?.userName }}</span>
                                     <span v-if="child.userId === props.pictureUserId" class="text-pink-500 text-[10px] font-bold">{{ t('comment.author') }}</span>
                                     <span class="text-[10px] text-slate-400 ml-auto">{{ formatTime(child.createTime) }}</span>
                                 </div>
                                 <p class="text-slate-600 text-xs mb-1.5 break-words">
                                     <span v-if="child.parentId !== item.id" class="text-primary font-medium mr-1">{{ t('comment.replyTag') }}</span>
                                     {{ child.content }}
                                 </p>
                                 <div class="flex items-center gap-3 text-[10px] text-slate-400">
                                     <button class="hover:text-red-500 flex items-center gap-0.5" @click="handleLike(child)">
                                         <span class="material-symbols-outlined text-sm" :class="{ 'text-red-500': child.isLiked }">favorite</span>
                                         {{ child.likeCount || '' }}
                                     </button>
                                     <button class="hover:text-primary" @click="handleReply(item, child)">{{ t('comment.reply') }}</button>
                                     <button v-if="canDelete(child)" class="hover:text-red-500 ml-auto opacity-0 group-hover/child:opacity-100" @click="doDelete(child)">{{ t('common.delete') }}</button>
                                 </div>
                             </div>
                         </div>
                     </div>

                 </div>
            </div>
        </div>
    </div>
    
    <!-- Empty State -->
    <div v-if="!loading && commentList.length === 0" class="text-center py-12 text-slate-400">
        <span class="material-symbols-outlined text-4xl mb-2 text-slate-200">chat_bubble_outline</span>
        <p class="text-sm">{{ t('comment.empty') }}</p>
    </div>

    <!-- Pagination -->
    <div style="text-align: center; margin-top: 32px" v-if="total > (searchParams.pageSize || 10)">
        <a-pagination 
            v-model:current="searchParams.current" 
            :total="total" 
            :pageSize="searchParams.pageSize" 
            @change="fetchData" 
            size="small"
            show-less-items
        />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import { addCommentUsingPost, deleteCommentUsingPost, listCommentVoByPageUsingPost, doCommentLikeUsingPost } from '@/api/comment/commentController'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const props = defineProps<{
  pictureId: number
  pictureUserId?: number
}>()

const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

// State
const commentList = ref<API.CommentVO[]>([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const commentText = ref('')
const replyingTo = ref<number | null>(null) // ID of parent comment being replied to
const replyText = ref('')
const replySubmitting = ref(false)
const replyTargetUser = ref('') // Just for placeholder text

const searchParams = reactive<API.CommentQueryRequest>({
    current: 1,
    pageSize: 10,
    sortField: 'createTime',
    sortOrder: 'descend',
    pictureId: props.pictureId
})

const formatTime = (time?: string) => {
    if (!time) return ''
    return dayjs(time).fromNow()
}

const fetchData = async () => {
    loading.value = true
    try {
        const res = await listCommentVoByPageUsingPost({
            ...searchParams,
            pictureId: props.pictureId
        })
        if (res.data.code === 0 && res.data.data) {
            commentList.value = res.data.data.records ?? []
            total.value = res.data.data.total ?? 0
        }
    } catch (e: any) {
        // quiet fail
    } finally {
        loading.value = false
    }
}

const doSubmit = async () => {
    if (!loginUserStore.loginUser.id) {
        message.warning(t.value('common.loginPrompt'))
        return
    }
    if(!commentText.value.trim()) return;

    submitting.value = true
    try {
        const res = await addCommentUsingPost({
            pictureId: props.pictureId,
            content: commentText.value
        })
        if (res.data.code === 0) {
            message.success(t.value('comment.posted'))
            commentText.value = ''
            searchParams.current = 1
            fetchData()
            window.dispatchEvent(new Event('notification-refresh'))
        } else {
            message.error(t.value('comment.postFailed') + ': ' + res.data.message)
        }
    } catch(e: any) {
         message.error(t.value('comment.postFailed'))
    } finally {
        submitting.value = false
    }
}

const handleReply = (parent: API.CommentVO, targetChild?: API.CommentVO) => {
    if (!loginUserStore.loginUser.id) {
        message.warning(t.value('common.loginPrompt'))
        return
    }
    replyingTo.value = parent.id || null
    replyTargetUser.value = targetChild?.user?.userName || parent.user?.userName || ''
    // If replying to a child, we could append "@User " to replyText, or handle it via logic. 
    // Here we stick to simple thread reply.
    replyText.value = targetChild ? `@${targetChild.user?.userName} ` : ''
    
    // Auto focus logic could go here if we had a ref to the input
}

const doSubmitReply = async (parentId?: number) => {
    if (!parentId || !replyText.value.trim()) return
     
    replySubmitting.value = true
    try {
        const res = await addCommentUsingPost({
            pictureId: props.pictureId,
            content: replyText.value,
            parentId: parentId
        })
        if (res.data.code === 0) {
            message.success(t.value('comment.posted'))
            replyText.value = ''
            replyingTo.value = null
            fetchData()
            window.dispatchEvent(new Event('notification-refresh'))
        } else {
            message.error(res.data.message || t.value('comment.replyFailed'))
        }
    } catch (e) {
        message.error(t.value('comment.replyFailed'))
    } finally {
        replySubmitting.value = false
    }
}

const handleLike = async (comment: API.CommentVO) => {
    if (!loginUserStore.loginUser.id) {
       message.warning(t.value('common.loginPrompt'))
       return
    }
    if (!comment.id) return
    
    // Optimistic UI update
    const previousLike = comment.isLiked
    const previousCount = comment.likeCount || 0
    
    comment.isLiked = !previousLike
    comment.likeCount = previousLike ? previousCount - 1 : previousCount + 1

    try {
        const res = await doCommentLikeUsingPost({ commentId: comment.id })
        if (res.data.code !== 0) {
            // Revert
             comment.isLiked = previousLike
             comment.likeCount = previousCount
        }
        window.dispatchEvent(new Event('notification-refresh'))
    } catch (e) {
        comment.isLiked = previousLike
        comment.likeCount = previousCount
    }
}

const canDelete = (comment: API.CommentVO) => {
    const user = loginUserStore.loginUser
    if (!user.id) return false
    return user.id === comment.userId || user.userRole === 'admin'
}

const doDelete = async (comment: API.CommentVO) => {
    if(!comment.id) return
    try {
        const res = await deleteCommentUsingPost({ id: comment.id })
    if (res.data.code === 0) {
        message.success(t.value('comment.deleted'))
        fetchData()
    } else {
        message.error(t.value('comment.deleteFailed'))
    }
  } catch(e) {
        message.error(t.value('comment.deleteFailed'))
  }
}

watch(() => props.pictureId, () => {
    searchParams.current = 1
    fetchData()
})

onMounted(() => {
    if(props.pictureId) {
        fetchData()
    }
})

</script>

<style scoped>
.picture-comment {
    margin-top: 32px;
    padding-top: 32px;
    border-top: 1px solid rgba(0,0,0,0.05);
}

/* Custom Scrollbar for list if needed */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
