<template>
  <div>
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
        <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
            <div class="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-500 flex items-center justify-center">
                <span class="material-symbols-outlined">group</span>
            </div>
            <div>
                <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.user.totalUsers') }}</p>
                <p class="text-2xl font-bold text-slate-800">{{ total }}</p>
            </div>
        </div>
        <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
            <div class="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-500 flex items-center justify-center">
                <span class="material-symbols-outlined">verified</span>
            </div>
            <div>
                <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.user.admins') }}</p>
                <p class="text-2xl font-bold text-slate-800">{{ stats.admins }}</p>
            </div>
        </div>
        <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
            <div class="w-12 h-12 rounded-2xl bg-purple-50 text-purple-500 flex items-center justify-center">
                <span class="material-symbols-outlined">collections</span>
            </div>
            <div>
                <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.user.users') }}</p>
                <p class="text-2xl font-bold text-slate-800">{{ stats.users }}</p>
            </div>
        </div>
        <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-5 border border-white/60 shadow-soft-diffusion flex items-center gap-4">
            <div class="w-12 h-12 rounded-2xl bg-rose-50 text-rose-500 flex items-center justify-center">
                <span class="material-symbols-outlined">block</span>
            </div>
            <div>
                <p class="text-slate-500 text-xs font-medium uppercase tracking-wider">{{ t('admin.user.banned') }}</p>
                <p class="text-2xl font-bold text-slate-800">{{ stats.banned }}</p>
            </div>
        </div>
    </div>

    <!-- User Table -->
    <div class="bg-white/60 backdrop-blur-md rounded-[20px] p-6 border border-white/60 shadow-soft-diffusion min-h-[360px] sm:min-h-[500px] flex flex-col">
        <!-- Controls -->
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
            <div class="flex flex-wrap items-center gap-2 w-full md:w-auto">
                <button 
                    :class="`px-4 py-2 rounded-xl text-sm font-semibold transition-colors ${!roleFilter ? 'bg-white/70 text-primary shadow-sm border border-primary/10' : 'bg-transparent hover:bg-white/40 text-slate-500'}`"
                    @click="handleFilterChange('')"
                >{{ t('admin.user.allUsers') }}</button>
                <button 
                    :class="`px-4 py-2 rounded-xl text-sm font-medium transition-colors ${roleFilter === 'user' ? 'bg-white/70 text-primary shadow-sm border border-primary/10' : 'bg-transparent hover:bg-white/40 text-slate-500'}`"
                    @click="handleFilterChange('user')"
                >{{ t('admin.user.filterUsers') }}</button>
                <button 
                    :class="`px-4 py-2 rounded-xl text-sm font-medium transition-colors ${roleFilter === 'admin' ? 'bg-white/70 text-primary shadow-sm border border-primary/10' : 'bg-transparent hover:bg-white/40 text-slate-500'}`"
                    @click="handleFilterChange('admin')"
                >{{ t('admin.user.filterAdmins') }}</button>
                 <button 
                    :class="`px-4 py-2 rounded-xl text-sm font-medium transition-colors ${roleFilter === 'ban' ? 'bg-white/70 text-primary shadow-sm border border-primary/10' : 'bg-transparent hover:bg-white/40 text-slate-500'}`"
                    @click="handleFilterChange('ban')"
                >{{ t('admin.user.filterBanned') }}</button>
            </div>
            <div class="flex items-center gap-3 w-full md:w-auto">
                 <!-- Search Input -->
                 <div class="relative w-full md:w-auto">
                    <input 
                        v-model="searchText" 
                        @keydown.enter="fetchUsers"
                        type="text" 
                        :placeholder="t('admin.user.searchPlaceholder')" 
                        class="bg-white/50 border border-white/60 rounded-xl py-2 pl-3 pr-10 text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary/20 w-full md:w-48 transition-all md:focus:w-64"
                    />
                    <button class="absolute right-2 top-2 text-slate-400 hover:text-primary" @click="fetchUsers">
                        <span class="material-symbols-outlined text-[20px]">search</span>
                    </button>
                 </div>
            </div>
        </div>

        <!-- Table -->
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
                <thead>
                    <tr class="text-slate-400 text-xs uppercase tracking-wider border-b border-slate-200/50">
                        <th class="pb-4 pl-4 font-semibold w-12">{{ t('admin.user.avatar') }}</th>
                        <th class="pb-4 font-semibold">{{ t('admin.user.nameAccount') }}</th>
                        <th class="pb-4 font-semibold">{{ t('admin.user.role') }}</th>
                        <th class="pb-4 font-semibold">{{ t('admin.user.registrationDate') }}</th>
                        <th class="pb-4 pr-4 text-right font-semibold">{{ t('admin.user.actions') }}</th>
                    </tr>
                </thead>
                <tbody class="text-sm">
                    <tr v-if="loading" class="text-center py-10">
                        <td colspan="5" class="py-10 text-slate-400">{{ t('admin.user.loading') }}</td>
                    </tr>
                    <tr v-else v-for="(user, i) in users" :key="user.id" class="group border-b border-slate-100/50 hover:bg-white/40 transition-colors">
                        <td class="py-4 pl-4 align-middle">
                            <div class="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-200 to-purple-200 p-0.5 shadow-sm">
                                <img v-if="user.userAvatar" :src="user.userAvatar" class="w-full h-full rounded-full object-cover border-2 border-white"/>
                                <div v-else class="w-full h-full rounded-full bg-white flex items-center justify-center text-indigo-600 font-bold text-xs border-2 border-white">{{ user.userName?.substring(0,2).toUpperCase() }}</div>
                            </div>
                        </td>
                        <td class="py-4 align-middle">
                            <div>
                                <p class="font-bold text-slate-800 text-sm">{{ user.userName }}</p>
                                <p class="text-xs text-slate-500">@{{ user.userAccount }}</p>
                            </div>
                        </td>
                        <td class="py-4 align-middle">
                            <span :class="`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${roleClasses[user.userRole || 'user']}`">
                                <span :class="`w-1.5 h-1.5 rounded-full ${roleDotClasses[user.userRole || 'user']}`"></span>
                                {{ user.userRole }}
                            </span>
                        </td>
                        <td class="py-4 align-middle text-slate-600">{{ formatDate(user.createTime) }}</td>
                        <td class="py-4 pr-4 text-right align-middle">
                            <div class="flex items-center justify-end gap-2 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-opacity">
                                <button class="w-8 h-8 rounded-lg bg-white/60 text-slate-500 hover:bg-primary hover:text-white border border-slate-200/60 transition-all flex items-center justify-center shadow-sm" :title="t('admin.user.edit')" @click="doEdit(user)">
                                    <span class="material-symbols-outlined text-[18px]">edit</span>
                                </button>
                                <button 
                                    :class="`w-8 h-8 rounded-lg border transition-all flex items-center justify-center shadow-sm ${user.userRole === 'ban' ? 'bg-emerald-50 text-emerald-500 border-emerald-200 hover:bg-emerald-500 hover:text-white' : 'bg-white/60 text-slate-500 border-slate-200/60 hover:bg-rose-500 hover:text-white hover:border-rose-200'}`" 
                                    :title="user.userRole === 'ban' ? t('admin.user.unbanUser') : t('admin.user.banUser')" 
                                    @click="doBan(user)"
                                >
                                    <span class="material-symbols-outlined text-[18px]">{{ user.userRole === 'ban' ? 'how_to_reg' : 'block' }}</span>
                                </button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

         <!-- Pagination -->
        <div class="flex items-center justify-between mt-6 px-2">
            <p class="text-sm text-slate-500">
                {{ t('admin.user.pageSummary', { start: users.length > 0 ? (current - 1) * pageSize + 1 : 0, end: Math.min(current * pageSize, total), total }) }}
            </p>
            <div class="flex items-center gap-2">
                <button 
                    class="w-8 h-8 flex items-center justify-center rounded-lg bg-white/50 border border-white/60 text-slate-400 hover:text-primary transition-colors disabled:opacity-50" 
                    :disabled="current <= 1"
                    @click="handlePageChange(current - 1)"
                >
                    <span class="material-symbols-outlined text-sm">chevron_left</span>
                </button>
                <button class="w-8 h-8 flex items-center justify-center rounded-lg bg-primary text-white shadow-glow">{{ current }}</button>
                <button 
                    class="w-8 h-8 flex items-center justify-center rounded-lg bg-white/50 border border-white/60 text-slate-600 hover:text-primary transition-colors disabled:opacity-50"
                    :disabled="current * pageSize >= total"
                    @click="handlePageChange(current + 1)"
                >
                    <span class="material-symbols-outlined text-sm">chevron_right</span>
                </button>
            </div>
        </div>
    </div>

    <!-- Edit User Modal -->
    <a-modal
      v-model:visible="editVisible"
      :title="t('userSettings.edit')"
      @ok="handleEditSave"
      :confirm-loading="editLoading"
      class="glass-modal"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item :label="t('userSettings.nickname')" required>
          <a-input v-model:value="editForm.userName" :placeholder="t('userSettings.nickname')" class="rounded-xl py-2" />
        </a-form-item>
        <a-form-item :label="t('userSettings.role')" required>
          <a-select v-model:value="editForm.userRole" class="w-full h-11 rounded-xl">
            <a-select-option value="user">{{ t('admin.user.filterUsers') }}</a-select-option>
            <a-select-option value="admin">{{ t('admin.user.filterAdmins') }}</a-select-option>
            <a-select-option value="ban">{{ t('admin.user.filterBanned') }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="t('userSettings.avatar')">
           <a-input v-model:value="editForm.userAvatar" :placeholder="t('userSettings.avatar')" class="rounded-xl py-2" />
        </a-form-item>
        <a-form-item :label="t('userSettings.bio')">
          <a-textarea v-model:value="editForm.userProfile" :placeholder="t('userSettings.bioPlaceholder')" :rows="3" class="rounded-xl" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { listUserVoByPageUsingPost, updateUserUsingPost } from '@/api/user/userController'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const users = ref<API.UserVO[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const searchText = ref('')
const roleFilter = ref('')

// Edit Modal State
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive<API.UserUpdateMyRequest & { id?: number, idStr?: string, userRole?: string }>({
    id: undefined,
    idStr: '',
    userName: '',
    userRole: 'user',
    userAvatar: '',
    userProfile: ''
})

// Mappings for display
const roleClasses: Record<string, string> = {
    'admin': 'bg-indigo-50 text-indigo-600 border border-indigo-100',
    'user': 'bg-purple-50 text-purple-600 border border-purple-100',
    'ban': 'bg-rose-50 text-rose-600 border border-rose-100'
}

const roleDotClasses: Record<string, string> = {
    'admin': 'bg-indigo-500',
    'user': 'bg-purple-500',
    'ban': 'bg-rose-500'
}

const fetchUsers = async () => {
    loading.value = true
    try {
        const res = await listUserVoByPageUsingPost({
            current: current.value,
            pageSize: pageSize.value,
            userName: searchText.value,
            userRole: roleFilter.value
        })
        if (res.data.code === 0 && res.data.data) {
            users.value = res.data.data.records || []
            total.value = Number(res.data.data.total) || 0
            fetchRoleStats()
        } else {
            message.error(t.value('admin.user.loadFailed') + ': ' + res.data.message)
        }
    } catch (e) {
        message.error(t.value('common.requestFailed'))
    } finally {
        loading.value = false
    }
}

const handlePageChange = (page: number) => {
    current.value = page
    fetchUsers()
}

const handleFilterChange = (role: string) => {
    roleFilter.value = role
    current.value = 1
    fetchUsers()
}

const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-'
    return dayjs(dateStr).format('MMM DD, YYYY')
}

const stats = ref({
    admins: 0,
    users: 0,
    banned: 0
})

const fetchRoleStats = async () => {
    try {
        const [adminRes, userRes, banRes] = await Promise.all([
            listUserVoByPageUsingPost({ current: 1, pageSize: 1, userRole: 'admin' }),
            listUserVoByPageUsingPost({ current: 1, pageSize: 1, userRole: 'user' }),
            listUserVoByPageUsingPost({ current: 1, pageSize: 1, userRole: 'ban' }),
        ])
        stats.value.admins = Number(adminRes.data.data?.total) || 0
        stats.value.users = Number(userRes.data.data?.total) || 0
        stats.value.banned = Number(banRes.data.data?.total) || 0
    } catch (e) {
        stats.value.admins = 0
        stats.value.users = 0
        stats.value.banned = 0
    }
}

const doEdit = (user: API.UserVO) => {
    editForm.idStr = user.idStr
    editForm.id = undefined
    editForm.userName = user.userName
    editForm.userRole = user.userRole
    editForm.userAvatar = user.userAvatar
    editForm.userProfile = user.userProfile
    editVisible.value = true
}

const handleEditSave = async () => {
    if (!editForm.userName) {
        return message.warning(t.value('admin.user.nameRequired'))
    }
    editLoading.value = true
    try {
        const res = await updateUserUsingPost(editForm)
        if (res.data.code === 0) {
            message.success(t.value('admin.user.updateSuccess'))
            editVisible.value = false
            fetchUsers()
        } else {
            message.error(t.value('admin.user.updateFailed') + ': ' + res.data.message)
        }
    } catch (e) {
        message.error(t.value('common.requestFailed'))
    } finally {
        editLoading.value = false
    }
}

const doBan = async (user: API.UserVO) => {
    try {
        const newRole = user.userRole === 'ban' ? 'user' : 'ban'
        await updateUserUsingPost({ idStr: user.idStr, userRole: newRole })
        message.success(newRole === 'ban' ? t.value('admin.user.banSuccess') : t.value('admin.user.unbanSuccess'))
        fetchUsers()
    } catch(e) {
        message.error(t.value('admin.user.operationFailed'))
    }
}

onMounted(() => {
    fetchUsers()
    fetchRoleStats()
})
</script>

<style scoped>
/* Scoped styles */
</style>
