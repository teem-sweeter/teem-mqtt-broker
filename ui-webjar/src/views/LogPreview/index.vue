<template>
  <div class="log-preview-container">
    <div class="log-header">
      <h2>{{ t('logSearch.title') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="loadLogs">{{ t('logSearch.refresh') }}</el-button>

      </div>
    </div>

    <div class="log-controls">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item :label="t('logSearch.linesToShow')">
            <el-select v-model="logForm.lines" :placeholder="t('logSearch.selectLines')" @change="loadLogs" style="width: 100%">
              <el-option :label="t('logSearch.lines50')" :value="50"></el-option>
              <el-option :label="t('logSearch.lines100')" :value="100"></el-option>
              <el-option :label="t('logSearch.lines200')" :value="200"></el-option>
              <el-option :label="t('logSearch.lines500')" :value="500"></el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="6">
          <el-form-item :label="t('logSearch.keywordFilter')">
            <el-input
              v-model="logForm.keyword"
              :placeholder="t('logSearch.enterKeyword')"
              @keyup.enter="loadLogs"
              clearable
              style="width: 100%"
            ></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="6">
          <el-form-item :label="t('logSearch.logLevel')">
            <el-select v-model="logForm.level" :placeholder="t('logSearch.selectLevel')" @change="loadLogs" style="width: 100%">
              <el-option :label="t('logSearch.all')" value="ALL"></el-option>
              <el-option label="DEBUG" value="DEBUG"></el-option>
              <el-option label="INFO" value="INFO"></el-option>
              <el-option label="WARN" value="WARN"></el-option>
              <el-option label="ERROR" value="ERROR"></el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="6">
          <el-form-item>
            <el-button type="primary" @click="loadLogs">{{ t('logSearch.search') }}</el-button>
            <el-button type="danger" @click="downloadLog">{{ t('logSearch.downloadLog') }}</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <div class="log-content">
      <el-scrollbar ref="scrollbarRef" class="log-scrollbar">
        <div class="log-lines">
          <div
            v-for="(line, index) in filteredLogs"
            :key="index"
            class="log-line"
            :class="getLogLevelClass(line)"
          >
            <pre>{{ line }}</pre>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
import { getRecentLogs, downloadLogFile } from '@/api/log.js'

// 日志表单
const logForm = ref({
  lines: 100,
  keyword: '',
  level: 'ALL'
})

// 日志数据
const logs = ref([])
const filteredLogs = ref([])
const scrollbarRef = ref(null)

// 获取日志级别class
const getLogLevelClass = (line) => {
  if (line.includes('ERROR') || line.includes('error')) {
    return 'log-error'
  } else if (line.includes('WARN') || line.includes('warn')) {
    return 'log-warn'
  } else if (line.includes('INFO') || line.includes('info')) {
    return 'log-info'
  } else if (line.includes('DEBUG') || line.includes('debug')) {
    return 'log-debug'
  }
  return ''
}

// 加载日志
const loadLogs = async () => {
  try {
    const response = await getRecentLogs(logForm.value.lines, logForm.value.level)
    logs.value = response || []
    filterLogs()
    scrollToBottom()
  } catch (error) {
    ElMessage.error(t('logSearch.loadFailed') + ': ' + error.message)
  }
}

// 过滤日志
const filterLogs = () => {
  if (!logForm.value.keyword) {
    filteredLogs.value = [...logs.value]
  } else {
    const keyword = logForm.value.keyword.toLowerCase()
    filteredLogs.value = logs.value.filter(line =>
      line.toLowerCase().includes(keyword)
    )
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (scrollbarRef.value) {
      const wrap = scrollbarRef.value.wrapRef
      if (wrap) {
        wrap.scrollTop = wrap.scrollHeight
      }
    }
  })
}

// 下载日志文件
const downloadLog = async () => {
  try {
    const response = await downloadLogFile(logForm.value.level)

    // 从响应头中获取文件名
    let filename = `log-${logForm.value.level.toLowerCase()}.txt`

    // 尝试从响应头中获取 Content-Disposition 字段
    const contentDisposition = response.headers['content-disposition'] || response.headers['Content-Disposition']
    if (contentDisposition) {
      // 匹配 filename 或 filename*=UTF-8'' 格式
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (filenameMatch && filenameMatch[1]) {
        // 去掉引号并解码
        filename = filenameMatch[1].replace(/['"]/g, '')
        // 如果是 UTF-8 编码格式，则解码
        if (filename.includes('UTF-8')) {
          const utf8Match = filename.match(/UTF-8''(.+)/)
          if (utf8Match && utf8Match[1]) {
            filename = decodeURIComponent(utf8Match[1])
          }
        }
      }
    }

    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()

    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success(t('logSearch.downloadSuccess'))
  } catch (error) {
    ElMessage.error(t('logSearch.downloadFailed') + ': ' + error.message)
  }
}

// 组件挂载时加载日志
onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.log-preview-container {
  padding: 20px;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  background-color: var(--bg-card);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border-darker);
}

.log-header h2 {
  margin: 0;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.log-controls {
  flex-shrink: 0;
  /*margin-bottom: 20px;*/
  padding: 20px;
  /*
  background-color: #f5f7fa;
  */
  border-radius: 4px;
}

.log-content {
  flex: 1;
  min-height: 0;
  /*border: 1px solid #ebeef5;*/
  border-radius: 4px;
  overflow: hidden;
  background-color: #333333;
}

.log-scrollbar {
  height: 100%;
}

.log-lines {
  padding: 5px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.4;
  color: #ffffff;
}

.log-line {
/*  padding: 2px 0;*/
 /* border-bottom: 1px solid #eee;*/
}

.log-error {
  color: #f56c6c;
 /* background-color: #fef0f0;*/
}

.log-warn {
  color: #e6a23c;
/*  background-color: #fdf6ec;*/
}

.log-info {
  color: #909399;
}

.log-debug {
  color: #409eff;
/*  background-color: #ecf5ff;*/
}
</style>
