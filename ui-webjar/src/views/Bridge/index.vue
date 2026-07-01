<template>
  <div class="bridge-page">
    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-cards">
        <div class="stat-card stat-card-blue">
          <div class="stat-icon"><el-icon><Connection /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.total || 0 }}</div>
            <div class="stat-label">{{ t('bridge.totalLinks') }}</div>
          </div>
        </div>
        <div class="stat-card stat-card-green">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.connected || 0 }}</div>
            <div class="stat-label">{{ t('bridge.connected') }}</div>
          </div>
        </div>
        <div class="stat-card stat-card-purple">
          <div class="stat-icon"><el-icon><Loading /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.reconnecting || 0 }}</div>
            <div class="stat-label">{{ t('bridge.reconnecting') }}</div>
          </div>
        </div>
        <div class="stat-card stat-card-orange">
          <div class="stat-icon"><el-icon><CircleClose /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.disconnected || 0 }}</div>
            <div class="stat-label">{{ t('bridge.disconnected') }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-section">
      <div class="main-card">
        <!-- 操作栏 -->
        <div class="toolbar">
          <div class="toolbar-title">
            <el-icon><Share /></el-icon>
            <span>{{ t('bridge.linkManagement') }}</span>
          </div>
          <div class="toolbar-actions">
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              {{ t('bridge.createBridge') }}
            </el-button>
            <el-button @click="fetchBridges" :loading="loading">
              <el-icon><Refresh /></el-icon>
              {{ t('common.refresh') }}
            </el-button>
          </div>
        </div>

        <!-- 数据表格 -->
        <el-table :data="bridges" v-loading="loading" row-key="id" stripe class="bridge-table">
          <el-table-column prop="name" :label="t('bridge.linkAlias')" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="name-cell">
                <el-icon class="name-icon"><Share /></el-icon>
                <span>{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="remoteUrl" :label="t('bridge.remoteUrl')" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="url-text">{{ row.remoteUrl }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="status" :label="t('bridge.connectionStatus')" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" effect="dark" size="small">
                <span class="status-dot" :class="'dot-' + row.status"></span>
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="enabled" :label="t('common.status')" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.enabled"
                @change="(val) => handleToggle(row, val)"
                :loading="row._toggling"
                inline-prompt
                :active-text="t('bridge.on')"
                :inactive-text="t('bridge.off')"
              />
            </template>
          </el-table-column>

          <el-table-column prop="clientId" label="Client ID" width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="mono-text">{{ row.clientId }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="routeRuleCount" :label="t('bridge.routeRules')" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="primary" size="small" effect="plain">{{ row.routeRuleCount || 0 }} {{ t('bridge.ruleUnit') }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="sentCount" :label="t('bridge.sent')" width="90" align="right">
            <template #default="{ row }">
              <span class="count-text">{{ formatCount(row.sentCount) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="receivedCount" :label="t('bridge.received')" width="90" align="right">
            <template #default="{ row }">
              <span class="count-text">{{ formatCount(row.receivedCount) }}</span>
            </template>
          </el-table-column>

          <el-table-column :label="t('common.operation')" width="240" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                {{ t('common.edit') }}
              </el-button>
              <el-button link type="success" size="small" @click="openRouteRules(row)">
                <el-icon><Switch /></el-icon>
                {{ t('bridge.routeRules') }}
              </el-button>
              <el-button link type="info" size="small" @click="handleViewStats(row)">
                <el-icon><DataAnalysis /></el-icon>
                {{ t('bridge.statistics') }}
              </el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)" :disabled="row.enabled">
                <el-icon><Delete /></el-icon>
                {{ t('common.delete') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && bridges.length === 0" :description="t('bridge.noLinks')" :image-size="100">
          <el-button type="primary" @click="handleCreate">{{ t('bridge.createFirstLink') }}</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? t('bridge.editLink') : t('bridge.newLink')"
      width="720px"
      class="bridge-dialog"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        label-position="right"
        class="bridge-form"
      >
        <!-- 基础连接 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Connection /></el-icon>
            <span>{{ t('bridge.basicConnection') }}</span>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item :label="t('bridge.linkAlias')" prop="name">
                <el-input v-model="form.name" :placeholder="t('bridge.linkAliasPlaceholder')" maxlength="50" show-word-limit />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Client ID" prop="clientId">
                <el-input v-model="form.clientId" :placeholder="t('bridge.clientIdPlaceholder')">
                  <template #append>
                    <el-button @click="generateClientId"><el-icon><Refresh /></el-icon></el-button>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item :label="t('bridge.remoteUrl')" prop="remoteUrl">
            <el-input v-model="form.remoteUrl" :placeholder="t('bridge.remoteUrlPlaceholder')">
              <template #prepend>
                <el-select v-model="urlProtocol" style="width: 90px;">
                  <el-option value="tcp://" label="tcp://" />
                  <el-option value="ssl://" label="ssl://" />
                  <el-option value="ws://" label="ws://" />
                  <el-option value="wss://" label="wss://" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item :label="t('bridge.keepAlive')" prop="keepAlive">
                <el-input-number v-model="form.keepAlive" :min="5" :max="300" :step="5" style="width: 100%;" />
                <span class="form-hint">{{ t('bridge.secondsUnit') }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="t('bridge.connectionTimeout')" prop="connectionTimeout">
                <el-input-number v-model="form.connectionTimeout" :min="3" :max="120" :step="1" style="width: 100%;" />
                <span class="form-hint">{{ t('bridge.secondsUnit') }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="QoS" prop="defaultQos">
                <el-select v-model="form.defaultQos" style="width: 100%;">
                  <el-option :value="0" :label="t('bridge.qos0')" />
                  <el-option :value="1" :label="t('bridge.qos1')" />
                  <el-option :value="2" :label="t('bridge.qos2')" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 安全认证 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Lock /></el-icon>
            <span>{{ t('bridge.security') }}</span>
          </div>

          <el-form-item :label="t('bridge.authType')">
            <el-radio-group v-model="form.authType" @change="handleAuthTypeChange">
              <el-radio value="anonymous">{{ t('bridge.anonymous') }}</el-radio>
              <el-radio value="password">{{ t('bridge.passwordAuth') }}</el-radio>
              <el-radio value="ssl">{{ t('bridge.sslCert') }}</el-radio>
            </el-radio-group>
          </el-form-item>

          <template v-if="form.authType === 'password'">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item :label="t('bridge.username')" prop="username">
                  <el-input v-model="form.username" :placeholder="t('bridge.mqttUsername')" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="t('bridge.password')" prop="password">
                  <el-input v-model="form.password" type="password" show-password :placeholder="t('bridge.mqttPassword')" />
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <template v-if="form.authType === 'ssl'">
            <el-alert type="info" :closable="false" show-icon class="ssl-hint">
              <template #title>{{ t('bridge.sslHint') }}</template>
            </el-alert>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item :label="t('bridge.caCert')">
                  <el-upload
                    :auto-upload="false"
                    :limit="1"
                    accept=".pem,.crt,.cer"
                    :on-change="(f) => handleCertFile(f, 'caCert')"
                    :on-remove="() => { form.caCert = '' }"
                    class="cert-upload"
                  >
                    <el-button size="small"><el-icon><Upload /></el-icon> {{ t('bridge.selectFile') }}</el-button>
                  </el-upload>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="t('bridge.clientCert')">
                  <el-upload
                    :auto-upload="false"
                    :limit="1"
                    accept=".pem,.crt,.cer"
                    :on-change="(f) => handleCertFile(f, 'clientCert')"
                    :on-remove="() => { form.clientCert = '' }"
                    class="cert-upload"
                  >
                    <el-button size="small"><el-icon><Upload /></el-icon> {{ t('bridge.selectFile') }}</el-button>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item :label="t('bridge.clientKey')">
              <el-upload
                :auto-upload="false"
                :limit="1"
                accept=".pem,.key"
                :on-change="(f) => handleCertFile(f, 'clientKey')"
                :on-remove="() => { form.clientKey = '' }"
                class="cert-upload"
              >
                <el-button size="small"><el-icon><Upload /></el-icon> {{ t('bridge.selectFile') }}</el-button>
              </el-upload>
            </el-form-item>
          </template>
        </div>

        <!-- 备注 -->
        <div class="form-section">
          <el-form-item :label="t('bridge.remark')">
            <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="t('bridge.remarkPlaceholder')" maxlength="200" show-word-limit />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleTestConnection" :loading="testing" :disabled="!form.remoteUrl">
            <el-icon><Link /></el-icon>
            {{ t('bridge.testConnection') }}
          </el-button>
          <div class="footer-right">
            <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              {{ isEdit ? t('bridge.saveChanges') : t('bridge.createLink') }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 路由规则弹窗 -->
    <el-dialog
      v-model="routeDialogVisible"
      :title="t('bridge.routeRuleConfig')"
      width="900px"
      class="route-dialog"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <!-- 链路信息 -->
      <div class="route-bridge-info" v-if="routeBridge">
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">{{ t('bridge.linkName') }}</span>
            <span class="info-value">{{ routeBridge.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">{{ t('bridge.connectionStatus') }}</span>
            <el-tag :type="statusTagType(routeBridge.status)" effect="dark" size="small">
              {{ statusLabel(routeBridge.status) }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">{{ t('bridge.remoteUrl') }}</span>
            <span class="info-value mono-text">{{ routeBridge.remoteUrl }}</span>
          </div>
        </div>
      </div>

      <!-- 防环警告 -->
      <el-alert
        v-if="hasRouteLoopWarning"
        type="warning"
        :closable="false"
        show-icon
        class="loop-warning"
      >
        <template #title>{{ t('bridge.loopWarning') }}</template>
      </el-alert>

      <!-- 工具栏 -->
      <div class="route-toolbar">
        <span class="route-count">{{ t('bridge.ruleCount', { count: routeRules.length }) }}</span>
        <el-button type="primary" size="small" @click="handleAddRouteRule">
          <el-icon><Plus /></el-icon>
          {{ t('bridge.addRule') }}
        </el-button>
      </div>

      <!-- 规则表格 -->
      <div class="route-table-wrapper" v-loading="routeLoading">
        <div class="route-table" v-if="routeRules.length > 0">
          <div class="route-table-header">
            <span class="rt-col-idx">#</span>
            <span class="rt-col-dir">{{ t('bridge.direction') }}</span>
            <span class="rt-col-src">{{ t('bridge.sourceTopic') }}</span>
            <span class="rt-col-dst">{{ t('bridge.destTopic') }}</span>
            <span class="rt-col-qos">QoS</span>
            <span class="rt-col-retain">{{ t('bridge.retain') }}</span>
            <span class="rt-col-act">{{ t('common.operation') }}</span>
          </div>
          <div v-for="(rule, index) in routeRules" :key="index" class="route-table-row">
            <span class="rt-col-idx">{{ index + 1 }}</span>
            <span class="rt-col-dir">
              <el-select v-model="rule.direction" size="small">
                <el-option value="outbound" :label="t('bridge.outbound')" />
                <el-option value="inbound" :label="t('bridge.inbound')" />
              </el-select>
            </span>
            <span class="rt-col-src">
              <el-input v-model="rule.sourceTopic" size="small" :placeholder="t('bridge.sourcePlaceholder')" />
            </span>
            <span class="rt-col-dst">
              <el-input v-model="rule.destTopic" size="small" :placeholder="t('bridge.destPlaceholder')" />
            </span>
            <span class="rt-col-qos">
              <el-select v-model="rule.qos" size="small">
                <el-option :value="-1" :label="t('bridge.passthrough')" />
                <el-option :value="0" label="QoS 0" />
                <el-option :value="1" label="QoS 1" />
                <el-option :value="2" label="QoS 2" />
              </el-select>
            </span>
            <span class="rt-col-retain">
              <el-select v-model="rule.retainHandling" size="small">
                <el-option value="keep" :label="t('bridge.keep')" />
                <el-option value="strip" :label="t('bridge.strip')" />
                <el-option value="ifRetained" :label="t('bridge.ifRetained')" />
              </el-select>
            </span>
            <span class="rt-col-act">
              <el-button link type="danger" size="small" @click="routeRules.splice(index, 1)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </span>
          </div>
        </div>
        <el-empty v-if="!routeLoading && routeRules.length === 0" :description="t('bridge.noRules')" :image-size="60">
          <el-button type="primary" size="small" @click="handleAddRouteRule">{{ t('bridge.addRule') }}</el-button>
        </el-empty>
      </div>

      <!-- 帮助 -->
      <el-collapse class="route-help">
        <el-collapse-item>
          <template #title>
            <div class="help-title"><el-icon><QuestionFilled /></el-icon><span>{{ t('bridge.ruleHelp') }}</span></div>
          </template>
          <div class="help-grid">
            <div class="help-item">
              <strong>{{ t('bridge.helpDirection') }}</strong>
            </div>
            <div class="help-item">
              <strong>{{ t('bridge.helpWildcard') }}</strong>
            </div>
            <div class="help-item">
              <strong>{{ t('bridge.helpVariable') }}</strong>
            </div>
            <div class="help-item">
              <strong>{{ t('bridge.helpQos') }}</strong>
            </div>
            <div class="help-item">
              <strong>{{ t('bridge.helpRetain') }}</strong>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>

      <template #footer>
        <div class="dialog-footer">
          <div></div>
          <div class="footer-right">
            <el-button @click="routeDialogVisible = false">{{ t('common.cancel') }}</el-button>
            <el-button type="primary" @click="handleSaveRouteRules" :loading="routeSaving">
              {{ t('bridge.saveRules') }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 统计弹窗 -->
    <el-dialog
      v-model="statsDialogVisible"
      :title="t('bridge.realtimeStats')"
      width="560px"
      class="stats-dialog"
      destroy-on-close
    >
      <div class="stats-detail" v-if="currentStats">
        <div class="stats-info-row">
          <span class="stats-label">{{ t('bridge.linkName') }}</span>
          <span class="stats-value">{{ currentStats.name }}</span>
        </div>
        <div class="stats-info-row">
          <span class="stats-label">{{ t('bridge.connectionStatus') }}</span>
          <el-tag :type="statusTagType(currentStats.status)" effect="dark" size="small">
            {{ statusLabel(currentStats.status) }}
          </el-tag>
        </div>
        <el-divider />
        <div class="stats-metrics">
          <div class="metric-item">
            <div class="metric-icon outbound"><el-icon><Top /></el-icon></div>
            <div class="metric-info">
              <div class="metric-value">{{ formatCount(currentStats.sentCount) }}</div>
              <div class="metric-label">{{ t('bridge.outboundMessages') }}</div>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-icon inbound"><el-icon><Bottom /></el-icon></div>
            <div class="metric-info">
              <div class="metric-value">{{ formatCount(currentStats.receivedCount) }}</div>
              <div class="metric-label">{{ t('bridge.inboundMessages') }}</div>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-icon bytes"><el-icon><Coin /></el-icon></div>
            <div class="metric-info">
              <div class="metric-value">{{ formatBytes(currentStats.sentBytes) }}</div>
              <div class="metric-label">{{ t('bridge.sentBytes') }}</div>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-icon bytes"><el-icon><Coin /></el-icon></div>
            <div class="metric-info">
              <div class="metric-value">{{ formatBytes(currentStats.receivedBytes) }}</div>
              <div class="metric-label">{{ t('bridge.receivedBytes') }}</div>
            </div>
          </div>
        </div>
        <div class="stats-info-row" v-if="currentStats.lastConnectedTime">
          <span class="stats-label">{{ t('bridge.lastConnected') }}</span>
          <span class="stats-value">{{ formatTime(currentStats.lastConnectedTime) }}</span>
        </div>
        <div class="stats-info-row" v-if="currentStats.lastError">
          <span class="stats-label">{{ t('bridge.lastError') }}</span>
          <span class="stats-value error-text">{{ currentStats.lastError }}</span>
        </div>
      </div>
      <el-empty v-else :description="t('bridge.noStats')" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  Connection, CircleCheck, CircleClose, Loading, Share, Plus, Refresh,
  Edit, Delete, DataAnalysis, Lock, Upload, Switch, Link, Top, Bottom, Coin,
  QuestionFilled
} from '@element-plus/icons-vue'
import {
  getBridges, createBridge, updateBridge, deleteBridge,
  toggleBridge, getBridgeStats, testBridgeConnection,
  getRouteRules, saveRouteRules
} from '@/api/bridge'

const { t } = useI18n()

// ===== 桥接列表状态 =====
const bridges = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const statsDialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const testing = ref(false)
const formRef = ref(null)
const currentStats = ref(null)
const urlProtocol = ref('tcp://')

// ===== 路由规则弹窗状态 =====
const routeDialogVisible = ref(false)
const routeBridge = ref(null)
const routeRules = ref([])
const routeLoading = ref(false)
const routeSaving = ref(false)

// 统计
const stats = computed(() => {
  const list = bridges.value
  return {
    total: list.length,
    connected: list.filter(b => b.status === 'CONNECTED').length,
    reconnecting: list.filter(b => b.status === 'RECONNECTING').length,
    disconnected: list.filter(b => b.status === 'DISCONNECTED' || b.status === 'FAILED').length
  }
})

// 表单
const defaultForm = () => ({
  name: '',
  remoteUrl: '',
  clientId: '',
  keepAlive: 60,
  connectionTimeout: 10,
  defaultQos: 0,
  authType: 'anonymous',
  username: '',
  password: '',
  caCert: '',
  clientCert: '',
  clientKey: '',
  remark: ''
})

const form = reactive(defaultForm())

// 校验规则
const rules = computed(() => ({
  name: [
    { required: true, message: t('bridge.nameRequired'), trigger: 'blur' },
    { min: 2, max: 50, message: t('bridge.nameLength'), trigger: 'blur' }
  ],
  remoteUrl: [
    { required: true, message: t('bridge.remoteUrlRequired'), trigger: 'blur' }
  ],
  keepAlive: [
    { required: true, message: t('bridge.keepAliveRequired'), trigger: 'change' }
  ]
}))

// ===== 路由规则弹窗 =====

const hasRouteLoopWarning = computed(() => {
  const r = routeRules.value
  if (r.length < 2) return false
  const outbounds = r.filter(rule => rule.direction === 'outbound').map(rule => rule.sourceTopic).filter(Boolean)
  const inbounds = r.filter(rule => rule.direction === 'inbound').map(rule => rule.sourceTopic).filter(Boolean)
  if (outbounds.length === 0 || inbounds.length === 0) return false
  for (const ob of outbounds) {
    for (const ib of inbounds) {
      if (topicsOverlap(ob, ib)) return true
    }
  }
  return false
})

function topicsOverlap(a, b) {
  if (!a || !b) return false
  if (a === b) return true
  const normalize = (t) => t.replace(/\+/g, '[^/]+').replace(/\/#$/, '(\/.*)?')
  try {
    const regexA = new RegExp('^' + normalize(a) + '$')
    const regexB = new RegExp('^' + normalize(b) + '$')
    return regexA.test(b) || regexB.test(a)
  } catch {
    return false
  }
}

async function openRouteRules(row) {
  routeBridge.value = row
  routeDialogVisible.value = true
  routeLoading.value = true
  try {
    const data = await getRouteRules(row.id)
    routeRules.value = (data || []).map(r => ({ ...r }))
  } catch {
    ElMessage.error(t('bridge.fetchRouteRulesFailed'))
    routeRules.value = []
  } finally {
    routeLoading.value = false
  }
}

function handleAddRouteRule() {
  routeRules.value.push({
    direction: 'outbound',
    sourceTopic: '',
    destTopic: '',
    qos: -1,
    retainHandling: 'keep'
  })
}

async function handleSaveRouteRules() {
  const emptyRules = routeRules.value.filter(r => !r.sourceTopic)
  if (emptyRules.length > 0) {
    ElMessage.warning(t('bridge.fillSourceTopic'))
    return
  }
  routeSaving.value = true
  try {
    const payload = routeRules.value.map(r => ({
      direction: r.direction,
      sourceTopic: r.sourceTopic,
      destTopic: r.destTopic || '',
      qos: r.qos,
      retainHandling: r.retainHandling
    }))
    await saveRouteRules(routeBridge.value.id, payload)
    ElMessage.success(t('bridge.saveRouteRulesSuccess'))
    routeDialogVisible.value = false
    fetchBridges()
  } catch {
    ElMessage.error(t('bridge.saveRouteRulesFailed'))
  } finally {
    routeSaving.value = false
  }
}

// ===== 桥接 CRUD =====

function generateClientId() {
  form.clientId = 'bridge-' + Math.random().toString(16).slice(2, 10)
}

function handleAuthTypeChange(val) {
  if (val !== 'password') {
    form.username = ''
    form.password = ''
  }
  if (val !== 'ssl') {
    form.caCert = ''
    form.clientCert = ''
    form.clientKey = ''
  }
}

function handleCertFile(file, field) {
  if (file?.raw) {
    const reader = new FileReader()
    reader.onload = (e) => {
      form[field] = e.target.result
    }
    reader.readAsText(file.raw)
  }
}

function handleCreate() {
  isEdit.value = false
  editingId.value = null
  Object.assign(form, defaultForm())
  urlProtocol.value = 'tcp://'
  generateClientId()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editingId.value = row.id
  const url = row.remoteUrl || ''
  const proto = url.match(/^(tcp|ssl|ws|wss):\/\//)
  urlProtocol.value = proto ? proto[0] : 'tcp://'
  const urlBody = proto ? url.slice(proto[0].length) : url

  Object.assign(form, {
    name: row.name || '',
    remoteUrl: urlBody,
    clientId: row.clientId || '',
    keepAlive: row.keepAlive || 60,
    connectionTimeout: row.connectionTimeout || 10,
    defaultQos: row.defaultQos ?? 0,
    authType: row.authType || 'anonymous',
    username: row.username || '',
    password: '',
    caCert: '',
    clientCert: '',
    clientKey: '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const cleanUrl = form.remoteUrl.replace(/^(tcp|ssl|ws|wss):\/\//, '')
    const payload = {
      ...form,
      remoteUrl: urlProtocol.value + cleanUrl
    }

    if (isEdit.value) {
      await updateBridge(editingId.value, payload)
      ElMessage.success(t('bridge.updateSuccess'))
    } else {
      await createBridge(payload)
      ElMessage.success(t('bridge.createSuccess'))
    }
    dialogVisible.value = false
    fetchBridges()
  } catch {
    ElMessage.error(isEdit.value ? t('bridge.updateFailed') : t('bridge.createFailed'))
  } finally {
    submitting.value = false
  }
}

async function handleTestConnection() {
  testing.value = true
  try {
    const cleanUrl = form.remoteUrl.replace(/^(tcp|ssl|ws|wss):\/\//, '')
    const payload = {
      remoteUrl: urlProtocol.value + cleanUrl,
      clientId: form.clientId,
      authType: form.authType,
      username: form.username,
      password: form.password,
      caCert: form.caCert,
      clientCert: form.clientCert,
      clientKey: form.clientKey,
      connectionTimeout: form.connectionTimeout
    }
    const result = await testBridgeConnection(payload)
    if (result.success) {
      ElMessage.success(t('bridge.connectSuccess', { latency: result.latency || 0 }))
    } else {
      ElMessage.error(t('bridge.connectFailed', { message: result.message || t('bridge.unknown') }))
    }
  } catch (error) {
    ElMessage.error(t('bridge.testConnectFailed', { message: error.message || t('bridge.networkError') }))
  } finally {
    testing.value = false
  }
}

async function handleToggle(row, enable) {
  row._toggling = true
  try {
    await toggleBridge(row.id, enable)
    ElMessage.success(enable ? t('bridge.enableSuccess') : t('bridge.disableSuccess'))
    fetchBridges()
  } catch {
    row.enabled = !enable
    ElMessage.error(t('bridge.operationFailed'))
  } finally {
    row._toggling = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      t('bridge.deleteConfirm', { name: row.name }),
      t('bridge.deleteConfirmTitle'),
      { type: 'warning', confirmButtonText: t('bridge.confirmDelete'), cancelButtonText: t('common.cancel') }
    )
    await deleteBridge(row.id)
    ElMessage.success(t('bridge.deleteSuccess'))
    fetchBridges()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('bridge.deleteFailed'))
    }
  }
}

async function handleViewStats(row) {
  try {
    currentStats.value = await getBridgeStats(row.id)
    statsDialogVisible.value = true
  } catch {
    ElMessage.error(t('bridge.fetchStatsFailed'))
  }
}

async function fetchBridges() {
  loading.value = true
  try {
    bridges.value = await getBridges()
  } catch {
    ElMessage.error(t('bridge.fetchBridgesFailed'))
  } finally {
    loading.value = false
  }
}

// ===== 格式化 =====

function statusTagType(status) {
  const map = { CONNECTED: 'success', RECONNECTING: 'warning', DISCONNECTED: 'info', FAILED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { CONNECTED: t('bridge.connected'), RECONNECTING: t('bridge.reconnecting'), DISCONNECTED: t('bridge.disconnected'), FAILED: t('bridge.failed') }
  return map[status] || status || t('bridge.unknown')
}

function formatCount(val) {
  if (val == null) return '0'
  if (val >= 1000000) return (val / 1000000).toFixed(1) + 'M'
  if (val >= 1000) return (val / 1000).toFixed(1) + 'K'
  return String(val)
}

function formatBytes(val) {
  if (val == null) return '0 B'
  if (val >= 1073741824) return (val / 1073741824).toFixed(2) + ' GB'
  if (val >= 1048576) return (val / 1048576).toFixed(2) + ' MB'
  if (val >= 1024) return (val / 1024).toFixed(2) + ' KB'
  return val + ' B'
}

function formatTime(ts) {
  if (!ts) return ''
  return new Date(ts).toLocaleString()
}

onMounted(() => {
  fetchBridges()
})
</script>

<style scoped>
.bridge-page {
  padding: 16px;
  background: var(--bg-secondary);
}

/* 统计卡片区域 */
.stats-section {
  margin-bottom: 16px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  min-height: 106px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.stat-card-blue {
  border-left: 4px solid #409eff;
}

.stat-card-green {
  border-left: 4px solid #67c23a;
}

.stat-card-purple {
  border-left: 4px solid #a855f7;
}

.stat-card-orange {
  border-left: 4px solid #f97316;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-card-blue .stat-icon {
  background: linear-gradient(135deg, #409eff20, #409eff10);
  color: #409eff;
}

.stat-card-green .stat-icon {
  background: linear-gradient(135deg, #67c23a20, #67c23a10);
  color: #67c23a;
}

.stat-card-purple .stat-icon {
  background: linear-gradient(135deg, #a855f720, #a855f710);
  color: #a855f7;
}

.stat-card-orange .stat-icon {
  background: linear-gradient(135deg, #f9731620, #f9731610);
  color: #f97316;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-placeholder);
  margin-top: 4px;
}

/* 主内容区 */
.main-section { margin-bottom: 20px; }
.main-card {
  background: var(--bg-card); border-radius: 12px; padding: 20px;
  box-shadow: var(--shadow-sm); overflow: hidden;
}

/* 工具栏 */
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 16px; font-weight: 600; color: var(--text-primary);
}
.toolbar-title .el-icon { color: #409eff; }
.toolbar-actions { display: flex; gap: 8px; }

/* 表格 */
.bridge-table { border-radius: 8px; overflow: hidden; }
.name-cell { display: flex; align-items: center; gap: 8px; font-weight: 500; }
.name-icon { color: #409eff; }
.url-text { font-family: 'Courier New', Consolas, monospace; font-size: 12px; color: var(--text-secondary); }
.mono-text { font-family: 'Courier New', Consolas, monospace; font-size: 12px; }
.count-text { font-weight: 500; color: var(--text-primary); }
.status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 4px; }
.dot-CONNECTED { background: #67c23a; }
.dot-RECONNECTING { background: #e6a23c; animation: blink 1s infinite; }
.dot-DISCONNECTED { background: #909399; }
.dot-FAILED { background: #f56c6c; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }

/* 弹窗通用 */
.bridge-dialog :deep(.el-dialog__header),
.stats-dialog :deep(.el-dialog__header),
.route-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff; padding: 16px 20px; margin: 0;
}
.bridge-dialog :deep(.el-dialog__title),
.stats-dialog :deep(.el-dialog__title),
.route-dialog :deep(.el-dialog__title) { color: #fff; }
.bridge-dialog :deep(.el-dialog__close),
.stats-dialog :deep(.el-dialog__close),
.route-dialog :deep(.el-dialog__close) { color: #fff; }
.bridge-dialog :deep(.el-dialog__body) { padding: 20px 24px; max-height: 65vh; overflow-y: auto; }

/* 表单 */
.form-section { margin-bottom: 24px; padding-bottom: 20px; border-bottom: 1px solid var(--border-light); }
.form-section:last-child { margin-bottom: 0; padding-bottom: 0; border-bottom: none; }
.section-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 16px;
}
.section-title .el-icon { color: #409eff; }
.form-hint { font-size: 12px; color: var(--text-placeholder); margin-left: 8px; }
.ssl-hint { margin-bottom: 16px; }
.cert-upload { width: 100%; }
.cert-upload :deep(.el-upload-list) { margin-top: 4px; }
.dialog-footer { display: flex; justify-content: space-between; align-items: center; }
.footer-right { display: flex; gap: 8px; }

/* 路由规则弹窗 */
.route-dialog :deep(.el-dialog__body) { padding: 16px 20px; }

.route-bridge-info {
  background: var(--bg-secondary); border-radius: 8px; padding: 12px 16px; margin-bottom: 16px;
}
.info-row { display: flex; gap: 24px; flex-wrap: wrap; }
.info-item { display: flex; align-items: center; gap: 8px; }
.info-label { font-size: 12px; color: var(--text-placeholder); }
.info-value { font-size: 13px; color: var(--text-primary); font-weight: 500; }

.loop-warning { margin-bottom: 12px; }

.route-toolbar {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
}
.route-count { font-size: 13px; color: var(--text-placeholder); }

/* 路由规则表格 */
.route-table-wrapper { max-height: 360px; overflow-y: auto; }
.route-table { border: 1px solid var(--border-light); border-radius: 8px; overflow: hidden; }
.route-table-header, .route-table-row {
  display: flex; gap: 8px; padding: 10px 12px; align-items: center;
}
.route-table-header {
  background: var(--bg-input); font-size: 12px; font-weight: 600;
  color: var(--text-secondary); border-bottom: 1px solid var(--border-light);
  position: sticky; top: 0; z-index: 1;
}
.route-table-row { border-bottom: 1px solid var(--border-light); }
.route-table-row:last-child { border-bottom: none; }

.rt-col-idx { width: 32px; flex-shrink: 0; text-align: center; font-size: 12px; color: var(--text-placeholder); }
.rt-col-dir { width: 100px; flex-shrink: 0; }
.rt-col-src { flex: 1; min-width: 0; }
.rt-col-dst { flex: 1; min-width: 0; }
.rt-col-qos { width: 90px; flex-shrink: 0; }
.rt-col-retain { width: 90px; flex-shrink: 0; }
.rt-col-act { width: 40px; flex-shrink: 0; text-align: center; }

/* 帮助 */
.route-help { margin-top: 12px; }
.help-title { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--text-placeholder); }
.help-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 8px; }
.help-item { font-size: 12px; color: var(--text-secondary); line-height: 1.6; }
.help-item strong { color: var(--text-primary); }
.help-item code {
  background: var(--bg-input); padding: 1px 5px; border-radius: 3px;
  font-family: 'Courier New', Consolas, monospace; font-size: 11px; color: #409eff;
}

/* 统计弹窗 */
.stats-info-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; }
.stats-label { font-size: 13px; color: var(--text-placeholder); width: 80px; flex-shrink: 0; }
.stats-value { font-size: 14px; color: var(--text-primary); }
.error-text { color: #f56c6c; }
.stats-metrics { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; padding: 8px 0; }
.metric-item {
  display: flex; align-items: center; gap: 12px; padding: 14px;
  background: var(--bg-secondary); border-radius: 10px;
}
.metric-icon {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; font-size: 18px;
}
.metric-icon.outbound { background: linear-gradient(135deg, #e6a23c20, #e6a23c10); color: #e6a23c; }
.metric-icon.inbound { background: linear-gradient(135deg, #67c23a20, #67c23a10); color: #67c23a; }
.metric-icon.bytes { background: linear-gradient(135deg, #409eff20, #409eff10); color: #409eff; }
.metric-value { font-size: 20px; font-weight: bold; color: var(--text-primary); }
.metric-label { font-size: 12px; color: var(--text-placeholder); margin-top: 2px; }

/* Element Plus 覆盖 */
:deep(.el-table) {
  --el-table-border-color: var(--border-light);
  --el-table-header-bg-color: var(--bg-input);
}
:deep(.el-table th) { font-weight: 600; color: var(--text-secondary); }
:deep(.el-table__row:hover > td) { background: var(--bg-hover) !important; }
:deep(.el-button + .el-button) { margin-left: 4px; }
:deep(.el-form-item__label) { font-size: 13px; }

:deep(.el-collapse-item__header) { background: transparent; border: none; height: 36px; line-height: 36px; }
:deep(.el-collapse-item__wrap) { background: transparent; border: none; }
:deep(.el-collapse) { border: none; }
:deep(.el-collapse-item__content) { padding-bottom: 8px; }

/* 响应式 */
@media (max-width: 1200px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .bridge-page { padding: 12px; }
  .stat-cards { grid-template-columns: 1fr; gap: 8px; }
  .stat-card { padding: 14px; }
  .stat-value { font-size: 22px; }
  .main-card { padding: 14px; }
  .toolbar { flex-direction: column; gap: 12px; align-items: stretch; }
  .toolbar-actions { justify-content: flex-end; }
  .bridge-dialog :deep(.el-dialog),
  .route-dialog :deep(.el-dialog) { width: 95% !important; }
  .info-row { flex-direction: column; gap: 8px; }
}
</style>
