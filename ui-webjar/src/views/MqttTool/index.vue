<template>
  <div class="mqtt-container">
    <!-- 顶部标题栏 -->
    <div class="mqtt-header">
      <div class="header-left">
        <h2>{{ t('mqttTool.title') }}</h2>
        <el-tag :type="connected ? 'success' : 'info'" effect="dark" size="small">
          {{ connected ? t('mqttTool.connected') : t('mqttTool.disconnected') }}
        </el-tag>
      </div>
    </div>

    <div class="mqtt-body">
      <!-- 左侧配置面板 -->
      <div class="config-panel">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-title">
              <el-icon><Setting /></el-icon>
              <span>{{ t('mqttTool.connectionSettings') }}</span>
            </div>
          </template>

          <el-form label-position="top" size="default">
            <el-form-item :label="t('mqttTool.server')">
              <div class="server-row">
                <el-input v-model="connConfig.host" placeholder="localhost" class="host-input" />
                <span class="separator">:</span>
                <el-input-number
                  v-model="connConfig.port"
                  :min="1"
                  :max="65535"
                  class="port-input"
                  controls-position="right"
                />
              </div>
            </el-form-item>

            <el-form-item :label="t('mqttTool.wsPath')">
              <el-input v-model="connConfig.path" placeholder="/mqtt" />
            </el-form-item>

            <el-form-item label="Client ID">
              <el-input v-model="connConfig.clientId">
                <template #append>
                  <el-button @click="generateClientId">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>

            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item :label="t('mqttTool.username')">
                  <el-input v-model="connConfig.username" :placeholder="t('mqttTool.optional')" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="t('mqttTool.password')">
                  <el-input v-model="connConfig.password" type="password" show-password :placeholder="t('mqttTool.optional')" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-button
              :type="connected ? 'danger' : 'primary'"
              @click="toggleConnection"
              style="width: 100%;"
              size="large"
            >
              <el-icon><Link /></el-icon>
              {{ connected ? t('mqttTool.disconnect') : t('mqttTool.connect') }}
            </el-button>
          </el-form>
        </el-card>

        <!-- 订阅管理 -->
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-title">
              <el-icon><Bell /></el-icon>
              <span>{{ t('mqttTool.topicSubscriptions') }}</span>
              <el-tag size="small" type="info">{{ subscriptions.length }}</el-tag>
            </div>
          </template>

          <div class="sub-add-row">
            <el-input
              v-model="newSub.topic"
              :placeholder="t('mqttTool.topicPlaceholder')"
              @keyup.enter="addSubscription"
            >
              <template #append>
                <el-select v-model="newSub.qos" style="width: 85px;">
                  <el-option :value="0" label="QoS 0" />
                  <el-option :value="1" label="QoS 1" />
                  <el-option :value="2" label="QoS 2" />
                </el-select>
              </template>
            </el-input>
            <el-button type="primary" @click="addSubscription" :disabled="!connected">
              {{ t('mqttTool.subscribe') }}
            </el-button>
          </div>

          <div class="sub-list">
            <TransitionGroup name="list">
              <div v-for="sub in subscriptions" :key="sub.topic" class="sub-item">
                <div class="sub-content">
                  <span class="sub-topic">{{ sub.topic }}</span>
                  <el-tag size="small" effect="plain">QoS {{ sub.qos }}</el-tag>
                </div>
                <el-button type="danger" text size="small" @click="removeSubscription(sub)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </TransitionGroup>
            <el-empty v-if="subscriptions.length === 0" :description="t('mqttTool.noSubscriptions')" :image-size="60" />
          </div>
        </el-card>
      </div>

      <!-- 右侧主内容区 -->
      <div class="main-panel">
        <!-- 发布区域 -->
        <el-card shadow="never" class="publish-card">
          <template #header>
            <div class="panel-title">
              <el-icon><Promotion /></el-icon>
              <span>{{ t('mqttTool.publishMessage') }}</span>
            </div>
          </template>

          <el-form label-position="top">
            <el-row :gutter="16">
              <el-col :span="16">
                <el-form-item :label="t('mqttTool.topic')">
                  <el-input v-model="pubMsg.topic" placeholder="test/topic" />
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item label="QoS">
                  <el-select v-model="pubMsg.qos" style="width: 100%;">
                    <el-option :value="0" label="QoS 0" />
                    <el-option :value="1" label="QoS 1" />
                    <el-option :value="2" label="QoS 2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item label=" ">
                  <el-checkbox v-model="pubMsg.retain">Retain</el-checkbox>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item :label="t('mqttTool.delayPublish')">
                  <el-input-number
                    v-model="pubMsg.delaySeconds"
                    :min="0"
                    :max="86400"
                    :placeholder="t('mqttTool.delayPlaceholder')"
                    style="width: 100%;"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item label=" ">
                  <span class="delay-hint">{{ t('mqttTool.delayUnit') }}</span>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item :label="t('mqttTool.messageContent')">
              <div class="payload-editor">
                <div class="payload-toolbar">
                  <el-select v-model="payloadFormat" size="small" style="width: 140px;">
                    <el-option value="none" :label="t('mqttTool.plainText')" />
                    <el-option value="json" :label="t('mqttTool.jsonFormat')" />
                    <el-option value="base64-encode" :label="t('mqttTool.base64Encode')" />
                    <el-option value="base64-decode" :label="t('mqttTool.base64Decode')" />
                    <el-option value="hex-encode" :label="t('mqttTool.hexEncode')" />
                    <el-option value="hex-decode" :label="t('mqttTool.hexDecode')" />
                    <el-option value="url-encode" :label="t('mqttTool.urlEncode')" />
                    <el-option value="url-decode" :label="t('mqttTool.urlDecode')" />
                  </el-select>
                  <el-button size="small" @click="formatPayload" :disabled="!pubMsg.payload">{{ t('mqttTool.format') }}</el-button>
                </div>
                <el-input
                  v-model="pubMsg.payload"
                  type="textarea"
                  :rows="12"
                  placeholder='{"temperature": 25.6, "humidity": 60}'
                  class="payload-textarea"
                />
              </div>
            </el-form-item>

            <div class="publish-action">
              <el-button v-if="pubMsg.delaySeconds > 0" type="warning" @click="publishDelayed" size="large">
                <el-icon><Timer /></el-icon>
                {{ t('mqttTool.delayedPublish') }}
              </el-button>
              <el-button type="primary" @click="publish" :disabled="!connected" size="large">
                <el-icon><Promotion /></el-icon>
                {{ t('mqttTool.publish') }}
              </el-button>
            </div>
          </el-form>
        </el-card>

        <!-- 消息日志 -->
        <div class="message-wrapper">
        <el-card shadow="never" class="message-card">
          <template #header>
            <div class="panel-title">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ t('mqttTool.messageLog') }}</span>
              <el-tag size="small" type="info">{{ messages.length }}</el-tag>
              <div class="title-actions">
                <el-input
                  v-model="msgFilter"
                  :placeholder="t('mqttTool.filterTopics')"
                  size="small"
                  prefix-icon="Search"
                  style="width: 180px;"
                  clearable
                />
                <el-tooltip :content="paused ? t('mqttTool.resume') : t('mqttTool.pause')" placement="top">
                  <el-button size="small" text @click="togglePause">
                    <el-icon :size="16">
                      <VideoPlay v-if="paused" />
                      <VideoPause v-else />
                    </el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip :content="t('mqttTool.clear')" placement="top">
                  <el-button size="small" text @click="clearMessages">
                    <el-icon :size="16"><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </template>

          <div class="message-list" ref="messageLogRef">
            <TransitionGroup name="msg">
              <div
                v-for="(msg, index) in filteredMessages"
                :key="index"
                class="message-item"
                :class="msg.direction"
              >
                <div class="msg-meta">
                  <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
                  <el-tag
                    :type="msg.direction === 'in' ? 'success' : 'warning'"
                    size="small"
                    effect="dark"
                    class="msg-tag"
                  >
                    {{ msg.direction === 'in' ? t('mqttTool.rx') : t('mqttTool.tx') }}
                  </el-tag>
                </div>
                <div class="msg-body">
                  <span class="msg-topic">{{ msg.topic }}</span>
                  <span class="msg-qos">QoS{{ msg.qos }}</span>
                </div>
                <div class="msg-payload">{{ msg.payload }}</div>
              </div>
            </TransitionGroup>
            <el-empty v-if="filteredMessages.length === 0" :description="t('mqttTool.noMessages')" :image-size="80" />
          </div>
        </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
import { createDelayedMessage } from '@/api/delayed'
import {
  Refresh, Link, Plus, Delete, Promotion, Timer,
  VideoPlay, VideoPause, ChatDotRound, Setting, Bell, Search
} from '@element-plus/icons-vue'

const connected = ref(false)
const messages = ref([])
const subscriptions = ref([])
const msgFilter = ref('')
const paused = ref(false)
const messageLogRef = ref(null)
let client = null

const connConfig = ref({
  host: window.location.hostname || 'localhost',
  port: 8083,
  path: '/mqtt',
  clientId: 'mqtt-tool-' + Math.random().toString(16).slice(2, 10),
  username: 'admin',
  password: 'fh'
})

const newSub = ref({ topic: 'test/#', qos: 0 })
const pubMsg = ref({ topic: 'test/topic', qos: 0, retain: false, payload: '{"message": "hello"}', delaySeconds: 0 })
const payloadFormat = ref('none')

const filteredMessages = computed(() => {
  if (!msgFilter.value) return messages.value
  return messages.value.filter(m => m.topic.includes(msgFilter.value))
})

function generateClientId() {
  connConfig.value.clientId = 'mqtt-tool-' + Math.random().toString(16).slice(2, 10)
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return d.toLocaleTimeString() + '.' + String(d.getMilliseconds()).padStart(3, '0')
}

function scrollToBottom() {
  nextTick(() => {
    if (messageLogRef.value) {
      messageLogRef.value.scrollTop = 0
    }
  })
}

function addMessage(direction, topic, payload, qos) {
  if (paused.value && direction === 'in') return
  messages.value.unshift({ timestamp: Date.now(), direction, topic, payload, qos })
  if (messages.value.length > 5000) messages.value.pop()
  scrollToBottom()
}

function toggleConnection() {
  connected.value ? disconnect() : connect()
}

function connect() {
  const { host, port, path, clientId, username, password } = connConfig.value
  const url = `ws://${host}:${port}${path}`

  try {
    const ws = new WebSocket(url)
    client = { ws, connected: false }
    ws.binaryType = 'arraybuffer'

    ws.onopen = () => ws.send(buildConnectPacket(clientId, username, password))

    ws.onmessage = (event) => {
      const data = new Uint8Array(event.data)
      const packetType = (data[0] >> 4) & 0x0f

      if (packetType === 2) {
        if (data[3] === 0) {
          connected.value = true
          client.connected = true
          ElMessage.success(t('mqttTool.connectSuccess'))
          subscriptions.value.forEach(sub => sendSubscribe(sub.topic, sub.qos))
        } else {
          ElMessage.error(t('mqttTool.connectRefused', { code: data[3] }))
          ws.close()
        }
      } else if (packetType === 3) {
        handleIncomingPublish(data)
      }
    }

    ws.onclose = () => {
      connected.value = false
      client = null
      ElMessage.info(t('mqttTool.connectClosed'))
    }

    ws.onerror = () => {
      ElMessage.error(t('mqttTool.connectFailed'))
      connected.value = false
      client = null
    }

    const pingInterval = setInterval(() => {
      if (client?.ws?.readyState === WebSocket.OPEN && connected.value) {
        client.ws.send(new Uint8Array([0xc0, 0x00]))
      } else {
        clearInterval(pingInterval)
      }
    }, 30000)

  } catch (e) {
    ElMessage.error(t('mqttTool.connectFailedMsg', { message: e.message }))
  }
}

function disconnect() {
  if (client?.ws) {
    client.ws.send(new Uint8Array([0xe0, 0x00]))
    client.ws.close()
  }
  connected.value = false
  client = null
}

function buildConnectPacket(clientId, username, password) {
  const enc = new TextEncoder()
  const clientIdBytes = enc.encode(clientId)
  const usernameBytes = username ? enc.encode(username) : null
  const passwordBytes = password ? enc.encode(password) : null

  const protocolName = [0x00, 0x04, 0x4d, 0x51, 0x54, 0x54]
  const protocolLevel = [0x04]
  let connectFlags = 0x02
  if (usernameBytes) connectFlags |= 0x80
  if (passwordBytes) connectFlags |= 0x40
  const keepAlive = [0x00, 0x78]

  const payload = []
  payload.push((clientIdBytes.length >> 8) & 0xff, clientIdBytes.length & 0xff)
  payload.push(...clientIdBytes)
  if (usernameBytes) {
    payload.push((usernameBytes.length >> 8) & 0xff, usernameBytes.length & 0xff)
    payload.push(...usernameBytes)
  }
  if (passwordBytes) {
    payload.push((passwordBytes.length >> 8) & 0xff, passwordBytes.length & 0xff)
    payload.push(...passwordBytes)
  }

  const variableHeader = [...protocolName, ...protocolLevel, connectFlags, ...keepAlive]
  const remainingLength = variableHeader.length + payload.length
  return new Uint8Array([0x10, remainingLength, ...variableHeader, ...payload])
}

function encodeUTF8(str) {
  return new TextEncoder().encode(str)
}

function encodeRemainingLength(length) {
  const bytes = []
  do {
    let encoded = length % 128
    length = Math.floor(length / 128)
    if (length > 0) encoded |= 0x80
    bytes.push(encoded)
  } while (length > 0)
  return bytes
}

function sendSubscribe(topic, qos) {
  if (!client?.ws) return
  const topicBytes = encodeUTF8(topic)
  const variableHeader = [0x00, 0x01]
  const payload = [(topicBytes.length >> 8) & 0xff, topicBytes.length & 0xff, ...topicBytes, qos]
  const remainingLength = variableHeader.length + payload.length
  client.ws.send(new Uint8Array([0x82, ...encodeRemainingLength(remainingLength), ...variableHeader, ...payload]))
}

function sendUnsubscribe(topic) {
  if (!client?.ws) return
  const topicBytes = encodeUTF8(topic)
  const variableHeader = [0x00, 0x01]
  const payload = [(topicBytes.length >> 8) & 0xff, topicBytes.length & 0xff, ...topicBytes]
  const remainingLength = variableHeader.length + payload.length
  client.ws.send(new Uint8Array([0xa2, ...encodeRemainingLength(remainingLength), ...variableHeader, ...payload]))
}

function handleIncomingPublish(data) {
  let offset = 1, multiplier = 1, remainingLength = 0, encodedByte
  do {
    encodedByte = data[offset++]
    remainingLength += (encodedByte & 0x7f) * multiplier
    multiplier *= 128
  } while ((encodedByte & 0x80) !== 0)

  const qos = (data[0] >> 1) & 0x03
  const topicLength = (data[offset] << 8) | data[offset + 1]
  offset += 2
  const topic = new TextDecoder().decode(data.slice(offset, offset + topicLength))
  offset += topicLength
  if (qos > 0) offset += 2

  const payloadBytes = data.slice(offset, offset + remainingLength - (offset - 2))
  const payload = new TextDecoder().decode(payloadBytes)
  addMessage('in', topic, payload, qos)
}

function addSubscription() {
  if (!newSub.value.topic) return ElMessage.warning(t('mqttTool.enterTopic'))
  if (subscriptions.value.some(s => s.topic === newSub.value.topic)) return ElMessage.warning(t('mqttTool.topicExists'))
  subscriptions.value.push({ topic: newSub.value.topic, qos: newSub.value.qos })
  if (connected.value) sendSubscribe(newSub.value.topic, newSub.value.qos)
  ElMessage.success(t('mqttTool.subscribeSuccess'))
}

function removeSubscription(sub) {
  const index = subscriptions.value.indexOf(sub)
  if (index > -1) {
    subscriptions.value.splice(index, 1)
    if (connected.value) sendUnsubscribe(sub.topic)
  }
}

function publish() {
  if (!connected.value) return ElMessage.warning(t('mqttTool.connectFirst'))
  if (!pubMsg.value.topic) return ElMessage.warning(t('mqttTool.enterTopic'))

  const { topic, qos, retain, payload } = pubMsg.value
  const topicBytes = encodeUTF8(topic)
  const payloadBytes = encodeUTF8(payload)

  let flags = 0x30
  if (retain) flags |= 0x01
  flags |= (qos << 1)

  const variableHeader = [(topicBytes.length >> 8) & 0xff, topicBytes.length & 0xff, ...topicBytes]
  if (qos > 0) variableHeader.push(0x00, 0x01)

  const remainingLength = variableHeader.length + payloadBytes.length
  client.ws.send(new Uint8Array([flags, ...encodeRemainingLength(remainingLength), ...variableHeader, ...payloadBytes]))
  addMessage('out', topic, payload, qos)
}

async function publishDelayed() {
  if (!pubMsg.value.topic) return ElMessage.warning(t('mqttTool.enterTopic'))
  if (!pubMsg.value.payload) return ElMessage.warning(t('mqttTool.enterPayload'))
  if (pubMsg.value.delaySeconds <= 0) return ElMessage.warning(t('mqttTool.enterDelay'))

  try {
    const res = await createDelayedMessage({
      topic: pubMsg.value.topic,
      qos: pubMsg.value.qos,
      payload: pubMsg.value.payload,
      retain: pubMsg.value.retain,
      delaySeconds: pubMsg.value.delaySeconds
    })
    if (res.success) {
      ElMessage.success(t('mqttTool.delayedPublishSuccess', { seconds: pubMsg.value.delaySeconds }))
      addMessage('out', pubMsg.value.topic, `[${t('mqttTool.delayedLabel', { seconds: pubMsg.value.delaySeconds })}] ${pubMsg.value.payload}`, pubMsg.value.qos)
    } else {
      ElMessage.error(res.message || t('mqttTool.delayedPublishFailed'))
    }
  } catch (e) {
    ElMessage.error(t('mqttTool.delayedPublishFailed'))
  }
}

function clearMessages() { messages.value = [] }
function togglePause() { paused.value = !paused.value }

function formatPayload() {
  if (!pubMsg.value.payload) return
  try {
    switch (payloadFormat.value) {
      case 'json':
        pubMsg.value.payload = JSON.stringify(JSON.parse(pubMsg.value.payload), null, 2)
        break
      case 'base64-encode':
        pubMsg.value.payload = btoa(unescape(encodeURIComponent(pubMsg.value.payload)))
        break
      case 'base64-decode':
        pubMsg.value.payload = decodeURIComponent(escape(atob(pubMsg.value.payload)))
        break
      case 'hex-encode':
        pubMsg.value.payload = Array.from(new TextEncoder().encode(pubMsg.value.payload))
          .map(b => b.toString(16).padStart(2, '0')).join(' ')
        break
      case 'hex-decode':
        pubMsg.value.payload = new TextDecoder().decode(
          new Uint8Array(pubMsg.value.payload.split(/[\s]+/).map(b => parseInt(b, 16)))
        )
        break
      case 'url-encode':
        pubMsg.value.payload = encodeURIComponent(pubMsg.value.payload)
        break
      case 'url-decode':
        pubMsg.value.payload = decodeURIComponent(pubMsg.value.payload)
        break
      default:
        break
    }
  } catch (e) {
    ElMessage.error(t('mqttTool.convertFailed', { message: e.message }))
  }
}

onMounted(() => generateClientId())
onUnmounted(() => disconnect())
</script>

<style scoped>
.mqtt-container {
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
  min-height: 0;
}

/* 顶部标题 */
.mqtt-header {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-darker);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

/* 主体布局 */
.mqtt-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  min-height: 0;
  overflow-y: auto;
}

/* 左侧配置面板 */
.config-panel {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-card {
  border: none;
  border-radius: 8px;
}

.panel-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.panel-card :deep(.el-card__body) {
  padding: 16px;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.panel-title .el-icon {
  color: #409eff;
}

.title-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 服务器输入行 */
.server-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.host-input {
  flex: 1;
}

.port-input {
  width: 100px;
}

.separator {
  font-weight: bold;
  color: var(--text-placeholder);
}

/* 订阅区域 */
.sub-add-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.sub-add-row .el-input {
  flex: 1;
}

.sub-list {
  max-height: 300px;
  overflow-y: auto;
}

.sub-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  margin-bottom: 8px;
  background: var(--bg-secondary);
  border-radius: 6px;
  transition: all 0.2s;
}

.sub-item:hover {
  background: var(--color-primary-hover);
}

.sub-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.sub-topic {
  font-size: 13px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 右侧主面板 */
.main-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
  min-height: 0;
}

/* 发布卡片 */
.publish-card {
  border: none;
  border-radius: 8px;
  flex-shrink: 0;
}

.publish-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.payload-textarea :deep(.el-textarea__inner) {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
  background: var(--bg-input);
  min-height: 200px;
  resize: vertical;
}

.payload-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.payload-editor :deep(.el-textarea) {
  width: 100%;
}

.publish-action {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.payload-toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 消息卡片包装器 */
.message-wrapper {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding-bottom: 16px;
  box-sizing: border-box;
}

/* 消息卡片 */
.message-card {
  border: none;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* 消息卡片内容区域 */
.message-card :deep(.el-card__body) {
  overflow-y: auto;
}

.message-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.message-card :deep(.el-card__body) {
  flex: 1;
  padding: 0;
  overflow: hidden;
}

/* 消息列表 */
.message-list {
  height: 100%;
  overflow-y: auto;
  padding: 8px;
}

.message-item {
  padding: 12px 16px;
  margin-bottom: 8px;
  background: var(--bg-card);
  border-radius: 8px;
  border-left: 4px solid transparent;
  transition: all 0.2s;
}

.message-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message-item.in {
  border-left-color: #67c23a;
}

.message-item.out {
  border-left-color: #e6a23c;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.msg-time {
  font-size: 12px;
  color: var(--text-placeholder);
  font-family: 'Courier New', monospace;
}

.msg-tag {
  font-size: 11px;
}

.msg-body {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.msg-topic {
  font-size: 14px;
  font-weight: 500;
  color: #409eff;
}

.msg-qos {
  font-size: 11px;
  color: var(--text-placeholder);
  background: var(--bg-secondary);
  padding: 2px 6px;
  border-radius: 4px;
}

.msg-payload {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: 'Courier New', Consolas, monospace;
  word-break: break-all;
  line-height: 1.5;
  background: var(--bg-input);
  padding: 8px 12px;
  border-radius: 4px;
}

/* 过渡动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.msg-enter-active {
  transition: all 0.2s ease;
}

.msg-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

/* 滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--border-base);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--border-darker);
}

.delay-hint {
  font-size: 12px;
  color: var(--text-placeholder);
  line-height: 32px;
}

/* Element Plus 覆盖 */
:deep(.el-card) {
  --el-card-border-color: transparent;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  font-size: 12px;
  color: var(--text-placeholder);
}

/* 响应式 */
@media (max-width: 900px) {
  .mqtt-body {
    flex-direction: column;
    padding: 12px;
    gap: 12px;
  }

  .config-panel {
    width: 100%;
  }

  .mqtt-header {
    padding: 12px 16px;
  }

  .header-left h2 {
    font-size: 16px;
  }

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}

@media (max-width: 600px) {
  .mqtt-body {
    padding: 8px;
    gap: 8px;
  }

  .mqtt-header {
    padding: 10px 12px;
  }

  .header-left h2 {
    font-size: 14px;
  }

  .panel-card :deep(.el-card__header),
  .publish-card :deep(.el-card__header),
  .message-card :deep(.el-card__header) {
    padding: 10px 12px;
  }

  .panel-card :deep(.el-card__body) {
    padding: 12px;
  }

  :deep(.el-form-item) {
    margin-bottom: 10px;
  }

  :deep(.el-form-item__label) {
    font-size: 11px;
  }

  .msg-payload {
    font-size: 12px;
    padding: 6px 10px;
  }
}
</style>
