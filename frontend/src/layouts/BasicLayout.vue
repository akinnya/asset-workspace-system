<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header" v-if="!route.meta.hideHeader && !route.path.startsWith('/admin')">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content :class="route.meta.fullScreen ? 'full-screen-content' : 'content'">
        <router-view v-slot="{ Component }">
          <transition name="page-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
      <a-layout-footer class="footer" v-if="!route.meta.hideHeader">
        <span>Asset Workspace System · GPL-3.0</span>
      </a-layout-footer>
    </a-layout>

    <!-- Background Particles -->
    <div class="bg-particles">
      <div 
        v-for="i in 15" 
        :key="i" 
        class="particle"
        :style="generateParticleStyle()"
      ></div>
    </div>

    <!-- Global Loading -->
    <GlobalLoading :loading="pageLoading" />

  </div>
</template>

<script setup lang="ts">
import GlobalHeader from "@/components/layout/GlobalHeader.vue";
import GlobalLoading from "@/components/layout/GlobalLoading.vue";
import { useRoute, useRouter } from "vue-router";
import { useLoginUserStore } from "@/stores/user/useLoginUserStore";
import { onMounted, onUnmounted, ref, watch } from "vue";

const route = useRoute();
const router = useRouter();
const loginUserStore = useLoginUserStore();
const pageLoading = ref(false);

// Route transition loading
watch(() => route.path, () => {
  pageLoading.value = true;
  setTimeout(() => {
    pageLoading.value = false;
  }, 600);
});

const generateParticleStyle = () => {
  const x = Math.random() * 400 - 200;
  const y = Math.random() * 400 - 200;
  const duration = 15 + Math.random() * 20;
  const delay = Math.random() * -20;
  const size = 2 + Math.random() * 4;
  const left = Math.random() * 100;
  const top = Math.random() * 100;
  
  return {
    '--x': `${x}px`,
    '--y': `${y}px`,
    '--d': `${duration}s`,
    left: `${left}%`,
    top: `${top}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${delay}s`
  };
};

/**
 * 游客资产保护：禁止右键和拖拽
 */
const preventGuestInteraction = (e: Event) => {
  if (!loginUserStore.loginUser.id) {
    // 如果是图片或者链接
    const target = e.target as HTMLElement;
    if (target.tagName === 'IMG' || target.tagName === 'A' || target.closest('.no-guest-interact')) {
        e.preventDefault();
        return false;
    }
  }
};

onMounted(() => {
  document.addEventListener("contextmenu", preventGuestInteraction);
  document.addEventListener("dragstart", preventGuestInteraction);
});

onUnmounted(() => {
  document.removeEventListener("contextmenu", preventGuestInteraction);
  document.removeEventListener("dragstart", preventGuestInteraction);
});
</script>

<style scoped>
#basicLayout {
  background: var(--bg-gradient);
  min-height: 100vh;
}

#basicLayout .content {
  background: transparent;
  padding: 16px;
  max-width: 1400px;
  margin: 0 auto;
  margin-top: 64px; /* Header height offset */
  width: 100%;
  min-width: 0; 
  box-sizing: border-box;
}

#basicLayout .full-screen-content {
  padding: 0;
  margin: 0;
  width: 100%;
  min-width: 0;
  max-width: none;
  background: transparent;
}

#basicLayout .footer {
  background: transparent;
  padding: 32px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.header {
  padding: 0;
  margin-bottom: 0;
  color: unset;
  background: rgba(255, 255, 255, 0.45); /* 降低过度的高斯模糊透明底板层级，使其更清爽 */
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(0,0,0,0.03);
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 100;
  box-shadow: none; /* Removed harsh shadow for cleaner look */
  display: flex;
  align-items: center;
  height: 64px;
}
</style>
