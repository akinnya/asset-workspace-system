<template>
  <a-config-provider :locale="appLocale">
  <div id="app">
    <BasicLayout />
  </div>
  </a-config-provider>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import enUS from 'ant-design-vue/es/locale/en_US';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import 'dayjs/locale/en';
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { computed } from 'vue';

const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()

const appLocale = computed(() => {
  if (languageStore.currentLang === 'en') {
      dayjs.locale('en');
      return enUS;
  }
  dayjs.locale('zh-cn');
  return zhCN;
})
</script>

<style scoped></style>
