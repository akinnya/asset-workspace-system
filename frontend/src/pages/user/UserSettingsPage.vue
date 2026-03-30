<template>
  <div class="relative w-full max-w-4xl mx-auto p-4 md:p-8 flex flex-col justify-center">
    <div class="mb-6 flex items-center gap-2 text-slate-500 hover:text-primary transition-colors w-fit">
        <span class="material-symbols-outlined text-sm">arrow_back</span>
        <router-link to="/user/profile" class="text-sm font-semibold">{{ t('userSettings.back') }}</router-link>
    </div>

    <div class="glass-panel rounded-3xl p-6 md:p-10 shadow-soft-diffusion relative overflow-hidden">
        <div class="absolute right-0 top-0 w-40 h-40 sm:w-52 sm:h-52 lg:w-64 lg:h-64 bg-primary/5 blur-[80px] rounded-full pointer-events-none"></div>
        <div class="relative">
            <!-- Header -->
            <div class="flex items-center justify-between mb-8 border-b border-slate-200/60 pb-6">
                <div>
                    <h1 class="font-display font-bold text-2xl md:text-3xl text-slate-800">{{ t('userSettings.title') }}</h1>
                    <p class="text-slate-500 text-sm mt-1">{{ t('userSettings.subtitle') }}</p>
                </div>
                <div class="hidden sm:flex gap-3">
                    <button class="px-5 py-2 rounded-xl text-slate-600 font-semibold hover:bg-white/50 transition-colors border border-transparent hover:border-slate-200" @click="handleCancel">{{ t('userSettings.cancel') }}</button>
                    <button class="px-6 py-2 rounded-xl bg-primary text-white font-bold shadow-glow hover:bg-primary-hover hover:shadow-lg transition-all transform active:scale-95" @click="handleSave">{{ t('userSettings.save') }}</button>
                </div>
            </div>

            <form class="space-y-10" @submit.prevent="handleSave">
                <!-- Basic Info -->
                <div class="flex flex-col sm:flex-row gap-8 items-center sm:items-start">
                    <div class="shrink-0 flex flex-col items-center gap-3">
                        <div class="relative group">
                            <div class="w-32 h-32 rounded-full p-1 bg-white shadow-glow ring-4 ring-white/50">
                                <img v-if="formState.userAvatar" :src="formState.userAvatar" class="w-full h-full object-cover rounded-full bg-primary/5" />
                                <div v-else class="w-full h-full rounded-full bg-primary/10 flex items-center justify-center text-4xl text-primary font-bold">
                                    {{ formState.userName?.substring(0, 1).toUpperCase() }}
                                </div>
                            </div>
                            <label class="absolute inset-0 flex items-center justify-center bg-slate-900/40 text-white rounded-full opacity-0 group-hover:opacity-100 transition-all duration-300 cursor-pointer backdrop-blur-sm" for="avatar-upload">
                                <span class="material-symbols-outlined mr-1">photo_camera</span>
                                <span class="text-sm font-medium">{{ t('userSettings.changeAvatar') }}</span>
                            </label>
                            <input accept="image/*" class="hidden" id="avatar-upload" type="file" @change="handleAvatarUpload"/>
                        </div>
                        <span class="text-xs text-slate-400 font-medium">{{ t('userSettings.maxSize') }}</span>
                    </div>
                    <div class="flex-1 w-full space-y-6">
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div class="space-y-2">
                                <label class="block text-sm font-bold text-slate-700 ml-1">{{ t('userSettings.nickname') }}</label>
                                <input v-model="formState.userName" class="w-full glass-input rounded-xl px-4 py-2.5 text-slate-700 placeholder-slate-400" type="text" />
                            </div>
                            <div class="space-y-2">
                                <label class="block text-sm font-bold text-slate-700 ml-1">{{ t('userSettings.role') }}</label>
                                <input :value="loginUserStore.loginUser.userRole" class="w-full glass-input rounded-xl px-4 py-2.5 text-slate-700 placeholder-slate-400" type="text" disabled/>
                            </div>
                        </div>
                        <div class="space-y-2">
                            <label class="block text-sm font-bold text-slate-700 ml-1">{{ t('userSettings.bio') }}</label>
                            <textarea v-model="formState.userProfile" class="w-full glass-input rounded-xl px-4 py-3 text-slate-700 placeholder-slate-400 resize-none" rows="3"></textarea>
                            <div class="flex justify-end">
                                <span class="text-xs text-slate-400 font-medium">{{ formState.userProfile?.length || 0 }}/300</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="h-px bg-slate-200/60 w-full"></div>

                <!-- Social Links (Placeholder) -->
                <div class="space-y-6">
                    <h3 class="font-display font-bold text-lg text-slate-800 flex items-center gap-2">
                        <span class="material-symbols-outlined text-primary">link</span>
                        {{ t('userSettings.social') }}
                    </h3>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div class="space-y-2">
                            <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider ml-1">{{ t('userSettings.portfolio') }}</label>
                            <div class="relative">
                                <span class="absolute left-3 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400 text-[20px]">language</span>
                                <input class="w-full glass-input rounded-xl pl-10 pr-4 py-2.5 text-slate-700" placeholder="https://your-portfolio.com" type="url"/>
                            </div>
                        </div>
                        <!-- More fields can be added here -->
                    </div>
                </div>

                <div class="h-px bg-slate-200/60 w-full"></div>

                <!-- Account Security -->
                <div class="space-y-6">
                    <h3 class="font-display font-bold text-lg text-slate-800 flex items-center gap-2">
                        <span class="material-symbols-outlined text-primary">security</span>
                        {{ t('userSettings.security') }}
                    </h3>
                    <div class="bg-white/40 border border-white/60 rounded-2xl p-6">
                        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
                             <div class="space-y-2">
                                <label class="block text-sm font-bold text-slate-700 ml-1">{{ t('userSettings.email') }}</label>
                                <div class="flex gap-2">
                                    <input 
                                        v-model="emailForm.userAccount" 
                                        :disabled="!isEmailEditing" 
                                        class="flex-1 glass-input rounded-xl px-4 py-2.5 text-slate-700 border-slate-200" 
                                        :class="{'bg-slate-50/50 text-slate-500 cursor-not-allowed': !isEmailEditing}"
                                        type="email"
                                    />
                                     <button 
                                        v-if="!isEmailEditing"
                                        class="px-3 py-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 font-medium text-sm transition-colors" 
                                        type="button"
                                        @click="startEmailEdit"
                                     >
                                        {{ t('userSettings.change') }}
                                     </button>
                                     <a-space v-else>
                                         <a-button type="primary" size="small" @click="handleEmailUpdate">{{ t('userSettings.save') }}</a-button>
                                         <a-button size="small" @click="resetEmailEdit">{{ t('userSettings.cancel') }}</a-button>
                                     </a-space>
                                </div>
                                <div v-if="isEmailEditing" class="flex gap-2">
                                    <input 
                                        v-model="emailForm.code"
                                        class="flex-1 glass-input rounded-xl px-4 py-2.5 text-slate-700 border-slate-200"
                                        :placeholder="t('userSettings.codePlaceholder')"
                                        type="text"
                                    />
                                    <button 
                                        class="px-3 py-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 font-medium text-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                        type="button"
                                        :disabled="emailCodeSending || emailCountdown > 0"
                                        @click="handleSendEmailCode"
                                    >
                                        {{ emailCountdown > 0 ? t('userSettings.resendIn', { seconds: emailCountdown }) : t('userSettings.sendCode') }}
                                    </button>
                                </div>
                                <p class="text-xs text-slate-400 ml-1">{{ t('userSettings.emailTip') }}</p>
                             </div>
                             
                             <!-- Password Change -->
                             <div class="space-y-4">
                                <h4 class="font-bold text-slate-700 text-sm">{{ t('userSettings.changePassword') }}</h4>
                                <div class="space-y-3">
                                    <input v-model="passwordForm.oldPassword" type="password" :placeholder="t('userSettings.currentPassword')" class="w-full glass-input rounded-xl px-4 py-2.5 text-slate-700 placeholder-slate-400"/>
                                    <input v-model="passwordForm.newPassword" type="password" :placeholder="t('userSettings.newPassword')" class="w-full glass-input rounded-xl px-4 py-2.5 text-slate-700 placeholder-slate-400"/>
                                    <input v-model="passwordForm.checkPassword" type="password" :placeholder="t('userSettings.confirmNewPassword')" class="w-full glass-input rounded-xl px-4 py-2.5 text-slate-700 placeholder-slate-400"/>
                                    <button 
                                        type="button"
                                        :disabled="!isPasswordValid"
                                        @click="handlePasswordUpdate"
                                        class="w-full py-2.5 rounded-xl bg-slate-800 text-white font-semibold hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                                    >
                                        {{ t('userSettings.updatePassword') }}
                                    </button>
                                </div>
                             </div>
                        </div>
                    </div>
                </div>

                <!-- Mobile Actions -->
                <div class="flex sm:hidden flex-col gap-3 pt-4">
                    <button type="submit" class="w-full py-3 rounded-xl bg-primary text-white font-bold shadow-glow hover:bg-primary-hover transition-all">{{ t('userSettings.saveChanges') }}</button>
                    <button type="button" class="w-full py-3 rounded-xl text-slate-600 font-semibold hover:bg-white/50 transition-colors border border-slate-200 bg-white/30" @click="handleCancel">{{ t('userSettings.cancel') }}</button>
                </div>
            </form>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { updateMyInfoUsingPost, updatePasswordUsingPost, sendEmailCodeUsingPost, updateEmailUsingPost } from '@/api/user/userController'
import { uploadFileUsingPost } from '@/api/file/fileController'
import { message } from 'ant-design-vue'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)
const formState = reactive<API.UserUpdateMyRequest>({})
const passwordForm = reactive<API.UserUpdatePasswordRequest>({
    oldPassword: '',
    newPassword: '',
    checkPassword: ''
})
const emailForm = reactive({
    userAccount: '',
    code: ''
})
const isEmailEditing = ref(false)
const emailCodeSending = ref(false)
const emailCountdown = ref(0)
let emailTimer: number | null = null

const isPasswordValid = computed(() => {
    return passwordForm.oldPassword && 
           passwordForm.newPassword && 
           passwordForm.checkPassword && 
           passwordForm.newPassword.length >= 8 &&
           passwordForm.newPassword === passwordForm.checkPassword
})

onMounted(() => {
    const user = loginUserStore.loginUser
    if (user) {
        formState.userName = user.userName
        formState.userAvatar = user.userAvatar
        formState.userProfile = user.userProfile
        emailForm.userAccount = user.userAccount || ''
        // role is not editable
    }
})

const handleAvatarUpload = async (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (!file) return

    try {
        const res = await uploadFileUsingPost({ biz: 'avatar' }, file)
        if (res.data.code === 0 && res.data.data) {
            formState.userAvatar = res.data.data
            message.success(t.value('userSettings.avatarUploadSuccess'))
        } else {
            message.error(t.value('common.uploadFailed') + ': ' + res.data.message)
        }
    } catch (error) {
        message.error(t.value('common.uploadFailed'))
    }
}

const handleSave = async () => {
    try {
        const res = await updateMyInfoUsingPost(formState)
        if (res.data.code === 0) {
            message.success(t.value('userSettings.profileUpdateSuccess'))
            await loginUserStore.fetchLoginUser()
            router.push('/user/profile')
        } else {
            message.error(t.value('common.actionFailed') + ': ' + res.data.message)
        }
    } catch (error) {
        message.error(t.value('common.actionFailed'))
    }
}

const handlePasswordUpdate = async () => {
    try {
        const res = await updatePasswordUsingPost(passwordForm)
        if (res.data.code === 0 && res.data.data) {
            message.success(t.value('userSettings.passwordUpdateSuccess'))
            passwordForm.oldPassword = ''
            passwordForm.newPassword = ''
            passwordForm.checkPassword = ''
        } else {
            message.error(t.value('userSettings.passwordUpdateFailed') + ': ' + res.data.message)
        }
    } catch (error) {
        message.error(t.value('common.requestFailed'))
    }
}

const handleEmailUpdate = async () => {
    if(!emailForm.userAccount) return message.warning(t.value('userSettings.emailEmpty'))
    if(!emailForm.code) return message.warning(t.value('userSettings.codeEmpty'))
    try {
        const res = await updateEmailUsingPost({
            email: emailForm.userAccount,
            code: emailForm.code
        })
        if (res.data.code === 0 && res.data.data) {
            message.success(t.value('userSettings.emailUpdateSuccess'))
            resetEmailEdit()
            await loginUserStore.fetchLoginUser()
        } else {
            message.error(t.value('common.actionFailed') + ': ' + res.data.message)
        }
    } catch (e) {
        message.error(t.value('common.actionFailed'))
    }
}

const handleSendEmailCode = async () => {
    if (!emailForm.userAccount) {
        message.warning(t.value('userSettings.emailEmpty'))
        return
    }
    if (emailCodeSending.value || emailCountdown.value > 0) return
    emailCodeSending.value = true
    try {
        const res = await sendEmailCodeUsingPost({ email: emailForm.userAccount })
        if (res.data.code === 0) {
            message.success(t.value('userSettings.codeSent'))
            startEmailCountdown()
        } else {
            message.error(t.value('common.actionFailed') + ': ' + res.data.message)
        }
    } catch (e: any) {
        const backendMessage = e?.response?.data?.message
        if (backendMessage) {
            message.error(`${t.value('common.actionFailed')}: ${backendMessage}`)
        } else {
            message.error(t.value('common.actionFailed'))
        }
    } finally {
        emailCodeSending.value = false
    }
}

const startEmailCountdown = () => {
    emailCountdown.value = 60
    if (emailTimer) {
        window.clearInterval(emailTimer)
    }
    emailTimer = window.setInterval(() => {
        emailCountdown.value -= 1
        if (emailCountdown.value <= 0 && emailTimer) {
            window.clearInterval(emailTimer)
            emailTimer = null
        }
    }, 1000)
}

const resetEmailEdit = () => {
    isEmailEditing.value = false
    emailForm.userAccount = loginUserStore.loginUser?.userAccount || ''
    emailForm.code = ''
    emailCountdown.value = 0
    if (emailTimer) {
        window.clearInterval(emailTimer)
        emailTimer = null
    }
}

const startEmailEdit = () => {
    resetEmailEdit()
    isEmailEditing.value = true
}

const handleCancel = () => {
    router.back()
}

onUnmounted(() => {
    if (emailTimer) {
        window.clearInterval(emailTimer)
        emailTimer = null
    }
})
</script>

<style scoped>
.glass-panel {
    background: rgba(255, 255, 255, 0.65);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.6);
}
.glass-input {
    background: rgba(255, 255, 255, 0.5);
    border: 1px solid rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(4px);
    transition: all 0.2s ease;
}
.glass-input:focus {
    background: rgba(255, 255, 255, 0.9);
    border-color: #0f766e;
    box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.16);
    outline: none;
}
</style>
