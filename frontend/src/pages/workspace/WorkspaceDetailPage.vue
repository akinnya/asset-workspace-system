<template>
  <div id="spaceDetailPage" class="min-h-screen bg-slate-50 relative pb-20 font-display">
    <!-- Hero / Worldview Banner Section -->
    <div class="relative h-[40vh] sm:h-[50vh] lg:h-[60vh] overflow-hidden group">
        <!-- Background Cover -->
        <div v-if="space.coverUrl" class="absolute inset-0 transition-transform duration-1000 group-hover:scale-105">
            <img :src="space.coverUrl" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/40 to-transparent"></div>
        </div>
        <div v-else class="absolute inset-0 bg-gradient-to-br from-slate-800 to-primary">
            <div class="absolute inset-0 opacity-20 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')]"></div>
        </div>

        <!-- Hero Content Overlay -->
        <div class="absolute inset-0 flex flex-col justify-end px-6 md:px-12 pb-12 sm:pb-16 pt-20 max-w-7xl mx-auto w-full">
            <div class="flex flex-col lg:flex-row justify-between items-end gap-8 relative z-10">
                <div class="max-w-3xl">
                    <h1 class="text-3xl sm:text-5xl lg:text-7xl font-black text-white mb-4 tracking-tighter drop-shadow-2xl">
                        {{ space.spaceName }}
                    </h1>
                    <p class="text-slate-200/80 text-sm sm:text-lg font-medium leading-relaxed line-clamp-2 max-w-2xl drop-shadow-md">
                        {{ space.introduction || t('space.detail.noIntro') }}
                    </p>
                </div>
                
                <!-- Action Buttons -->
                <div class="flex flex-wrap items-center gap-3 w-full lg:w-auto">
                    <button
                        v-if="canManageMembers"
                        class="px-8 py-3 rounded-2xl bg-primary text-white font-black shadow-glow hover:scale-105 active:scale-95 transition-all flex items-center gap-2"
                        @click="openWorkspaceInvite"
                    >
                        <PlusOutlined /> {{ t('space.detail.inviteMembers') }}
                    </button>
                    <button
                        v-if="canManageSpace"
                        class="px-8 py-3 rounded-2xl bg-white/90 text-slate-700 font-black shadow-sm hover:scale-105 active:scale-95 transition-all flex items-center gap-2"
                        @click="handleExportSpace"
                    >
                        {{ t('space.detail.exportZip') }}
                    </button>
                    <button
                        v-if="isSpaceOwner"
                        class="px-8 py-3 rounded-2xl bg-rose-500 text-white font-black shadow-glow hover:scale-105 active:scale-95 transition-all flex items-center gap-2"
                        @click="confirmDeleteSpace"
                    >
                        <DeleteOutlined /> {{ t('space.detail.delete') }}
                    </button>
                </div>
            </div>
            
            <!-- Creator Info in Hero -->
            <div class="mt-8 flex items-center gap-3 pt-8 border-t border-white/10 w-fit">
                <div class="w-10 h-10 rounded-full border-2 border-white/20 overflow-hidden bg-slate-800">
                    <img v-if="space.user?.userAvatar" :src="space.user.userAvatar" class="w-full h-full object-cover" />
                </div>
                <div class="flex flex-col">
                    <span class="text-[10px] font-black text-slate-200/50 uppercase tracking-widest">{{ t('space.creator') }}</span>
                    <span class="text-sm font-bold text-white">{{ space.user?.userName || t('space.detail.creatorFallback') }}</span>
                </div>
            </div>
        </div>
    </div>

    <!-- Main Content Area -->
    <div class="max-w-7xl mx-auto px-6 md:px-12 py-10 relative z-20">
        <div class="flex flex-wrap items-center gap-3 mb-8">
            <button
                v-for="tab in tabs"
                :key="tab.key"
                :class="`px-5 py-2.5 rounded-full transition-all flex items-center gap-2 text-sm font-bold ${currentTab === tab.key ? 'bg-primary text-white shadow-glow shadow-primary/30' : 'bg-white/70 hover:bg-white text-slate-600 border border-white/70'}`"
                @click="currentTab = tab.key"
            >
                <span class="material-symbols-outlined text-[18px]">{{ tab.icon }}</span>
                {{ tab.label }}
            </button>
        </div>

        <!-- Pictures -->
        <div v-if="currentTab === 'pictures'">
            <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-8">
                <div>
                    <h2 class="text-2xl font-black text-slate-800 tracking-tight flex items-center gap-3">
                        <div class="w-2 h-8 bg-sky-500 rounded-full"></div>
                        {{ t('space.detail.pictureTab') }}
                    </h2>
                    <p class="text-xs font-semibold text-slate-400 mt-2">{{ t('space.detail.pictureCount', { total: filteredSpacePictures.length }) }}</p>
                </div>
                <div class="flex flex-wrap items-center gap-3">
                    <button
                        class="px-4 py-2 rounded-full bg-white/70 hover:bg-white text-slate-600 font-semibold border border-white/70 shadow-sm"
                        :disabled="spacePictureLoading"
                        @click="fetchSpacePictures"
                    >
                        {{ t('space.detail.pictureRefresh') }}
                    </button>
                    <button
                        class="px-4 py-2 rounded-full font-semibold border shadow-sm transition-all"
                        :class="batchMode ? 'bg-primary text-white border-primary shadow-glow' : 'bg-white/70 hover:bg-white text-slate-600 border-white/70'"
                        @click="toggleBatchMode"
                    >
                        {{ batchMode ? t('space.detail.exitBatchMode') : t('space.detail.batchManage') }}
                    </button>
                </div>
            </div>

            <div class="bg-white/80 rounded-[28px] border border-white shadow-sm p-4 md:p-5 mb-6">
                <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-7 gap-3">
                    <a-input
                        v-model:value="spacePictureFilters.keyword"
                        :placeholder="t('space.detail.pictureSearchPlaceholder')"
                        class="!rounded-xl"
                    />
                    <a-select
                        v-model:value="spacePictureFilters.category"
                        allow-clear
                        :placeholder="t('space.detail.pictureCategory')"
                        :options="spaceCategoryOptions"
                        class="w-full"
                    />
                    <a-select
                        v-model:value="spacePictureFilters.tag"
                        allow-clear
                        :placeholder="t('space.detail.pictureTag')"
                        :options="spaceTagOptions"
                        class="w-full"
                    />
                    <a-select
                        v-model:value="spacePictureFilters.fileType"
                        :options="spacePictureTypeOptions"
                        class="w-full"
                    />
                    <a-select
                        v-model:value="spacePictureFilters.uploaderId"
                        allow-clear
                        :placeholder="t('space.detail.pictureUploader')"
                        :options="spaceUploaderOptions"
                        class="w-full"
                    />
                    <a-select
                        v-model:value="spacePictureFilters.sortBy"
                        :placeholder="t('space.detail.pictureSort')"
                        :options="spacePictureSortOptions"
                        class="w-full"
                    />
                </div>
                <div class="mt-4 flex justify-end">
                    <button
                        class="px-4 py-2 rounded-xl bg-slate-100 text-slate-500 text-sm font-semibold hover:bg-slate-200"
                        @click="clearSpacePictureFilters"
                    >
                        {{ t('space.detail.clearFilters') }}
                    </button>
                </div>
            </div>

            <div v-if="batchMode" class="bg-white/85 rounded-[28px] border border-white shadow-sm p-4 md:p-5 mb-6 space-y-4">
                <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3">
                    <div>
                        <p class="text-sm font-black text-slate-800">{{ t('space.detail.batchToolbar') }}</p>
                        <p class="text-xs text-slate-400">
                            {{ t('space.detail.batchSelectedCount', { total: selectedSpacePictureIds.length }) }}
                        </p>
                    </div>
                    <div class="flex flex-wrap items-center gap-3">
                        <button
                            class="px-4 py-2 rounded-xl bg-slate-100 text-slate-600 text-sm font-semibold hover:bg-slate-200"
                            @click="selectAllFilteredPictures"
                        >
                            {{ t('space.detail.batchSelectAll') }}
                        </button>
                        <button
                            class="px-4 py-2 rounded-xl bg-slate-100 text-slate-600 text-sm font-semibold hover:bg-slate-200"
                            :disabled="selectedSpacePictureIds.length === 0"
                            @click="clearSelectedSpacePictures"
                        >
                            {{ t('space.detail.batchClearSelection') }}
                        </button>
                        <button
                            v-if="canBatchEditSpacePictures"
                            class="px-4 py-2 rounded-xl bg-primary/10 text-primary text-sm font-semibold hover:bg-primary/15"
                            :disabled="selectedSpacePictureIds.length === 0"
                            @click="openBatchEditModal"
                        >
                            {{ t('space.detail.batchEdit') }}
                        </button>
                        <button
                            v-if="canBatchEditSpacePictures"
                            class="px-4 py-2 rounded-xl bg-amber-100 text-amber-700 text-sm font-semibold hover:bg-amber-200"
                            :disabled="selectedSpacePictureIds.length === 0 || batchSharing"
                            @click="batchGenerateShareLinks"
                        >
                            {{ batchSharing ? t('space.detail.batchSharing') : t('space.detail.batchShare') }}
                        </button>
                        <button
                            v-if="canBatchEditSpacePictures"
                            class="px-4 py-2 rounded-xl bg-slate-200 text-slate-700 text-sm font-semibold hover:bg-slate-300"
                            :disabled="selectedSpacePictureIds.length === 0 || batchUnsharing"
                            @click="batchClearShareLinks"
                        >
                            {{ batchUnsharing ? t('space.detail.batchUnsharing') : t('space.detail.batchUnshare') }}
                        </button>
                        <button
                            class="px-4 py-2 rounded-xl bg-sky-100 text-sky-600 text-sm font-semibold hover:bg-sky-200"
                            :disabled="selectedSpacePictureIds.length === 0 || batchDownloading"
                            @click="batchDownloadSelectedPictures"
                        >
                            {{ batchDownloading ? t('space.detail.batchDownloading') : t('space.detail.batchDownload') }}
                        </button>
                        <button
                            v-if="canManageSpace"
                            class="px-4 py-2 rounded-xl bg-rose-100 text-rose-600 text-sm font-semibold hover:bg-rose-200"
                            :disabled="selectedSpacePictureIds.length === 0 || batchDeleting"
                            @click="batchDeleteSelectedPictures"
                        >
                            {{ batchDeleting ? t('space.detail.batchDeleting') : t('space.detail.batchDelete') }}
                        </button>
                    </div>
                </div>
                <p class="text-xs text-slate-400">{{ t('space.detail.batchModeHint') }}</p>
            </div>

            <div v-if="spacePictureLoading" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-5">
                <div v-for="i in 6" :key="i" class="aspect-[4/3] rounded-[28px] bg-white animate-pulse"></div>
            </div>
            <div v-else-if="filteredSpacePictures.length === 0" class="py-24 text-center bg-white rounded-[40px] border-2 border-dashed border-slate-100">
                <p class="text-slate-400 font-bold">{{ t('space.detail.pictureEmpty') }}</p>
            </div>
            <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-5">
                <div
                    v-for="pic in filteredSpacePictures"
                    :key="pic.id"
                    class="group relative bg-white/90 rounded-[28px] overflow-hidden border shadow-sm transition-all cursor-pointer"
                    :class="isSpacePictureSelected(pic) ? 'border-primary shadow-xl ring-4 ring-primary/10' : 'border-white hover:shadow-xl'"
                    @click="handleSpacePictureCardClick(pic)"
                >
                    <div
                        v-if="!batchMode && (canQuickEditSpacePicture(pic) || canQuickDeleteSpacePicture(pic))"
                        class="absolute top-3 left-3 z-20 flex flex-wrap gap-2 opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                        <button
                            v-if="canQuickEditSpacePicture(pic)"
                            class="w-9 h-9 rounded-2xl bg-white/90 text-slate-600 shadow-sm backdrop-blur hover:bg-white hover:text-primary flex items-center justify-center"
                            :title="t('space.detail.quickEditTitle')"
                            @click.stop="quickEditSpacePicture(pic)"
                        >
                            <span class="material-symbols-outlined text-[18px]">edit</span>
                        </button>
                        <button
                            v-if="canQuickShareSpacePicture(pic)"
                            class="w-9 h-9 rounded-2xl shadow-sm backdrop-blur flex items-center justify-center transition-colors"
                            :class="pic.shareCode ? 'bg-amber-100/95 text-amber-700 hover:bg-amber-200' : 'bg-white/90 text-slate-600 hover:bg-white hover:text-amber-600'"
                            :title="pic.shareCode ? t('space.detail.quickUnshareTitle') : t('space.detail.quickShareTitle')"
                            @click.stop="toggleSpacePictureShare(pic)"
                        >
                            <span class="material-symbols-outlined text-[18px]">{{ pic.shareCode ? 'lock_open' : 'share' }}</span>
                        </button>
                        <button
                            v-if="pic.shareCode"
                            class="w-9 h-9 rounded-2xl bg-white/90 text-slate-600 shadow-sm backdrop-blur hover:bg-white hover:text-sky-600 flex items-center justify-center"
                            :title="t('space.detail.quickCopyLinkTitle')"
                            @click.stop="copySpacePictureShareLink(pic)"
                        >
                            <span class="material-symbols-outlined text-[18px]">link</span>
                        </button>
                        <button
                            v-if="canQuickDeleteSpacePicture(pic)"
                            class="w-9 h-9 rounded-2xl bg-white/90 text-slate-600 shadow-sm backdrop-blur hover:bg-rose-100 hover:text-rose-600 flex items-center justify-center"
                            :title="t('space.detail.quickDeleteTitle')"
                            @click.stop="deleteSpacePictureQuick(pic)"
                        >
                            <span class="material-symbols-outlined text-[18px]">delete</span>
                        </button>
                    </div>
                    <button
                        v-if="batchMode"
                        class="absolute top-3 right-3 z-20 w-9 h-9 rounded-2xl border border-white/80 shadow-sm flex items-center justify-center transition-all"
                        :class="isSpacePictureSelected(pic) ? 'bg-primary text-white' : 'bg-white/90 text-slate-400 hover:text-primary'"
                        @click.stop="toggleSpacePictureSelection(pic)"
                    >
                        <CheckOutlined />
                    </button>
                    <div class="aspect-[4/3] overflow-hidden bg-slate-100">
                        <img
                            :src="pic.thumbnailUrl || pic.url"
                            v-img-fallback="pic.url"
                            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                        />
                    </div>
                    <div class="p-4 md:p-5">
                        <div class="flex items-start justify-between gap-3 mb-3">
                            <div class="min-w-0">
                                <p class="font-black text-slate-800 truncate">{{ pic.name || `#${pic.id}` }}</p>
                                <div class="flex flex-wrap gap-2 mt-2">
                                    <span class="px-2.5 py-1 rounded-full bg-slate-100 text-slate-500 text-[11px] font-bold whitespace-nowrap">
                                        {{ getSpacePictureTypeLabel(pic) }}
                                    </span>
                                    <span
                                        v-if="getSpacePictureStatusLabel(pic)"
                                        class="px-2.5 py-1 rounded-full text-[11px] font-bold whitespace-nowrap"
                                        :class="getSpacePictureStatusClass(pic)"
                                    >
                                        {{ getSpacePictureStatusLabel(pic) }}
                                    </span>
                                    <span v-if="pic.shareCode" class="px-2.5 py-1 rounded-full bg-amber-100 text-amber-700 text-[11px] font-bold whitespace-nowrap">
                                        {{ t('space.detail.pictureShared') }}
                                    </span>
                                </div>
                            </div>
                            <div class="flex flex-col items-end gap-2 shrink-0">
                                <span v-if="pic.category" class="px-2.5 py-1 rounded-full bg-slate-100 text-slate-500 text-[11px] font-bold whitespace-nowrap">
                                    {{ pic.category }}
                                </span>
                            </div>
                        </div>
                        <p v-if="pic.introduction" class="text-sm text-slate-500 leading-relaxed line-clamp-2 min-h-[2.75rem]">
                            {{ pic.introduction }}
                        </p>
                        <div v-if="Array.isArray(pic.tags) && pic.tags.length" class="flex flex-wrap gap-2 mt-3">
                            <span
                                v-for="tag in pic.tags.slice(0, 3)"
                                :key="tag"
                                class="px-2.5 py-1 rounded-full bg-primary/10 text-primary text-[11px] font-bold"
                            >
                                #{{ tag }}
                            </span>
                        </div>
                        <div class="mt-4 pt-4 border-t border-slate-100 space-y-2 text-xs text-slate-400 font-semibold">
                            <div class="flex items-center justify-between gap-3">
                                <span>{{ t('space.detail.pictureBy') }}</span>
                                <span class="truncate text-right">{{ getSpacePictureUploaderLabel(pic) }}</span>
                            </div>
                            <div class="flex items-center justify-between gap-3">
                                <span>{{ t('space.detail.pictureUpdatedAt') }}</span>
                                <span>{{ formatDate(pic.editTime || pic.createTime) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Activity -->
        <div v-else-if="currentTab === 'activity'">
            <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-8">
                <div>
                    <h2 class="text-2xl font-black text-slate-800 tracking-tight flex items-center gap-3">
                        <div class="w-2 h-8 bg-cyan-500 rounded-full"></div>
                        {{ t('space.detail.activityHeading') }}
                    </h2>
                    <p class="text-xs font-semibold text-slate-400 mt-2">{{ t('space.detail.activitySubtitle') }}</p>
                </div>
                <div class="flex flex-wrap items-center gap-3">
                    <a-input
                        v-model:value="activityFilters.keyword"
                        :placeholder="t('space.detail.activityKeyword')"
                        class="w-[220px]"
                        allow-clear
                    />
                    <a-select
                        v-model:value="activityFilters.scope"
                        :options="activityScopeOptions"
                        class="w-[180px]"
                    />
                    <a-select
                        v-model:value="activityFilters.type"
                        :options="activityTypeOptions"
                        class="w-[180px]"
                    />
                    <a-select
                        v-model:value="activityFilters.range"
                        :options="activityRangeOptions"
                        class="w-[180px]"
                    />
                    <button
                        class="px-4 py-2 rounded-full bg-white/70 hover:bg-white text-slate-600 font-semibold border border-white/70 shadow-sm"
                        :disabled="activityLoading"
                        @click="fetchSpaceActivities"
                    >
                        {{ t('space.detail.activityRefresh') }}
                    </button>
                    <button
                        class="px-4 py-2 rounded-full bg-cyan-100 hover:bg-cyan-200 text-cyan-700 font-semibold border border-cyan-200 shadow-sm"
                        :disabled="activityLoading || filteredSpaceActivities.length === 0"
                        @click="exportSpaceActivities"
                    >
                        {{ t('space.detail.activityExport') }}
                    </button>
                </div>
            </div>

            <div v-if="activityLoading" class="space-y-4">
                <div v-for="i in 4" :key="`activity-loading-${i}`" class="h-24 rounded-2xl bg-white animate-pulse"></div>
            </div>
            <div v-else-if="filteredSpaceActivities.length === 0" class="py-24 text-center bg-white rounded-[40px] border-2 border-dashed border-slate-100">
                <p class="text-slate-400 font-bold">{{ t('space.detail.activityEmpty') }}</p>
            </div>
            <div v-else class="space-y-4">
                <div
                    v-for="activity in filteredSpaceActivities"
                    :key="`${activity.activityType}-${activity.id}`"
                    class="bg-white/90 rounded-2xl p-4 md:p-5 border border-white shadow-sm hover:shadow-md transition-all cursor-pointer"
                    :class="activity.pictureId ? 'cursor-pointer hover:shadow-md' : 'cursor-default'"
                    @click="activity.pictureId ? openSpacePictureDetail({ id: activity.pictureId } as API.PictureVO) : undefined"
                >
                    <div class="flex gap-4">
                        <div class="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0" :class="getSpaceActivityIconClass(activity)">
                            <span class="material-symbols-outlined text-[20px]">{{ getSpaceActivityIcon(activity) }}</span>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-3">
                                <div class="min-w-0">
                                    <div class="flex items-center gap-3 flex-wrap">
                                        <div class="w-9 h-9 rounded-full overflow-hidden bg-slate-100">
                                            <img :src="activity.actorUser?.userAvatar || defaultAvatar" class="w-full h-full object-cover" />
                                        </div>
                                        <div class="min-w-0">
                                            <p class="font-bold text-slate-800 truncate">{{ getSpaceActivitySummary(activity) }}</p>
                                            <p class="text-xs text-slate-400 truncate">{{ getSpaceActivityLabel(activity) }}</p>
                                        </div>
                                    </div>
                                    <p v-if="activity.detail" class="text-sm text-slate-600 mt-3 leading-relaxed break-words">
                                        {{ activity.detail }}
                                    </p>
                                </div>
                                <span class="text-xs font-semibold text-slate-400 whitespace-nowrap">{{ formatDateTime(activity.createTime) }}</span>
                            </div>
                            <div v-if="activity.pictureId" class="mt-4 pt-4 border-t border-slate-100 flex items-center gap-3">
                                <div class="w-14 h-14 rounded-xl overflow-hidden bg-slate-100 shrink-0">
                                    <img :src="activity.pictureThumbnailUrl || activity.pictureUrl" v-img-fallback="activity.pictureUrl" class="w-full h-full object-cover" />
                                </div>
                                <div class="min-w-0">
                                    <p class="text-sm font-bold text-slate-700 truncate">{{ activity.pictureName || `#${activity.pictureId}` }}</p>
                                    <p class="text-xs text-slate-400">#{{ activity.pictureId }}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Members -->
        <div v-else>
            <div class="flex justify-between items-center mb-8">
                <h2 class="text-2xl font-black text-slate-800 tracking-tight flex items-center gap-3">
                    <div class="w-2 h-8 bg-rose-500 rounded-full"></div>
                    {{ t('space.detail.memberTab') }}
                </h2>
                <span class="text-xs font-semibold text-slate-400">{{ t('space.detail.manageMembers') }}</span>
            </div>

            <div v-if="canManageMembers" class="bg-white/80 rounded-2xl p-4 md:p-5 border border-white shadow-sm mb-6">
                <div class="flex flex-col md:flex-row gap-3 md:items-end">
                    <div class="flex-1">
                        <label class="text-xs font-semibold text-slate-400 block mb-2">{{ t('space.detail.addByUserId') }}</label>
                        <a-input v-model:value="inviteForm.userId" :placeholder="t('space.detail.enterUserId')" class="!rounded-xl" />
                    </div>
                    <button
                        class="px-5 py-2.5 rounded-xl bg-primary text-white font-bold shadow-glow hover:scale-105 transition-all"
                        :disabled="inviteSubmitting"
                        @click="handleInviteMember"
                    >
                        {{ t('space.detail.add') }}
                    </button>
                    <button
                        class="px-5 py-2.5 rounded-xl bg-white text-slate-600 font-semibold border border-slate-200 hover:border-primary"
                        @click="copyText(inviteLink)"
                    >
                        {{ t('space.detail.copy') }}
                    </button>
                </div>
                <p class="text-xs text-slate-400 mt-3">{{ t('space.detail.shareLinkTip') }}</p>
            </div>

            <div v-if="canManageMembers" class="bg-white/80 rounded-2xl p-4 md:p-5 border border-white shadow-sm mb-6">
                <div class="flex items-center justify-between gap-3 mb-4">
                    <div>
                        <p class="text-sm font-black text-slate-800">{{ t('space.detail.inviteRequestTitle') }}</p>
                        <p class="text-xs text-slate-400">{{ t('space.detail.manageMembers') }}</p>
                    </div>
                    <button
                        class="px-4 py-2 rounded-xl bg-white text-slate-600 font-semibold border border-slate-200 hover:border-primary"
                        :disabled="inviteRequestLoading"
                        @click="fetchInviteRequests"
                    >
                        {{ t('space.detail.refreshInvites') }}
                    </button>
                </div>

                <div v-if="inviteRequestLoading" class="space-y-3">
                    <div v-for="i in 2" :key="`invite-loading-${i}`" class="h-20 rounded-2xl bg-slate-100 animate-pulse"></div>
                </div>
                <div v-else class="space-y-4">
                    <div v-if="invitePendingList.length === 0" class="text-sm text-slate-400">
                        {{ t('space.detail.noInviteRequests') }}
                    </div>
                    <div
                        v-for="request in invitePendingList"
                        :key="request.id"
                        class="flex flex-col md:flex-row md:items-center gap-3 justify-between rounded-2xl bg-slate-50 px-4 py-3"
                    >
                        <div class="min-w-0">
                            <p class="font-bold text-slate-800 truncate">
                                {{ request.user?.userName || t('common.unknown') }}
                            </p>
                            <p class="text-xs text-slate-400 truncate">
                                @{{ request.user?.userAccount || '-' }}
                            </p>
                            <p v-if="request.inviter?.userName" class="text-xs text-slate-400 truncate mt-1">
                                {{ t('space.detail.inviteInviter') }}：{{ request.inviter.userName }}
                            </p>
                            <p class="text-xs text-slate-400 truncate mt-1">
                                {{ t('space.detail.inviteSentAt') }}：{{ formatDate(request.createTime) }}
                            </p>
                        </div>
                        <div class="flex items-center gap-2">
                            <a-popconfirm
                                :title="t('space.detail.confirmCancelInvite')"
                                @confirm="cancelInviteRequest(request)"
                            >
                                <button
                                    class="px-3 py-1.5 rounded-lg bg-rose-100 text-rose-500 text-xs font-bold"
                                    :disabled="inviteCancelingId === request.id"
                                >
                                    {{ t('space.detail.inviteCancel') }}
                                </button>
                            </a-popconfirm>
                        </div>
                    </div>

                    <div class="pt-2 border-t border-slate-100">
                        <div class="flex items-center justify-between gap-3 mb-3">
                            <p class="text-sm font-black text-slate-800">{{ t('space.detail.inviteHistoryTitle') }}</p>
                            <span class="text-xs text-slate-400">{{ inviteHistoryList.length }}</span>
                        </div>
                        <div v-if="inviteHistoryList.length === 0" class="text-sm text-slate-400">
                            {{ t('space.detail.noInviteHistory') }}
                        </div>
                        <div v-else class="space-y-3">
                            <div
                                v-for="request in inviteHistoryList"
                                :key="`invite-history-${request.id}`"
                                class="rounded-2xl bg-slate-50 px-4 py-3"
                            >
                                <div class="flex flex-col md:flex-row md:items-start gap-3 justify-between">
                                    <div class="min-w-0">
                                        <div class="flex items-center gap-2 flex-wrap">
                                            <p class="font-bold text-slate-800 truncate">
                                                {{ request.user?.userName || t('common.unknown') }}
                                            </p>
                                            <span
                                                class="px-2 py-0.5 rounded-full text-[10px] font-black"
                                                :class="getRequestStatusClass(request)"
                                            >
                                                {{ getRequestStatusLabel(request) }}
                                            </span>
                                        </div>
                                        <p class="text-xs text-slate-400 truncate">
                                            @{{ request.user?.userAccount || '-' }}
                                        </p>
                                        <p v-if="request.inviter?.userName" class="text-xs text-slate-400 truncate mt-1">
                                            {{ t('space.detail.inviteInviter') }}：{{ request.inviter.userName }}
                                        </p>
                                        <p class="text-xs text-slate-400 truncate mt-1">
                                            {{ t('space.detail.requestReviewedAt') }}：{{ formatDateTime(request.reviewTime || request.updateTime || request.createTime) }}
                                        </p>
                                        <p class="text-xs text-slate-500 mt-1 break-all">
                                            {{ t('space.detail.requestReviewMessage') }}：{{ request.reviewMessage || '-' }}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="canManageMembers" class="bg-white/80 rounded-2xl p-4 md:p-5 border border-white shadow-sm mb-6">
                <div class="flex items-center justify-between gap-3 mb-4">
                    <div>
                        <p class="text-sm font-black text-slate-800">{{ t('space.detail.joinRequestTitle') }}</p>
                        <p class="text-xs text-slate-400">{{ t('space.detail.manageMembers') }}</p>
                    </div>
                    <button
                        class="px-4 py-2 rounded-xl bg-white text-slate-600 font-semibold border border-slate-200 hover:border-primary"
                        :disabled="joinRequestLoading"
                        @click="fetchJoinRequests"
                    >
                        {{ t('space.detail.refreshRequests') }}
                    </button>
                </div>

                <div v-if="joinRequestLoading" class="space-y-3">
                    <div v-for="i in 2" :key="i" class="h-16 rounded-2xl bg-slate-100 animate-pulse"></div>
                </div>
                <div v-else class="space-y-4">
                    <div v-if="joinPendingList.length === 0" class="text-sm text-slate-400">
                        {{ t('space.detail.noJoinRequests') }}
                    </div>
                    <div
                        v-for="request in joinPendingList"
                        :key="request.id"
                        class="flex flex-col md:flex-row md:items-center gap-3 justify-between rounded-2xl bg-slate-50 px-4 py-3"
                    >
                        <div class="min-w-0">
                            <p class="font-bold text-slate-800 truncate">
                                {{ request.user?.userName || t('common.unknown') }}
                            </p>
                            <p class="text-xs text-slate-400 truncate">
                                @{{ request.user?.userAccount || '-' }}
                            </p>
                        </div>
                        <div class="flex items-center gap-2">
                            <button
                                class="px-3 py-1.5 rounded-lg bg-emerald-100 text-emerald-600 text-xs font-bold"
                                :disabled="joinReviewingId === request.id"
                                @click="reviewJoinRequest(request, 1)"
                            >
                                {{ t('space.detail.joinApprove') }}
                            </button>
                            <button
                                class="px-3 py-1.5 rounded-lg bg-rose-100 text-rose-500 text-xs font-bold"
                                :disabled="joinReviewingId === request.id"
                                @click="reviewJoinRequest(request, 2)"
                            >
                                {{ t('space.detail.joinReject') }}
                            </button>
                        </div>
                    </div>

                    <div class="pt-2 border-t border-slate-100">
                        <div class="flex items-center justify-between gap-3 mb-3">
                            <p class="text-sm font-black text-slate-800">{{ t('space.detail.joinHistoryTitle') }}</p>
                            <span class="text-xs text-slate-400">{{ joinHistoryList.length }}</span>
                        </div>
                        <div v-if="joinHistoryList.length === 0" class="text-sm text-slate-400">
                            {{ t('space.detail.noJoinHistory') }}
                        </div>
                        <div v-else class="space-y-3">
                            <div
                                v-for="request in joinHistoryList"
                                :key="`join-history-${request.id}`"
                                class="rounded-2xl bg-slate-50 px-4 py-3"
                            >
                                <div class="flex flex-col md:flex-row md:items-start gap-3 justify-between">
                                    <div class="min-w-0">
                                        <div class="flex items-center gap-2 flex-wrap">
                                            <p class="font-bold text-slate-800 truncate">
                                                {{ request.user?.userName || t('common.unknown') }}
                                            </p>
                                            <span
                                                class="px-2 py-0.5 rounded-full text-[10px] font-black"
                                                :class="getRequestStatusClass(request)"
                                            >
                                                {{ getRequestStatusLabel(request) }}
                                            </span>
                                        </div>
                                        <p class="text-xs text-slate-400 truncate">
                                            @{{ request.user?.userAccount || '-' }}
                                        </p>
                                        <p class="text-xs text-slate-400 truncate mt-1">
                                            {{ t('space.detail.requestReviewedAt') }}：{{ formatDateTime(request.reviewTime || request.updateTime || request.createTime) }}
                                        </p>
                                        <p class="text-xs text-slate-500 mt-1 break-all">
                                            {{ t('space.detail.requestReviewMessage') }}：{{ request.reviewMessage || '-' }}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="memberLoading" class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="i in 4" :key="i" class="h-20 rounded-2xl bg-white animate-pulse"></div>
            </div>
            <div v-else-if="memberDisplayList.length === 0" class="py-24 text-center bg-white rounded-[40px] border-2 border-dashed border-slate-100">
                <p class="text-slate-400 font-bold">{{ t('common.empty') }}</p>
            </div>
            <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div
                    v-for="member in memberDisplayList"
                    :key="member.id || member.userId"
                    class="flex items-center gap-4 bg-white/80 rounded-2xl p-4 border border-white shadow-sm"
                >
                    <div class="w-12 h-12 rounded-full overflow-hidden bg-slate-100">
                        <img :src="member.user?.userAvatar || defaultAvatar" class="w-full h-full object-cover" />
                    </div>
                    <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2 flex-wrap">
                            <p class="font-bold text-slate-800 truncate">
                                {{ member.user?.userName || t('common.unknown') }}
                            </p>
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-black"
                                :class="member.isOwner ? 'bg-amber-100 text-amber-600' : 'bg-slate-100 text-slate-500'"
                            >
                                {{ member.isOwner ? t('space.owner') : t('space.member') }}
                            </span>
                            <span
                                v-if="!member.isOwner"
                                class="px-2 py-0.5 rounded-full text-[10px] font-black"
                                :class="getMemberRoleClass(member.spaceRole)"
                            >
                                {{ getMemberRoleLabel(member.spaceRole) }}
                            </span>
                        </div>
                        <p class="text-xs text-slate-400 truncate">@{{ member.user?.userAccount || '-' }}</p>
                    </div>
                    <div class="text-xs text-slate-400 font-semibold">
                        {{ formatDate(member.createTime) }}
                    </div>
                    <div v-if="!member.isOwner && typeof member.id === 'number'" class="flex items-center gap-2">
                        <a-select
                            v-if="canManageMembers"
                            :value="member.spaceRole || 'viewer'"
                            :options="memberRoleOptions"
                            size="small"
                            class="w-[112px]"
                            :disabled="roleUpdatingId === member.id"
                            @change="handleMemberRoleChange(member, String($event))"
                        />
                        <a-popconfirm :title="t('space.detail.confirmRemove')" @confirm.stop="removeMember(member)">
                            <button
                                v-if="canManageMembers"
                                class="px-3 py-1 rounded-lg bg-rose-100 text-rose-500 text-xs font-bold"
                                @click.stop
                            >
                                {{ t('space.detail.remove') }}
                            </button>
                        </a-popconfirm>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <a-modal
      v-model:open="batchEditVisible"
      :title="t('space.detail.batchEditTitle')"
      :confirmLoading="batchEditing"
      @ok="submitBatchEdit"
      class="custom-modal"
    >
        <div class="space-y-4">
            <div>
                <label class="text-xs font-semibold text-slate-400 block mb-2">{{ t('space.detail.pictureCategory') }}</label>
                <a-select
                    v-model:value="batchEditForm.category"
                    allow-clear
                    :placeholder="t('space.detail.batchCategoryPlaceholder')"
                    :options="spaceCategoryOptions"
                    class="w-full"
                />
            </div>
            <div>
                <label class="text-xs font-semibold text-slate-400 block mb-2">{{ t('space.detail.pictureTag') }}</label>
                <a-select
                    v-model:value="batchEditForm.tags"
                    mode="tags"
                    :placeholder="t('space.detail.batchTagsPlaceholder')"
                    :options="spaceTagOptions"
                    class="w-full"
                />
            </div>
            <p class="text-xs text-slate-400">
                {{ t('space.detail.batchSelectedCount', { total: selectedSpacePictureIds.length }) }}
            </p>
        </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { deleteSpaceUsingPost, getSpaceVoByIdUsingGet, listSpaceActivityUsingGet } from '@/api/workspace/workspaceController'
import {
    addSpaceUserUsingPost,
    cancelSpaceInviteUsingPost,
    deleteSpaceUserUsingPost,
    editSpaceUserUsingPost,
    listSpaceJoinRequestUsingPost,
    listSpaceUserUsingPost,
    reviewSpaceJoinRequestUsingPost,
} from '@/api/workspace/workspaceUserController'
import {
    clearPictureShareCodeUsingPost,
    deletePictureUsingPost,
    deletePictureBatchUsingPost,
    editPictureUsingPost,
    generatePictureShareCodeUsingPost,
    getPictureDownloadInfoUsingGet,
    listPictureVoByPageUsingPost,
} from '@/api/asset/assetController'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined, EditOutlined, CheckOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'
import { downloadImage } from '@/utils'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { saveAs } from 'file-saver'

const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const space = ref<API.SpaceVO>({})

const defaultAvatar = '/default_avatar.png'
type SpaceTabKey = 'pictures' | 'members' | 'activity'
const currentTab = ref<SpaceTabKey>('pictures')
const tabs = computed<Array<{ key: SpaceTabKey; label: string; icon: string }>>(() => ([
    { key: 'pictures', label: t.value('space.detail.pictureTab'), icon: 'photo_library' },
    { key: 'activity', label: t.value('space.detail.activityTab'), icon: 'bolt' },
    { key: 'members', label: t.value('space.detail.memberTab'), icon: 'groups' },
]))

const memberList = ref<API.SpaceUserVO[]>([])
const memberLoading = ref(false)
const roleUpdatingId = ref<number | null>(null)
const inviteRequestList = ref<API.SpaceJoinRequestVO[]>([])
const inviteRequestLoading = ref(false)
const inviteCancelingId = ref<number | null>(null)
const joinRequestList = ref<API.SpaceJoinRequestVO[]>([])
const joinRequestLoading = ref(false)
const joinReviewingId = ref<number | null>(null)
const inviteForm = reactive({
    userId: '',
})
const inviteSubmitting = ref(false)

const spacePictures = ref<API.PictureVO[]>([])
const spacePictureLoading = ref(false)
const batchMode = ref(false)
const batchEditVisible = ref(false)
const batchEditing = ref(false)
const batchDownloading = ref(false)
const batchDeleting = ref(false)
const batchSharing = ref(false)
const batchUnsharing = ref(false)
const selectedSpacePictureIds = ref<number[]>([])
const batchEditForm = reactive({
    category: undefined as string | undefined,
    tags: [] as string[],
})
const spacePictureFilters = reactive({
    keyword: '',
    category: undefined as string | undefined,
    tag: undefined as string | undefined,
    fileType: 'all',
    uploaderId: undefined as string | undefined,
    sortBy: 'latest',
})

const spaceActivities = ref<API.SpaceActivityVO[]>([])
const activityLoading = ref(false)
const activityFilters = reactive({
    scope: 'all',
    type: 'all',
    keyword: '',
    range: 'all',
})

const isSpaceOwner = computed(() => loginUserStore.loginUser.id === space.value.userId)
const isAdminUser = computed(() => loginUserStore.loginUser.userRole === 'admin')
const currentUserSpaceRole = computed(() => {
    const loginUserId = loginUserStore.loginUser.id
    if (!loginUserId) return ''
    if (isSpaceOwner.value) return 'admin'
    const currentMember = memberList.value.find(item => item.userId === loginUserId)
    return currentMember?.spaceRole || ''
})
const canManageSpace = computed(() => {
    return isSpaceOwner.value || isAdminUser.value || currentUserSpaceRole.value === 'admin'
})
const canManageMembers = computed(() => canManageSpace.value)
const canBatchEditSpacePictures = computed(() => canManageSpace.value || currentUserSpaceRole.value === 'editor')
const memberRoleOptions = computed(() => ([
    { label: t.value('space.detail.roleViewer'), value: 'viewer' },
    { label: t.value('space.detail.roleEditor'), value: 'editor' },
    { label: t.value('space.detail.roleAdmin'), value: 'admin' },
]))

const ownerMember = computed(() => {
    if (!space.value.userId) return null
    return {
        id: `owner-${space.value.userId}`,
        userId: space.value.userId,
        spaceRole: 'admin',
        user: space.value.user,
        createTime: space.value.createTime,
        isOwner: true,
    } as any
})

const memberDisplayList = computed(() => {
    const result: Array<API.SpaceUserVO & { isOwner?: boolean }> = []
    const seen = new Set<string>()
    if (ownerMember.value) {
        result.push(ownerMember.value)
        seen.add(String(ownerMember.value.userId))
    }
    memberList.value.forEach((item) => {
        const id = item.userId ?? item.user?.id
        if (!id) return
        const key = String(id)
        if (seen.has(key)) return
        result.push({ ...item, isOwner: false })
        seen.add(key)
    })
    return result
})

const invitePendingList = computed(() =>
    inviteRequestList.value.filter(item => Number(item.status) === 0)
)

const inviteHistoryList = computed(() =>
    inviteRequestList.value.filter(item => Number(item.status) !== 0)
)

const joinPendingList = computed(() =>
    joinRequestList.value.filter(item => Number(item.status) === 0)
)

const joinHistoryList = computed(() =>
    joinRequestList.value.filter(item => Number(item.status) !== 0)
)

const inviteLink = computed(() => {
    if (!space.value.id) return ''
    return `${window.location.origin}/workspace/invite/${space.value.id}`
})

const getMemberRoleLabel = (role?: string) => {
    if (role === 'admin') return t.value('space.detail.roleAdmin')
    if (role === 'editor') return t.value('space.detail.roleEditor')
    return t.value('space.detail.roleViewer')
}

const getMemberRoleClass = (role?: string) => {
    if (role === 'admin') return 'bg-violet-100 text-violet-600'
    if (role === 'editor') return 'bg-sky-100 text-sky-600'
    return 'bg-emerald-100 text-emerald-600'
}

const isInviteCanceled = (request?: API.SpaceJoinRequestVO) => {
    const reviewMessage = String(request?.reviewMessage || '')
    return Number(request?.requestType) === 1 && Number(request?.status) === 2 && reviewMessage.includes('撤回')
}

const getRequestStatusLabel = (request?: API.SpaceJoinRequestVO) => {
    if (isInviteCanceled(request)) return t.value('space.detail.requestStatusCanceled')
    if (Number(request?.status) === 1) return t.value('space.detail.requestStatusPassed')
    if (Number(request?.status) === 2) return t.value('space.detail.requestStatusRejected')
    return t.value('space.detail.requestStatusPending')
}

const getRequestStatusClass = (request?: API.SpaceJoinRequestVO) => {
    if (isInviteCanceled(request)) return 'bg-slate-100 text-slate-500'
    if (Number(request?.status) === 1) return 'bg-emerald-100 text-emerald-600'
    if (Number(request?.status) === 2) return 'bg-rose-100 text-rose-500'
    return 'bg-amber-100 text-amber-600'
}

const selectedSpacePictureIdSet = computed(() => new Set(selectedSpacePictureIds.value))

const selectedSpacePictureList = computed(() =>
    spacePictures.value.filter((pic) => typeof pic.id === 'number' && selectedSpacePictureIdSet.value.has(pic.id))
)

const selectedSharedSpacePictureList = computed(() =>
    selectedSpacePictureList.value.filter(pic => !!pic.shareCode)
)

const isCurrentUserPictureOwner = (picture?: API.PictureVO | null) => {
    if (!picture?.userId || !loginUserStore.loginUser.id) return false
    return Number(picture.userId) === Number(loginUserStore.loginUser.id)
}

const canQuickEditSpacePicture = (picture?: API.PictureVO | null) => {
    return isCurrentUserPictureOwner(picture) || canBatchEditSpacePictures.value
}

const canQuickShareSpacePicture = (picture?: API.PictureVO | null) => {
    return canQuickEditSpacePicture(picture)
}

const canQuickDeleteSpacePicture = (picture?: API.PictureVO | null) => {
    return isCurrentUserPictureOwner(picture) || canManageSpace.value
}

const normalizePictureFormat = (picture?: API.PictureVO | null) => {
    const raw = String(picture?.picFormat || '').toLowerCase().replace(/^\./, '')
    if (raw) return raw
    const url = String(picture?.url || '')
    const cleanUrl = url.split('?')[0]
    const extension = cleanUrl.split('.').pop() || ''
    return extension.toLowerCase().replace(/^\./, '')
}

const getSpacePictureType = (picture?: API.PictureVO | null) => {
    const format = normalizePictureFormat(picture)
    const category = String(picture?.category || '')
    if (['pmx', 'pmd', 'glb', 'gltf', 'vrm', 'obj', 'fbx'].includes(format) || ['素材', '模型', '3D模型'].includes(category)) {
        return 'model'
    }
    if (['mp4', 'webm', 'mov', 'avi', 'mkv'].includes(format)) {
        return 'video'
    }
    if (['mp3', 'wav', 'flac', 'm4a', 'ogg', 'aac'].includes(format)) {
        return 'audio'
    }
    if (['txt', 'md', 'markdown', 'pdf', 'doc', 'docx', 'rtf', 'csv', 'json', 'xml', 'yaml', 'yml'].includes(format)) {
        return 'text'
    }
    return 'image'
}

const getSpacePictureTypeLabel = (picture?: API.PictureVO | null) => {
    const type = getSpacePictureType(picture)
    if (type === 'video') return t.value('space.detail.filterVideo')
    if (type === 'model') return t.value('space.detail.filterModel')
    if (type === 'audio') return t.value('space.detail.filterAudio')
    if (type === 'text') return t.value('space.detail.filterText')
    return t.value('space.detail.filterImage')
}

const getSpacePictureStatusLabel = (picture?: API.PictureVO | null) => {
    const status = Number(picture?.status)
    if (Number.isNaN(status)) return ''
    if (status === 0) return t.value('space.detail.pictureStatusDraft')
    if (status === 1) return t.value('space.detail.pictureStatusLine')
    if (status === 2) return t.value('space.detail.pictureStatusColor')
    if (status === 3) return t.value('space.detail.pictureStatusFinal')
    return ''
}

const getSpacePictureStatusClass = (picture?: API.PictureVO | null) => {
    const status = Number(picture?.status)
    if (status === 0) return 'bg-slate-100 text-slate-500'
    if (status === 1) return 'bg-sky-100 text-sky-600'
    if (status === 2) return 'bg-pink-100 text-pink-600'
    if (status === 3) return 'bg-emerald-100 text-emerald-600'
    return 'bg-slate-100 text-slate-500'
}

const getSpacePictureUploaderId = (picture?: API.PictureVO | null) => {
    const raw = picture?.user?.id ?? picture?.userId
    if (raw === undefined || raw === null) return ''
    return String(raw)
}

const getSpacePictureUploaderLabel = (picture?: API.PictureVO | null) => {
    return picture?.user?.userName || t.value('common.unknown')
}

const spacePictureTypeOptions = computed(() => ([
    { label: t.value('space.detail.filterAll'), value: 'all' },
    { label: t.value('space.detail.filterImage'), value: 'image' },
    { label: t.value('space.detail.filterVideo'), value: 'video' },
    { label: t.value('space.detail.filterModel'), value: 'model' },
    { label: t.value('space.detail.filterAudio'), value: 'audio' },
    { label: t.value('space.detail.filterText'), value: 'text' },
]))

const spacePictureSortOptions = computed(() => ([
    { label: t.value('space.detail.sortLatest'), value: 'latest' },
    { label: t.value('space.detail.sortOldest'), value: 'oldest' },
    { label: t.value('space.detail.sortPopular'), value: 'popular' },
    { label: t.value('space.detail.sortName'), value: 'name' },
]))

const spaceCategoryOptions = computed(() => Array.from(new Set(
    spacePictures.value
        .map(pic => String(pic.category || '').trim())
        .filter(Boolean)
)).map(item => ({ label: item, value: item })))

const spaceTagOptions = computed(() => Array.from(new Set(
    spacePictures.value
        .flatMap(pic => Array.isArray(pic.tags) ? pic.tags : [])
        .map(tag => String(tag || '').trim())
        .filter(Boolean)
)).map(item => ({ label: item, value: item })))

const spaceUploaderOptions = computed(() => {
    const userMap = new Map<string, string>()
    spacePictures.value.forEach((pic) => {
        const value = getSpacePictureUploaderId(pic)
        if (!value) return
        userMap.set(value, getSpacePictureUploaderLabel(pic))
    })
    return Array.from(userMap.entries()).map(([value, label]) => ({ value, label }))
})

const getSpacePictureSortTime = (picture?: API.PictureVO | null) => {
    const raw = picture?.editTime || picture?.updateTime || picture?.createTime
    if (!raw) return 0
    const timestamp = new Date(raw).getTime()
    return Number.isNaN(timestamp) ? 0 : timestamp
}

const filteredSpacePictures = computed(() => {
    const keyword = spacePictureFilters.keyword.trim().toLowerCase()
    const filtered = spacePictures.value.filter((pic) => {
        const typeMatched = spacePictureFilters.fileType === 'all'
            || getSpacePictureType(pic) === spacePictureFilters.fileType
        const categoryMatched = !spacePictureFilters.category || pic.category === spacePictureFilters.category
        const tagMatched = !spacePictureFilters.tag || (Array.isArray(pic.tags) && pic.tags.includes(spacePictureFilters.tag))
        const uploaderMatched = !spacePictureFilters.uploaderId || getSpacePictureUploaderId(pic) === spacePictureFilters.uploaderId
        const keywordMatched = !keyword || [pic.name, pic.introduction, pic.category, getSpacePictureUploaderLabel(pic), ...(Array.isArray(pic.tags) ? pic.tags : [])]
            .filter(Boolean)
            .some(item => String(item).toLowerCase().includes(keyword))
        return typeMatched && categoryMatched && tagMatched && uploaderMatched && keywordMatched
    })

    return [...filtered].sort((a, b) => {
        if (spacePictureFilters.sortBy === 'oldest') {
            return getSpacePictureSortTime(a) - getSpacePictureSortTime(b)
        }
        if (spacePictureFilters.sortBy === 'popular') {
            const likeDiff = Number(b.likeCount || 0) - Number(a.likeCount || 0)
            return likeDiff || getSpacePictureSortTime(b) - getSpacePictureSortTime(a)
        }
        if (spacePictureFilters.sortBy === 'name') {
            return String(a.name || '').localeCompare(String(b.name || ''), 'zh-Hans-CN')
        }
        return getSpacePictureSortTime(b) - getSpacePictureSortTime(a)
    })
})

const clearSpacePictureFilters = () => {
    spacePictureFilters.keyword = ''
    spacePictureFilters.category = undefined
    spacePictureFilters.tag = undefined
    spacePictureFilters.fileType = 'all'
    spacePictureFilters.uploaderId = undefined
    spacePictureFilters.sortBy = 'latest'
}

const isSpacePictureSelected = (pictureItem?: API.PictureVO) => {
    if (typeof pictureItem?.id !== 'number') return false
    return selectedSpacePictureIdSet.value.has(pictureItem.id)
}

const clearSelectedSpacePictures = () => {
    selectedSpacePictureIds.value = []
}

const toggleBatchMode = () => {
    batchMode.value = !batchMode.value
    if (!batchMode.value) {
        clearSelectedSpacePictures()
        batchEditVisible.value = false
    }
}

const toggleSpacePictureSelection = (pictureItem?: API.PictureVO) => {
    if (typeof pictureItem?.id !== 'number') return
    if (selectedSpacePictureIdSet.value.has(pictureItem.id)) {
        selectedSpacePictureIds.value = selectedSpacePictureIds.value.filter(id => id !== pictureItem.id)
        return
    }
    selectedSpacePictureIds.value = [...selectedSpacePictureIds.value, pictureItem.id]
}

const selectAllFilteredPictures = () => {
    selectedSpacePictureIds.value = filteredSpacePictures.value
        .map(pic => pic.id)
        .filter((id): id is number => typeof id === 'number')
}

const handleSpacePictureCardClick = (pictureItem?: API.PictureVO) => {
    if (batchMode.value) {
        toggleSpacePictureSelection(pictureItem)
        return
    }
    openSpacePictureDetail(pictureItem)
}

const openSpacePictureDetail = (pictureItem?: API.PictureVO) => {
    if (!pictureItem?.id) return
    router.push(`/asset/${pictureItem.id}`)
}

const openBatchEditModal = () => {
    if (!selectedSpacePictureIds.value.length) {
        message.warning(t.value('space.detail.batchNoSelection'))
        return
    }
    batchEditForm.category = undefined
    batchEditForm.tags = []
    batchEditVisible.value = true
}

const buildBatchTags = (pictureItem?: API.PictureVO) => {
    const existingTags = Array.isArray(pictureItem?.tags)
        ? pictureItem!.tags.map(tag => String(tag || '').trim()).filter(Boolean)
        : []
    const newTags = batchEditForm.tags.map(tag => String(tag || '').trim()).filter(Boolean)
    return Array.from(new Set([...existingTags, ...newTags]))
}

const getPictureLastEditTimestamp = (pictureItem?: API.PictureVO) => {
    const raw = pictureItem?.editTime || pictureItem?.updateTime || pictureItem?.createTime
    if (!raw) return undefined
    const parsed = Date.parse(raw)
    return Number.isNaN(parsed) ? undefined : parsed
}

const isBatchEditConflictMessage = (msg?: string) => {
    if (!msg) return false
    return msg.includes('资源已被他人更新') || msg.includes('当前资源正在被其他成员编辑')
}

const submitBatchEdit = async () => {
    if (!selectedSpacePictureIds.value.length) {
        message.warning(t.value('space.detail.batchNoSelection'))
        return
    }
    const nextCategory = String(batchEditForm.category || '').trim()
    const appendTags = batchEditForm.tags.map(tag => String(tag || '').trim()).filter(Boolean)
    if (!nextCategory && appendTags.length === 0) {
        message.warning(t.value('space.detail.batchNoChanges'))
        return
    }
    batchEditing.value = true
    let successCount = 0
    let skippedCount = 0
    let conflictCount = 0
    try {
        for (const pictureItem of selectedSpacePictureList.value) {
            if (!pictureItem.id) continue
            const mergedTags = buildBatchTags(pictureItem)
            const shouldUpdateCategory = !!nextCategory && nextCategory !== pictureItem.category
            const shouldUpdateTags = appendTags.length > 0
                && JSON.stringify(mergedTags) !== JSON.stringify((pictureItem.tags || []).map(tag => String(tag || '').trim()).filter(Boolean))
            if (!shouldUpdateCategory && !shouldUpdateTags) {
                skippedCount++
                continue
            }
            const res = await editPictureUsingPost({
                id: pictureItem.id,
                ...(getPictureLastEditTimestamp(pictureItem) ? { lastKnownEditTime: getPictureLastEditTimestamp(pictureItem) } : {}),
                ...(shouldUpdateCategory ? { category: nextCategory } : {}),
                ...(shouldUpdateTags ? { tags: mergedTags } : {}),
            })
            if (res.data.code === 0) {
                successCount++
            } else {
                if (isBatchEditConflictMessage(res.data.message)) {
                    conflictCount++
                }
                skippedCount++
            }
        }
        if (successCount > 0) {
            message.success(t.value('space.detail.batchEditSuccess', { success: successCount, skipped: skippedCount }))
            if (conflictCount > 0) {
                message.warning(t.value('space.detail.batchEditConflictHint', { count: conflictCount }))
            }
            batchEditVisible.value = false
            await fetchSpacePictures()
            await fetchSpaceActivities()
        } else {
            message.warning(conflictCount > 0
                ? t.value('space.detail.batchEditConflictHint', { count: conflictCount })
                : t.value('space.detail.batchEditNoop'))
        }
    } catch (error) {
        message.error(t.value('space.detail.batchEditFailed'))
    } finally {
        batchEditing.value = false
    }
}

const triggerPictureDownload = (info: API.DownloadInfoVO) => {
    if (info.url.startsWith('/api')) {
        const link = document.createElement('a')
        link.href = info.url
        link.target = '_blank'
        link.download = info.fileName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        return
    }
    downloadImage(info.url, info.fileName)
}

const batchDownloadSelectedPictures = async () => {
    if (!selectedSpacePictureIds.value.length) {
        message.warning(t.value('space.detail.batchNoSelection'))
        return
    }
    batchDownloading.value = true
    let successCount = 0
    let skippedCount = 0
    try {
        for (const pictureItem of selectedSpacePictureList.value) {
            if (!pictureItem.id) continue
            try {
                const res = await getPictureDownloadInfoUsingGet({ pictureId: pictureItem.id })
                if (res.data.code === 0 && res.data.data) {
                    triggerPictureDownload(res.data.data)
                    successCount++
                } else {
                    skippedCount++
                }
            } catch (error) {
                skippedCount++
            }
            await new Promise(resolve => setTimeout(resolve, 120))
        }
        message.success(t.value('space.detail.batchDownloadSummary', { success: successCount, skipped: skippedCount }))
    } catch (error) {
        message.error(t.value('space.detail.batchDownloadFailed'))
    } finally {
        batchDownloading.value = false
    }
}

const batchDeleteSelectedPictures = () => {
    if (!selectedSpacePictureIds.value.length) {
        message.warning(t.value('space.detail.batchNoSelection'))
        return
    }
    Modal.confirm({
        title: t.value('space.detail.batchDeleteConfirmTitle'),
        content: t.value('space.detail.batchDeleteConfirmContent', { total: selectedSpacePictureIds.value.length }),
        okText: t.value('common.delete'),
        okButtonProps: {
            danger: true,
            loading: batchDeleting.value,
        },
        onOk: async () => {
            batchDeleting.value = true
            try {
                const res = await deletePictureBatchUsingPost({
                    idList: [...selectedSpacePictureIds.value],
                })
                if (res.data.code === 0) {
                    message.success(t.value('space.detail.batchDeleteSuccess', { total: selectedSpacePictureIds.value.length }))
                    clearSelectedSpacePictures()
                    await Promise.all([
                        fetchSpacePictures(),
                        fetchSpaceActivities(),
                    ])
                    return
                }
                message.error(res.data.message || t.value('space.detail.batchDeleteFailed'))
            } catch (error) {
                message.error(t.value('space.detail.batchDeleteFailed'))
            } finally {
                batchDeleting.value = false
            }
            return Promise.reject(new Error('batch delete failed'))
        },
    })
}

const updateSpacePictureShareCode = (pictureId: number, shareCode?: string) => {
    const target = spacePictures.value.find(item => Number(item.id) === Number(pictureId))
    if (target) {
        target.shareCode = shareCode
    }
}

const buildPictureShareUrl = (pictureId: number, shareCode: string) => {
    const sharePath = router.resolve({ path: `/asset/${pictureId}`, query: { shareCode } }).href
    return `${window.location.origin}${sharePath}`
}

const copyTextSafely = async (text: string) => {
    if (!text) return false
    try {
        if (navigator.clipboard && window.isSecureContext) {
            await navigator.clipboard.writeText(text)
            return true
        }
    } catch {}
    const textArea = document.createElement('textarea')
    textArea.value = text
    textArea.style.position = 'fixed'
    textArea.style.left = '-9999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()
    let success = false
    try {
        success = document.execCommand('copy')
    } catch {}
    document.body.removeChild(textArea)
    return success
}

const quickEditSpacePicture = (picture?: API.PictureVO) => {
    if (!picture?.id) return
    router.push(`/asset/upload?id=${picture.id}`)
}

const copySpacePictureShareLink = async (picture?: API.PictureVO) => {
    if (!picture?.id || !picture.shareCode) {
        message.warning(t.value('space.detail.quickCopyLinkUnavailable'))
        return
    }
    const copied = await copyTextSafely(buildPictureShareUrl(picture.id, picture.shareCode))
    message[copied ? 'success' : 'warning'](
        copied ? t.value('space.detail.quickCopyLinkSuccess') : t.value('space.detail.quickCopyLinkFailed')
    )
}

const toggleSpacePictureShare = async (picture?: API.PictureVO) => {
    if (!picture?.id) return
    if (picture.shareCode) {
        Modal.confirm({
            title: t.value('space.detail.quickUnshareTitle'),
            content: t.value('space.detail.quickUnshareConfirmContent'),
            onOk: async () => {
                try {
                    const res = await clearPictureShareCodeUsingPost({ pictureId: picture.id as number })
                    if (res.data.code === 0) {
                        updateSpacePictureShareCode(picture.id as number, undefined)
                        message.success(t.value('space.detail.quickUnshareSuccess'))
                        return
                    }
                    message.error(res.data.message || t.value('space.detail.quickUnshareFailed'))
                } catch (error) {
                    message.error(t.value('space.detail.quickUnshareFailed'))
                }
                return Promise.reject(new Error('quick unshare failed'))
            },
        })
        return
    }
    try {
        const res = await generatePictureShareCodeUsingPost({ pictureId: picture.id as number })
        if (res.data.code === 0 && res.data.data) {
            updateSpacePictureShareCode(picture.id as number, res.data.data)
            const copied = await copyTextSafely(buildPictureShareUrl(picture.id as number, res.data.data))
            message[copied ? 'success' : 'warning'](
                copied ? t.value('space.detail.quickShareSuccess') : t.value('space.detail.quickShareCopyManual')
            )
            return
        }
        message.error(res.data.message || t.value('space.detail.quickShareFailed'))
    } catch (error) {
        message.error(t.value('space.detail.quickShareFailed'))
    }
}

const deleteSpacePictureQuick = (picture?: API.PictureVO) => {
    if (!picture?.id) return
    Modal.confirm({
        title: t.value('space.detail.quickDeleteTitle'),
        content: t.value('space.detail.quickDeleteConfirmContent', { name: picture.name || `#${picture.id}` }),
        okText: t.value('common.delete'),
        okButtonProps: {
            danger: true,
        },
        onOk: async () => {
            try {
                const res = await deletePictureUsingPost({ id: picture.id as number })
                if (res.data.code === 0) {
                    message.success(t.value('space.detail.quickDeleteSuccess'))
                    await Promise.all([
                        fetchSpacePictures(),
                        fetchSpaceActivities(),
                    ])
                    return
                }
                message.error(res.data.message || t.value('space.detail.quickDeleteFailed'))
            } catch (error) {
                message.error(t.value('space.detail.quickDeleteFailed'))
            }
            return Promise.reject(new Error('quick delete failed'))
        },
    })
}

const batchGenerateShareLinks = async () => {
    if (!selectedSpacePictureIds.value.length) {
        message.warning(t.value('space.detail.batchNoSelection'))
        return
    }
    batchSharing.value = true
    let generatedCount = 0
    let reusedCount = 0
    let failedCount = 0
    const shareLinks: string[] = []
    try {
        for (const pictureItem of selectedSpacePictureList.value) {
            if (!pictureItem.id) continue
            let shareCode = pictureItem.shareCode || ''
            if (shareCode) {
                reusedCount++
            } else {
                try {
                    const res = await generatePictureShareCodeUsingPost({ pictureId: pictureItem.id })
                    if (res.data.code === 0 && res.data.data) {
                        shareCode = res.data.data
                        generatedCount++
                    } else {
                        failedCount++
                        continue
                    }
                } catch (error) {
                    failedCount++
                    continue
                }
            }
            shareLinks.push(buildPictureShareUrl(pictureItem.id, shareCode))
        }
        if (!shareLinks.length) {
            message.warning(t.value('space.detail.batchShareFailed'))
            return
        }
        const copied = await copyTextSafely(shareLinks.join('\n'))
        Modal.success({
            title: t.value('space.detail.batchShareReadyTitle'),
            content: t.value('space.detail.batchShareSummary', {
                generated: generatedCount,
                reused: reusedCount,
                failed: failedCount,
            }),
        })
        message[copied ? 'success' : 'warning'](
            copied ? t.value('space.detail.batchShareCopied') : t.value('space.detail.batchShareCopyManual')
        )
        await fetchSpacePictures()
    } finally {
        batchSharing.value = false
    }
}

const batchClearShareLinks = () => {
    if (!selectedSpacePictureIds.value.length) {
        message.warning(t.value('space.detail.batchNoSelection'))
        return
    }
    if (!selectedSharedSpacePictureList.value.length) {
        message.warning(t.value('space.detail.batchUnshareNoop'))
        return
    }
    Modal.confirm({
        title: t.value('space.detail.batchUnshareConfirmTitle'),
        content: t.value('space.detail.batchUnshareConfirmContent', { total: selectedSharedSpacePictureList.value.length }),
        onOk: async () => {
            batchUnsharing.value = true
            let successCount = 0
            let failedCount = 0
            try {
                for (const pictureItem of selectedSharedSpacePictureList.value) {
                    if (!pictureItem.id) continue
                    try {
                        const res = await clearPictureShareCodeUsingPost({ pictureId: pictureItem.id })
                        if (res.data.code === 0) {
                            successCount++
                        } else {
                            failedCount++
                        }
                    } catch (error) {
                        failedCount++
                    }
                }
                if (successCount > 0) {
                    message.success(t.value('space.detail.batchUnshareSuccess', { success: successCount, failed: failedCount }))
                    await fetchSpacePictures()
                    return
                }
                message.error(t.value('space.detail.batchUnshareFailed'))
            } finally {
                batchUnsharing.value = false
            }
            return Promise.reject(new Error('batch unshare failed'))
        },
    })
}

const formatDate = (value?: string) => {
    if (!value) return t.value('common.unknown')
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return t.value('common.unknown')
    return date.toLocaleDateString()
}

const formatDateTime = (value?: string) => {
    if (!value) return t.value('common.unknown')
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return t.value('common.unknown')
    return date.toLocaleString()
}

const activityScopeOptions = computed(() => ([
    { label: t.value('space.detail.activityScopeAll'), value: 'all' },
    { label: t.value('space.detail.activityScopeAsset'), value: 'asset' },
    { label: t.value('space.detail.activityScopeMember'), value: 'member' },
]))

const activityTypeOptions = computed(() => ([
    { label: t.value('space.detail.activityTypeAll'), value: 'all' },
    { label: t.value('space.detail.activityTypeEdit'), value: 'edit' },
    { label: t.value('space.detail.activityTypeComment'), value: 'comment' },
    { label: t.value('space.detail.activityTypeLike'), value: 'like' },
    { label: t.value('space.detail.activityTypeFavorite'), value: 'favorite' },
    { label: t.value('space.detail.activityTypeMember'), value: 'member' },
    { label: t.value('space.detail.activityTypeRole'), value: 'member_role' },
]))

const activityRangeOptions = computed(() => ([
    { label: t.value('space.detail.activityRangeAll'), value: 'all' },
    { label: t.value('space.detail.activityRange7d'), value: '7d' },
    { label: t.value('space.detail.activityRange30d'), value: '30d' },
]))

const getSpaceActivityTimestamp = (activity?: API.SpaceActivityVO) => {
    if (!activity?.createTime) return 0
    const timestamp = new Date(activity.createTime).getTime()
    return Number.isNaN(timestamp) ? 0 : timestamp
}

const filteredSpaceActivities = computed(() => {
    const keyword = activityFilters.keyword.trim().toLowerCase()
    const now = dayjs()
    return spaceActivities.value.filter((item) => {
        const type = item.activityType || ''
        const scopeMatched = activityFilters.scope === 'all'
            || (activityFilters.scope === 'asset' && ['edit', 'comment', 'like', 'favorite'].includes(type))
            || (activityFilters.scope === 'member' && ['member', 'member_role'].includes(type))
        const typeMatched = activityFilters.type === 'all' || type === activityFilters.type
        const keywordMatched = !keyword || [
            item.actorUser?.userName,
            item.pictureName,
            item.detail,
            getSpaceActivityLabel(item),
            getSpaceActivitySummary(item),
        ]
            .filter(Boolean)
            .some(value => String(value).toLowerCase().includes(keyword))
        const activityTimestamp = getSpaceActivityTimestamp(item)
        const rangeMatched = activityFilters.range === 'all'
            || (activityFilters.range === '7d' && activityTimestamp > 0 && dayjs(activityTimestamp).isAfter(now.subtract(7, 'day')))
            || (activityFilters.range === '30d' && activityTimestamp > 0 && dayjs(activityTimestamp).isAfter(now.subtract(30, 'day')))
        return scopeMatched && typeMatched && keywordMatched && rangeMatched
    })
})

const getSpaceActivityIcon = (activity?: API.SpaceActivityVO) => {
    if (activity?.activityType === 'comment') return 'chat'
    if (activity?.activityType === 'like') return 'favorite'
    if (activity?.activityType === 'favorite') return 'star'
    if (activity?.activityType === 'member') return 'group_add'
    if (activity?.activityType === 'member_role') return 'admin_panel_settings'
    return 'edit_square'
}

const getSpaceActivityIconClass = (activity?: API.SpaceActivityVO) => {
    if (activity?.activityType === 'comment') return 'bg-sky-100 text-sky-600'
    if (activity?.activityType === 'like') return 'bg-rose-100 text-rose-500'
    if (activity?.activityType === 'favorite') return 'bg-amber-100 text-amber-600'
    if (activity?.activityType === 'member') return 'bg-violet-100 text-violet-600'
    if (activity?.activityType === 'member_role') return 'bg-fuchsia-100 text-fuchsia-600'
    return 'bg-emerald-100 text-emerald-600'
}

const getSpaceActivityLabel = (activity?: API.SpaceActivityVO) => {
    if (activity?.activityType === 'comment') return t.value('space.detail.activityTypeComment')
    if (activity?.activityType === 'like') return t.value('space.detail.activityTypeLike')
    if (activity?.activityType === 'favorite') return t.value('space.detail.activityTypeFavorite')
    if (activity?.activityType === 'member') return t.value('space.detail.activityTypeMember')
    if (activity?.activityType === 'member_role') return t.value('space.detail.activityTypeRole')
    return t.value('space.detail.activityTypeEdit')
}

const getSpaceActivitySummary = (activity?: API.SpaceActivityVO) => {
    const actorName = activity?.actorUser?.userName || t.value('common.unknown')
    const pictureName = activity?.pictureName || `#${activity?.pictureId ?? ''}`
    if (activity?.activityType === 'member' || activity?.activityType === 'member_role') {
        return activity.detail || getSpaceActivityLabel(activity)
    }
    if (activity?.activityType === 'comment') {
        return t.value('space.detail.activitySummaryComment', { actor: actorName, picture: pictureName })
    }
    if (activity?.activityType === 'like') {
        return t.value('space.detail.activitySummaryLike', { actor: actorName, picture: pictureName })
    }
    if (activity?.activityType === 'favorite') {
        return t.value('space.detail.activitySummaryFavorite', { actor: actorName, picture: pictureName })
    }
    return t.value('space.detail.activitySummaryEdit', { actor: actorName, picture: pictureName })
}

const escapeCsvCell = (value?: string | number | null) => {
    const text = String(value ?? '')
    return `"${text.replace(/"/g, '""')}"`
}

const exportSpaceActivities = () => {
    if (!filteredSpaceActivities.value.length) {
        message.warning(t.value('space.detail.activityEmpty'))
        return
    }
    const header = [
        t.value('space.detail.activityExportTime'),
        t.value('space.detail.activityExportType'),
        t.value('space.detail.activityExportActor'),
        t.value('space.detail.activityExportPicture'),
        t.value('space.detail.activityExportSummary'),
        t.value('space.detail.activityExportDetail'),
        'pictureId',
    ]
    const rows = filteredSpaceActivities.value.map((activity) => ([
        formatDateTime(activity.createTime),
        getSpaceActivityLabel(activity),
        activity.actorUser?.userName || t.value('common.unknown'),
        activity.pictureName || '',
        getSpaceActivitySummary(activity),
        activity.detail || '',
        activity.pictureId || '',
    ].map(item => escapeCsvCell(item)).join(',')))
    const csvContent = `\uFEFF${header.map(item => escapeCsvCell(item)).join(',')}\n${rows.join('\n')}`
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const safeSpaceName = String(space.value.spaceName || 'space').replace(/[\\/:*?"<>|]+/g, '-')
    const fileName = `${safeSpaceName}-activity-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
    saveAs(blob, fileName)
    message.success(t.value('space.detail.activityExportSuccess', { total: filteredSpaceActivities.value.length }))
}

const confirmDeleteSpace = () => {
    if (!space.value.id) return
    Modal.confirm({
        title: t.value('space.detail.deleteConfirmTitle'),
        content: t.value('space.detail.deleteConfirmContent').replace('{name}', space.value.spaceName || ''),
        okText: t.value('space.detail.deleteText'),
        okType: 'danger',
        onOk: async () => {
            const res = await deleteSpaceUsingPost({ id: space.value.id })
            if (res.data.code === 0) {
                message.success(t.value('space.detail.deleteSuccess'))
                router.push('/workspace/my')
            } else {
                message.error(t.value('space.detail.deleteFailed'))
            }
        }
    })
}

const fetchSpaceDetail = async () => {
    const id = route.params.id
    if (!id) return
    const res = await getSpaceVoByIdUsingGet({ id: id as any, _t: Date.now() } as any)
    if (res.data.code === 0 && res.data.data) {
        space.value = res.data.data
    }
}

const resolveSpaceId = () => {
    const routeId = route.params.id
    return routeId ?? (space.value.id as any)
}

const fetchMembers = async () => {
    const id = route.params.id || space.value.id
    if (!id) return
    memberLoading.value = true
    try {
        const res = await listSpaceUserUsingPost({ spaceId: id as any })
        if (res.data.code === 0) {
            memberList.value = res.data.data ?? []
        }
    } finally {
        memberLoading.value = false
    }
}

const fetchJoinRequests = async () => {
    if (!canManageMembers.value || !space.value.id) {
        joinRequestList.value = []
        return
    }
    joinRequestLoading.value = true
    try {
        const res = await listSpaceJoinRequestUsingPost({
            spaceId: space.value.id as any,
        })
        if (res.data.code === 0) {
            joinRequestList.value = res.data.data ?? []
        } else {
            message.error(res.data.message || t.value('common.actionFailed'))
        }
    } finally {
        joinRequestLoading.value = false
    }
}

const fetchInviteRequests = async () => {
    if (!canManageMembers.value || !space.value.id) {
        inviteRequestList.value = []
        return
    }
    inviteRequestLoading.value = true
    try {
        const res = await listSpaceJoinRequestUsingPost({
            spaceId: space.value.id as any,
            requestType: 1,
        })
        if (res.data.code === 0) {
            inviteRequestList.value = res.data.data ?? []
        } else {
            message.error(res.data.message || t.value('common.actionFailed'))
        }
    } finally {
        inviteRequestLoading.value = false
    }
}

const fetchSpacePictures = async () => {
    const id = route.params.id || space.value.id
    if (!id) return
    spacePictureLoading.value = true
    try {
        const res = await listPictureVoByPageUsingPost({
            spaceId: id as any,
            current: 1,
            pageSize: 200,
        } as any)
        if (res.data.code === 0) {
            spacePictures.value = res.data.data?.records ?? []
        }
    } finally {
        spacePictureLoading.value = false
    }
}

const copyText = (text: string) => {
    if (!text) return
    navigator.clipboard.writeText(text)
    message.success(t.value('common.copySuccess') || '已复制到剪贴板')
}

const handleExportSpace = () => {
    if (!space.value.id) return
    window.open(`/api/workspace/download?spaceId=${space.value.id}`, '_blank')
}

const handleInviteMember = async () => {
    if (!space.value.id) return
    const userId = Number(inviteForm.userId)
    if (!userId || Number.isNaN(userId)) {
        message.warning(t.value('space.detail.enterUserId'))
        return
    }
    inviteSubmitting.value = true
    try {
        const res = await addSpaceUserUsingPost({
            spaceId: space.value.id as any,
            userId,
        })
        if (res.data.code === 0) {
            message.success(t.value('space.detail.inviteSent') || t.value('common.actionSuccess'))
            inviteForm.userId = ''
            await fetchInviteRequests()
        } else {
            message.error(res.data.message || t.value('common.actionFailed'))
        }
    } finally {
        inviteSubmitting.value = false
    }
}

const removeMember = async (member: API.SpaceUserVO) => {
    if (!member?.id) return
    const res = await deleteSpaceUserUsingPost({ id: member.id })
    if (res.data.code === 0) {
        message.success(t.value('common.actionSuccess'))
        await fetchMembers()
    } else {
        message.error(res.data.message || t.value('common.actionFailed'))
    }
}

const handleMemberRoleChange = async (member: API.SpaceUserVO, spaceRole: string) => {
    if (!member?.id || !spaceRole || member.spaceRole === spaceRole) return
    roleUpdatingId.value = member.id
    try {
        const res = await editSpaceUserUsingPost({
            id: member.id,
            spaceRole,
        })
        if (res.data.code === 0) {
            message.success(t.value('common.actionSuccess'))
            await fetchMembers()
        } else {
            message.error(res.data.message || t.value('common.actionFailed'))
        }
    } finally {
        roleUpdatingId.value = null
    }
}

const reviewJoinRequest = async (request: API.SpaceJoinRequestVO, status: number) => {
    if (!request.id) return
    joinReviewingId.value = request.id
    try {
        const res = await reviewSpaceJoinRequestUsingPost({
            id: request.id,
            status,
        })
        if (res.data.code === 0) {
            message.success(t.value('common.actionSuccess'))
            await fetchJoinRequests()
            await fetchMembers()
        } else {
            message.error(res.data.message || t.value('common.actionFailed'))
        }
    } finally {
        joinReviewingId.value = null
    }
}

const cancelInviteRequest = async (request: API.SpaceJoinRequestVO) => {
    if (!request.id) return
    inviteCancelingId.value = request.id
    try {
        const res = await cancelSpaceInviteUsingPost({ id: request.id })
        if (res.data.code === 0) {
            message.success(t.value('common.actionSuccess'))
            await fetchInviteRequests()
        } else {
            message.error(res.data.message || t.value('common.actionFailed'))
        }
    } finally {
        inviteCancelingId.value = null
    }
}

const fetchSpaceActivities = async () => {
    const routeId = route.params.id
    const id = routeId ?? (space.value.id as any)
    const spaceId = id !== undefined && id !== null ? String(id) : ''
    if (!spaceId) return
    activityLoading.value = true
    try {
        const res = await listSpaceActivityUsingGet({ spaceId, limit: 50, _t: Date.now() } as any)
        if (res.data.code === 0) {
            spaceActivities.value = res.data.data ?? []
        } else {
            spaceActivities.value = []
        }
    } finally {
        activityLoading.value = false
    }
}

const openWorkspaceInvite = () => {
    if (!space.value.id) return
    router.push(`/workspace/invite/${space.value.id}`)
}

onMounted(async () => {
    await fetchSpaceDetail()
    await fetchMembers()
    await fetchInviteRequests()
    await fetchJoinRequests()
    await fetchSpacePictures()
    await fetchSpaceActivities()
})
</script>

<style scoped>
.shadow-glow {
    box-shadow: 0 10px 40px rgba(109, 174, 239, 0.5);
}

#spaceDetailPage {
    margin-bottom: 24px;
}

.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #e2e8f0;
    border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}
</style>
