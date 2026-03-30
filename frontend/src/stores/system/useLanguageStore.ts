import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { messages } from '@/locales/messages'

export const useLanguageStore = defineStore('language', () => {
    const normalizeLang = (lang?: string | null) => {
        if (!lang) return 'zh'
        if (lang.startsWith('en')) return 'en'
        if (lang.startsWith('zh')) return 'zh'
        return 'zh'
    }
    // 默认从 localStorage 读取，否则 'zh'
    const currentLang = ref<string>(normalizeLang(localStorage.getItem('app_language')))

    const setLanguage = (lang: string) => {
        const normalized = normalizeLang(lang)
        currentLang.value = normalized
        localStorage.setItem('app_language', normalized)
    }

    const toggleLanguage = () => {
        if (currentLang.value === 'zh') {
            setLanguage('en')
        } else {
            setLanguage('zh')
        }
    }

    // Generic translation function with parameter support
    const t = computed(() => {
        return (key: string, params?: Record<string, any>) => {
            const keys = key.split('.')
            let value: any = messages[currentLang.value as keyof typeof messages]
            for (const k of keys) {
                if (value && value[k] !== undefined) {
                    value = value[k]
                } else {
                    return key // fallback to key
                }
            }

            if (params && typeof value === 'string') {
                Object.keys(params).forEach(p => {
                    value = value.replace(`{${p}}`, params[p])
                })
            }
            return value
        }
    })

    return {
        currentLang,
        setLanguage,
        toggleLanguage,
        t
    }
})
