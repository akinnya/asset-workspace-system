import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUserUsingGet, refreshTokenUsingPost } from '@/api/user/userController'
import ACCESS_ENUM from '@/access/accessEnum'

const GUEST_USER: API.LoginUserVO = {
  userName: '未登录',
  userRole: ACCESS_ENUM.NOT_LOGIN,
}

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>(GUEST_USER)

  function clearLocalTokens() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  function hasLocalToken() {
    return !!localStorage.getItem('accessToken') || !!localStorage.getItem('refreshToken')
  }

  async function refreshLoginUserByToken() {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      return false
    }
    const refreshRes = await refreshTokenUsingPost({ refreshToken })
    if (refreshRes.data.code === 0 && refreshRes.data.data) {
      setLoginUser(refreshRes.data.data)
      return true
    }
    return false
  }

  async function fetchLoginUser() {
    if (!hasLocalToken()) {
      loginUser.value = GUEST_USER
      return
    }
    try {
      const res = await getLoginUserUsingGet()
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = {
          ...res.data.data,
          accessToken: localStorage.getItem('accessToken') || undefined,
          refreshToken: localStorage.getItem('refreshToken') || undefined,
        }
      } else {
        const refreshed = await refreshLoginUserByToken()
        if (!refreshed) {
          loginUser.value = GUEST_USER
          clearLocalTokens()
        }
      }
    } catch (error) {
      try {
        const refreshed = await refreshLoginUserByToken()
        if (!refreshed) {
          loginUser.value = GUEST_USER
          clearLocalTokens()
        }
      } catch (refreshError) {
        loginUser.value = GUEST_USER
        clearLocalTokens()
      }
    }
  }


  function setLoginUser(newLoginUser: any) {
    if (!newLoginUser || !newLoginUser.id) {
      loginUser.value = GUEST_USER
      clearLocalTokens()
      return
    }
    loginUser.value = newLoginUser
    if (newLoginUser.accessToken) {
      localStorage.setItem('accessToken', newLoginUser.accessToken)
    }
    if (newLoginUser.refreshToken) {
      localStorage.setItem('refreshToken', newLoginUser.refreshToken)
    }
  }

  function logout() {
    loginUser.value = GUEST_USER
    clearLocalTokens()
  }

  return { loginUser, setLoginUser, fetchLoginUser, logout }
})
