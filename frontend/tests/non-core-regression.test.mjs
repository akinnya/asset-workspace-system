import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'

const frontendRoot = new URL('..', import.meta.url)

const spaceDetailPath = join(frontendRoot.pathname, 'src/pages/workspace/WorkspaceDetailPage.vue')
const userProfilePath = join(frontendRoot.pathname, 'src/pages/user/UserProfilePage.vue')
const localePath = join(frontendRoot.pathname, 'src/locales/messages.ts')
const typingsPath = join(frontendRoot.pathname, 'src/api/typings.d.ts')
const routerPath = join(frontendRoot.pathname, 'src/router/index.ts')
const globalHeaderPath = join(frontendRoot.pathname, 'src/components/layout/GlobalHeader.vue')
const adminLayoutPath = join(frontendRoot.pathname, 'src/layouts/AdminLayout.vue')
const workDetailPath = join(frontendRoot.pathname, 'src/pages/asset/AssetDetailPage.vue')
const exhibitionPath = join(frontendRoot.pathname, 'src/pages/asset/AssetGalleryPage.vue')
const searchPagePath = join(frontendRoot.pathname, 'src/pages/asset/AssetSearchPage.vue')
const notificationPath = join(frontendRoot.pathname, 'src/pages/notification/NotificationPage.vue')
const userMediaPath = join(frontendRoot.pathname, 'src/pages/user/UserMediaPage.vue')
const characterApiDir = join(frontendRoot.pathname, 'src/api/character')
const characterShowcasePath = join(frontendRoot.pathname, 'src/constants/characterShowcase.ts')

test('前端不应保留已删除子域的 API 与常量文件', () => {
  assert.equal(existsSync(characterApiDir), false, 'src/api/character 目录仍存在')
  assert.equal(existsSync(characterShowcasePath), false, 'src/constants/characterShowcase.ts 仍存在')
})

test('工作区与个人页不应保留条目与关系图字段', () => {
  const spaceDetail = readFileSync(spaceDetailPath, 'utf8')
  const userProfile = readFileSync(userProfilePath, 'utf8')
  const bannedSpaceTokens = [
    'characterList',
    'characterOptions',
    'characterName',
    'characterId',
    'clearCharacterId',
    '/character/detail/',
    "currentTab === 'characters'",
    "currentTab === 'timeline'",
    'timelineEvents',
    'getTimelineDate',
    'socialData',
    'SpaceTimelineEventVO',
  ]

  for (const token of bannedSpaceTokens) {
    assert.equal(spaceDetail.includes(token), false, `WorkspaceDetailPage.vue 仍包含非核心残留: ${token}`)
  }

  assert.equal(
    userProfile.includes('userStats.characterCount'),
    false,
    'UserProfilePage.vue 仍显示 characterCount',
  )
})

test('类型声明不应保留旧子域生成物', () => {
  const typings = readFileSync(typingsPath, 'utf8')
  const bannedTypingTokens = [
    'type SpaceSocialUpdateRequest = {',
    'type SpaceTimelineEventVO = {',
    'type Character = {',
    'type CharacterEvent = {',
    'type PlaylistVO = {',
    'type AssistantChatRequest = {',
    'socialData?: string',
    'characterId?: number',
    'characterCanDownload?: number',
    'myOcs:',
    'characterCount?: number',
  ]

  for (const token of bannedTypingTokens) {
    assert.equal(typings.includes(token), false, `typings.d.ts 仍包含非核心残留: ${token}`)
  }
})

test('多语言文案不应暴露已删除的非核心入口', () => {
  const locale = readFileSync(localePath, 'utf8')
  const bannedLocaleTokens = [
    "artistLab: {",
    "lab: {",
    "myCharacters: '",
    "ocShowcase: '",
    "chatbot: '",
    "desktopPet: '",
    "linkOc: '",
    "linkedCharacter: '",
    "createOc: '",
    "addCharacter: '",
    "addCharacterHint: '",
    "socialTab: '",
    "socialHeading: '",
    "timelineTab: '",
    "timelineHeading: '",
    "pictureCharacter: '",
    "batchCharacterPlaceholder: '",
    "batchClearCharacter: '",
    "timelineTitle: '",
    "noCharacters: '",
    "editSocial: '",
    "socialEditorTitle: '",
    "unknownCharacter: '",
    "timelineEmpty: '",
    "music: {",
    "characters: '条目'",
    "character: '条目'",
    "characters: 'Items'",
    "character: 'Item'",
    "game: '互动区'",
    "game: 'Interactive Zone'",
  ]

  for (const token of bannedLocaleTokens) {
    assert.equal(locale.includes(token), false, `messages.ts 仍包含非核心文案: ${token}`)
  }
})

test('前端不应再暴露旧的 picture 路由路径', () => {
  const files = [
    routerPath,
    globalHeaderPath,
    adminLayoutPath,
    workDetailPath,
    exhibitionPath,
    searchPagePath,
    notificationPath,
    userMediaPath,
    userProfilePath,
    spaceDetailPath,
  ]
  const bannedRouteTokens = [
    "path: '/add_picture'",
    "path: '/add_picture/batch'",
    "path: '/picture/:id'",
    "path: 'picture'",
    "path: '/search'",
    '`/picture/${',
    '`/add_picture?id=${',
    "'/add_picture?id='",
    '`/search?pictureId=${',
    "'/search?pictureId='",
    "'/add_picture'",
    '"/add_picture"',
    "'/add_picture/batch'",
    '"/add_picture/batch"',
    "'/admin/picture'",
    '"/admin/picture"',
    "'/admin/spaceAnalyze'",
    '"/admin/spaceAnalyze"',
    "'/admin/teamSpace'",
    '"/admin/teamSpace"',
    "'/search'",
    '"/search"',
  ]

  for (const file of files) {
    const content = readFileSync(file, 'utf8')
    for (const token of bannedRouteTokens) {
      assert.equal(content.includes(token), false, `${file} 仍包含旧路由语义: ${token}`)
    }
  }
})

test('路由名称和英文文案应统一为 asset/workspace 语义', () => {
  const router = readFileSync(routerPath, 'utf8')
  const locale = readFileSync(localePath, 'utf8')
  const bannedRouterTokens = ['添加图片', '图片详情', '批量生成图片', '图片搜索']
  const bannedLocaleTokens = [
    "totalPictures: '总图片数'",
    "platformPictures: '平台图片'",
    "allPictures: '全部图片'",
    "pictureManage: 'Asset Manage'",
    "name: 'Space Name'",
    "spaceName: 'Space Name'",
    "privateSpaces: 'Private Spaces'",
    "joinedTeams: 'Joined Teams'",
    "shareTitle: 'Share Space'",
    "shareTip: 'Space ID copied. Share it with your collaborators.'",
    "idLabel: 'Space ID'",
    "copyIdSuccess: 'Space ID copied'",
    "viewAll: 'Go to My Spaces'",
    "requestJoinTitle: 'Request to Join Space'",
    "joinModalTitle: 'Join Space'",
    "enterSpaceIdRequired: 'Please enter Space ID'",
    "uploadArt: 'Upload Art'",
    "enterId: 'Enter Space ID'",
    "pictureTab: 'Pictures'",
    "delete: 'Delete Space'",
    "deleteConfirmTitle: 'Delete this space?'",
    "totalPictures: 'Total Pictures'",
    "platformPictures: 'Platform Pictures'",
    "allPictures: 'All Pictures'",
  ]

  for (const token of bannedRouterTokens) {
    assert.equal(router.includes(token), false, `router/index.ts 仍包含旧名称: ${token}`)
  }

  for (const token of bannedLocaleTokens) {
    assert.equal(locale.includes(token), false, `messages.ts 仍包含旧语义文案: ${token}`)
  }
})
