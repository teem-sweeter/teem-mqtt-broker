<template>
  <div class="system-upgrade">
    <div class="upgrade-header">
      <h1 class="upgrade-title">{{ t('upgrade.title') }}</h1>
      <p class="upgrade-subtitle">{{ t('upgrade.subtitle') }}</p>
    </div>

    <div class="upgrade-container">
      <!-- 升级文件上传区域 -->
      <div v-if="!isUpgrading" class="upload-section">
        <div class="upload-card">
          <div class="upload-container">
            <el-upload
              class="single-upload"
              drag
              action=""
              :auto-upload="false"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :file-list="fileList"
              :show-file-list="false"
              :limit="1"
              accept=".zip"
            >
              <el-icon class="upload-icon"><upload-filled /></el-icon>
              <div class="upload-text">
                <div class="upload-title">{{ t('upgrade.dragHere') }}</div>
                <div class="upload-desc">{{ t('upgrade.or') }} <em class="upload-link">{{ t('upgrade.clickToUpload') }}</em></div>
              </div>
              <template #tip>
                <div class="upload-tip">
                  {{ t('upgrade.uploadHint') }}
                </div>
              </template>
            </el-upload>

            <!-- 文件预览区域 -->
            <div v-if="selectedFile" class="file-preview">
              <div class="file-info">
                <el-icon class="file-icon"><Folder /></el-icon>
                <div class="file-details">
                  <div class="file-name">{{ selectedFile.name }}</div>
                  <div class="file-size">{{ formatFileSize(selectedFile.size) }}</div>
                </div>
                <el-button type="default" icon="delete" circle @click="clearSelectedFile" size="small" class="clear-btn"></el-button>
              </div>
            </div>
          </div>

          <div class="upload-actions">
            <el-button
              type="primary"
              @click="uploadUpdatePackage"
              :disabled="!selectedFile || isUpgrading"
              size="large"
              class="upgrade-btn"
            >
              {{ isUpgrading ? t('upgrade.upgrading') : t('upgrade.startUpgrade') }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 升级状态显示区域 -->
      <div v-if="showStatusSection" class="status-section">
        <div class="status-card">
          <div class="status-display">
            <!-- 升级动画 -->
            <div v-if="isUpgrading" class="upgrade-animation">
              <div class="pulse-ring"></div>
              <div class="pulse-ring delay-1"></div>
              <div class="pulse-ring delay-2"></div>
              <el-icon class="upgrade-icon"><UploadFilled /></el-icon>
            </div>

            <!-- 升级完成动画 -->
            <div v-else-if="upgradeStatus.text === t('upgrade.upgradeComplete')" class="success-animation">
              <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
            </div>

            <!-- 状态文本 -->
            <div class="status-text">{{ upgradeStatus.text }}</div>
            <p v-if="upgradeStatus.message" class="status-message">{{ upgradeStatus.message }}</p>
          </div>

          <div class="status-actions" v-if="upgradeStatus.text === t('upgrade.upgradeComplete')">
            <el-button
              type="primary"
              @click="reloadPage"
              size="large"
              class="reload-btn"
            >
              {{ t('upgrade.reloadPage') }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
import { UploadFilled, Document, Upload, CircleCheckFilled } from '@element-plus/icons-vue';
import { uploadUpdatePackage as uploadUpdateApi, checkSystemHealth } from "@/api/upgrade.js";

// 文件相关
const selectedFile = ref(null);
const fileList = ref([]); // 添加文件列表响应式变量
const isUpgrading = ref(false);
const showStatusSection = ref(false); // 控制状态区域显示

// 升级状态
const upgradeStatus = reactive({
  text: t('upgrade.ready'),
  message: ''
});

// 连续成功响应计数器
const successCount = ref(0);
const requiredSuccessCount = 3; // 需要连续成功的次数

// 定时器
let statusCheckTimer = null;

// 处理文件选择
const handleFileChange = (file, uploadFileList) => {
  selectedFile.value = file.raw;
  fileList.value = uploadFileList; // 同步文件列表
};

// 处理文件移除
const handleFileRemove = () => {
  selectedFile.value = null;
  fileList.value = []; // 清空文件列表
};

// 清除选定的文件
const clearSelectedFile = () => {
  selectedFile.value = null;
  fileList.value = []; // 同时清空文件列表
  // 重置el-upload组件
  const uploadComponent = document.querySelector('.el-upload__input');
  if (uploadComponent) {
    uploadComponent.value = '';
  }
};

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let unitIndex = 0;
  let fileSize = size;
  while (fileSize >= 1024 && unitIndex < units.length - 1) {
    fileSize /= 1024;
    unitIndex++;
  }
  return `${fileSize.toFixed(2)} ${units[unitIndex]}`;
};

// 上传升级包
const uploadUpdatePackage = async () => {
  if (!selectedFile.value) {
    ElMessage.warning(t('upgrade.selectFileFirst'));
    return;
  }

  try {
    isUpgrading.value = true;
    showStatusSection.value = true; // 显示状态区域
    upgradeStatus.text = t('upgrade.uploading');
    upgradeStatus.message = '';

    const formData = new FormData();
    formData.append('file', selectedFile.value);

    const response = await uploadUpdateApi(formData);
    console.log(response);
  //  ElMessage.success('升级包上传成功，开始升级...');
    selectedFile.value = null;
    // 开始检查升级状态
    startStatusCheck();
  } catch (error) {
    isUpgrading.value = false;
    ElMessage.error(t('upgrade.uploadFailed') + ': ' + (error.response?.data || error.message));
  }
};

// 开始检查系统状态
const startStatusCheck = () => {
  // 每2秒检查一次
  statusCheckTimer = setInterval(checkStatus, 3000);
};

// 检查系统状态
const checkStatus = async () => {
  try {
    const response = await checkSystemHealth();
    if (response && response.healthy) {
      // 增加成功计数
      successCount.value++;
      // 检查是否达到所需的连续成功次数
      if (successCount.value >= requiredSuccessCount) {
        upgradeStatus.text = t('upgrade.upgradeComplete');
        upgradeStatus.message = t('upgrade.upgradeSuccess');
        stopStatusCheck();
        setTimeout(()=>{
          // 重新加载页面
          window.location.reload();
        },5000)
      } else {
        // 还未达到所需次数，继续检查
        upgradeStatus.text = t('upgrade.verifying');
        upgradeStatus.message = `${t('upgrade.confirmStability')} (${successCount.value}/${requiredSuccessCount})`;
      }
    } else {
      // 如果不是200状态码，重置成功计数
      successCount.value = 0;
      upgradeStatus.text = t('upgrade.upgrading');
    }
  } catch (error) {
    console.error('检查系统状态失败:', error);
    // 网络错误或请求失败，重置成功计数
    successCount.value = 0;
    upgradeStatus.text = t('upgrade.systemRestarting');
    upgradeStatus.message = t('upgrade.waitingRecovery');
  }
};


// 停止状态检查
const stopStatusCheck = () => {
  if (statusCheckTimer) {
    clearInterval(statusCheckTimer);
    statusCheckTimer = null;
  }
};

// 重新加载页面
const reloadPage = () => {
  window.location.reload();
};

onMounted(() => {
  // 清理可能存在的定时器
  stopStatusCheck();
});

// 组件销毁前清理定时器
onUnmounted(() => {
  stopStatusCheck();
});
</script>

<style scoped>
.system-upgrade {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 40px;
}

.upgrade-header {
  text-align: center;
  margin-top: 24px;
  margin-bottom: 24px;
  width: 100%;
  max-width: 800px;
  padding: 0 16px;
}

.upgrade-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.upgrade-subtitle {
  font-size: 15px;
  color: var(--text-muted);
  margin: 0;
  font-weight: 300;
}

.upgrade-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  max-width: 900px;
  padding: 0 16px;
  box-sizing: border-box;
}

.upload-section, .status-section {
  display: flex;
  justify-content: center;
  width: 100%;
}

.upload-card, .status-card {
  width: 100%;
  max-width: 700px;
  background: var(--bg-card);
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  overflow: hidden;
  transition: all 0.3s ease;
}

.upload-card:hover, .status-card:hover {
  box-shadow: var(--shadow-lg);
}

.upload-container {
  padding: 40px 30px 20px;
}

.single-upload {
  width: 100%;
  margin-bottom: 20px;
}

.single-upload :deep(.el-upload) {
  width: 100%;
}

.single-upload :deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  border: 2px dashed var(--border-base);
  border-radius: 12px;
  background: var(--bg-input);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
}

.single-upload :deep(.el-upload-dragger):hover {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}

.upload-icon {
  font-size: 48px;
  color: var(--color-primary);
  margin-bottom: 16px;
}

.upload-text {
  text-align: center;
}

.upload-title {
  font-size: 18px;
  color: var(--text-primary);
  margin-bottom: 8px;
  font-weight: 500;
}

.upload-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.upload-link {
  color: var(--color-primary);
  font-weight: 500;
  cursor: pointer;
}

.upload-tip {
  text-align: center;
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 12px;
}

.file-preview {
  margin-top: 20px;
  padding: 15px;
  border-radius: 8px;
  background-color: var(--bg-input);
  border: 1px solid var(--border-darker);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  color: var(--color-primary);
  font-size: 20px;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.file-size {
  font-size: 12px;
  color: var(--text-placeholder);
}

.clear-btn {
  color: var(--text-placeholder);
  border-color: var(--border-base);
  background: var(--bg-card);
}

.clear-btn:hover {
  color: var(--danger-color);
  border-color: var(--danger-color);
  background: var(--danger-bg);
}

.upload-actions {
  padding: 20px 30px 30px;
  text-align: center;
  border-top: 1px solid var(--border-light);
}

.upgrade-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.5px;
  border-radius: 8px;
  background: var(--color-primary-gradient);
  border: none;
}

.upgrade-btn:hover {
  background: var(--color-primary-interactive);
}

.status-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 30px;
  text-align: center;
}

.status-text {
  font-size: 22px;
  font-weight: 600;
  margin: 20px 0 10px;
  color: var(--text-primary);
}

.status-message {
  margin: 10px 0;
  color: var(--text-secondary);
  font-size: 16px;
  line-height: 1.5;
}

/* 升级动画 */
.upgrade-animation {
  position: relative;
  width: 120px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 20px auto;
}

.pulse-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 4px solid var(--color-primary);
  border-radius: 50%;
  animation: pulsing 2s ease-out infinite;
}

.pulse-ring.delay-1 {
  animation-delay: 0.5s;
}

.pulse-ring.delay-2 {
  animation-delay: 1s;
}

.upgrade-icon {
  font-size: 40px;
  color: var(--color-primary);
  z-index: 1;
}

@keyframes pulsing {
  0% {
    transform: scale(0.1);
    opacity: 0.8;
  }
  50% {
    opacity: 0.4;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

/* 成功动画 */
.success-animation {
  margin: 20px auto;
}

.success-icon {
  font-size: 80px;
  color: var(--color-primary);
  animation: scaleIn 0.5s ease-out;
}

@keyframes scaleIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.status-actions {
  padding: 0 30px 30px;
  text-align: center;
}

.reload-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.5px;
  border-radius: 8px;
  background: var(--color-primary-gradient);
  border: none;
}

.reload-btn:hover {
  background: var(--color-primary-interactive);
}

@media (max-width: 768px) {
  .upgrade-header {
    margin-top: 16px;
    margin-bottom: 16px;
  }

  .upgrade-title {
    font-size: 20px;
  }

  .upgrade-subtitle {
    font-size: 14px;
  }

  .upload-container {
    padding: 20px 16px 12px;
  }

  .single-upload :deep(.el-upload-dragger) {
    height: 140px;
  }

  .upload-icon {
    font-size: 36px;
    margin-bottom: 10px;
  }

  .upload-actions, .status-actions {
    padding: 12px 16px 16px;
  }

  .status-display {
    padding: 30px 16px;
  }
}
</style>
