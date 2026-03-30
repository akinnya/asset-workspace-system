<template>
  <div id="userLoginPage" class="login-container">
    <!-- Left Side: Workspace Background -->
    <div class="left-panel hidden-mobile">
      <div class="absolute top-6 left-6 z-50">
          <button class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-white/20 hover:bg-white/40 backdrop-blur-md text-white border border-white/30 transition-all font-medium text-sm" @click="languageStore.toggleLanguage">
              <span class="material-symbols-outlined text-[18px]">translate</span>
              {{ currentLang === 'zh' ? 'EN' : '中文' }}
          </button>
      </div>
      <div class="bg-layer" />
      <div class="bg-overlay" />
      
      <div class="brand-content">
         <div class="logo-pill">
            <database-outlined class="logo-icon" />
            <span class="logo-text">Asset Workspace</span>
         </div>
         <h1 class="hero-title">{{ t('user.loginHeroTitle') }}</h1>
         <p class="hero-subtitle">{{ t('user.loginHeroSubtitle') }}</p>
      </div>
    </div>

    <!-- Right Side: Login Form -->
    <div class="right-panel">
       <!-- Decorative Blurs -->
       <div class="blur-blob blob-1" />
       <div class="blur-blob blob-2" />
       
       <div class="login-card glass-panel">
           <div class="card-header relative">
             <div class="absolute left-0 top-0 -ml-2 -mt-2">
                <button @click="router.push('/')" class="text-slate-400 hover:text-primary transition-colors p-2" :title="t('user.backHome')">
                   <arrow-left-outlined class="text-xl" />
                </button>
             </div>
             <div class="mascot-wrapper">
                <img src="@/assets/brand-mark.svg" alt="Asset Workspace Logo" />
             </div>
             <h2 class="welcome-title">{{ t('user.loginTitle') }}</h2>
             <p class="welcome-subtitle">{{ t('user.loginSubtitle') }}</p>
          </div>

          <form class="login-form" @submit.prevent="handleSubmit">
             <!-- Account Input -->
             <div class="input-group">
                <label for="account">{{ t('user.account') }}</label>
                <div class="glass-input-wrapper">
                   <user-outlined class="input-icon" />
                   <input 
                      id="account"
                      v-model="formState.userAccount" 
                      type="text" 
                      :placeholder="t('user.account')" 
                      required
                   />
                </div>
             </div>

             <!-- Password Input -->
             <div class="input-group">
                <div class="label-row">
                   <label for="password">{{ t('user.password') }}</label>
                   <a href="javascript:void(0)" class="forgot-link" @click="handleForgotPassword">{{ t('user.forgotPassword') }}</a>
                </div>
                <div class="glass-input-wrapper">
                   <lock-outlined class="input-icon" />
                   <input 
                      id="password"
                      v-model="formState.userPassword" 
                      :type="showPassword ? 'text' : 'password'" 
                      :placeholder="t('user.password')"
                      required
                   />
                   <button type="button" class="visibility-btn" @click="showPassword = !showPassword">
                      <eye-outlined v-if="showPassword" />
                      <eye-invisible-outlined v-else />
                   </button>
                </div>
             </div>

             <!-- Submit Button -->
             <button type="submit" class="submit-btn" :disabled="loading">
                <span v-if="!loading">{{ t('user.signIn') }}</span>
                <span v-else>{{ t('common.loading') }}</span>
                <arrow-right-outlined v-if="!loading" class="btn-icon" />
             </button>
          </form>



          <!-- Register Link -->
          <div class="footer-text">
             {{ t('user.noAccount') }} 
             <RouterLink to="/user/register" class="create-account-link">{{ t('user.createAccount') }}</RouterLink>
          </div>
       </div>
       
       <div class="mobile-footer">© 2026 Asset Workspace System.</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { message } from 'ant-design-vue'
import { userLoginUsingPost } from '@/api/user/userController'
import { 
  DatabaseOutlined, UserOutlined, LockOutlined, EyeOutlined, EyeInvisibleOutlined, 
  ArrowRightOutlined, GoogleOutlined, GithubOutlined, ArrowLeftOutlined 
} from '@ant-design/icons-vue'
import { Modal } from 'ant-design-vue'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()
const { t, currentLang } = storeToRefs(languageStore)

const formState = reactive({
  userAccount: '',
  userPassword: '',
})

const loading = ref(false)
const showPassword = ref(false)

const handleSubmit = async () => {
  if (!formState.userAccount || !formState.userPassword) {
     message.error(t.value('user.fillAll'))
     return
  }

  loading.value = true
  try {
    const res = await userLoginUsingPost(formState)
    if (res.data.code === 0 && res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
      message.success(t.value('user.loginSuccess'))
      router.push({ path: '/', replace: true })
    } else {
      message.error(t.value('user.loginFailed') + '，' + res.data.message)
    }
  } catch (e) {
    message.error(t.value('user.loginFailedRetry'))
  } finally {
    loading.value = false
  }
}

const handleForgotPassword = () => {
  Modal.info({
    title: t.value('user.forgotPassword'),
    content: t.value('user.forgotPasswordContent'),
    okText: t.value('user.understood'),
    centered: true,
    maskClosable: true,
  })
}
</script>

<style scoped>
/* Reset & Base */
.login-container {
  display: flex;
  min-height: 100vh;
  background-color: #f4f7f8;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
}

/* Animations included in global css or here? Adding keyframes if needed */

/* Left Panel */
.left-panel {
  width: 50%;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 3rem;
  background-color: #0f172a;
}

.bg-layer {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 20%, rgba(20, 184, 166, 0.32), transparent 28%),
    radial-gradient(circle at 82% 18%, rgba(15, 118, 110, 0.24), transparent 24%),
    linear-gradient(135deg, #0f172a 0%, #111827 52%, #164e63 100%);
  z-index: 0;
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.06) 1px, transparent 1px);
  background-size: 36px 36px;
  z-index: 1;
}

.brand-content {
  position: relative;
  z-index: 10;
  max-width: 500px;
}

.logo-pill {
  display: inline-flex;
  align-items: center;
  background: rgba(15, 23, 42, 0.42);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(148, 163, 184, 0.22);
  padding: 0.75rem 1rem;
  border-radius: 1rem;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.18);
  margin-bottom: 1.5rem;
}

.logo-icon {
  color: #99f6e4;
  margin-right: 0.5rem;
  font-size: 1.25rem;
}

.logo-text {
  color: white;
  font-weight: 700;
  letter-spacing: 0.025em;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  color: white;
  line-height: 1.1;
  margin-bottom: 1rem;
  text-shadow: none;
}

.hero-subtitle {
  font-size: 1.125rem;
  color: rgba(226, 232, 240, 0.92);
  font-weight: 500;
  text-shadow: none;
}

/* Right Panel */
.right-panel {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  background-color: #f4f7f8;
}

/* Blur Blobs */
.blur-blob {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.blob-1 {
  top: -10%; right: -5%; width: 24rem; height: 24rem;
  background: rgba(15, 118, 110, 0.14);
  filter: blur(100px);
}
.blob-2 {
  bottom: -10%; left: -5%; width: 20rem; height: 20rem;
  background: rgba(148, 163, 184, 0.18);
  filter: blur(80px);
}

/* Glass Card */
.login-card {
  width: 100%;
  max-width: 480px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(10px);
  border-radius: 1.5rem;
  padding: 2.5rem; /* p-8 to p-12 */
  position: relative;
  z-index: 10;
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 22px 42px -18px rgba(15, 23, 42, 0.18);
}

.card-header {
  text-align: center;
  margin-bottom: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.mascot-wrapper {
  width: 6rem;
  height: 6rem;
  margin-bottom: 0.5rem;
}
.mascot-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
}

.welcome-title {
  color: #1b212c;
  font-size: 1.875rem;
  font-weight: 700;
  margin: 0;
}

.welcome-subtitle {
  color: #64748b;
  font-size: 0.875rem;
  font-weight: 500;
}

/* Form */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.input-group label {
  display: block;
  color: #475569;
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  margin-left: 0.25rem;
}

.glass-input-wrapper {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(229, 231, 235, 0.5);
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  padding: 0 1rem;
  height: 3.5rem;
  transition: all 0.3s ease;
}

.glass-input-wrapper:focus-within {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(15, 118, 110, 0.4);
  box-shadow: 0 0 0 4px rgba(15, 118, 110, 0.12);
}

.input-icon {
  color: rgba(15, 118, 110, 0.7);
  margin-right: 0.75rem;
  font-size: 1.1rem;
}

.glass-input-wrapper input {
  background: transparent;
  border: none;
  width: 100%;
  color: #1b212c;
  outline: none;
  font-size: 1rem;
}

.glass-input-wrapper input::placeholder {
  color: #94a3b8;
}

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  margin-left: 0.25rem;
}
.label-row label {
    margin-bottom: 0;
}

.forgot-link {
  color: #0f766e;
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s;
}
.forgot-link:hover {
  text-decoration: underline;
  color: #115e59;
}

.visibility-btn {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 0.25rem;
  margin-left: 0.5rem;
}
.visibility-btn:hover { color: #0f766e; }

/* Buttons */
.submit-btn {
  margin-top: 0.5rem;
  width: 100%;
  height: 3.5rem;
  background: #0f766e;
  color: white;
  font-weight: 600;
  border-radius: 0.75rem;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(15, 118, 110, 0.2);
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background: #115e59;
  transform: scale(1.02);
}
.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.divider {
  position: relative;
  margin: 2rem 0;
  text-align: center;
}
.divider .line {
  position: absolute;
  top: 50%; left: 0; right: 0;
  border-top: 1px solid #e2e8f0;
}
.divider-text {
  position: relative;
  background: rgba(255,255,255,0.8);
  padding: 0 0.75rem;
  color: #94a3b8;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 9999px;
}

.social-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  height: 3rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  color: #334155;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: background-color 0.2s;
}
.social-btn:hover {
  background-color: #f0f9ff;
}

.footer-text {
  margin-top: 2rem;
  text-align: center;
  color: #64748b;
  font-size: 0.875rem;
}

.create-account-link {
  color: #0f766e;
  font-weight: 600;
  text-decoration: none;
}
.create-account-link:hover {
  text-decoration: underline;
}

.mobile-footer {
  display: none;
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 2rem;
}

/* Response */
@media (max-width: 1024px) {
  .left-panel { display: none; }
  .mobile-footer { display: block; }
}

@media (max-width: 640px) {
  .login-card {
    padding: 1.5rem;
    border-radius: 1.25rem;
  }
  .blob-1 {
    width: 14rem;
    height: 14rem;
    filter: blur(70px);
  }
  .blob-2 {
    width: 12rem;
    height: 12rem;
    filter: blur(60px);
  }
  .mascot-wrapper {
    width: 4.5rem;
    height: 4.5rem;
  }
}
</style>
