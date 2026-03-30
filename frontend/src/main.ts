import { createApp } from 'vue'
import type { DirectiveBinding } from 'vue'
import { createPinia } from 'pinia'
import {
    Avatar,
    Button,
    Checkbox,
    ConfigProvider,
    Drawer,
    Dropdown,
    Empty,
    Form,
    FormItem,
    Image,
    Input,
    InputGroup,
    InputNumber,
    InputSearch,
    Layout,
    LayoutContent,
    LayoutFooter,
    LayoutHeader,
    List,
    ListItem,
    Menu,
    MenuDivider,
    MenuItem,
    Modal,
    Pagination,
    Popconfirm,
    Popover,
    RadioButton,
    RadioGroup,
    Select,
    SelectOption,
    Slider,
    Space,
    Spin,
    Switch,
    Table,
    Tabs,
    TabPane,
    Tag,
    Textarea,
    Tooltip,
    Upload,
    UploadDragger,
    message as antMessage,
    notification as antNotification,
} from 'ant-design-vue'

import App from './App.vue'
import router from './router'
import 'ant-design-vue/dist/reset.css'
import '@/access'
import './assets/main.css'



const app = createApp(App)

const IMG_FALLBACK_APPLIED_FLAG = 'imgFallbackApplied'
const IMG_FALLBACK_HANDLER_KEY = '__imgFallbackHandler'

const resetImgFallbackState = (img: HTMLImageElement) => {
    img.dataset[IMG_FALLBACK_APPLIED_FLAG] = '0'
}

const bindImgFallback = (img: HTMLImageElement, fallbackUrl?: string) => {
    const normalizedFallback = typeof fallbackUrl === 'string' ? fallbackUrl.trim() : ''
    if (normalizedFallback) {
        img.dataset.fallbackSrc = normalizedFallback
    } else {
        delete img.dataset.fallbackSrc
    }
    resetImgFallbackState(img)
}

app.directive('img-fallback', {
    mounted(el: HTMLElement, binding: DirectiveBinding<string | undefined>) {
        if (!(el instanceof HTMLImageElement)) {
            return
        }
        bindImgFallback(el, binding.value)
        const onError = () => {
            const fallbackSrc = el.dataset.fallbackSrc
            if (!fallbackSrc) {
                return
            }
            if (el.dataset[IMG_FALLBACK_APPLIED_FLAG] === '1') {
                return
            }
            if (el.currentSrc === fallbackSrc || el.src === fallbackSrc) {
                el.dataset[IMG_FALLBACK_APPLIED_FLAG] = '1'
                return
            }
            el.dataset[IMG_FALLBACK_APPLIED_FLAG] = '1'
            el.src = fallbackSrc
        }
        ;(el as any)[IMG_FALLBACK_HANDLER_KEY] = onError
        el.addEventListener('error', onError)
    },
    updated(el: HTMLElement, binding: DirectiveBinding<string | undefined>) {
        if (!(el instanceof HTMLImageElement)) {
            return
        }
        bindImgFallback(el, binding.value)
    },
    unmounted(el: HTMLElement) {
        if (!(el instanceof HTMLImageElement)) {
            return
        }
        const onError = (el as any)[IMG_FALLBACK_HANDLER_KEY]
        if (typeof onError === 'function') {
            el.removeEventListener('error', onError)
        }
        delete (el as any)[IMG_FALLBACK_HANDLER_KEY]
    },
})

const antComponents = [
    Avatar,
    Button,
    Checkbox,
    ConfigProvider,
    Drawer,
    Dropdown,
    Empty,
    Form,
    FormItem,
    Image,
    Input,
    InputGroup,
    InputNumber,
    InputSearch,
    Layout,
    LayoutContent,
    LayoutFooter,
    LayoutHeader,
    List,
    ListItem,
    Menu,
    MenuDivider,
    MenuItem,
    Modal,
    Pagination,
    Popconfirm,
    Popover,
    RadioButton,
    RadioGroup,
    Select,
    SelectOption,
    Slider,
    Space,
    Spin,
    Switch,
    Table,
    Tabs,
    TabPane,
    Tag,
    Textarea,
    Tooltip,
    Upload,
    UploadDragger,
]
antComponents.forEach((component) => app.use(component))

antMessage.config({
    duration: 3, // Set notification hide time to 3 seconds for better readability on mobile
})
antNotification.config({
    duration: 4,
})
app.use(createPinia())
app.use(router)

app.mount('#app')

const isProtectedMediaTarget = (target: EventTarget | null) => {
    if (!target || !(target instanceof Element)) return false
    const media = target.closest('img, video')
    if (!media) return false
    if (media.getAttribute('data-allow-touch') === 'true') return false
    return true
}

// 禁止图片右键菜单（移动端长按）
document.addEventListener('contextmenu', (event) => {
    if (isProtectedMediaTarget(event.target)) {
        event.preventDefault()
    }
}, { capture: true })
