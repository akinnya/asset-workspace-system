<template>
  <div class="work-detail-page bg-slate-50 min-h-screen pb-12 overflow-x-hidden">
      <!-- Fixed Background -->
     <div class="fixed inset-0 overflow-hidden pointer-events-none z-0">
         <div class="absolute top-[-10%] left-[-5%] w-[260px] h-[260px] sm:w-[420px] sm:h-[420px] lg:w-[600px] lg:h-[600px] bg-primary/20 rounded-full blur-[120px] mix-blend-multiply opacity-40 animate-blob"></div>
         <div class="absolute bottom-[20%] right-[10%] w-[320px] h-[320px] sm:w-[500px] sm:h-[500px] lg:w-[700px] lg:h-[700px] bg-accent-pink/20 rounded-full blur-[140px] mix-blend-multiply opacity-40 animate-blob animation-delay-2000"></div>
         <div class="absolute top-[40%] right-[20%] w-[220px] h-[220px] sm:w-[300px] sm:h-[300px] lg:w-[400px] lg:h-[400px] bg-secondary-color/20 rounded-full blur-[100px] mix-blend-overlay opacity-30 animate-blob animation-delay-4000"></div>
     </div>
 
      <!-- Main Content -->
     <div class="content-wrapper max-w-[1400px] mx-auto px-4 pt-6 sm:pt-12 relative z-10" v-if="picture">
         <div class="flex flex-col lg:flex-row gap-6 sm:gap-10">
             
             <!-- Left Column: Media & Comments -->
             <div class="lg:w-[72%] space-y-10">
                <!-- Media Viewer Section -->
                <section class="glass-effect min-h-[300px] md:min-h-[500px] max-h-[900px] rounded-[1.5rem] md:rounded-[2.5rem] relative overflow-hidden flex items-center justify-center p-4 md:p-6 shadow-soft group">
                     <!-- Viewers -->
                     <template v-if="mediaType === 'model'">
                          <ThreeModelViewer 
                             v-if="picture.url && supportsModelPreview"
                             :url="picture.url"
                             :format="picture.picFormat"
                             class="w-full h-full rounded-[2rem]"
                          />
                          <div
                              v-else
                              class="w-full h-full rounded-[2rem] bg-white/80 backdrop-blur-md border border-white/60 shadow-soft p-6 md:p-8 flex flex-col items-center justify-center text-center gap-5"
                          >
                              <div class="w-20 h-20 rounded-3xl bg-slate-100 text-slate-500 flex items-center justify-center">
                                  <span class="material-symbols-outlined text-4xl">deployed_code</span>
                              </div>
                              <div class="space-y-2 max-w-xl">
                                  <h3 class="text-2xl font-black text-slate-800">{{ t('detail.modelPreviewUnsupportedTitle') }}</h3>
                                  <p class="text-sm md:text-base font-medium text-slate-500 leading-relaxed">
                                      {{ t('detail.modelPreviewUnsupportedDesc', { format: displayFormat }) }}
                                  </p>
                              </div>
                              <button
                                  v-if="showDownloadAction"
                                  class="px-5 py-2.5 rounded-2xl bg-primary text-white font-bold shadow-glow hover:-translate-y-0.5 transition-all"
                                  @click="doDownload"
                              >
                                  {{ t('detail.download') }}
                              </button>
                          </div>
                     </template>
                     <template v-else-if="mediaType === 'video'">
                         <video controls @contextmenu.prevent class="work-detail-video rounded-[2rem] shadow-2xl" :poster="picture.url">
                             <source :src="picture.url" :type="`video/${picture.picFormat || 'mp4'}`">
                         </video>
                     </template>
                     <template v-else-if="mediaType === 'audio'">
                         <div class="work-detail-audio w-full h-full rounded-[2rem] bg-white/80 backdrop-blur-md border border-white/60 shadow-soft p-6 md:p-8 flex flex-col gap-6">
                             <div class="flex flex-col sm:flex-row sm:items-center gap-4">
                                 <div class="audio-icon flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 text-primary">
                                     <span class="material-symbols-outlined text-3xl">music_note</span>
                                 </div>
                                 <div class="flex-1 min-w-0">
                                     <h3 class="text-xl sm:text-2xl font-black text-slate-800 truncate">{{ picture.name }}</h3>
                                     <p class="text-xs sm:text-sm font-bold text-slate-400">
                                         <span>{{ displayFormat }}</span>
                                         <span v-if="picture.picSize"> · {{ formatSize(picture.picSize) }}</span>
                                         <span v-if="audioDuration"> · {{ formatDuration(audioDuration) }}</span>
                                     </p>
                                 </div>
                                 <button v-if="showDownloadAction" class="px-4 py-2 rounded-xl bg-white text-slate-600 font-bold border border-slate-200 hover:bg-primary hover:text-white transition-all" @click="doDownload">
                                     {{ t('detail.download') }}
                                 </button>
                             </div>
                             <div class="audio-visual" :class="{ 'is-playing': audioPlaying }">
                                 <span v-for="n in 24" :key="n"></span>
                             </div>
                             <div class="audio-progress">
                                 <div class="audio-progress-bar" :style="{ width: `${audioProgress * 100}%` }"></div>
                             </div>
                             <div class="audio-time text-xs font-bold text-slate-400">
                                 {{ formatDuration(audioCurrentTime) }} / {{ formatDuration(audioDuration) }}
                             </div>
                             <audio
                                 ref="audioRef"
                                 :src="picture.url"
                                 controls
                                 preload="metadata"
                                 class="w-full"
                                 @loadedmetadata="handleAudioMetadata"
                                 @timeupdate="handleAudioTimeUpdate"
                                 @play="audioPlaying = true"
                                 @pause="audioPlaying = false"
                                 @ended="audioPlaying = false"
                             >
                                 <source :src="picture.url" :type="`audio/${picture.picFormat || 'mp3'}`">
                             </audio>
                         </div>
                     </template>
                     <template v-else-if="mediaType === 'text'">
                         <div class="work-detail-text w-full h-full rounded-[2rem] bg-white/80 backdrop-blur-md border border-white/60 shadow-soft p-6 md:p-8 flex flex-col gap-6">
                             <div class="flex flex-col sm:flex-row sm:items-center gap-4">
                                 <div class="text-icon flex items-center justify-center w-16 h-16 rounded-2xl bg-slate-100 text-slate-500">
                                     <span class="material-symbols-outlined text-3xl">description</span>
                                 </div>
                                 <div class="flex-1 min-w-0">
                                     <h3 class="text-xl sm:text-2xl font-black text-slate-800 truncate">{{ picture.name }}</h3>
                                     <p class="text-xs sm:text-sm font-bold text-slate-400">
                                         <span>{{ displayFormat }}</span>
                                         <span v-if="picture.picSize"> · {{ formatSize(picture.picSize) }}</span>
                                     </p>
                                 </div>
                                 <button v-if="showDownloadAction" class="px-4 py-2 rounded-xl bg-white text-slate-600 font-bold border border-slate-200 hover:bg-primary hover:text-white transition-all" @click="doDownload">
                                     {{ t('detail.download') }}
                                 </button>
                             </div>
                             <div v-if="showOfficeViewer" class="text-pdf-wrapper">
                                 <iframe class="text-pdf-frame" :src="officePreviewUrl" title="Office Preview"></iframe>
                                 <p class="text-pdf-hint">{{ t('detail.textPreviewHint') }}</p>
                             </div>
                             <div v-else-if="textPreviewType === 'pdf'" class="text-pdf-wrapper">
                                 <iframe class="text-pdf-frame" :src="picture.url" title="PDF Preview"></iframe>
                                 <p class="text-pdf-hint">{{ t('detail.textPreviewHint') }}</p>
                             </div>
                             <div v-else-if="['text', 'docx', 'rtf'].includes(textPreviewType)" class="text-preview-wrapper" :style="readerStyle">
                                 <div class="reader-toolbar">
                                     <div class="reader-toolbar-row">
                                         <div class="reader-toolbar-title">
                                             <span class="material-symbols-outlined">menu_book</span>
                                             <span>{{ t('detail.readingSettings') }}</span>
                                         </div>
                                         <div class="reader-toolbar-actions">
                                             <button class="reader-btn" @click="resetReaderSettings">{{ t('detail.readingReset') }}</button>
                                             <button class="reader-btn primary" @click="showReaderSettings = !showReaderSettings">
                                                 {{ showReaderSettings ? t('detail.readingCollapse') : t('detail.readingExpand') }}
                                             </button>
                                         </div>
                                     </div>
                                     <div v-if="showReaderSettings" class="reader-toolbar-controls">
                                         <div class="reader-control">
                                             <span>{{ t('detail.readingFont') }}</span>
                                             <input type="range" min="14" max="26" step="1" v-model.number="readerSettings.fontSize" />
                                             <span class="reader-value">{{ readerSettings.fontSize }}px</span>
                                         </div>
                                         <div class="reader-control">
                                             <span>{{ t('detail.readingLineHeight') }}</span>
                                             <input type="range" min="1.4" max="2.4" step="0.05" v-model.number="readerSettings.lineHeight" />
                                             <span class="reader-value">{{ readerSettings.lineHeight.toFixed(2) }}</span>
                                         </div>
                                         <div class="reader-control">
                                             <span>{{ t('detail.readingParagraph') }}</span>
                                             <input type="range" min="0.6" max="2" step="0.1" v-model.number="readerSettings.paragraphSpacing" />
                                             <span class="reader-value">{{ readerSettings.paragraphSpacing.toFixed(1) }}</span>
                                         </div>
                                         <div class="reader-control">
                                             <span>{{ t('detail.readingWidth') }}</span>
                                             <input type="range" min="420" max="900" step="20" v-model.number="readerSettings.contentWidth" />
                                             <span class="reader-value">{{ readerSettings.contentWidth }}px</span>
                                         </div>
                                         <div class="reader-control">
                                             <span>{{ t('detail.readingIndent') }}</span>
                                             <input type="range" min="0" max="3" step="0.5" v-model.number="readerSettings.indent" />
                                             <span class="reader-value">{{ readerSettings.indent.toFixed(1) }}em</span>
                                         </div>
                                         <div class="reader-control theme">
                                             <span>{{ t('detail.readingTheme') }}</span>
                                             <div class="reader-theme-buttons">
                                                 <button :class="{ active: readerSettings.theme === 'paper' }" @click="readerSettings.theme = 'paper'">{{ t('detail.readingThemePaper') }}</button>
                                                 <button :class="{ active: readerSettings.theme === 'warm' }" @click="readerSettings.theme = 'warm'">{{ t('detail.readingThemeWarm') }}</button>
                                                 <button :class="{ active: readerSettings.theme === 'night' }" @click="readerSettings.theme = 'night'">{{ t('detail.readingThemeNight') }}</button>
                                             </div>
                                         </div>
                                     </div>
                                 </div>
                                 <div v-if="textPreviewLoading" class="text-preview-state">{{ t('detail.textPreviewLoading') }}</div>
                                 <div v-else-if="textPreviewError" class="text-preview-state">{{ t('detail.textPreviewFailed') }}</div>
                                 <div v-else-if="!textPreviewHtml" class="text-preview-state">{{ t('detail.textPreviewEmpty') }}</div>
                                 <div v-else class="novel-reader-shell">
                                     <div class="novel-reader" v-html="textPreviewHtml"></div>
                                 </div>
                                 <div v-if="textPreviewTruncated" class="text-preview-tip">{{ t('detail.textPreviewTruncated') }}</div>
                             </div>
                             <div v-else class="text-preview-state">{{ t('detail.textPreviewUnsupported') }}</div>
                         </div>
                     </template>
                      <template v-else>
                         <div class="relative group/media overflow-hidden rounded-2xl w-full h-full flex items-center justify-center">
                             <a-image :src="picture.url" :alt="picture.name" @contextmenu.prevent class="work-detail-image shadow-diffusion transition-all duration-700 hover:scale-[1.02]" />
                         </div>
                     </template>
 
                     <!-- Floating Actions -->
                     <div class="absolute top-4 right-4 md:top-8 md:right-8 flex flex-col gap-3 md:gap-4 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-all duration-500 scale-100 md:scale-90 md:group-hover:scale-100">
                         <button v-if="showDownloadAction" class="w-10 h-10 md:w-12 md:h-12 rounded-2xl bg-white/90 backdrop-blur-xl border border-white flex items-center justify-center text-slate-600 hover:bg-primary hover:text-white transition-all shadow-md hover:shadow-glow hover:-translate-y-1" @click="doDownload"><DownloadOutlined class="text-base md:text-lg" /></button>
                         <button v-if="canShare" class="w-10 h-10 md:w-12 md:h-12 rounded-2xl bg-white/90 backdrop-blur-xl border border-white flex items-center justify-center text-slate-600 hover:bg-primary hover:text-white transition-all shadow-md hover:shadow-glow hover:-translate-y-1" @click="doShare"><ShareAltOutlined class="text-base md:text-lg" /></button>
                         <button class="w-10 h-10 md:w-12 md:h-12 rounded-2xl bg-white/90 backdrop-blur-xl border border-white flex items-center justify-center text-slate-600 hover:bg-primary hover:text-white transition-all shadow-md hover:shadow-glow hover:-translate-y-1" @click="scrollToComments">
                             <span class="material-symbols-outlined text-[20px] md:text-[24px]">comment</span>
                         </button>
                     </div>
                 </section>
 
                <!-- Description Section -->
                <section class="glass-effect p-6 md:p-10 rounded-[1.5rem] md:rounded-[2.5rem]">
                     <h1 class="text-2xl sm:text-3xl md:text-5xl font-black text-slate-800 mb-4 sm:mb-6 tracking-tight leading-tight break-words">{{ picture.name }}</h1>
                     <p class="text-slate-600 leading-relaxed text-base sm:text-lg md:text-xl mb-8 sm:mb-10 whitespace-pre-wrap font-medium opacity-90">
                         {{ picture.introduction || t('detail.artistBioPlaceholder') }}
                     </p>
                     <div v-if="showDownloadAction" class="flex flex-wrap gap-3 mb-8 sm:mb-10">
                          <button class="px-5 py-2.5 rounded-2xl bg-white text-slate-700 border border-slate-200 font-bold hover:bg-primary hover:text-white transition-all" @click="doDownload">
                              {{ t('detail.download') }}
                          </button>
                     </div>
                     <!-- Tags -->
                     <div class="flex flex-wrap gap-3 pt-8 border-t border-slate-100/50">
                          <a-tag v-for="tag in picture.tags" :key="tag" class="!border-none !bg-slate-100/80 !text-slate-600 !px-6 !py-2.5 !rounded-2xl !text-sm !font-bold hover:!bg-primary/20 hover:!text-primary cursor-pointer transition-all hover:-translate-y-1">#{{ tag }}</a-tag>
                     </div>
                 </section>
 
                <!-- Comment Section -->
                <section v-if="canViewComments" class="glass-effect p-6 md:p-10 rounded-[1.5rem] md:rounded-[2.5rem]" id="comments">
                     <PictureComment :picture-id="picture.id" :picture-user-id="picture.userId" />
                 </section>
                <section v-else-if="picture.spaceId" class="glass-effect p-6 md:p-10 rounded-[1.5rem] md:rounded-[2.5rem] text-slate-500 text-sm font-semibold">
                     {{ t('detail.spaceCommentPrivate') }}
                 </section>
             </div>
 
             <!-- Right Column: Sidebar Info -->
            <div class="lg:w-[28%] space-y-6 md:space-y-8">
                <!-- Author Card -->
                <div class="glass-effect p-6 md:p-8 rounded-[1.5rem] md:rounded-[2.5rem] relative overflow-hidden group">
                     <div class="absolute top-0 right-0 w-24 h-24 bg-primary/5 rounded-full -mr-12 -mt-12 transition-all group-hover:scale-150"></div>
                     <div class="flex items-center gap-5 mb-8 relative z-10">
                         <div class="w-16 h-16 rounded-3xl border-4 border-white shadow-md overflow-hidden flex-shrink-0 rotate-3 group-hover:rotate-0 transition-transform">
                             <img :src="picture.user?.userAvatar || 'https://via.placeholder.com/100'" class="w-full h-full object-cover" />
                         </div>
                         <div class="flex-1 min-w-0">
                             <h3 class="font-black text-slate-800 truncate text-xl">{{ picture.user?.userName || t('detail.defaultArtist') }}</h3>
                             <p class="text-sm font-bold text-primary truncate opacity-80 cursor-pointer hover:underline">@{{ picture.user?.userAccount || t('common.unknown') }}</p>
                         </div>
                     </div>
                     <button 
                        v-if="!isOwner"
                        class="w-full py-4 font-black rounded-2xl shadow-glow hover:-translate-y-1 active:scale-95 transition-all flex items-center justify-center gap-2"
                        :class="isFollowing ? 'bg-slate-100 text-slate-500 hover:bg-slate-200 shadow-none' : 'bg-primary text-white hover:shadow-primary/40'"
                        @click="handleFollow"
                        :disabled="followLoading"
                    >
                        <template v-if="followLoading">
                            <a-spin size="small" />
                        </template>
                        <template v-else>
                            <span v-if="isFollowing" class="material-symbols-outlined text-[20px]">check</span>
                            <span v-else class="material-symbols-outlined text-[20px]">add</span>
                            {{ isFollowing ? t('common.following') : t('detail.follow') }}
                        </template>
                    </button>
                 </div>
 
                 <div class="glass-effect p-8 rounded-[2.5rem]">
                     <div class="grid grid-cols-1 gap-5">
                         <button 
                             :class="`flex flex-row items-center justify-center gap-3 p-5 rounded-3xl transition-all ${picture.isLiked ? 'bg-red-50 text-red-500 shadow-glow shadow-red-200' : 'bg-slate-50 text-slate-400 hover:bg-red-50/50'}`" 
                             @click="doLike"
                         >
                              <HeartFilled v-if="picture.isLiked" class="text-2xl animate-pulse" />
                              <HeartOutlined v-else class="text-2xl" />
                              <span class="text-sm font-black uppercase tracking-wider">{{ formatCount(picture.likeCount) || 0 }} {{ t('detail.likes') }}</span>
                         </button>
                         <button
                             :class="`flex flex-row items-center justify-center gap-3 p-5 rounded-3xl transition-all ${picture.isFavorited ? 'bg-amber-50 text-amber-500 shadow-glow shadow-amber-200' : 'bg-slate-50 text-slate-400 hover:bg-amber-50/60'}`"
                             @click="doFavorite"
                         >
                              <StarFilled v-if="picture.isFavorited" class="text-2xl" />
                              <StarOutlined v-else class="text-2xl" />
                              <span class="text-sm font-black uppercase tracking-wider">
                                  {{ picture.isFavorited ? t('detail.favorited') : t('detail.favorite') }}
                              </span>
                         </button>
                     </div>
	                 </div>

	                 <div v-if="shouldShowEditLockCard" class="glass-effect p-8 rounded-[2.5rem] space-y-4">
	                     <div class="flex items-center justify-between gap-3">
	                         <h4 class="text-xs font-black text-slate-400 uppercase tracking-widest">{{ t('detail.editLockTitle') }}</h4>
	                         <span
	                             class="px-3 py-1 rounded-full text-xs font-bold"
	                             :class="isLockedByOtherUser ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'"
	                         >
	                             {{ isLockedByOtherUser ? t('detail.editLockStatusBusy') : t('detail.editLockStatusFree') }}
	                         </span>
	                     </div>
	                     <p
	                         class="text-sm font-semibold leading-relaxed"
	                         :class="isLockedByOtherUser ? 'text-amber-700' : 'text-emerald-700'"
	                     >
	                         {{ isLockedByOtherUser ? t('detail.editLockBusy', { user: editLockInfo?.userName || t('common.unknown') }) : t('detail.editLockFree') }}
	                     </p>
	                     <p v-if="isLockedByOtherUser && editLockInfo?.expireAt" class="text-xs font-semibold text-slate-400">
	                         {{ t('detail.editLockExpire', { time: formatLockExpireTime(editLockInfo.expireAt) }) }}
	                     </p>
	                 </div>

	                <!-- Details Specs -->
	                 <div class="glass-effect p-8 rounded-[2.5rem] space-y-5">
                      <h4 class="text-xs font-black text-slate-400 uppercase tracking-widest mb-4">{{ t('detail.infoTitle') }}</h4>
                     <div class="flex flex-col gap-2 text-sm">
                         <span class="text-slate-500 font-bold flex items-center gap-3 leading-tight"><CalendarOutlined class="text-primary" /> {{ t('detail.dateLabel') }}</span>
                         <span class="font-black text-slate-800 leading-tight">{{ formatDate(picture.createTime) }}</span>
                     </div>
                     <div class="flex flex-col gap-2 text-sm">
                         <span class="text-slate-500 font-bold flex items-center gap-3 leading-tight"><FileImageOutlined class="text-primary" /> {{ t('detail.formatLabel') }}</span>
                         <span class="font-black text-slate-800 uppercase leading-tight">{{ displayFormat }}</span>
                     </div>
                     <div v-if="picture.picSize" class="flex flex-col gap-2 text-sm">
                         <span class="text-slate-500 font-bold flex items-center gap-3 leading-tight"><HddOutlined class="text-primary" /> {{ t('detail.fileSizeLabel') }}</span>
                         <span class="font-black text-slate-800 leading-tight">{{ formatSize(picture.picSize) }}</span>
                     </div>
                     <div v-if="showDuration" class="flex flex-col gap-2 text-sm">
                         <span class="text-slate-500 font-bold flex items-center gap-3 leading-tight"><ClockCircleOutlined class="text-primary" /> {{ t('detail.durationLabel') }}</span>
                         <span class="font-black text-slate-800 leading-tight">{{ formatDuration(audioDuration) }}</span>
                     </div>
                     <div v-if="showDimension" class="flex flex-col gap-2 text-sm">
                         <span class="text-slate-500 font-bold flex items-center gap-3 leading-tight"><ColumnWidthOutlined class="text-primary" /> {{ t('detail.scaleLabel') }}</span>
                         <span class="font-black text-slate-800 leading-tight">{{ picture.picWidth }} × {{ picture.picHeight }}</span>
                     </div>
                     <div v-if="canEdit || canDelete" class="pt-6 mt-4 border-t border-slate-100/50 flex gap-4">
                          <a-button v-if="canEdit" class="flex-1 !h-12 !rounded-2xl !font-bold" @click="doEdit"><template #icon><EditOutlined /></template> {{ t('detail.edit') }}</a-button>
                          <a-button v-if="canDelete" danger class="!h-12 !w-12 !rounded-2xl !font-bold flex items-center justify-center p-0" @click="doDelete"><template #icon><DeleteOutlined class="!m-0" /></template></a-button>
                     </div>
                 </div>

                 <div v-if="canViewEditLogs" class="glass-effect p-8 rounded-[2.5rem] space-y-5">
                      <div class="flex items-center justify-between gap-3">
                          <h4 class="text-xs font-black text-slate-400 uppercase tracking-widest">{{ t('detail.collabTitle') }}</h4>
                          <span v-if="editLogs.length" class="px-3 py-1 rounded-full bg-slate-100 text-slate-500 text-xs font-bold">{{ filteredEditLogs.length }}/{{ editLogs.length }}</span>
                      </div>
                      <div v-if="editLogLoading" class="text-sm font-semibold text-slate-400">
                          {{ t('detail.collabLoading') }}
                      </div>
                      <div v-else-if="!editLogs.length" class="text-sm font-semibold text-slate-400">
                          {{ t('detail.collabEmpty') }}
                      </div>
                      <div v-else class="space-y-4">
                          <div class="grid grid-cols-1 md:grid-cols-[minmax(0,1fr)_220px_auto] gap-3">
                              <a-input
                                  v-model:value="editLogFilters.keyword"
                                  :placeholder="t('detail.collabSearchPlaceholder')"
                                  class="!rounded-2xl"
                              />
                              <a-select
                                  v-model:value="editLogFilters.operatorId"
                                  allow-clear
                                  :placeholder="t('detail.collabOperator')"
                                  :options="editLogOperatorOptions"
                                  class="w-full"
                              />
                              <button
                                  class="px-4 py-2 rounded-2xl bg-slate-100 text-slate-500 text-sm font-semibold hover:bg-slate-200"
                                  @click="clearEditLogFilters"
                              >
                                  {{ t('detail.collabClearFilters') }}
                              </button>
                          </div>

                          <div v-if="!filteredEditLogs.length" class="text-sm font-semibold text-slate-400">
                              {{ t('detail.collabNoMatch') }}
                          </div>
                          <template v-else>
                              <div
                                  v-for="log in visibleEditLogs"
                                  :key="log.id"
                                  class="rounded-3xl border border-slate-100 bg-slate-50/80 px-5 py-4 space-y-3"
                              >
                                  <div class="flex items-start gap-3">
                                      <img
                                          :src="log.operatorUser?.userAvatar || defaultAvatar"
                                          :alt="log.operatorUser?.userName || t('common.unknown')"
                                          class="w-11 h-11 rounded-2xl object-cover border border-white shadow-sm flex-shrink-0"
                                      />
                                      <div class="min-w-0 flex-1">
                                          <div class="flex items-center justify-between gap-3">
                                              <p class="font-black text-slate-800 truncate">{{ log.operatorUser?.userName || t('common.unknown') }}</p>
                                              <span class="text-xs font-semibold text-slate-400 whitespace-nowrap">{{ formatDateTime(log.createTime) }}</span>
                                          </div>
                                          <p class="mt-1 text-sm font-semibold text-slate-500 leading-relaxed">
                                              {{ log.changeSummary || t('detail.collabEdited') }}
                                          </p>
                                      </div>
                                  </div>
                                  <div class="space-y-2 text-xs leading-relaxed">
                                      <p class="font-semibold text-slate-500">
                                          <span class="text-slate-400">{{ t('detail.collabBefore') }}</span>{{ log.beforeSummary || '-' }}
                                      </p>
                                      <p class="font-semibold text-slate-700">
                                          <span class="text-slate-400">{{ t('detail.collabAfter') }}</span>{{ log.afterSummary || '-' }}
                                      </p>
                                  </div>
                              </div>

                              <div v-if="filteredEditLogs.length > 5" class="flex flex-wrap justify-center gap-3">
                                  <button
                                      v-if="hasMoreEditLogs"
                                      class="px-4 py-2 rounded-2xl bg-primary/10 text-primary text-sm font-semibold hover:bg-primary/15"
                                      @click="editLogVisibleCount += 5"
                                  >
                                      {{ t('detail.collabLoadMore') }}
                                  </button>
                                  <button
                                      v-if="editLogVisibleCount > 5"
                                      class="px-4 py-2 rounded-2xl bg-slate-100 text-slate-500 text-sm font-semibold hover:bg-slate-200"
                                      @click="collapseEditLogs"
                                  >
                                      {{ t('detail.collabCollapseLogs') }}
                                  </button>
                              </div>
                          </template>
                      </div>
                 </div>
             </div>
 
         </div>
     </div>

    <!-- Loading -->
    <div v-else class="flex justify-center items-center h-screen">
        <a-spin size="large" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  deletePictureUsingPost,
  getPictureEditLockUsingGet,
  getPictureVoByIdUsingGet,
  generatePictureShareCodeUsingPost,
  getPictureDownloadInfoUsingGet,
  listPictureEditLogsUsingGet,
} from '@/api/asset/assetController'
import { 
    followUserUsingPost, 
    unfollowUserUsingPost, 
    isFollowingUsingGet 
} from '@/api/user/userFollowController'
import { doPictureLikeUsingPost } from '@/api/asset/assetLikeController'
import { addFavoriteUsingPost, cancelFavoriteUsingPost } from '@/api/favorite/favoriteController'
import { listMySpaceUserUsingGet } from '@/api/workspace/workspaceUserController'
import { message, Modal } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/user/useLoginUserStore'
import { useLanguageStore } from '@/stores/system/useLanguageStore'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import defaultAvatar from '@/assets/avatar.png'
import { 
  DownloadOutlined, ShareAltOutlined, HeartOutlined, HeartFilled, StarOutlined, StarFilled,
  EditOutlined, DeleteOutlined, ExpandOutlined,
  FileImageOutlined, HddOutlined, ColumnWidthOutlined, CalendarOutlined, EyeOutlined, ClockCircleOutlined
} from '@ant-design/icons-vue'
import { downloadImage, formatSize } from '@/utils'
import ThreeModelViewer from '@/components/model/ThreeModelViewer.vue'
import dayjs from 'dayjs'

const props = defineProps<{
  id?: string | number
}>()

// State
const picture = ref<API.PictureVO>()
const loginUserStore = useLoginUserStore()
const languageStore = useLanguageStore()
const { t } = storeToRefs(languageStore)
const route = useRoute()
const router = useRouter()
const shareCodeParam = computed(() => {
    const raw = route.query.shareCode
    if (!raw) return ''
    return Array.isArray(raw) ? String(raw[0] || '') : String(raw)
})
const isFollowing = ref(false)
const followLoading = ref(false)
const likeLoading = ref(false)
const currentSpaceRole = ref('')
const editLogs = ref<API.PictureEditLogVO[]>([])
const editLogLoading = ref(false)
const editLockInfo = ref<API.PictureEditLockVO>()
const editLockLoading = ref(false)
const editLogFilters = reactive({
    keyword: '',
    operatorId: undefined as string | undefined,
})
const editLogVisibleCount = ref(5)
const audioRef = ref<HTMLAudioElement | null>(null)
const audioDuration = ref(0)
const audioCurrentTime = ref(0)
const audioPlaying = ref(false)
const textPreviewHtml = ref('')
const textPreviewLoading = ref(false)
const textPreviewError = ref('')
const textPreviewTruncated = ref(false)
const officeFallback = ref(false)
let textPreviewAbort: AbortController | null = null
const showReaderSettings = ref(false)
const defaultReaderSettings = {
    fontSize: 17,
    lineHeight: 1.9,
    contentWidth: 680,
    paragraphSpacing: 1.1,
    indent: 2,
    theme: 'paper',
}
const readerSettings = ref({ ...defaultReaderSettings })

const readerThemes = {
    paper: {
        background: 'linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96))',
        text: '#1e293b',
        border: 'rgba(226, 232, 240, 0.9)',
        accent: '#94a3b8',
        toolbarBg: 'rgba(255, 255, 255, 0.9)',
        toolbarBorder: 'rgba(226, 232, 240, 0.8)',
        toolbarText: '#475569',
    },
    warm: {
        background: 'linear-gradient(180deg, rgba(255, 249, 240, 0.98), rgba(251, 244, 232, 0.96))',
        text: '#3f2d1c',
        border: 'rgba(238, 220, 198, 0.9)',
        accent: '#c29b6a',
        toolbarBg: 'rgba(255, 248, 235, 0.92)',
        toolbarBorder: 'rgba(236, 214, 186, 0.9)',
        toolbarText: '#6b4b2a',
    },
    night: {
        background: 'linear-gradient(180deg, rgba(15, 23, 42, 0.98), rgba(2, 6, 23, 0.96))',
        text: '#e2e8f0',
        border: 'rgba(30, 41, 59, 0.9)',
        accent: '#94a3b8',
        toolbarBg: 'rgba(15, 23, 42, 0.88)',
        toolbarBorder: 'rgba(51, 65, 85, 0.9)',
        toolbarText: '#cbd5f5',
    },
} as const

type MarkdownItCtor = typeof import('markdown-it')['default']
type MarkdownItInstance = InstanceType<MarkdownItCtor>
type DomPurifyInstance = (typeof import('dompurify'))['default']
type MammothModule = typeof import('mammoth')
type RtfJsModule = typeof import('rtf.js')

let markdownParser: MarkdownItInstance | null = null
let domPurify: DomPurifyInstance | null = null
let mammothModule: MammothModule | null = null
let rtfJsModule: RtfJsModule | null = null

const ensureDomPurify = async () => {
    if (!domPurify) {
        const module = await import('dompurify')
        domPurify = module.default
    }
    return domPurify
}

const sanitizeTextPreviewHtml = async (html: string) => {
    const purifier = await ensureDomPurify()
    return purifier.sanitize(html)
}

const ensureMarkdownParser = async () => {
    if (!markdownParser) {
        const module = await import('markdown-it')
        const MarkdownIt = module.default
        markdownParser = new MarkdownIt({
            html: false,
            linkify: true,
            breaks: true,
        })
    }
    return markdownParser
}

const ensureMammoth = async () => {
    if (!mammothModule) {
        mammothModule = await import('mammoth')
    }
    return mammothModule
}

const ensureRtfJs = async () => {
    if (!rtfJsModule) {
        rtfJsModule = await import('rtf.js')
    }
    return rtfJsModule
}

const mediaFormats = {
    image: ['jpg', 'jpeg', 'png', 'webp', 'gif', 'bmp', 'tif', 'tiff', 'svg'],
    video: ['mp4', 'webm', 'mov', 'avi', 'mkv'],
    audio: ['mp3', 'wav', 'flac', 'm4a', 'ogg', 'aac'],
    model: ['pmx', 'pmd', 'glb', 'gltf', 'vrm', 'obj', 'fbx'],
    text: ['txt', 'md', 'markdown', 'pdf', 'doc', 'docx', 'rtf', 'csv', 'json', 'xml', 'yaml', 'yml'],
}
const knownFormats = new Set(Object.values(mediaFormats).flat())
const stableModelPreviewFormats = ['pmx', 'pmd', 'glb', 'gltf', 'vrm']
const textPreviewableFormats = ['txt', 'md', 'markdown', 'csv', 'json', 'xml', 'yaml', 'yml']
const docxFormats = ['docx']
const rtfFormats = ['rtf']
const officeFormats = ['doc']

const normalizeFormat = (value?: string) => {
    if (!value) return ''
    let format = value.toString().trim().toLowerCase()
    if (format.includes('/')) {
        format = format.split('/').pop() || ''
    }
    if (format.includes(';')) {
        format = format.split(';')[0]
    }
    if (format.includes('+')) {
        format = format.split('+')[0]
    }
    return format.replace(/^\./, '')
}

// Computed
const canEdit = computed(() => {
    const loginUser = loginUserStore.loginUser
    if (!loginUser.id || !picture.value) return false
    const user = picture.value.user || {}
    return loginUser.id === user.id
        || loginUser.userRole === 'admin'
        || ['admin', 'editor'].includes(currentSpaceRole.value)
})

const canDelete = computed(() => {
    const loginUser = loginUserStore.loginUser
    if (!loginUser.id || !picture.value) return false
    return loginUser.id === picture.value.userId
        || loginUser.userRole === 'admin'
        || currentSpaceRole.value === 'admin'
})

const canShare = computed(() => {
    const loginUser = loginUserStore.loginUser
    if (!loginUser.id || !picture.value) return false
    return loginUser.id === picture.value.userId
        || loginUser.userRole === 'admin'
        || ['admin', 'editor'].includes(currentSpaceRole.value)
})

const canViewComments = computed(() => {
    if (!picture.value?.spaceId) return true
    const loginUser = loginUserStore.loginUser
    if (loginUser.userRole === 'admin' || loginUser.id === picture.value.userId) {
        return true
    }
    return ['viewer', 'editor', 'admin'].includes(currentSpaceRole.value)
})

const canViewEditLogs = computed(() => {
    return !!picture.value?.spaceId && canViewComments.value
})

const shouldShowEditLockCard = computed(() => {
    return !!picture.value?.spaceId && !!loginUserStore.loginUser.id && canEdit.value
})

const isLockedByOtherUser = computed(() => {
    return !!editLockInfo.value?.supported && !!editLockInfo.value?.locked && !editLockInfo.value?.lockedByCurrentUser
})

const getEditLogOperatorId = (log?: API.PictureEditLogVO) => {
    const raw = log?.operatorUser?.id ?? log?.operatorUserId
    if (raw === undefined || raw === null) return ''
    return String(raw)
}

const editLogOperatorOptions = computed(() => {
    const userMap = new Map<string, string>()
    editLogs.value.forEach((log) => {
        const id = getEditLogOperatorId(log)
        if (!id) return
        userMap.set(id, log.operatorUser?.userName || t.value('common.unknown'))
    })
    return Array.from(userMap.entries()).map(([value, label]) => ({ value, label }))
})

const filteredEditLogs = computed(() => {
    const keyword = editLogFilters.keyword.trim().toLowerCase()
    return editLogs.value.filter((log) => {
        const operatorMatched = !editLogFilters.operatorId || getEditLogOperatorId(log) === editLogFilters.operatorId
        const keywordMatched = !keyword || [
            log.operatorUser?.userName,
            log.changeSummary,
            log.beforeSummary,
            log.afterSummary,
        ]
            .filter(Boolean)
            .some((item) => String(item).toLowerCase().includes(keyword))
        return operatorMatched && keywordMatched
    })
})

const visibleEditLogs = computed(() => filteredEditLogs.value.slice(0, editLogVisibleCount.value))

const hasMoreEditLogs = computed(() => filteredEditLogs.value.length > editLogVisibleCount.value)

const clearEditLogFilters = () => {
    editLogFilters.keyword = ''
    editLogFilters.operatorId = undefined
    editLogVisibleCount.value = 5
}

const collapseEditLogs = () => {
    editLogVisibleCount.value = 5
}

const isOwner = computed(() => {
    const loginUser = loginUserStore.loginUser
    if (!loginUser.id || !picture.value) return false
    return loginUser.id === picture.value.userId
})

const hasDownloadHintTag = computed(() => {
    const tags = picture.value?.tags ?? []
    if (!Array.isArray(tags)) return false
    return tags.some((tag) => {
        const text = String(tag || '').trim().toLowerCase()
        if (!text) return false
        return text.includes('允许下载') || text.includes('可下载') || text.includes('download')
    })
})

const canDownload = computed(() => {
    if (!picture.value) return false
    const loginUser = loginUserStore.loginUser
    if (!loginUser.id) return false
    return loginUser.userRole === 'admin' || loginUser.id === picture.value.userId
})

const showDownloadAction = computed(() => {
    return canDownload.value || hasDownloadHintTag.value
})

const currentFormat = computed(() => {
    if (!picture.value) return ''
    const raw = normalizeFormat(picture.value.picFormat)
    const urlFormat = normalizeFormat(picture.value.url?.split('?')?.[0]?.split('.')?.pop())
    if (raw && knownFormats.has(raw)) return raw
    if (urlFormat && knownFormats.has(urlFormat)) return urlFormat
    return ''
})

const displayFormat = computed(() => {
    if (currentFormat.value) return currentFormat.value.toUpperCase()
    if (mediaType.value === 'image') return 'PNG'
    const fallback = mediaFormats[mediaType.value]?.[0]
    return fallback ? fallback.toUpperCase() : t.value('common.unknown')
})

const mediaType = computed(() => {
    if (!picture.value) return 'image'
    const fmt = currentFormat.value
    const cat = picture.value.category || ''

    if (cat === '素材' || cat === '模型' || cat === '3D模型' || mediaFormats.model.includes(fmt)) {
        return 'model'
    }
    if (mediaFormats.video.includes(fmt)) {
        return 'video'
    }
    if (mediaFormats.audio.includes(fmt)) {
        return 'audio'
    }
    if (mediaFormats.text.includes(fmt)) {
        return 'text'
    }
    return 'image'
})

const textPreviewType = computed(() => {
    if (mediaType.value !== 'text') return 'none'
    if (currentFormat.value === 'pdf') return 'pdf'
    if (docxFormats.includes(currentFormat.value)) return 'docx'
    if (rtfFormats.includes(currentFormat.value)) return 'rtf'
    if (officeFormats.includes(currentFormat.value)) return 'office'
    if (textPreviewableFormats.includes(currentFormat.value)) return 'text'
    return 'none'
})

const textPreviewMode = computed(() => {
    if (textPreviewType.value !== 'text') return 'none'
    if (['md', 'markdown'].includes(currentFormat.value)) return 'markdown'
    return 'plain'
})

const showOfficeViewer = computed(() => {
    return (textPreviewType.value === 'office' || officeFallback.value) && !!picture.value?.url
})

const supportsModelPreview = computed(() => {
    if (mediaType.value !== 'model') return false
    return stableModelPreviewFormats.includes(currentFormat.value)
})

const officePreviewUrl = computed(() => {
    if (!showOfficeViewer.value || !picture.value?.url) return ''
    return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(picture.value.url)}`
})

const readerStyle = computed(() => {
    const theme = readerThemes[readerSettings.value.theme as keyof typeof readerThemes] || readerThemes.paper
    return {
        '--reader-font-size': `${readerSettings.value.fontSize}px`,
        '--reader-line-height': String(readerSettings.value.lineHeight),
        '--reader-width': `${readerSettings.value.contentWidth}px`,
        '--reader-paragraph-gap': `${readerSettings.value.paragraphSpacing}em`,
        '--reader-indent': `${readerSettings.value.indent}em`,
        '--reader-bg': theme.background,
        '--reader-text': theme.text,
        '--reader-border': theme.border,
        '--reader-accent': theme.accent,
        '--reader-toolbar-bg': theme.toolbarBg,
        '--reader-toolbar-border': theme.toolbarBorder,
        '--reader-toolbar-text': theme.toolbarText,
    } as Record<string, string>
})

const showDimension = computed(() => {
    return !!picture.value?.picWidth && !!picture.value?.picHeight
})

const showDuration = computed(() => {
    return mediaType.value === 'audio' && audioDuration.value > 0
})

const audioProgress = computed(() => {
    if (!audioDuration.value) return 0
    return Math.min(audioCurrentTime.value / audioDuration.value, 1)
})

const clampNumber = (value: number, min: number, max: number) => {
    return Math.min(Math.max(value, min), max)
}

const loadReaderSettings = () => {
    try {
        const raw = localStorage.getItem('novelReaderSettings')
        if (!raw) return
        const parsed = JSON.parse(raw) as Partial<typeof defaultReaderSettings>
        const theme = parsed.theme && readerThemes[parsed.theme as keyof typeof readerThemes] ? parsed.theme : defaultReaderSettings.theme
        Object.assign(readerSettings.value, {
            fontSize: clampNumber(Number(parsed.fontSize ?? defaultReaderSettings.fontSize), 14, 26),
            lineHeight: clampNumber(Number(parsed.lineHeight ?? defaultReaderSettings.lineHeight), 1.4, 2.4),
            contentWidth: clampNumber(Number(parsed.contentWidth ?? defaultReaderSettings.contentWidth), 420, 900),
            paragraphSpacing: clampNumber(Number(parsed.paragraphSpacing ?? defaultReaderSettings.paragraphSpacing), 0.6, 2),
            indent: clampNumber(Number(parsed.indent ?? defaultReaderSettings.indent), 0, 3),
            theme,
        })
    } catch {
        // ignore invalid storage
    }
}

const resetReaderSettings = () => {
    Object.assign(readerSettings.value, { ...defaultReaderSettings })
}

const resetAudioState = () => {
    audioDuration.value = 0
    audioCurrentTime.value = 0
    audioPlaying.value = false
}

const handleAudioMetadata = (event: Event) => {
    const target = event.target as HTMLAudioElement | null
    const duration = target?.duration ?? 0
    audioDuration.value = Number.isFinite(duration) ? duration : 0
}

const handleAudioTimeUpdate = (event: Event) => {
    const target = event.target as HTMLAudioElement | null
    audioCurrentTime.value = target?.currentTime ?? 0
}

const resetTextPreview = () => {
    if (textPreviewAbort) {
        textPreviewAbort.abort()
        textPreviewAbort = null
    }
    textPreviewHtml.value = ''
    textPreviewError.value = ''
    textPreviewLoading.value = false
    textPreviewTruncated.value = false
    officeFallback.value = false
}

const escapeHtml = (value: string) => {
    return value
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
}

const renderPlainText = (content: string) => {
    const normalized = content.replace(/\r\n/g, '\n')
    const blocks = normalized.split(/\n{2,}/).map(block => block.trim()).filter(Boolean)
    if (blocks.length === 0) return ''
    return blocks
        .map(block => `<p>${escapeHtml(block).replace(/\n/g, '<br />')}</p>`)
        .join('')
}

const loadDocxPreview = async (buffer: ArrayBuffer) => {
    const mammoth = await ensureMammoth()
    const result = await mammoth.convertToHtml({ arrayBuffer: buffer })
    textPreviewHtml.value = await sanitizeTextPreviewHtml(result.value || '')
}

const loadRtfPreview = async (buffer: ArrayBuffer) => {
    const rtfJs = await ensureRtfJs()
    rtfJs.RTFJS.loggingEnabled(false)
    rtfJs.WMFJS.loggingEnabled(false)
    rtfJs.EMFJS.loggingEnabled(false)
    const doc = new rtfJs.RTFJS.Document(buffer)
    const htmlElements = await doc.render()
    const container = document.createElement('div')
    container.append(...htmlElements)
    textPreviewHtml.value = await sanitizeTextPreviewHtml(container.innerHTML)
}

const loadTextPreview = async () => {
    if (!picture.value?.url) return
    const previewType = textPreviewType.value
    if (previewType === 'none' || previewType === 'pdf' || previewType === 'office') return
    if (textPreviewAbort) {
        textPreviewAbort.abort()
    }
    const controller = new AbortController()
    textPreviewAbort = controller
    textPreviewHtml.value = ''
    textPreviewError.value = ''
    textPreviewLoading.value = true
    textPreviewTruncated.value = false
    try {
        const response = await fetch(picture.value.url, { signal: controller.signal })
        if (!response.ok) {
            throw new Error('preview failed')
        }
        if (previewType === 'docx') {
            const buffer = await response.arrayBuffer()
            await loadDocxPreview(buffer)
        } else if (previewType === 'rtf') {
            const buffer = await response.arrayBuffer()
            await loadRtfPreview(buffer)
        } else {
            const content = await response.text()
            const limit = 12000
            const sliced = content.slice(0, limit)
            if (content.length > limit) {
                textPreviewTruncated.value = true
            }
            let html = ''
            if (textPreviewMode.value === 'markdown') {
                const markdownParser = await ensureMarkdownParser()
                html = markdownParser.render(sliced)
            } else {
                html = renderPlainText(sliced)
            }
            textPreviewHtml.value = await sanitizeTextPreviewHtml(html)
        }
    } catch (e) {
        if (controller.signal.aborted) return
        textPreviewError.value = 'failed'
        if (previewType === 'docx' || previewType === 'rtf') {
            officeFallback.value = true
        }
    } finally {
        if (textPreviewAbort === controller) {
            textPreviewAbort = null
        }
        textPreviewLoading.value = false
    }
}

const prepareMediaPreview = () => {
    resetAudioState()
    resetTextPreview()
    if (mediaType.value === 'text' && textPreviewType.value !== 'office' && textPreviewType.value !== 'pdf') {
        loadTextPreview()
    }
}

const fetchCurrentSpaceRole = async () => {
    currentSpaceRole.value = ''
    const loginUserId = loginUserStore.loginUser.id
    const spaceId = picture.value?.spaceId
    if (!loginUserId || !spaceId) return
    try {
        const res = await listMySpaceUserUsingGet()
        if (res.data.code === 0) {
            const matched = (res.data.data ?? []).find(item => item.spaceId === spaceId)
            currentSpaceRole.value = matched?.spaceRole || ''
        }
    } catch (e) {
        currentSpaceRole.value = ''
    }
}

const fetchEditLogs = async () => {
    if (!picture.value?.id || !canViewEditLogs.value) {
        editLogs.value = []
        editLogLoading.value = false
        return
    }
    editLogLoading.value = true
    try {
        const res = await listPictureEditLogsUsingGet({ pictureId: picture.value.id })
        if (res.data.code === 0) {
            editLogs.value = res.data.data ?? []
            editLogVisibleCount.value = 5
        } else {
            editLogs.value = []
        }
    } catch (e) {
        editLogs.value = []
    } finally {
        editLogLoading.value = false
    }
}

const fetchEditLockInfo = async (silent = true) => {
    if (!picture.value?.id || !picture.value?.spaceId || !loginUserStore.loginUser.id || !canEdit.value) {
        editLockInfo.value = undefined
        editLockLoading.value = false
        return true
    }
    editLockLoading.value = true
    try {
        const res = await getPictureEditLockUsingGet({ pictureId: picture.value.id })
        if (res.data.code === 0) {
            editLockInfo.value = res.data.data
            if (isLockedByOtherUser.value && !silent) {
                message.warning(t.value('detail.editLockBusy', { user: editLockInfo.value?.userName || t.value('common.unknown') }))
            }
            return !isLockedByOtherUser.value
        }
        editLockInfo.value = undefined
        return true
    } catch (e) {
        if (!silent) {
            message.warning(t.value('detail.editLockFetchFailed'))
        }
        return true
    } finally {
        editLockLoading.value = false
    }
}

watch(
    () => [editLogFilters.keyword, editLogFilters.operatorId],
    () => {
        editLogVisibleCount.value = 5
    },
)

// Actions
const fetchPicture = async () => {
  try {
    const pictureId = String(props.id || route.params.id || '')
    if (!pictureId) {
        message.warning(t.value('common.actionFailed')) // Or redirect
        return
    }
    const res = await getPictureVoByIdUsingGet({ id: pictureId as any, shareCode: shareCodeParam.value || undefined } as any)
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
      prepareMediaPreview()
      await fetchCurrentSpaceRole()
      await fetchEditLogs()
      await fetchEditLockInfo()
      checkFollow()
    } else {
      message.error(t.value('detail.loadFailed') + ': ' + res.data.message)
    }
  } catch (e) {
    message.error(t.value('detail.requestFailed'))
  }
}

const checkFollow = async () => {
    if (!picture.value?.userId || !loginUserStore.loginUser.id) return
    if (picture.value.userId === loginUserStore.loginUser.id) return
    try {
        const res = await isFollowingUsingGet({ followingId: picture.value.userId })
        if (res.data.code === 0) {
            isFollowing.value = res.data.data ?? false
        }
    } catch (e) {
        // silent fail
    }
}

const handleFollow = async () => {
    if (!loginUserStore.loginUser.id) {
        message.warning(t.value('common.loginPrompt'))
        return
    }
    if (!picture.value?.userId) return
    
    followLoading.value = true
    try {
        if (isFollowing.value) {
            const res = await unfollowUserUsingPost(picture.value.userId) // Controller expects body as number, passed directly?
            // checking controller: public BaseResponse<Boolean> unfollowUser(@RequestBody Long followingId)
            // Axios might need explicit object if not configured to send primitive as body.
            // But let's try calling the generated function which expects number. 
            // NOTE: The generated code `unfollowUserUsingPost(body: number)` puts body directly in `data`. 
            // Standard JSON behavior `123` is valid.
            if (res.data.code === 0) {
                isFollowing.value = false
                message.success(t.value('detail.unfollowed'))
            }
        } else {
            const res = await followUserUsingPost(picture.value.userId)
            if (res.data.code === 0) {
                isFollowing.value = true
                message.success(t.value('detail.followed'))
            }
        }
    } catch (e) {
        message.error(t.value('detail.actionFailed'))
    } finally {
        followLoading.value = false
    }
}

const doDownload = async () => {
    if (!showDownloadAction.value) {
        return
    }
    if (!canDownload.value) {
        if (!loginUserStore.loginUser.id) {
            message.warning(t.value('common.loginPrompt'))
        } else {
            message.warning(t.value('detail.noDownloadPermission'))
        }
        return
    }
    if (!picture.value?.id) return
    try {
        const res = await getPictureDownloadInfoUsingGet({ pictureId: picture.value.id })
        if (res.data.code === 0 && res.data.data) {
            const info = res.data.data
            if (info.needsWatermark) {
                message.info(t.value('detail.watermarkNotice'))
            }
            if (info.url.startsWith('/api')) {
                // Backend file download
               const link = document.createElement('a')
               link.href = info.url
               link.target = '_blank'
               link.download = info.fileName
               document.body.appendChild(link)
               link.click()
               document.body.removeChild(link)
            } else {
                 downloadImage(info.url, info.fileName)
            }
        } else {
            message.error(t.value('detail.downloadInfoFailed'))
        }
    } catch (e) {
        message.error(t.value('detail.downloadFailed'))
    }
}

const doShare = async () => {
    if (!loginUserStore.loginUser.id) {
        message.warning(t.value('common.loginPrompt'))
        return
    }
    if (!picture.value?.id) return
    try {
        const res = await generatePictureShareCodeUsingPost({ pictureId: picture.value.id })
        if (res.data.code === 0 && res.data.data) {
             const code = res.data.data
             const sharePath = router.resolve({ path: `/asset/${picture.value.id}`, query: { shareCode: code } }).href
             const shareUrl = `${window.location.origin}${sharePath}`
             Modal.success({
                 title: t.value('detail.shareCreated'),
                 content: `${t.value('detail.shareCode')}: ${code}`,
                 onOk() {
                     // Fallback copy for non-HTTPS environments
                     const copyText = async (text: string) => {
                         try {
                             if (navigator.clipboard && window.isSecureContext) {
                                 await navigator.clipboard.writeText(text)
                                 return true
                             }
                         } catch {}
                         // Fallback
                         const textArea = document.createElement('textarea')
                         textArea.value = text
                         textArea.style.position = 'fixed'
                         textArea.style.left = '-9999px'
                         document.body.appendChild(textArea)
                         textArea.focus()
                         textArea.select()
                         let success = false
                         try { success = document.execCommand('copy') } catch {}
                         document.body.removeChild(textArea)
                         return success
                     }
                     copyText(shareUrl).then(ok => {
                         if (ok) message.success(t.value('detail.copySuccess'))
                         else message.error(t.value('detail.copyFailed'))
                     })
                 }
             })
        } else {
            message.error(t.value('detail.shareFailed'))
        }
    } catch (e) {
        message.error(t.value('detail.shareFailed'))
    }
}

const doEdit = async () => {
    if (!loginUserStore.loginUser.id) {
        message.warning(t.value('common.loginPrompt'))
        return
    }
    const lockAvailable = await fetchEditLockInfo(false)
    if (!lockAvailable) {
        return
    }
    router.push('/asset/upload?id=' + picture.value?.id)
}

const doDelete = async () => {
    if (!loginUserStore.loginUser.id) {
        message.warning(t.value('common.loginPrompt'))
        return
    }
    if (!picture.value?.id) return
    const res = await deletePictureUsingPost({ id: picture.value.id })
    if (res.data.code === 0) {
        message.success(t.value('detail.deleteSuccess'))
        router.push('/')
    } else {
        message.error(t.value('detail.deleteFailed'))
    }
}

const doLike = async () => {
    if (!loginUserStore.loginUser.id) {
        message.warning(t.value('common.loginPrompt'))
        return
    }
    if (!picture.value?.id || likeLoading.value) return
    likeLoading.value = true
    try {
        const res = await doPictureLikeUsingPost({ id: picture.value.id })
        if (res.data.code === 0 && picture.value) {
             const delta = Number(res.data.data ?? 0) || 0
             picture.value.isLiked = delta === 1
             const nextCount = Number(picture.value.likeCount ?? 0) + delta
             picture.value.likeCount = Math.max(0, nextCount)
             await fetchPicture()
             window.dispatchEvent(new Event('notification-refresh'))
        } else {
            message.error(t.value('detail.actionFailed'))
        }
    } catch(e) {
        message.error(t.value('detail.actionFailed'))
    } finally {
        likeLoading.value = false
    }
}

const doFavorite = async () => {
    if (!loginUserStore.loginUser.id) {
        message.warning(t.value('common.loginPrompt'))
        return
    }
    if (!picture.value?.id) return
    try {
        if (picture.value.isFavorited) {
             const res = await cancelFavoriteUsingPost({ pictureId: picture.value.id })
             if (res.data.code === 0) {
                 message.success(t.value('detail.unfavorited'))
                 fetchPicture()
                 window.dispatchEvent(new Event('notification-refresh'))
             }
        } else {
             const res = await addFavoriteUsingPost({ pictureId: picture.value.id })
             if (res.data.code === 0) {
                 message.success(t.value('detail.favorited'))
                 fetchPicture()
                 window.dispatchEvent(new Event('notification-refresh'))
             }
        }
    } catch (e) {
        message.error(t.value('detail.actionFailed'))
    }
}

const scrollToComments = () => {
    document.getElementById('comments')?.scrollIntoView({ behavior: 'smooth' })
}

const toggleFullscreen = () => {
    message.info(t.value('detail.fullscreen'))
}

const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-'
    return dayjs(dateStr).format('YYYY-MM-DD')
}

const formatDateTime = (dateStr?: string) => {
    if (!dateStr) return '-'
    return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

const formatLockExpireTime = (timestamp?: number) => {
    if (!timestamp) return '--:--'
    return dayjs(timestamp).format('HH:mm:ss')
}

const formatDuration = (value?: number) => {
    if (value === undefined || value === null || !Number.isFinite(value)) return '--:--'
    const total = Math.floor(value)
    const minutes = Math.floor(total / 60)
    const seconds = total % 60
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

const formatCount = (count?: number) => {
    if (!count) return '0'
    if (count < 1000) return count.toString()
    if (count < 10000) return (count / 1000).toFixed(1) + 'k'
    return (count / 10000).toFixed(1) + 'w'
}

onMounted(() => {
    loadReaderSettings()
    fetchPicture()
})

watch(readerSettings, (value) => {
    localStorage.setItem('novelReaderSettings', JSON.stringify(value))
}, { deep: true })

watch(() => [props.id, route.params.id, route.query.shareCode], () => {
    fetchPicture()
})
</script>

<style scoped>
.shadow-diffusion {
    box-shadow: 0 40px 100px -20px rgba(15, 118, 110, 0.15), 0 20px 40px -20px rgba(15, 118, 110, 0.1);
}

.animate-blob {
    animation: blob 10s infinite;
}

.animation-delay-2000 {
    animation-delay: 2s;
}

.animation-delay-4000 {
    animation-delay: 4s;
}

@keyframes blob {
    0% { transform: translate(0px, 0px) scale(1); }
    33% { transform: translate(50px, -70px) scale(1.15); }
    66% { transform: translate(-30px, 40px) scale(0.9); }
    100% { transform: translate(0px, 0px) scale(1); }
}

@keyframes pulse-custom {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.1); }
}

.animate-pulse {
    animation: pulse-custom 0.4s ease-out;
}

:deep(.work-detail-image) {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

:deep(.work-detail-image .ant-image-img) {
    width: 100%;
    height: 100%;
    object-fit: contain;
    object-position: center;
    display: block;
}

.work-detail-video {
    width: 100%;
    height: 100%;
    object-fit: contain;
    object-position: center;
    display: block;
}

.audio-visual {
    display: grid;
    grid-template-columns: repeat(24, 1fr);
    gap: 6px;
    height: 80px;
    align-items: end;
    padding: 8px 6px;
    background: rgba(148, 163, 184, 0.08);
    border-radius: 16px;
}

.audio-visual span {
    display: block;
    width: 100%;
    height: 20%;
    background: rgba(15, 118, 110, 0.45);
    border-radius: 6px;
    animation: audio-wave 1.2s ease-in-out infinite;
    animation-play-state: paused;
}

.audio-visual.is-playing span {
    animation-play-state: running;
}

.audio-visual span:nth-child(3n) {
    animation-delay: 0.2s;
}

.audio-visual span:nth-child(4n) {
    animation-delay: 0.35s;
}

.audio-visual span:nth-child(5n) {
    animation-delay: 0.5s;
}

.audio-progress {
    height: 6px;
    background: rgba(148, 163, 184, 0.2);
    border-radius: 999px;
    overflow: hidden;
}

.audio-progress-bar {
    height: 100%;
    background: linear-gradient(90deg, #0f766e, #115e59);
    transition: width 0.2s ease;
}

.text-preview-wrapper,
.text-preview-state {
    background: rgba(248, 250, 252, 0.9);
    border: 1px solid rgba(226, 232, 240, 0.8);
    border-radius: 18px;
    padding: 16px;
}

.text-preview-wrapper {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.reader-toolbar {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 12px 14px;
    border-radius: 14px;
    border: 1px solid var(--reader-toolbar-border);
    background: var(--reader-toolbar-bg);
}

.reader-toolbar-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.reader-toolbar-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 800;
    font-size: 13px;
    color: var(--reader-toolbar-text);
    letter-spacing: 0.06em;
    text-transform: uppercase;
}

.reader-toolbar-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.reader-btn {
    padding: 6px 12px;
    border-radius: 10px;
    border: 1px solid rgba(203, 213, 225, 0.8);
    background: rgba(255, 255, 255, 0.9);
    color: var(--reader-toolbar-text);
    font-weight: 700;
    font-size: 12px;
    transition: all 0.2s ease;
}

.reader-btn.primary {
    background: #0f766e;
    border-color: transparent;
    color: #fff;
}

.reader-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 12px rgba(15, 23, 42, 0.08);
}

.reader-toolbar-controls {
    display: grid;
    gap: 10px;
}

.reader-control {
    display: grid;
    grid-template-columns: minmax(72px, 1fr) 1fr minmax(64px, auto);
    align-items: center;
    gap: 12px;
    font-size: 12px;
    font-weight: 700;
    color: var(--reader-toolbar-text);
}

.reader-control input[type="range"] {
    width: 100%;
}

.reader-value {
    text-align: right;
    color: var(--reader-toolbar-text);
    font-weight: 800;
    font-size: 12px;
}

.reader-control.theme {
    grid-template-columns: minmax(72px, 1fr) 1fr;
}

.reader-theme-buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.reader-theme-buttons button {
    padding: 6px 10px;
    border-radius: 10px;
    border: 1px solid rgba(203, 213, 225, 0.8);
    background: rgba(255, 255, 255, 0.9);
    color: var(--reader-toolbar-text);
    font-weight: 700;
    font-size: 12px;
}

.reader-theme-buttons button.active {
    background: #0f172a;
    color: #fff;
    border-color: transparent;
}

@media (max-width: 640px) {
    .reader-control {
        grid-template-columns: 1fr;
        align-items: start;
    }

    .reader-value {
        text-align: left;
    }
}

.novel-reader-shell {
    border-radius: 18px;
    border: 1px solid var(--reader-border);
    background: var(--reader-bg);
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
    padding: 18px 20px;
}

.novel-reader {
    max-height: 520px;
    overflow: auto;
    max-width: min(var(--reader-width), 100%);
    margin: 0 auto;
    font-size: var(--reader-font-size);
    line-height: var(--reader-line-height);
    color: var(--reader-text);
    letter-spacing: 0.02em;
    font-family: var(--font-family-reading, "Noto Serif SC", "Source Han Serif SC", "Songti SC", serif);
}

.novel-reader p {
    margin: 0 0 var(--reader-paragraph-gap);
    text-indent: var(--reader-indent);
}

.novel-reader p:last-child {
    margin-bottom: 0;
}

.novel-reader h1,
.novel-reader h2,
.novel-reader h3 {
    margin: 1.2em 0 0.6em;
    font-family: var(--font-family-display, "Montserrat", sans-serif);
    letter-spacing: 0.01em;
    color: #0f172a;
}

.novel-reader ul,
.novel-reader ol {
    margin: 0 0 1em 1.4em;
    padding: 0;
    text-indent: 0;
    line-height: 1.8;
}

.novel-reader li {
    margin-bottom: 0.4em;
}

.novel-reader code {
    padding: 0.12em 0.4em;
    border-radius: 6px;
    background: rgba(15, 23, 42, 0.06);
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
    font-size: 0.9em;
}

.novel-reader pre {
    padding: 1em;
    border-radius: 12px;
    background: rgba(15, 23, 42, 0.06);
    overflow: auto;
}

.novel-reader blockquote {
    margin: 1em 0;
    padding: 0.6em 1em;
    border-left: 4px solid rgba(15, 118, 110, 0.35);
    background: rgba(15, 118, 110, 0.08);
    border-radius: 12px;
    text-indent: 0;
}

.novel-reader a {
    color: var(--reader-accent);
    font-weight: 600;
}

.text-preview-state {
    text-align: center;
    color: #94a3b8;
    font-weight: 700;
    letter-spacing: 0.02em;
}

.text-preview-tip {
    margin-top: 12px;
    text-align: center;
    font-size: 12px;
    font-weight: 700;
    color: #94a3b8;
}

.text-pdf-wrapper {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.text-pdf-frame {
    width: 100%;
    height: 520px;
    border: none;
    border-radius: 18px;
    background: #fff;
}

.text-pdf-hint {
    font-size: 12px;
    font-weight: 700;
    color: #94a3b8;
    text-align: center;
}

@keyframes audio-wave {
    0%, 100% { height: 20%; opacity: 0.5; }
    50% { height: 90%; opacity: 1; }
}
</style>
