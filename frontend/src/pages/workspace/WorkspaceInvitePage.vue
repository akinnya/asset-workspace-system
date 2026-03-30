<template>
  <div id="spaceInvitePage" class="min-h-screen bg-slate-50 flex items-center justify-center p-6">
    <div class="max-w-md w-full bg-white rounded-[32px] shadow-xl border border-white p-6 sm:p-10 text-center relative overflow-hidden">
      <div class="absolute top-0 right-0 w-64 h-64 bg-primary/5 rounded-full blur-3xl pointer-events-none"></div>

      <div v-if="loading" class="py-12">
        <a-spin size="large" />
        <p class="mt-4 text-slate-400 font-medium">{{ t('space.invite.loading') }}</p>
      </div>

      <div v-else-if="error" class="py-12">
        <div class="w-16 h-16 bg-rose-50 rounded-2xl flex items-center justify-center text-rose-500 mx-auto mb-6">
          <CloseOutlined class="text-3xl" />
        </div>
        <h2 class="text-2xl font-bold text-slate-800 mb-2">{{ t('space.invite.invalid') }}</h2>
        <p class="text-slate-500 mb-8">{{ error }}</p>
        <a-button type="primary" size="large" shape="round" @click="router.push('/')">{{ t('space.invite.backHome') }}</a-button>
      </div>

      <div v-else class="relative z-10">
        <div class="w-24 h-24 rounded-[32px] bg-gradient-to-br from-primary to-purple-500 flex items-center justify-center text-white font-black text-4xl shadow-glow mx-auto mb-8">
          {{ space.spaceName?.charAt(0).toUpperCase() }}
        </div>

        <h1 class="text-2xl sm:text-3xl font-black text-slate-800 tracking-tight mb-3">{{ titleText }}</h1>
        <p class="text-slate-500 text-base sm:text-lg mb-6 leading-relaxed">
          {{ descriptionText }}
        </p>

        <div class="bg-slate-50 rounded-2xl p-4 mb-6 flex items-center gap-4 text-left">
          <a-avatar :src="inviteInviter?.userAvatar" :size="48" class="shadow-sm" />
          <div>
            <p class="text-xs font-bold text-slate-400 uppercase tracking-widest leading-none mb-1">
              {{ t('space.invite.invitedBy') }}
            </p>
            <p class="text-slate-700 font-bold">@{{ inviteInviter?.userName || t('common.unknown') }}</p>
          </div>
        </div>

        <div v-if="!hasPendingInvite" class="mb-8 rounded-2xl bg-amber-50 border border-amber-100 px-4 py-3 text-sm text-amber-700 font-medium">
          {{ t('space.invite.noPendingTip') }}
        </div>

        <div class="flex flex-col gap-3">
          <a-button
            type="primary"
            size="large"
            block
            shape="round"
            class="!font-bold shadow-glow"
            :loading="submitting"
            @click="doPrimaryAction"
          >
            {{ primaryActionText }}
          </a-button>
          <a-button
            v-if="hasPendingInvite"
            danger
            size="large"
            block
            shape="round"
            class="!font-bold"
            :loading="rejecting"
            @click="doRejectInvite"
          >
            {{ t('space.invite.reject') }}
          </a-button>
          <a-button type="text" block class="text-slate-400 hover:text-slate-600" @click="router.push('/')">
            {{ t('space.invite.later') }}
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { CloseOutlined } from '@ant-design/icons-vue'
import { storeToRefs } from 'pinia'
import { getSpaceVoByIdUsingGet } from '@/api/workspace/workspaceController'
import {
  getCurrentSpaceInviteUsingGet,
  joinSpaceUsingPost,
  respondSpaceInviteUsingPost,
} from '@/api/workspace/workspaceUserController'
import { useLanguageStore } from '@/stores/system/useLanguageStore'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const rejecting = ref(false)
const space = ref<API.SpaceVO>({})
const inviteRequest = ref<API.SpaceJoinRequestVO>()
const error = ref('')

const hasPendingInvite = computed(() => {
  return inviteRequest.value?.status === 0 && inviteRequest.value?.requestType === 1
})

const inviteInviter = computed(() => {
  return inviteRequest.value?.inviter || space.value.user
})

const titleText = computed(() => {
  return hasPendingInvite.value ? t.value('space.invite.joinTitle') : t.value('space.invite.noPendingTitle')
})

const descriptionText = computed(() => {
  return hasPendingInvite.value
    ? t.value('space.invite.inviteTip', { name: space.value.spaceName })
    : t.value('space.invite.inviteMissing', { name: space.value.spaceName })
})

const primaryActionText = computed(() => {
  return hasPendingInvite.value ? t.value('space.invite.accept') : t.value('space.invite.applyJoin')
})

const fetchInvite = async (spaceId: string | number) => {
  try {
    const res = await getCurrentSpaceInviteUsingGet({ spaceId: spaceId as any })
    if (res.data.code === 0) {
      inviteRequest.value = res.data.data || undefined
    }
  } catch {
    inviteRequest.value = undefined
  }
}

const fetchData = async () => {
  const rawSpaceId = route.params.id
  const spaceId = Array.isArray(rawSpaceId) ? rawSpaceId[0] : rawSpaceId
  if (!spaceId) {
    error.value = t.value('space.invite.missingId')
    loading.value = false
    return
  }
  try {
    const res = await getSpaceVoByIdUsingGet({ id: spaceId as any })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
      await fetchInvite(spaceId)
    } else {
      error.value = res.data.message || t.value('space.invite.notFound')
    }
  } catch {
    error.value = t.value('space.invite.networkError')
  } finally {
    loading.value = false
  }
}

const doPrimaryAction = async () => {
  if (!space.value.id) return
  submitting.value = true
  try {
    if (hasPendingInvite.value) {
      const res = await respondSpaceInviteUsingPost({
        spaceId: space.value.id,
        accept: true,
      })
      if (res.data.code === 0) {
        message.success(t.value('space.invite.welcome'))
        router.push(`/workspace/detail/${space.value.id}`)
        return
      }
      message.error(res.data.message || t.value('space.invite.joinFailed'))
      return
    }

    const res = await joinSpaceUsingPost({
      spaceId: space.value.id,
    })
    if (res.data.code === 0) {
      message.success(t.value('space.invite.requestSent'))
      await fetchInvite(space.value.id)
    } else {
      message.error(res.data.message || t.value('space.invite.joinFailed'))
    }
  } catch (e: any) {
    message.error(t.value('space.invite.joinFailed') + ': ' + (e?.message || ''))
  } finally {
    submitting.value = false
  }
}

const doRejectInvite = async () => {
  if (!space.value.id || !hasPendingInvite.value) return
  rejecting.value = true
  try {
    const res = await respondSpaceInviteUsingPost({
      spaceId: space.value.id,
      accept: false,
    })
    if (res.data.code === 0) {
      message.success(t.value('space.invite.rejected'))
      router.push('/')
    } else {
      message.error(res.data.message || t.value('space.invite.rejectFailed'))
    }
  } catch (e: any) {
    message.error(t.value('space.invite.rejectFailed') + ': ' + (e?.message || ''))
  } finally {
    rejecting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#spaceInvitePage {
  background: radial-gradient(circle at top right, #f0f9ff 0%, #f8fafc 100%);
}
</style>
