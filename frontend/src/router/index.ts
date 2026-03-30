import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import ACCESS_ENUM from '@/access/accessEnum'

const isCapacitorRuntime = import.meta.env.VITE_APP_RUNTIME === 'capacitor'

const router = createRouter({
  history: isCapacitorRuntime
    ? createWebHashHistory(import.meta.env.BASE_URL)
    : createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '展览',
      component: () => import('@/pages/asset/AssetGalleryPage.vue'),
      meta: {
        hideHeader: false,
        fullScreen: true,
      }
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: () => import('@/pages/user/UserLoginPage.vue'),
      meta: {
        hideHeader: true,
        fullScreen: true,
      }
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('@/pages/user/UserRegisterPage.vue'),
      meta: {
        hideHeader: true,
        fullScreen: true,
      }
    },
    {
      path: '/user/profile/:id?',
      name: '用户主页',
      component: () => import('@/pages/user/UserProfilePage.vue'),
      meta: {
        access: ACCESS_ENUM.LOGIN,
      },
    },
    {
      path: '/user/settings',
      name: '个人设置',
      component: () => import('@/pages/user/UserSettingsPage.vue'),
      meta: {
        access: ACCESS_ENUM.LOGIN,
      },
    },
    {
      path: '/user/media',
      name: '媒体管理',
      component: () => import('@/pages/user/UserMediaPage.vue'),
      meta: {
        access: ACCESS_ENUM.LOGIN,
        hideHeader: true,
        fullScreen: true,
      },
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN,
        hideHeader: true,
        fullScreen: true,
      },
      children: [
        {
          path: '',
          name: 'AdminDashboard',
          component: () => import('@/pages/admin/AdminHomePage.vue'),
        },
        {
          path: 'analytics',
          name: 'AdminAnalytics',
          component: () => import('@/pages/admin/AdminAnalyticsPage.vue'),
        },
        {
          path: 'assets',
          name: 'AdminAssetManage',
          component: () => import('@/pages/admin/AssetManagePage.vue'),
        },
        {
          path: 'user',
          name: 'AdminUserManage',
          component: () => import('@/pages/admin/UserManagePage.vue'),
        },
        {
          path: 'workspace-analytics',
          name: 'WorkspaceAnalytics',
          component: () => import('@/pages/admin/WorkspaceAnalyticsPage.vue'),
        },
        {
          path: 'workspaces',
          name: 'WorkspaceManage',
          component: () => import('@/pages/admin/WorkspaceManagePage.vue'),
        },
        {
          path: 'recycle',
          name: 'RecycleBin',
          component: () => import('@/pages/admin/RecycleBinPage.vue'),
        },
        {
          path: 'logs',
          name: 'SensitiveLogManage',
          component: () => import('@/pages/admin/SensitiveLogPage.vue'),
        }
      ]
    },
    {
      path: '/asset/upload',
      name: '上传资产',
      component: () => import('@/pages/asset/AssetUploadPage.vue'),
      meta: {
        access: ACCESS_ENUM.LOGIN,
      },
    },
    {
      path: '/asset/:id',
      name: '资产详情',
      component: () => import('@/pages/asset/AssetDetailPage.vue'),
      props: true,
    },
    {
      path: '/asset/upload/batch',
      name: '批量导入资产',
      component: () => import('@/pages/asset/AssetBatchUploadPage.vue'),
      meta: {
        access: ACCESS_ENUM.LOGIN,
      },
    },
    // Recycle Bin moved to Admin Layout
    {
      path: '/asset/search',
      name: '资产搜索',
      component: () => import('@/pages/asset/AssetSearchPage.vue'),
    },
    {
      path: '/notifications',
      name: '通知中心',
      component: () => import('@/pages/notification/NotificationPage.vue'),
      meta: {
        access: ACCESS_ENUM.LOGIN,
      },
    },
    {
      path: '/workspace/add',
      name: '创建工作区',
      component: () => import('@/pages/workspace/CreateWorkspacePage.vue'),
      meta: {
        access: ACCESS_ENUM.LOGIN,
      },
    },
    {
      path: '/workspace/my',
      name: '我的工作区',
      component: () => import('@/pages/workspace/MyWorkspacePage.vue'),
      meta: {
        access: ACCESS_ENUM.LOGIN,
      },
    },
    {
      path: '/workspace/detail/:id',
      name: '工作区详情',
      component: () => import('@/pages/workspace/WorkspaceDetailPage.vue'),
    },
    {
      path: '/workspace/invite/:id',
      name: '工作区邀请',
      component: () => import('@/pages/workspace/WorkspaceInvitePage.vue'),
    },
  ],
})

export default router
