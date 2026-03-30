<template>
  <div class="space-y-6">
    <!-- Top Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
       <!-- Total Users -->
       <div class="group bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion">
           <div class="flex justify-between items-start mb-4">
               <div class="w-12 h-12 rounded-2xl bg-primary/10 text-primary flex items-center justify-center">
                   <span class="material-symbols-outlined">group</span>
               </div>
               <div v-if="(stats?.todayNewUsers ?? 0) > 0" class="flex items-center gap-1 bg-emerald-100 text-emerald-600 px-2 py-1 rounded-full text-xs font-semibold">
                   <span class="material-symbols-outlined text-[14px]">trending_up</span>
                   <span>{{ t('admin.home.todayIncrease', { count: stats?.todayNewUsers }) }}</span>
               </div>
           </div>
           <h3 class="text-slate-500 text-sm font-medium">{{ t('admin.home.totalUsers') }}</h3>
           <p class="text-3xl font-bold text-slate-800 mt-1 tracking-tight">{{ stats?.totalUsers }}</p>
       </div>
       
       <!-- Total Uploads -->
       <div class="group bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion">
           <div class="flex justify-between items-start mb-4">
               <div class="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-500 flex items-center justify-center">
                   <span class="material-symbols-outlined">cloud_upload</span>
               </div>
               <div v-if="(stats?.todayNewPictures ?? 0) > 0" class="flex items-center gap-1 bg-emerald-100 text-emerald-600 px-2 py-1 rounded-full text-xs font-semibold">
                   <span class="material-symbols-outlined text-[14px]">trending_up</span>
                   <span>{{ t('admin.home.todayIncrease', { count: stats?.todayNewPictures }) }}</span>
               </div>
           </div>
           <h3 class="text-slate-500 text-sm font-medium">{{ t('admin.home.platformPictures') }}</h3>
           <p class="text-3xl font-bold text-slate-800 mt-1 tracking-tight">{{ stats?.totalPictures }}</p>
       </div>

       <!-- Total Views -->
       <div class="group bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion">
           <div class="flex justify-between items-start mb-4">
               <div class="w-12 h-12 rounded-2xl bg-amber-50 text-amber-500 flex items-center justify-center">
                   <span class="material-symbols-outlined">visibility</span>
               </div>
           </div>
           <h3 class="text-slate-500 text-sm font-medium">{{ t('admin.home.totalViews') }}</h3>
           <p class="text-3xl font-bold text-slate-800 mt-1 tracking-tight">-</p>
       </div>
    </div>

    <!-- Mail Health -->
    <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 sm:p-6 border border-white/60 shadow-soft-diffusion">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
            <div>
                <h3 class="font-bold text-slate-800 text-lg">{{ t('admin.home.mailHealthTitle') }}</h3>
                <p class="text-xs text-slate-500 mt-0.5">{{ t('admin.home.mailHealthDesc') }}</p>
            </div>
            <div
                class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-semibold"
                :class="mailHealthReady ? 'bg-emerald-100 text-emerald-600' : 'bg-amber-100 text-amber-700'"
            >
                <span class="material-symbols-outlined text-[14px]">
                    {{ mailHealthReady ? 'verified' : 'warning' }}
                </span>
                <span>{{ mailHealthReady ? t('admin.home.mailReady') : t('admin.home.mailNotReady') }}</span>
            </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-5">
            <div class="rounded-xl bg-slate-50/80 border border-slate-100 p-4">
                <p class="text-xs text-slate-500 mb-1">{{ t('admin.home.mailEndpoint') }}</p>
                <p class="text-sm font-semibold text-slate-700 break-all">
                    {{ mailHealth?.endpoint || '-' }}
                </p>
                <p class="text-xs text-slate-500 mt-2">
                    {{ t('admin.home.mailRegion') }}：{{ mailHealth?.regionId || '-' }}
                </p>
            </div>
            <div class="rounded-xl bg-slate-50/80 border border-slate-100 p-4">
                <p class="text-xs text-slate-500 mb-1">{{ t('admin.home.mailAccount') }}</p>
                <p class="text-sm font-semibold text-slate-700 break-all">
                    {{ mailHealth?.accountName || '-' }}
                </p>
                <p class="text-xs text-slate-500 mt-2">
                    Alias：{{ mailHealth?.fromAlias || '-' }}
                </p>
            </div>
            <div class="rounded-xl bg-slate-50/80 border border-slate-100 p-4">
                <p class="text-xs text-slate-500 mb-1">{{ t('admin.home.mailTemplate') }}</p>
                <p class="text-sm font-semibold text-slate-700 break-all">
                    {{ mailHealth?.verificationTemplateId || '-' }}
                </p>
                <p class="text-xs text-slate-500 mt-2">
                    Test：{{ mailHealth?.testTemplateId || '-' }}
                </p>
            </div>
        </div>

        <div v-if="(mailHealth?.issues?.length ?? 0) > 0" class="mt-4 p-4 rounded-xl border border-amber-200 bg-amber-50/70">
            <p class="text-xs font-semibold text-amber-700 mb-2">{{ t('admin.home.mailIssueList') }}</p>
            <ul class="space-y-1 text-xs text-amber-700">
                <li v-for="(issue, index) in mailHealth?.issues" :key="`mail-issue-${index}`">
                    {{ issue }}
                </li>
            </ul>
        </div>

        <div class="mt-4 p-4 rounded-xl border border-slate-200 bg-white/70">
            <p class="text-xs font-semibold text-slate-500 mb-2">{{ t('admin.home.mailTestEmail') }}</p>
            <div class="flex flex-col sm:flex-row gap-3">
                <input
                    v-model="mailTestEmail"
                    type="email"
                    class="flex-1 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-primary"
                    :placeholder="t('admin.home.mailTestEmailPlaceholder')"
                />
                <button
                    class="px-4 py-2 rounded-xl bg-primary text-white text-sm font-semibold disabled:opacity-60 disabled:cursor-not-allowed"
                    :disabled="mailTestSending"
                    @click="sendTestMail"
                >
                    {{ mailTestSending ? t('admin.home.mailTestSending') : t('admin.home.mailTestSend') }}
                </button>
            </div>
        </div>
    </div>

    <!-- Recent Uploads -->
    <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 sm:p-6 border border-white/60 shadow-soft-diffusion min-h-[400px]">
        <div class="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-6">
            <div>
            <h3 class="font-bold text-slate-800 text-lg">{{ t('admin.home.recentUploads') }}</h3>
            <p class="text-xs text-slate-500 mt-0.5">{{ t('admin.home.recentUploadsDesc') }}</p>
        </div>
        <router-link to="/admin/assets" class="text-primary hover:text-primary-hover text-sm font-semibold hover:underline flex items-center gap-1 w-full sm:w-auto justify-start sm:justify-end">
                {{ t('admin.home.manageAll') }} <span class="material-symbols-outlined text-sm">arrow_forward</span>
        </router-link>
    </div>
        
        <div v-if="loading" class="flex justify-center py-20">
            <a-spin :tip="t('admin.home.fetchingData')" />
        </div>
        
        <div v-else class="text-center py-16 text-slate-400">
            <div class="flex flex-col items-center">
               <span class="material-symbols-outlined text-4xl mb-2 text-slate-200">dashboard</span>
               <p>{{ t('admin.home.visitManageTip') }}</p>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getAdminMailHealthUsingGet, getAdminStatsUsingGet, sendAdminMailTestUsingPost } from '@/api/admin/adminController'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)
const loginUserStore = useLoginUserStore()

const loading = ref(true)
const mailTestSending = ref(false)
const mailTestEmail = ref('')
const stats = ref<API.AdminStatsVO>({
    totalUsers: 0,
    todayNewUsers: 0,
    totalPictures: 0,
    todayNewPictures: 0,
})
const mailHealth = ref<API.MailHealthStatusVO>({
    ready: false,
    accessKeyConfigured: false,
    regionConfigured: false,
    endpointConfigured: false,
    accountNameConfigured: false,
    verificationTemplateConfigured: false,
    testTemplateConfigured: false,
    fromAliasConfigured: false,
    maskedAccessKeyId: '',
    regionId: '',
    endpoint: '',
    accountName: '',
    verificationTemplateId: '',
    testTemplateId: '',
    fromAlias: '',
    issues: [],
})
const mailHealthReady = computed(() => mailHealth.value.ready === true)

const fetchAll = async () => {
    loading.value = true
    try {
        const [statsRes, mailRes] = await Promise.all([
            getAdminStatsUsingGet(),
            getAdminMailHealthUsingGet(),
        ])

        if (statsRes.data.code === 0 && statsRes.data.data) {
            stats.value = statsRes.data.data
        }
        if (mailRes.data.code === 0 && mailRes.data.data) {
            mailHealth.value = mailRes.data.data
        }
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

const sendTestMail = async () => {
    if (mailTestSending.value) return
    mailTestSending.value = true
    try {
        const email = mailTestEmail.value.trim()
        const res = await sendAdminMailTestUsingPost({
            email: email || undefined,
        })
        if (res.data.code === 0) {
            message.success(t.value('admin.home.mailTestSuccess'))
            await fetchAll()
            return
        }
        message.error(res.data.message || t.value('common.actionFailed'))
    } catch (e) {
        message.error(t.value('common.requestFailed'))
    } finally {
        mailTestSending.value = false
    }
}

onMounted(() => {
    mailTestEmail.value = loginUserStore.loginUser.userAccount || ''
    fetchAll()
})
</script>

<style scoped>
/* Dashboard Styles */
</style>
