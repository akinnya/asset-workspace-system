import router from '@/router'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import ACCESS_ENUM from './accessEnum'
import checkAccess from './checkAccess'

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN

  let loginUser = loginUserStore.loginUser
  const hasLocalToken =
    !!localStorage.getItem('accessToken') || !!localStorage.getItem('refreshToken')
  // 只要本地仍有令牌且当前是游客态，就优先尝试恢复登录态，避免刷新公开页后丢失状态
  const shouldRestoreLoginUser =
    hasLocalToken &&
    (!loginUser?.id || !loginUser?.userRole || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN)
  if (shouldRestoreLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
  }
  console.log('登陆用户信息', loginUser)

  // 要跳转的页面必须要登陆
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    // 如果没登陆，跳转到登录页面
    if (!loginUser || !loginUser.userRole || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
    // 如果已经登陆了，但是权限不足，那么跳转到无权限页面
    if (!checkAccess(loginUser, needAccess)) {
      next('/noAuth')
      return
    }
  }
  next()
})
