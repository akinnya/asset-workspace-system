import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'

const frontendRoot = new URL('..', import.meta.url)

const brandFiles = [
  'src/pages/user/UserLoginPage.vue',
  'src/pages/user/UserProfilePage.vue',
  'src/pages/user/UserSettingsPage.vue',
  'src/pages/user/UserMediaPage.vue',
  'src/pages/asset/AssetSearchPage.vue',
  'src/pages/asset/AssetUploadPage.vue',
  'src/pages/asset/AssetDetailPage.vue',
]

const legacyBrandTokens = [
  '#F0F8FF',
  '#3b82f6',
  'bg-blue-500',
  'to-blue-400',
  'to-sky-300',
  'bg-sky-50',
  'bg-sky-100',
  'text-sky-600',
  'shadow-blue-500/5',
  '#6daeef',
  'rgba(109,174,239,0.5)',
  'rgba(109, 174, 239',
  'rgba(14, 165, 233',
  'rgba(59,130,246,0.5)',
]

test('资产端入口页面不应残留旧蓝色品牌 token', () => {
  for (const relativePath of brandFiles) {
    const content = readFileSync(join(frontendRoot.pathname, relativePath), 'utf8')

    for (const token of legacyBrandTokens) {
      assert.equal(
        content.includes(token),
        false,
        `${relativePath} 仍包含旧蓝色品牌 token: ${token}`,
      )
    }
  }
})
