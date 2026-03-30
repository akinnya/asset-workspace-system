<template>
  <transition name="fade">
    <div v-if="loading" class="mochi-loading-mask fixed inset-0 z-[9999] flex flex-col items-center justify-center bg-white/60 backdrop-blur-md">
      <div class="mochi-loader relative w-16 h-16 bg-primary rounded-3xl shadow-glow overflow-hidden mb-4">
        <div class="mochi-eyes">
          <div class="eye"></div>
          <div class="eye"></div>
        </div>
      </div>
      <div class="loading-text text-primary font-black tracking-widest uppercase text-sm animate-pulse">
        {{ message || 'Loading Content...' }}
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
defineProps<{
  loading: boolean
  message?: string
}>()
</script>

<style scoped>
.mochi-eyes {
  display: flex;
  gap: 12px;
  position: absolute;
  top: 15px;
  left: 18px;
}

.eye {
  width: 6px;
  height: 6px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.loading-text {
  color: var(--primary-color);
  margin-top: 10px;
}
</style>
