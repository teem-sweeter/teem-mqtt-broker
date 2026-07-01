<template>
  <div class="actuator-dashboard">
    <div class="dashboard-content">
      <!-- 顶部统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon health">
                <el-icon :size="24"><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ t('actuator.healthStatus') }}</div>
                <div class="stat-value" :class="healthStatus.class">{{ healthStatus.text }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon uptime">
                <el-icon :size="24"><Timer /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ t('actuator.startTime') }}</div>
                <div class="stat-value">{{ appInfoData?.startTime || '-' }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon runtime">
                <el-icon :size="24"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ t('actuator.runtime') }}</div>
                <div class="stat-value">{{ uptime }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon memory">
                <el-icon :size="24"><DataLine /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ t('actuator.memoryUsage') }}</div>
                <div class="stat-value">{{ memoryUsage }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" type="border-card" class="main-tabs" @tab-change="onTabChange">
        <!-- 健康检查 -->
        <el-tab-pane name="health" :label="t('actuator.healthCheck')">
          <el-card class="tab-card" v-loading="loading.health">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.appHealthStatus') }}</span>
                <div class="header-actions">
                  <el-button type="warning" size="small" @click="enhancedRestart">{{ t('actuator.restartService') }}</el-button>
                  <el-button type="danger" size="small" @click="enhancedShutdown">{{ t('actuator.shutdownService') }}</el-button>
                  <el-button type="primary" size="small" @click="loadHealth">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>
            <el-empty v-if="!healthData.status" :description="t('actuator.noData')" :image-size="80"></el-empty>
            <div v-else class="health-content">
              <div class="health-summary">
                <el-tag :type="healthData.status === 'UP' ? 'success' : (healthData.status === 'DOWN' ? 'danger' : 'warning')" size="large">
                  {{ healthData.status }}
                </el-tag>
                <span class="summary-text">{{ t('actuator.overallStatus') }}</span>
              </div>
              <div class="health-details">
                <el-row :gutter="20">
                  <el-col :span="12" v-if="healthData.components?.diskSpace">
                    <div class="component-item">
                      <div class="component-header">
                        <el-tag :type="healthData.components.diskSpace.status === 'UP' ? 'success' : 'danger'" size="small">
                          {{ healthData.components.diskSpace.status }}
                        </el-tag>
                        <span class="component-name">{{ t('actuator.diskSpace') }}</span>
                      </div>
                      <div class="component-details" v-if="healthData.components.diskSpace.details">
                        <p>{{ t('actuator.total') }}: {{ formatBytes(healthData.components.diskSpace.details.total) }}</p>
                        <p>{{ t('actuator.available') }}: {{ formatBytes(healthData.components.diskSpace.details.free) }}</p>
                        <p>{{ t('actuator.path') }}: {{ healthData.components.diskSpace.details.path }}</p>
                      </div>
                    </div>
                  </el-col>

                  <el-col :span="12" v-if="healthData.components?.db">
                    <div class="component-item">
                      <div class="component-header">
                        <el-tag :type="healthData.components.db.status === 'UP' ? 'success' : 'danger'" size="small">
                          {{ healthData.components.db.status }}
                        </el-tag>
                        <span class="component-name">{{ t('actuator.database') }}</span>
                      </div>
                      <div class="component-details" v-if="healthData.components.db.details">
                        <p>{{ healthData.components.db.details.database }}</p>
                      </div>
                    </div>
                  </el-col>

                  <el-col :span="12" v-if="healthData.components?.ping">
                    <div class="component-item">
                      <div class="component-header">
                        <el-tag :type="healthData.components.ping.status === 'UP' ? 'success' : 'danger'" size="small">
                          {{ healthData.components.ping.status }}
                        </el-tag>
                        <span class="component-name">Ping</span>
                      </div>
                    </div>
                  </el-col>
                  <template v-for="(value, key) in healthData.components" :key="key">
                    <el-col :span="12"
                      v-if="key !== 'diskSpace' && key !== 'db' && key !== 'ping'">
                      <div class="component-item">
                        <div class="component-header">
                          <el-tag :type="value.status === 'UP' ? 'success' : (value.status === 'DOWN' ? 'danger' : 'warning')" size="small">
                            {{ value.status || value }}
                          </el-tag>
                          <span class="component-name">{{ key }}</span>
                        </div>
                        <div class="component-details" v-if="value.details">
                          <p>{{ JSON.stringify(value.details) }}</p>
                        </div>
                      </div>
                    </el-col>
                  </template>
                </el-row>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
        <el-tab-pane name="info" :label="t('actuator.appInfo')">
          <el-card class="tab-card" v-loading="loading.info">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.appInfo') }}</span>
                <el-button type="primary" size="small" @click="loadInfo">{{ t('actuator.refresh') }}</el-button>
              </div>
            </template>
            <el-tabs type="card" class="info-tabs">
              <el-tab-pane :label="t('actuator.basicInfo')">
                <el-descriptions :column="2" v-if="appInfoData" size="small">
                  <el-descriptions-item :label="t('actuator.appName')">MQTT Broker</el-descriptions-item>
                  <el-descriptions-item :label="t('actuator.javaVersion')">{{ appInfoData.javaVersion || '-' }}</el-descriptions-item>
                  <el-descriptions-item :label="t('actuator.osName')">{{ appInfoData.osName || '-' }}</el-descriptions-item>
                  <el-descriptions-item :label="t('actuator.osArch')">{{ appInfoData.osArch || '-' }}</el-descriptions-item>
                </el-descriptions>
              </el-tab-pane>

              <el-tab-pane :label="t('actuator.cpuInfo')">
                <div v-if="cpuMetrics">
                  <el-descriptions :column="2" size="small">
                    <el-descriptions-item :label="t('actuator.cpuCores')">{{ cpuMetrics.cpuCount || '-' }}</el-descriptions-item>
                    <el-descriptions-item :label="t('actuator.processCpuUsage')">{{ cpuMetrics.processCpuUsage != null ? (cpuMetrics.processCpuUsage * 100).toFixed(1) + '%' : '-' }}</el-descriptions-item>
                    <el-descriptions-item :label="t('actuator.systemCpuUsage')">{{ cpuMetrics.systemCpuUsage != null ? (cpuMetrics.systemCpuUsage * 100).toFixed(1) + '%' : '-' }}</el-descriptions-item>
                    <el-descriptions-item :label="t('actuator.systemLoadAverage')">{{ cpuMetrics.systemLoadAverage >= 0 ? cpuMetrics.systemLoadAverage.toFixed(2) : '-' }}</el-descriptions-item>
                  </el-descriptions>
                </div>
                <el-empty v-else :description="t('actuator.noCpuData')" :image-size="60"></el-empty>
              </el-tab-pane>

              <el-tab-pane :label="t('actuator.memoryInfo')">
                <div v-if="jvmMemoryMetrics" class="metrics-container">
                  <h4>{{ t('actuator.jvmMemoryMetrics') }}</h4>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <div class="metric-card">
                        <div class="metric-label">{{ t('actuator.jvmMaxMemory') }}</div>
                        <div class="metric-value">{{ formatBytes(jvmMemoryMetrics.max?.measurements?.[0]?.value || 0) }}</div>
                      </div>
                    </el-col>
                    <el-col :span="12">
                      <div class="metric-card">
                        <div class="metric-label">{{ t('actuator.jvmUsedMemory') }}</div>
                        <div class="metric-value">{{ formatBytes(jvmMemoryMetrics.used?.measurements?.[0]?.value || 0) }}</div>
                      </div>
                    </el-col>
                  </el-row>
                  <el-progress
                    :percentage="calculateMemoryUsagePercentage()"
                    :status="calculateMemoryUsagePercentage() > 80 ? 'exception' : 'success'"
                    :stroke-width="20"
                    striped
                    striped-flow
                  />
                  <div class="memory-usage-text">
                    {{ t('actuator.memoryUsageRate') }}: {{ calculateMemoryUsagePercentage() }}%
                  </div>
                  <h5 style="margin-top: 20px;">{{ t('actuator.heapMemory') }}</h5>
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <div class="metric-card">
                        <div class="metric-label">{{ t('actuator.used') }}</div>
                        <div class="metric-value">{{ formatBytes(jvmMemoryMetrics.heap?.used?.measurements?.[0]?.value || 0) }}</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="metric-card">
                        <div class="metric-label">{{ t('actuator.committed') }}</div>
                        <div class="metric-value">{{ formatBytes(jvmMemoryMetrics.heap?.committed?.measurements?.[0]?.value || 0) }}</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="metric-card">
                        <div class="metric-label">{{ t('actuator.max') }}</div>
                        <div class="metric-value">{{ formatBytes(jvmMemoryMetrics.heap?.max?.measurements?.[0]?.value || 0) }}</div>
                      </div>
                    </el-col>
                  </el-row>
                  <el-progress
                    :percentage="calculateHeapMemoryUsagePercentage()"
                    :status="calculateHeapMemoryUsagePercentage() > 80 ? 'warning' : ''"
                    :stroke-width="15"
                  />
                  <div class="memory-usage-text">
                    {{ t('actuator.heapMemoryUsageRate') }}: {{ calculateHeapMemoryUsagePercentage() }}%
                  </div>

                  <!-- 非堆内存指标 -->
                  <h5 style="margin-top: 20px;">{{ t('actuator.nonHeapMemory') }}</h5>
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <div class="metric-card">
                        <div class="metric-label">{{ t('actuator.used') }}</div>
                        <div class="metric-value">{{ formatBytes(jvmMemoryMetrics.nonheap?.used?.measurements?.[0]?.value || 0) }}</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="metric-card">
                        <div class="metric-label">{{ t('actuator.committed') }}</div>
                        <div class="metric-value">{{ formatBytes(jvmMemoryMetrics.nonheap?.committed?.measurements?.[0]?.value || 0) }}</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="metric-card">
                        <div class="metric-label">{{ t('actuator.max') }}</div>
                        <div class="metric-value">{{ formatBytes(jvmMemoryMetrics.nonheap?.max?.measurements?.[0]?.value || 0) }}</div>
                      </div>
                    </el-col>
                  </el-row>
                  <el-progress
                    :percentage="calculateNonHeapMemoryUsagePercentage()"
                    :status="calculateNonHeapMemoryUsagePercentage() > 80 ? 'warning' : ''"
                    :stroke-width="15"
                  />
                  <div class="memory-usage-text">
                    {{ t('actuator.nonHeapMemoryUsageRate') }}: {{ calculateNonHeapMemoryUsagePercentage() }}%
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-tab-pane>
        <!-- 环境信息 -->
        <el-tab-pane name="env" :label="t('actuator.envInfo')">
          <el-card class="tab-card" v-loading="loading.env">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.systemEnvAndProps') }}</span>
                <div class="header-actions">
                  <el-input
                    v-model="envSearch"
                    :placeholder="t('actuator.searchProps')"
                    size="small"
                    clearable
                    @input="filterEnv"
                    class="search-input"
                  />
                  <el-button type="primary" size="small" @click="loadEnv">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>
            <el-tabs v-model="envActiveTab" type="card" class="env-tabs">
              <el-tab-pane :label="t('actuator.systemProps')" name="systemProperties">
                <el-table
                  :data="filteredSystemProps"
                  height="400"
                  style="width: 100%"
                  class="env-table"
                  :empty-text="t('actuator.noSystemProps')"
                >
                  <el-table-column prop="key" :label="t('actuator.propName')" width="300" sortable />
                  <el-table-column prop="value" :label="t('actuator.value')" show-overflow-tooltip />
                </el-table>
              </el-tab-pane>
              <el-tab-pane :label="t('actuator.envVars')" name="systemEnvironment">
                <el-table
                  :data="filteredEnvVars"
                  height="400"
                  style="width: 100%"
                  class="env-table"
                  :empty-text="t('actuator.noEnvVars')"
                >
                  <el-table-column prop="key" :label="t('actuator.varName')" width="300" sortable />
                  <el-table-column prop="value" :label="t('actuator.value')" show-overflow-tooltip />
                </el-table>
              </el-tab-pane>
              <el-tab-pane :label="t('actuator.configProps')" name="configurationProperties">
                <el-table
                  :data="filteredConfigProps"
                  height="400"
                  style="width: 100%"
                  class="env-table"
                  :empty-text="t('actuator.noConfigProps')"
                >
                  <el-table-column prop="key" :label="t('actuator.configItem')" width="400" sortable />
                  <el-table-column prop="value" :label="t('actuator.value')" show-overflow-tooltip />
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-tab-pane>

        <!-- 日志级别 -->
        <el-tab-pane name="loggers" :label="t('actuator.logInfo')">
          <el-card class="tab-card" v-loading="loading.loggers">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.logLevel') }}</span>
                <div class="header-actions">
                  <el-input
                    v-model="loggerSearch"
                    :placeholder="t('actuator.searchLoggers')"
                    size="small"
                    clearable
                    @input="filterLoggers"
                    class="search-input"
                  />
                  <el-button type="primary" size="small" @click="loadLoggers">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>

            <div v-if="loggersData.length > 0">
              <el-table
                :data="paginatedLoggersData"
                style="width: 100%"
                height="450"
                class="loggers-table"
              >
                <el-table-column prop="name" :label="t('actuator.loggerName')" min-width="300" sortable />
                <el-table-column prop="configuredLevel" :label="t('actuator.configuredLevel')" width="120" align="center">
                  <template #default="scope">
                    <el-tag :type="getLevelType(scope.row.configuredLevel)" size="small">{{ scope.row.configuredLevel || '-' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="effectiveLevel" :label="t('actuator.effectiveLevel')" width="120" align="center">
                  <template #default="scope">
                    <el-tag :type="getLevelType(scope.row.effectiveLevel)" size="small">{{ scope.row.effectiveLevel }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="t('actuator.operation')" align="center" width="150">
                  <template #default="scope">
                    <el-select
                      v-model="scope.row.newLevel"
                      :placeholder="t('actuator.adjustLevel')"
                      size="small"
                      @change="(val) => changeLogLevel(scope.row.name, val)"
                    >
                      <el-option label="TRACE" value="TRACE" />
                      <el-option label="DEBUG" value="DEBUG" />
                      <el-option label="INFO" value="INFO" />
                      <el-option label="WARN" value="WARN" />
                      <el-option label="ERROR" value="ERROR" />
                      <el-option label="OFF" value="OFF" />
                    </el-select>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                v-model:current-page="loggerPagination.currentPage"
                v-model:page-size="loggerPagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :small="true"
                :disabled="false"
                :background="true"
                layout="total, sizes, prev, pager, next, jumper"
                :total="filteredLoggersData.length"
                @size-change="handleLoggerPageSizeChange"
                @current-change="handleLoggerCurrentPageChange"
                class="logger-pagination"
              />
            </div>

            <el-empty v-else :description="t('actuator.noLoggers')" :image-size="80"></el-empty>
          </el-card>
        </el-tab-pane>
        <el-tab-pane name="threads" :label="t('actuator.threadMonitor')">
          <el-card class="tab-card" v-loading="loading.threads">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.threadStackInfo') }}</span>
                <div class="header-actions">
                  <el-button type="primary" size="small" @click="loadThreads">{{ t('actuator.refresh') }}</el-button>
                  <el-button type="success" size="small" @click="downloadThreadDump">{{ t('actuator.exportThreadDump') }}</el-button>
                </div>
              </div>
            </template>
            <div v-if="threadsData.length > 0" class="thread-stats">
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="stat-card-simple">
                    <div class="stat-title">{{ t('actuator.totalThreads') }}</div>
                    <div class="stat-value">{{ threadsData.length }}</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="stat-card-simple">
                    <div class="stat-title">{{ t('actuator.running') }}</div>
                    <div class="stat-value">{{ threadsData.filter(item => item.state === 'RUNNABLE').length }}</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="stat-card-simple">
                    <div class="stat-title">{{ t('actuator.blocked') }}</div>
                    <div class="stat-value">{{ threadsData.filter(item => item.state === 'BLOCKED').length }}</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="stat-card-simple">
                    <div class="stat-title">{{ t('actuator.waiting') }}</div>
                    <div class="stat-value">{{ threadsData.filter(item => item.state === 'WAITING' || item.state === 'TIMED_WAITING').length }}</div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <el-table
              :data="threadsData"
              style="width: 100%"
              height="500"
              @row-click="showThreadDetail"
              class="threads-table"
              :empty-text="t('actuator.noThreads')"
            >
              <el-table-column prop="id" :label="t('actuator.id')" width="80" sortable />
              <el-table-column prop="name" :label="t('actuator.name')" min-width="250" />
              <el-table-column prop="state" :label="t('actuator.state')" width="120" align="center">
                <template #default="scope">
                  <el-tag :type="getThreadStateType(scope.row.state)" size="small">{{ scope.row.state }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="cpuTime" :label="t('actuator.cpuTime')" width="120" />
              <el-table-column prop="blockedTime" :label="t('actuator.blockedTime')" width="120" />
            </el-table>
          </el-card>
        </el-tab-pane>
        <el-tab-pane name="files" :label="t('actuator.fileDownload')">
          <el-card class="tab-card">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.systemFileDownload') }}</span>
              </div>
            </template>
            <div class="files-content">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="file-card">
                    <div class="file-icon">
                      <el-icon :size="40" color="#409eff"><Document /></el-icon>
                    </div>
                    <div class="file-info">
                      <h3>{{ t('actuator.appLog') }}</h3>
                      <p>{{ t('actuator.appLogDesc') }}</p>
                      <el-button
                        type="primary"
                        @click="downloadLogFile"
                        :loading="loading.logfile"
                      >
                        {{ loading.logfile ? t('actuator.downloading') : t('actuator.downloadLogFile') }}
                      </el-button>
                    </div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="file-card">
                    <div class="file-icon">
                      <el-icon :size="40" color="#e6a23c"><Coin /></el-icon>
                    </div>
                    <div class="file-info">
                      <h3>{{ t('actuator.heapDumpFile') }}</h3>
                      <p>{{ t('actuator.heapDumpDesc') }}</p>
                      <el-button
                        type="warning"
                        @click="downloadHeapDump"
                        :loading="loading.heapdump"
                      >
                        {{ loading.heapdump ? t('actuator.downloading') : t('actuator.downloadHeapDump') }}
                      </el-button>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-tab-pane>
        <el-tab-pane name="metrics" :label="t('actuator.metricsMonitor')">
          <el-card class="tab-card" v-loading="loading.metrics">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.appMetricsMonitor') }}</span>
                <div class="header-actions">
                  <el-input
                    v-model="metricsSearch"
                    :placeholder="t('actuator.searchMetrics')"
                    size="small"
                    clearable
                    @input="filterMetrics"
                    class="search-input"
                  />
                  <el-button type="primary" size="small" @click="loadMetricsList">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>
            <div v-if="jvmMemoryMetrics" class="metrics-overview">
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="metric-overview-card">
                    <div class="metric-overview-title">{{ t('actuator.jvmMemoryUsage') }}</div>
                    <div class="metric-overview-value">{{ calculateMemoryUsagePercentage() }}%</div>
                    <el-progress
                      :percentage="calculateMemoryUsagePercentage()"
                      :status="calculateMemoryUsagePercentage() > 80 ? 'exception' : 'success'"
                      :stroke-width="10"
                      :show-text="false"
                    />
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="metric-overview-card">
                    <div class="metric-overview-title">{{ t('actuator.heapMemoryUsage') }}</div>
                    <div class="metric-overview-value">{{ calculateHeapMemoryUsagePercentage() }}%</div>
                    <el-progress
                      :percentage="calculateHeapMemoryUsagePercentage()"
                      :status="calculateHeapMemoryUsagePercentage() > 80 ? 'warning' : ''"
                      :stroke-width="10"
                      :show-text="false"
                    />
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="metric-overview-card">
                    <div class="metric-overview-title">{{ t('actuator.nonHeapMemoryUsage') }}</div>
                    <div class="metric-overview-value">{{ calculateNonHeapMemoryUsagePercentage() }}%</div>
                    <el-progress
                      :percentage="calculateNonHeapMemoryUsagePercentage()"
                      :status="calculateNonHeapMemoryUsagePercentage() > 80 ? 'warning' : ''"
                      :stroke-width="10"
                      :show-text="false"
                    />
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="metric-overview-card">
                    <div class="metric-overview-title">{{ t('actuator.threadCount') }}</div>
                    <div class="metric-overview-value">{{ threadsData.length || 0 }}</div>
                    <el-progress
                      :percentage="Math.min(100, (threadsData.length || 0) / 100 * 100)"
                      status="success"
                      :stroke-width="10"
                      :show-text="false"
                    />
                  </div>
                </el-col>
              </el-row>
            </div>
            <div class="metrics-categories">
              <el-tabs v-model="activeMetricsCategory" type="card" @tab-change="onMetricsCategoryChange">
                <el-tab-pane :label="t('actuator.all')" name="all"></el-tab-pane>
                <el-tab-pane label="JVM" name="jvm"></el-tab-pane>
                <el-tab-pane label="HTTP" name="http"></el-tab-pane>
                <el-tab-pane :label="t('actuator.database')" name="database"></el-tab-pane>
                <el-tab-pane :label="t('actuator.threads')" name="threads"></el-tab-pane>
                <el-tab-pane :label="t('actuator.system')" name="system"></el-tab-pane>
                <el-tab-pane :label="t('actuator.security')" name="security"></el-tab-pane>
                <el-tab-pane :label="t('actuator.other')" name="other"></el-tab-pane>
              </el-tabs>
            </div>
            <div class="metrics-list-container">
              <el-table
                :data="paginatedMetricsData"
                style="width: 100%"
                height="400"
                @row-click="showMetricDetail"
                class="metrics-table"
              >
                <el-table-column prop="name" :label="t('actuator.metricName')" min-width="300" sortable />
                <el-table-column :label="t('actuator.value')" width="200" align="center">
                  <template #default="scope">
                    <span v-if="scope.row.measurements && scope.row.measurements.length > 0">
                      {{ formatMetricValue(scope.row.measurements[0].value, scope.row.baseUnit) }}
                    </span>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column :label="t('actuator.unit')" width="100" align="center">
                  <template #default="scope">
                    {{ scope.row.baseUnit || '-' }}
                  </template>
                </el-table-column>
                <el-table-column :label="t('actuator.description')" show-overflow-tooltip>
                  <template #default="scope">
                    {{ scope.row.description || '-' }}
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                v-model:current-page="metricsPagination.currentPage"
                v-model:page-size="metricsPagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :small="true"
                :disabled="false"
                :background="true"
                layout="total, sizes, prev, pager, next, jumper"
                :total="filteredMetricsData.length"
                @size-change="handleMetricsPageSizeChange"
                @current-change="handleMetricsCurrentPageChange"
                class="metrics-pagination"
              />
            </div>
          </el-card>
          <el-dialog
            v-model="metricDetailDialogVisible"
            :title="t('actuator.metricDetail')"
            width="60%"
            :before-close="handleCloseMetricDetail"
          >
            <div v-if="selectedMetric" class="metric-detail-content">
              <el-descriptions :column="1" border>
                <el-descriptions-item :label="t('actuator.metricName')">{{ selectedMetric.name }}</el-descriptions-item>
                <el-descriptions-item :label="t('actuator.baseUnit')">{{ selectedMetric.baseUnit || '-' }}</el-descriptions-item>
                <el-descriptions-item :label="t('actuator.description')">{{ selectedMetric.description || '-' }}</el-descriptions-item>
                <el-descriptions-item :label="t('actuator.type')">{{ selectedMetric.type || '-' }}</el-descriptions-item>
              </el-descriptions>

              <h4 style="margin-top: 20px;">{{ t('actuator.measurements') }}</h4>
              <el-table
                :data="selectedMetric.measurements"
                style="width: 100%"
                max-height="300"
              >
                <el-table-column prop="statistic" :label="t('actuator.statisticType')" width="150" />
                <el-table-column prop="value" :label="t('actuator.value')">
                  <template #default="scope">
                    {{ formatMetricValue(scope.row.value, selectedMetric.baseUnit) }}
                  </template>
                </el-table-column>
              </el-table>

              <div v-if="selectedMetric.availableTags && selectedMetric.availableTags.length > 0">
                <h4 style="margin-top: 20px;">{{ t('actuator.availableTags') }}</h4>
                <el-table
                  :data="selectedMetric.availableTags"
                  style="width: 100%"
                  max-height="300"
                >
                  <el-table-column prop="tag" :label="t('actuator.tagName')" width="200" />
                  <el-table-column prop="values" :label="t('actuator.tagValues')">
                    <template #default="scope">
                      <el-tag
                        v-for="value in scope.row.values"
                        :key="value"
                        size="small"
                        style="margin-right: 5px; margin-bottom: 5px;"
                      >
                        {{ value }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-dialog>
        </el-tab-pane>

        <!-- Mappings 映射 -->
        <el-tab-pane name="mappings" :label="t('actuator.apiMappings')">
          <el-card class="tab-card" v-loading="loading.mappings">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.dispatcherServletMappings') }}</span>
                <div class="header-actions">
                  <el-input
                    v-model="mappingsSearch"
                    :placeholder="t('actuator.searchMappings')"
                    size="small"
                    clearable
                    @input="filterMappings"
                    class="search-input"
                  />
                  <el-button type="primary" size="small" @click="loadMappings">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>
            <el-table :data="paginatedMappingsData" style="width: 100%" height="500">
              <el-table-column prop="predicate" :label="t('actuator.predicate')" min-width="300" sortable />
              <el-table-column prop="handler" :label="t('actuator.handler')" min-width="400" show-overflow-tooltip />
            </el-table>
            <el-pagination
              v-model:current-page="mappingsPagination.currentPage"
              v-model:page-size="mappingsPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :small="true"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredMappingsData.length"
              @size-change="handleMappingsPageSizeChange"
              @current-change="handleMappingsCurrentPageChange"
              class="logger-pagination"
            />
          </el-card>
        </el-tab-pane>

        <!-- Beans 列表 -->
        <el-tab-pane name="beans" label="Beans">
          <el-card class="tab-card" v-loading="loading.beans">
            <template #header>
              <div class="card-header">
                <span>Spring Context Beans</span>
                <div class="header-actions">
                  <el-input
                    v-model="beansSearch"
                    :placeholder="t('actuator.searchBeans')"
                    size="small"
                    clearable
                    @input="filterBeans"
                    class="search-input"
                  />
                  <el-button type="primary" size="small" @click="loadBeans">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>
            <el-table :data="paginatedBeansData" style="width: 100%" height="500">
              <el-table-column prop="name" :label="t('actuator.beanName')" min-width="250" sortable show-overflow-tooltip />
              <el-table-column prop="type" :label="t('actuator.type')" min-width="300" show-overflow-tooltip />
              <el-table-column prop="scope" :label="t('actuator.scope')" width="100" align="center" />
              <el-table-column :label="t('actuator.aliases')" min-width="150">
                <template #default="scope">
                  {{ scope.row.aliases?.join(', ') || '-' }}
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="beansPagination.currentPage"
              v-model:page-size="beansPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :small="true"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredBeansData.length"
              @size-change="handleBeansPageSizeChange"
              @current-change="handleBeansCurrentPageChange"
              class="logger-pagination"
            />
          </el-card>
        </el-tab-pane>

        <!-- ConfigProps 配置属性 -->
        <el-tab-pane name="configprops" :label="t('actuator.configProps')">
          <el-card class="tab-card" v-loading="loading.configprops">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.configPropsTitle') }}</span>
                <div class="header-actions">
                  <el-input
                    v-model="configPropsSearch"
                    :placeholder="t('actuator.searchPrefix')"
                    size="small"
                    clearable
                    @input="filterConfigProps"
                    class="search-input"
                  />
                  <el-button type="primary" size="small" @click="loadConfigProps">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>
            <el-table :data="paginatedConfigPropsData" style="width: 100%" height="500">
              <el-table-column prop="prefix" :label="t('actuator.prefix')" width="200" sortable />
              <el-table-column prop="name" :label="t('actuator.beanName')" min-width="250" show-overflow-tooltip />
              <el-table-column :label="t('actuator.value')" min-width="400">
                <template #default="scope">
                  <el-popover placement="left" :title="t('actuator.propDetail')" :width="400" trigger="hover">
                    <template #reference>
                      <span class="property-preview">{{ JSON.stringify(scope.row.properties).substring(0, 100) }}...</span>
                    </template>
                    <pre class="json-content">{{ JSON.stringify(scope.row.properties, null, 2) }}</pre>
                  </el-popover>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="configPropsPagination.currentPage"
              v-model:page-size="configPropsPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :small="true"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredConfigPropsData.length"
              @size-change="handleConfigPropsPageSizeChange"
              @current-change="handleConfigPropsCurrentPageChange"
              class="logger-pagination"
            />
          </el-card>
        </el-tab-pane>

        <!-- Caches 缓存 -->
        <el-tab-pane name="caches" :label="t('actuator.cacheMonitor')">
          <el-card class="tab-card" v-loading="loading.caches">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.appCacheInfo') }}</span>
                <div class="header-actions">
                  <el-input
                    v-model="cachesSearch"
                    :placeholder="t('actuator.searchCaches')"
                    size="small"
                    clearable
                    @input="filterCaches"
                    class="search-input"
                  />
                  <el-button type="primary" size="small" @click="loadCaches">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>
            <el-table :data="paginatedCachesData" style="width: 100%" height="500">
              <el-table-column prop="manager" :label="t('actuator.cacheManager')" min-width="200" sortable />
              <el-table-column prop="name" :label="t('actuator.cacheName')" min-width="200" sortable />
              <el-table-column prop="target" :label="t('actuator.cacheTarget')" min-width="300" show-overflow-tooltip />
            </el-table>
            <el-pagination
              v-model:current-page="cachesPagination.currentPage"
              v-model:page-size="cachesPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :small="true"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredCachesData.length"
              @size-change="handleCachesPageSizeChange"
              @current-change="handleCachesCurrentPageChange"
              class="logger-pagination"
            />
          </el-card>
        </el-tab-pane>

        <!-- Scheduled Tasks 定时任务 -->
        <el-tab-pane name="scheduledtasks" :label="t('actuator.scheduledTasks')">
          <el-card class="tab-card" v-loading="loading.scheduledtasks">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.appScheduledTasks') }}</span>
                <el-button type="primary" size="small" @click="loadScheduledTasks">{{ t('actuator.refresh') }}</el-button>
              </div>
            </template>

            <div v-if="scheduledTasksData.cron.length > 0">
              <h4 class="section-title">{{ t('actuator.cronTasks') }}</h4>
              <el-table :data="scheduledTasksData.cron" style="width: 100%" border size="small">
                <el-table-column prop="runnable.target" :label="t('actuator.targetTask')" min-width="250" show-overflow-tooltip />
                <el-table-column prop="expression" :label="t('actuator.expression')" width="150" />
                <el-table-column :label="t('actuator.nextExecutionTime')" width="200">
                  <template #default="scope">
                    {{ scope.row.nextExecution?.time ? new Date(scope.row.nextExecution.time).toLocaleString() : '-' }}
                  </template>
                </el-table-column>
                <el-table-column :label="t('actuator.lastExecutionStatus')" width="150">
                  <template #default="scope">
                    <el-tag v-if="scope.row.lastExecution" :type="scope.row.lastExecution.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
                      {{ scope.row.lastExecution.status }}
                    </el-tag>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div v-if="scheduledTasksData.fixedRate.length > 0" style="margin-top: 20px;">
              <h4 class="section-title">{{ t('actuator.fixedRateTasks') }}</h4>
              <el-table :data="scheduledTasksData.fixedRate" style="width: 100%" border size="small">
                <el-table-column prop="runnable.target" :label="t('actuator.targetTask')" min-width="250" show-overflow-tooltip />
                <el-table-column prop="interval" :label="t('actuator.intervalMs')" width="120" />
                <el-table-column prop="initialDelay" :label="t('actuator.initialDelayMs')" width="120" />
                <el-table-column :label="t('actuator.nextExecutionTime')" width="200">
                  <template #default="scope">
                    {{ scope.row.nextExecution?.time ? new Date(scope.row.nextExecution.time).toLocaleString() : '-' }}
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div v-if="scheduledTasksData.fixedDelay.length > 0" style="margin-top: 20px;">
              <h4 class="section-title">{{ t('actuator.fixedDelayTasks') }}</h4>
              <el-table :data="scheduledTasksData.fixedDelay" style="width: 100%" border size="small">
                <el-table-column prop="runnable.target" :label="t('actuator.targetTask')" min-width="250" show-overflow-tooltip />
                <el-table-column prop="interval" :label="t('actuator.intervalMs')" width="120" />
                <el-table-column prop="initialDelay" :label="t('actuator.initialDelayMs')" width="120" />
                <el-table-column :label="t('actuator.nextExecutionTime')" width="200">
                  <template #default="scope">
                    {{ scope.row.nextExecution?.time ? new Date(scope.row.nextExecution.time).toLocaleString() : '-' }}
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div v-if="scheduledTasksData.custom.length > 0" style="margin-top: 20px;">
              <h4 class="section-title">{{ t('actuator.customTriggerTasks') }}</h4>
              <el-table :data="scheduledTasksData.custom" style="width: 100%" border size="small">
                <el-table-column prop="runnable.target" :label="t('actuator.targetTask')" min-width="250" show-overflow-tooltip />
                <el-table-column prop="trigger" :label="t('actuator.trigger')" min-width="200" show-overflow-tooltip />
              </el-table>
            </div>

            <el-empty
              v-if="scheduledTasksData.cron.length === 0 && scheduledTasksData.fixedRate.length === 0 && scheduledTasksData.fixedDelay.length === 0 && scheduledTasksData.custom.length === 0"
              :description="t('actuator.noScheduledTasks')"
            />
          </el-card>
        </el-tab-pane>

        <!-- Conditions 条件评估报告 -->
        <el-tab-pane name="conditions" :label="t('actuator.conditions')">
          <el-card class="tab-card" v-loading="loading.conditions">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.autoConfigReport') }}</span>
                <div class="header-actions">
                  <el-input
                    v-model="conditionsSearch"
                    :placeholder="t('actuator.searchConfigClasses')"
                    size="small"
                    clearable
                    @input="filterConditions"
                    class="search-input"
                  />
                  <el-button type="primary" size="small" @click="loadConditions">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>

            <el-tabs type="card">
              <el-tab-pane :label="t('actuator.positiveMatches')">
                <el-table :data="paginatedPositiveConditions" style="width: 100%" size="small">
                  <el-table-column type="expand">
                    <template #default="scope">
                      <div style="padding: 10px 20px;">
                        <div v-for="(match, idx) in scope.row.matches" :key="idx" style="margin-bottom: 10px;">
                          <el-tag type="success" size="small">{{ match.condition }}</el-tag>
                          <p style="margin: 5px 0; color: #666;">{{ match.message }}</p>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="name" :label="t('actuator.configClassBean')" min-width="400" sortable />
                </el-table>
                <el-pagination
                  v-model:current-page="conditionsPagination.positiveCurrentPage"
                  v-model:page-size="conditionsPagination.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  :small="true"
                  :background="true"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="filteredConditionsData.positiveMatches.length"
                  @size-change="handleConditionsPageSizeChange"
                  @current-change="handlePositiveConditionsPageChange"
                  class="logger-pagination"
                />
              </el-tab-pane>

              <el-tab-pane :label="t('actuator.negativeMatches')">
                <el-table :data="paginatedNegativeConditions" style="width: 100%" size="small">
                  <el-table-column type="expand">
                    <template #default="scope">
                      <div style="padding: 10px 20px;">
                        <h5 v-if="scope.row.notMatched.length > 0">{{ t('actuator.notMatchedConditions') }}</h5>
                        <div v-for="(miss, idx) in scope.row.notMatched" :key="'miss-'+idx" style="margin-bottom: 10px;">
                          <el-tag type="danger" size="small">{{ miss.condition }}</el-tag>
                          <p style="margin: 5px 0; color: #666;">{{ miss.message }}</p>
                        </div>
                        <h5 v-if="scope.row.matched.length > 0" style="margin-top: 15px;">{{ t('actuator.matchedConditions') }}</h5>
                        <div v-for="(match, idx) in scope.row.matched" :key="'match-'+idx" style="margin-bottom: 10px;">
                          <el-tag type="info" size="small">{{ match.condition }}</el-tag>
                          <p style="margin: 5px 0; color: #666;">{{ match.message }}</p>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="name" :label="t('actuator.configClassBean')" min-width="400" sortable />
                </el-table>
                <el-pagination
                  v-model:current-page="conditionsPagination.negativeCurrentPage"
                  v-model:page-size="conditionsPagination.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  :small="true"
                  :background="true"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="filteredConditionsData.negativeMatches.length"
                  @size-change="handleConditionsPageSizeChange"
                  @current-change="handleNegativeConditionsPageChange"
                  class="logger-pagination"
                />
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-tab-pane>

        <!-- Startup 启动分析 -->
        <el-tab-pane name="startup" :label="t('actuator.startupAnalysis')">
          <el-card class="tab-card" v-loading="loading.startup">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.startupSteps') }}</span>
                <div class="header-actions">
                  <el-input
                    v-model="startupSearch"
                    :placeholder="t('actuator.searchStepsOrTags')"
                    size="small"
                    clearable
                    @input="filterStartup"
                    class="search-input"
                  />
                  <el-button type="primary" size="small" @click="loadStartup">{{ t('actuator.refresh') }}</el-button>
                </div>
              </div>
            </template>

            <div v-if="startupData" class="startup-overview" style="margin-bottom: 20px;">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item :label="t('actuator.springBootVersion')">{{ startupData.springBootVersion }}</el-descriptions-item>
                <el-descriptions-item :label="t('actuator.startupStartTime')">{{ new Date(startupData.timeline.startTime).toLocaleString() }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <el-table :data="paginatedStartupEvents" style="width: 100%" size="small">
              <el-table-column type="expand">
                <template #default="scope">
                  <div style="padding: 10px 20px;">
                    <h5>{{ t('actuator.detailInfo') }}</h5>
                    <el-descriptions :column="1" size="small" border>
                      <el-descriptions-item :label="t('actuator.stepId')">{{ scope.row.startupStep.id }}</el-descriptions-item>
                      <el-descriptions-item :label="t('actuator.parentStepId')" v-if="scope.row.startupStep.parentId">{{ scope.row.startupStep.parentId }}</el-descriptions-item>
                      <el-descriptions-item :label="t('actuator.startTimeLabel')">{{ new Date(scope.row.startTime).toISOString() }}</el-descriptions-item>
                      <el-descriptions-item :label="t('actuator.endTime')">{{ new Date(scope.row.endTime).toISOString() }}</el-descriptions-item>
                    </el-descriptions>

                    <h5 style="margin-top: 15px;">{{ t('actuator.tags') }}</h5>
                    <el-table :data="scope.row.startupStep.tags" size="small" border>
                      <el-table-column prop="key" :label="t('actuator.key')" width="200" />
                      <el-table-column prop="value" :label="t('actuator.value')" />
                    </el-table>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="startupStep.name" :label="t('actuator.stepName')" min-width="300" sortable />
              <el-table-column prop="duration" :label="t('actuator.duration')" width="150" sortable>
                <template #default="scope">
                  {{ formatDuration(scope.row.duration) }}
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              v-model:current-page="startupPagination.currentPage"
              v-model:page-size="startupPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :small="true"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredStartupEvents.length"
              @size-change="handleStartupPageSizeChange"
              @current-change="handleStartupCurrentPageChange"
              class="logger-pagination"
            />
          </el-card>
        </el-tab-pane>

        <!-- SBOM 软件物料清单 -->
        <el-tab-pane name="sbom" label="SBOM">
          <el-card class="tab-card" v-loading="sbomLoading.list">
            <template #header>
              <div class="card-header">
                <span>{{ t('actuator.sbomTitle') }}</span>
                <el-button type="primary" size="small" @click="loadSbom">{{ t('actuator.refresh') }}</el-button>
              </div>
            </template>

            <div v-if="sbomIds.length > 0" class="sbom-list">
              <el-alert
                :title="t('actuator.sbomDesc')"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 20px;"
              >
                <template #title>
                  <span>{{ t('actuator.sbomDesc') }}</span>
                </template>
              </el-alert>

              <el-row :gutter="20">
                <el-col :xs="24" :sm="12" :md="8" v-for="id in sbomIds" :key="id">
                  <div class="sbom-card">
                    <div class="sbom-icon">
                      <el-icon :size="40" color="#409eff"><Document /></el-icon>
                    </div>
                    <div class="sbom-info">
                      <h3>{{ id }}</h3>
                      <p>{{ t('actuator.sbomFormat') }}</p>
                    </div>
                    <div class="sbom-actions">
                      <el-button type="primary" size="small" @click="downloadSbom(id)">
                        {{ t('actuator.downloadSbom') }}
                      </el-button>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <el-empty v-else :description="t('actuator.noSbom')" :image-size="80" />
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
    <el-dialog
      v-model="threadDetailVisible"
      :title="t('actuator.threadStackDetail')"
      width="70%"
      destroy-on-close
    >
      <div v-if="selectedThread" class="thread-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="t('actuator.threadId')">{{ selectedThread.id }}</el-descriptions-item>
          <el-descriptions-item :label="t('actuator.threadName')">{{ selectedThread.name }}</el-descriptions-item>
          <el-descriptions-item :label="t('actuator.threadState')">
            <el-tag :type="getThreadStateType(selectedThread.state)">{{ selectedThread.state }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('actuator.cpuTime')">{{ selectedThread.cpuTime }}</el-descriptions-item>
          <el-descriptions-item :label="t('actuator.blockedTime')">{{ selectedThread.blockedTime }}</el-descriptions-item>
          <el-descriptions-item :label="t('actuator.isDaemon')">{{ selectedThread.daemon ? t('actuator.yes') : t('actuator.no') }}</el-descriptions-item>
          <el-descriptions-item :label="t('actuator.priority')">{{ selectedThread.priority }}</el-descriptions-item>
          <el-descriptions-item :label="t('actuator.threadGroup')">{{ selectedThread.threadGroupName }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 20px;">{{ t('actuator.lockInfo') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="t('actuator.lockObject')" v-if="selectedThread.lockInfo">
            {{ selectedThread.lockInfo.className }}@{{ selectedThread.lockInfo.identityHashCode }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('actuator.lockOwner')" v-if="selectedThread.lockOwnerName">
            {{ selectedThread.lockOwnerName }} #{{ selectedThread.lockOwnerId }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('actuator.isLockWait')">{{ selectedThread.lockedSynchronizers && selectedThread.lockedSynchronizers.length > 0 ? t('actuator.yes') : t('actuator.no') }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin-top: 20px;">{{ t('actuator.stackTrace') }}</h4>
        <div class="stack-trace-container">
          <div
            v-for="(trace, index) in selectedThread.stackTrace"
            :key="index"
            class="stack-trace-item"
          >
            <div class="stack-line-number">{{ trace.lineNumber }}</div>
            <div class="stack-content">
              <div class="stack-class">{{ trace.className }}.{{ trace.methodName }}</div>
              <div class="stack-file">({{ trace.fileName }})</div>
            </div>
          </div>
          <div v-if="!selectedThread.stackTrace || selectedThread.stackTrace.length === 0" class="no-stack-trace">
            {{ t('actuator.noStackTrace') }}
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="threadDetailVisible = false">{{ t('actuator.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  health, info, env, metrics, customMetrics, threaddump, loggers, updateLoggerLevel, logfile, heapdump, restart, shutdown,
  mappings, configprops, beans, caches, scheduledtasks, conditions, startup, sbom, getSbomById
} from '@/api/actuator'
import { Document, Coin } from '@element-plus/icons-vue'

const { t } = useI18n()

// 状态管理
const activeTab = ref('health')
const envActiveTab = ref('systemProperties')
const envSearch = ref('')
const threadDetailVisible = ref(false)
const selectedThread = ref(null)

// 搜索过滤相关状态
const mappingsSearch = ref('')
const configPropsSearch = ref('')
const beansSearch = ref('')
const cachesSearch = ref('')
const conditionsSearch = ref('')

const filteredMappingsData = ref([])
const filteredConfigPropsData = ref([])
const filteredBeansData = ref([])
const filteredCachesData = ref([])
const filteredConditionsData = ref({
  positiveMatches: [],
  negativeMatches: []
})

const mappingsPagination = reactive({ currentPage: 1, pageSize: 20 })
const configPropsPagination = reactive({ currentPage: 1, pageSize: 20 })
const beansPagination = reactive({ currentPage: 1, pageSize: 20 })
const cachesPagination = reactive({ currentPage: 1, pageSize: 20 })
const conditionsPagination = reactive({
  positiveCurrentPage: 1,
  negativeCurrentPage: 1,
  pageSize: 20
})

// 日志记录器相关状态
const loggerSearch = ref('')
const filteredLoggersData = ref([])
const loggerPagination = reactive({
  currentPage: 1,
  pageSize: 20
})

// 加载状态
const loading = reactive({
  health: false,
  env: false,
  loggers: false,
  threads: false,
  info: false,
  logfile: false,
  heapdump: false,
  metrics: false,
  mappings: false,
  configprops: false,
  beans: false,
  caches: false,
  scheduledtasks: false,
  conditions: false,
  startup: false,
  sbom: false
})

// 数据存储
const healthData = ref({})
const appInfo = ref({})
const appInfoData = ref(null)
const systemProps = ref([])
const envVars = ref([])
const configProps = ref([])
const loggersData = ref([])
const threadsData = ref([])
const mappingsData = ref([])
const configPropsData = ref([])
const beansData = ref([])
const cachesData = ref([])
const scheduledTasksData = ref({
  cron: [],
  fixedDelay: [],
  fixedRate: [],
  custom: []
})
const conditionsData = ref({
  positiveMatches: [],
  negativeMatches: []
})
const startupData = ref(null)
const startupSearch = ref('')
const filteredStartupEvents = ref([])
const startupPagination = reactive({
  currentPage: 1,
  pageSize: 20
})

const sbomIds = ref([])
const sbomDetail = ref(null)
const sbomLoading = reactive({ list: false, detail: false })
const startTime = ref(null) // 添加启动时间记录
const elapsedSeconds = ref(0) // 添加已过去的秒数
// 添加JVM内存指标数据
const jvmMemoryMetrics = ref(null)
// 添加CPU指标数据
const cpuMetrics = ref(null)
// 添加Metrics相关数据
const metricsData = ref([])
const filteredMetricsData = ref([])
const metricsSearch = ref('')
const metricsPagination = reactive({
  currentPage: 1,
  pageSize: 20
})
// 添加指标分类相关数据
const activeMetricsCategory = ref('all')
// 添加指标详情相关数据
const metricDetailDialogVisible = ref(false)
const selectedMetric = ref(null)
let timer = null // 添加定时器
let metricsTimer = null // 添加内存指标定时器

// 计算后的数据
const filteredSystemProps = ref([])
const filteredEnvVars = ref([])
const filteredConfigProps = ref([])

// 计算分页后的日志记录器数据
const paginatedLoggersData = computed(() => {
  const start = (loggerPagination.currentPage - 1) * loggerPagination.pageSize
  const end = start + loggerPagination.pageSize
  return filteredLoggersData.value.slice(start, end)
})

// 计算分页后的Metrics数据
const paginatedMetricsData = computed(() => {
  const start = (metricsPagination.currentPage - 1) * metricsPagination.pageSize
  const end = start + metricsPagination.pageSize
  return filteredMetricsData.value.slice(start, end)
})

const paginatedMappingsData = computed(() => {
  const start = (mappingsPagination.currentPage - 1) * mappingsPagination.pageSize
  const end = start + mappingsPagination.pageSize
  return filteredMappingsData.value.slice(start, end)
})

const paginatedConfigPropsData = computed(() => {
  const start = (configPropsPagination.currentPage - 1) * configPropsPagination.pageSize
  const end = start + configPropsPagination.pageSize
  return filteredConfigPropsData.value.slice(start, end)
})

const paginatedBeansData = computed(() => {
  const start = (beansPagination.currentPage - 1) * beansPagination.pageSize
  const end = start + beansPagination.pageSize
  return filteredBeansData.value.slice(start, end)
})

const paginatedCachesData = computed(() => {
  const start = (cachesPagination.currentPage - 1) * cachesPagination.pageSize
  const end = start + cachesPagination.pageSize
  return filteredCachesData.value.slice(start, end)
})

const paginatedPositiveConditions = computed(() => {
  const start = (conditionsPagination.positiveCurrentPage - 1) * conditionsPagination.pageSize
  const end = start + conditionsPagination.pageSize
  return filteredConditionsData.value.positiveMatches.slice(start, end)
})

const paginatedNegativeConditions = computed(() => {
  const start = (conditionsPagination.negativeCurrentPage - 1) * conditionsPagination.pageSize
  const end = start + conditionsPagination.pageSize
  return filteredConditionsData.value.negativeMatches.slice(start, end)
})

const paginatedStartupEvents = computed(() => {
  const start = (startupPagination.currentPage - 1) * startupPagination.pageSize
  const end = start + startupPagination.pageSize
  return filteredStartupEvents.value.slice(start, end)
})

// 计算属性
const healthStatus = computed(() => {
  const status = healthData.value.status
  if (status === 'UP') return { text: t('actuator.statusUp'), class: 'status-up' }
  if (status === 'DOWN') return { text: t('actuator.statusDown'), class: 'status-down' }
  if (status === 'OUT_OF_SERVICE') return { text: t('actuator.statusOutOfService'), class: 'status-warn' }
  return { text: t('actuator.statusUnknown'), class: 'status-unknown' }
})

const memoryUsage = computed(() => {
  if (!appInfo.value.mem) return '-'
  const { used, max } = appInfo.value.mem
  return used && max ? `${(used / 1024 / 1024).toFixed(1)}M / ${(max / 1024 / 1024).toFixed(1)}M` : '-'
})

// 添加运行时长计算属性，基于累加的秒数
const uptime = computed(() => {
  if (!startTime.value) return '-'
  const seconds = elapsedSeconds.value
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainingSeconds = seconds % 60
  return `${hours}${t('actuator.hours')}${minutes}${t('actuator.minutes')}${remainingSeconds}${t('actuator.seconds')}`
})

// 生命周期
onMounted(() => {
  loadHealth()
  loadInfo()
  loadAppInfo()
  // 加载JVM内存和CPU指标
  loadJvmMemoryMetrics()
  loadCpuMetrics()
  // 加载线程数据
  loadThreads()

  // 定期更新指标
  metricsTimer = setInterval(() => {
    if (activeTab.value === 'health' || activeTab.value === 'info' || activeTab.value === 'metrics') {
      loadJvmMemoryMetrics()
      loadCpuMetrics()
    }
  }, 30000)
})

// 页面卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
  if (metricsTimer) {
    clearInterval(metricsTimer)
  }
})

// 方法定义
const loadHealth = async () => {
  loading.health = true
  try {
    const res = await health()
    healthData.value = res
  } catch (err) {
    ElMessage.error(t('actuator.fetchHealthFailed', { message: err.message }))
  } finally {
    loading.health = false
  }
}

const loadMappings = async () => {
  loading.mappings = true
  try {
    const res = await mappings()
    const allMappings = []
    if (res.contexts) {
      Object.keys(res.contexts).forEach(contextId => {
        const dispatcherServlets = res.contexts[contextId].mappings.dispatcherServlets || {}
        Object.keys(dispatcherServlets).forEach(servletName => {
          dispatcherServlets[servletName].forEach(mapping => {
            allMappings.push({
              handler: mapping.handler,
              predicate: mapping.predicate,
              details: mapping.details
            })
          })
        })
      })
    }
    mappingsData.value = allMappings
    filterMappings()
  } catch (err) {
    ElMessage.error(t('actuator.fetchMappingsFailed', { message: err.message }))
  } finally {
    loading.mappings = false
  }
}

const loadConfigProps = async () => {
  loading.configprops = true
  try {
    const res = await configprops()
    const allProps = []
    if (res.contexts) {
      Object.keys(res.contexts).forEach(contextId => {
        const beans = res.contexts[contextId].beans || {}
        Object.keys(beans).forEach(beanName => {
          allProps.push({
            name: beanName,
            prefix: beans[beanName].prefix,
            properties: beans[beanName].properties
          })
        })
      })
    }
    configPropsData.value = allProps
    filterConfigProps()
  } catch (err) {
    ElMessage.error(t('actuator.fetchConfigPropsFailed', { message: err.message }))
  } finally {
    loading.configprops = false
  }
}

const loadBeans = async () => {
  loading.beans = true
  try {
    const res = await beans()
    const allBeans = []
    if (res.contexts) {
      Object.keys(res.contexts).forEach(contextId => {
        const beans = res.contexts[contextId].beans || {}
        Object.keys(beans).forEach(beanName => {
          allBeans.push({
            name: beanName,
            aliases: beans[beanName].aliases,
            scope: beans[beanName].scope,
            type: beans[beanName].type,
            resource: beans[beanName].resource,
            dependencies: beans[beanName].dependencies
          })
        })
      })
    }
    beansData.value = allBeans
    filterBeans()
  } catch (err) {
    ElMessage.error(t('actuator.fetchBeansFailed', { message: err.message }))
  } finally {
    loading.beans = false
  }
}

const loadCaches = async () => {
  loading.caches = true
  try {
    const res = await caches()
    const allCaches = []
    if (res.cacheManagers) {
      Object.keys(res.cacheManagers).forEach(managerName => {
        const caches = res.cacheManagers[managerName].caches || {}
        Object.keys(caches).forEach(cacheName => {
          allCaches.push({
            manager: managerName,
            name: cacheName,
            target: caches[cacheName].target
          })
        })
      })
    }
    cachesData.value = allCaches
    filterCaches()
  } catch (err) {
    ElMessage.error(t('actuator.fetchCachesFailed', { message: err.message }))
  } finally {
    loading.caches = false
  }
}

const loadScheduledTasks = async () => {
  loading.scheduledtasks = true
  try {
    const res = await scheduledtasks()
    scheduledTasksData.value = {
      cron: res.cron || [],
      fixedDelay: res.fixedDelay || [],
      fixedRate: res.fixedRate || [],
      custom: res.custom || []
    }
  } catch (err) {
    ElMessage.error(t('actuator.fetchScheduledTasksFailed', { message: err.message }))
  } finally {
    loading.scheduledtasks = false
  }
}

const loadConditions = async () => {
  loading.conditions = true
  try {
    const res = await conditions()
    const positiveMatches = []
    const negativeMatches = []

    if (res.contexts) {
      Object.keys(res.contexts).forEach(contextId => {
        const context = res.contexts[contextId]
        if (context.positiveMatches) {
          Object.entries(context.positiveMatches).forEach(([name, matches]) => {
            positiveMatches.push({ name, matches })
          })
        }
        if (context.negativeMatches) {
          Object.entries(context.negativeMatches).forEach(([name, details]) => {
            negativeMatches.push({
              name,
              notMatched: details.notMatched || [],
              matched: details.matched || []
            })
          })
        }
      })
    }
    conditionsData.value = { positiveMatches, negativeMatches }
    filterConditions()
  } catch (err) {
    ElMessage.error(t('actuator.fetchConditionsFailed', { message: err.message }))
  } finally {
    loading.conditions = false
  }
}

const loadStartup = async () => {
  loading.startup = true
  try {
    const res = await startup()
    startupData.value = res
    filterStartup()
  } catch (err) {
    ElMessage.error(t('actuator.fetchStartupFailed', { message: err.message }))
  } finally {
    loading.startup = false
  }
}

const loadSbom = async () => {
  sbomLoading.list = true
  try {
    const res = await sbom()
    sbomIds.value = res.ids || []
  } catch (err) {
    ElMessage.error(t('actuator.fetchSbomFailed', { message: err.message }))
  } finally {
    sbomLoading.list = false
  }
}

const downloadSbom = async (id) => {
  sbomLoading.detail = true
  try {
    const response = await getSbomById(id)
    const blob = new Blob([response.data || response], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `sbom-${id}-${new Date().toISOString().slice(0, 10)}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success(t('actuator.sbomDownloadSuccess'))
  } catch (err) {
    ElMessage.error(t('actuator.sbomDownloadFailed', { message: err.message || t('actuator.statusUnknown') }))
  } finally {
    sbomLoading.detail = false
  }
}

const filterStartup = () => {
  if (!startupData.value?.timeline?.events) {
    filteredStartupEvents.value = []
    return
  }

  if (!startupSearch.value) {
    filteredStartupEvents.value = startupData.value.timeline.events
  } else {
    const search = startupSearch.value.toLowerCase()
    filteredStartupEvents.value = startupData.value.timeline.events.filter(event =>
      event.startupStep.name.toLowerCase().includes(search) ||
      event.startupStep.tags.some(tag =>
        tag.key.toLowerCase().includes(search) ||
        tag.value.toString().toLowerCase().includes(search)
      )
    )
  }
  startupPagination.currentPage = 1
}

const loadInfo = async () => {
  loading.info = true
  try {
    const res = await info()

    // 从 /actuator/info 映射嵌套结构到扁平字段
    const mapped = {
      springBootVersion: res?.build?.version || '-',
      javaVersion: res?.java?.version || res?.java?.runtime || '-',
      osName: res?.os?.name || '-',
      osArch: res?.os?.arch || '-',
      startTime: res?.startTime || '-'
    }

    appInfoData.value = mapped

    // 根据启动时间计算运行时长
    if (res?.startTime) {
      const start = new Date(res.startTime).getTime()
      startTime.value = start
      elapsedSeconds.value = Math.floor((Date.now() - start) / 1000)
      if (timer) clearInterval(timer)
      timer = setInterval(() => {
        elapsedSeconds.value = Math.floor((Date.now() - start) / 1000)
      }, 1000)
    }

    // 加载JVM内存和CPU指标
    await Promise.all([loadJvmMemoryMetrics(), loadCpuMetrics()])
  } catch (err) {
    ElMessage.error(t('actuator.fetchAppInfoFailed', { message: err.message }))
  } finally {
    loading.info = false
  }
}

const loadAppInfo = async () => {
  try {
    // 获取内存信息
    const memRes = await metrics('jvm.memory.used')
    const maxMemRes = await metrics('jvm.memory.max')

    appInfo.value.mem = {
      used: memRes.measurements?.[0]?.value || 0,
      max: maxMemRes.measurements?.[0]?.value || 0
    }

    // 获取版本信息
    const infoRes = await info()
    appInfo.value.version = infoRes?.app?.version || '-'
  } catch (err) {
    console.error(t('actuator.fetchAppInfoFailed', { message: '' }), err)
  }
}

// 添加加载JVM内存指标的方法
const loadJvmMemoryMetrics = async () => {
  try {
    // 获取基本内存指标
    const [maxRes, usedRes] = await Promise.all([
      metrics('jvm.memory.max'),
      metrics('jvm.memory.used')
    ])

    // 获取堆内存指标
    const [heapUsedRes, heapCommittedRes, heapMaxRes] = await Promise.all([
      metrics('jvm.memory.used', {tag: 'area:heap'}),
      metrics('jvm.memory.committed', {tag: 'area:heap'}),
      metrics('jvm.memory.max', {tag: 'area:heap'})
    ])

    // 获取非堆内存指标
    const [nonHeapUsedRes, nonHeapCommittedRes, nonHeapMaxRes] = await Promise.all([
      metrics('jvm.memory.used', {tag: 'area:nonheap'}),
      metrics('jvm.memory.committed', {tag: 'area:nonheap'}),
      metrics('jvm.memory.max', {tag: 'area:nonheap'})
    ])

    jvmMemoryMetrics.value = {
      max: maxRes,
      used: usedRes,
      heap: {
        used: heapUsedRes,
        committed: heapCommittedRes,
        max: heapMaxRes
      },
      nonheap: {
        used: nonHeapUsedRes,
        committed: nonHeapCommittedRes,
        max: nonHeapMaxRes
      }
    }
  } catch (err) {
    console.error(t('actuator.fetchJvmMemoryFailed'), err)
  }
}

// 计算内存使用百分比

// 加载CPU指标
const loadCpuMetrics = async () => {
  try {
    const [cpuCountRes, processCpuRes, systemCpuRes, loadAvgRes] = await Promise.all([
      metrics('system.cpu.count').catch(() => null),
      metrics('process.cpu.usage').catch(() => null),
      metrics('system.cpu.usage').catch(() => null),
      metrics('system.load.average').catch(() => null)
    ])

    cpuMetrics.value = {
      cpuCount: cpuCountRes?.measurements?.[0]?.value || '-',
      processCpuUsage: processCpuRes?.measurements?.[0]?.value ?? null,
      systemCpuUsage: systemCpuRes?.measurements?.[0]?.value ?? null,
      systemLoadAverage: loadAvgRes?.measurements?.[0]?.value ?? -1
    }
  } catch (err) {
    console.error(t('actuator.fetchCpuFailed'), err)
  }
}
const calculateMemoryUsagePercentage = () => {
  if (!jvmMemoryMetrics.value) return 0
  const max = jvmMemoryMetrics.value.max?.measurements?.[0]?.value || 0
  const used = jvmMemoryMetrics.value.used?.measurements?.[0]?.value || 0
  if (max === 0) return 0
  return Math.round((used / max) * 100)
}

// 计算堆内存使用百分比
const calculateHeapMemoryUsagePercentage = () => {
  if (!jvmMemoryMetrics.value?.heap) return 0
  const max = jvmMemoryMetrics.value.heap.max?.measurements?.[0]?.value || 0
  const used = jvmMemoryMetrics.value.heap.used?.measurements?.[0]?.value || 0
  if (max === 0) return 0
  return Math.round((used / max) * 100)
}

// 计算非堆内存使用百分比
const calculateNonHeapMemoryUsagePercentage = () => {
  if (!jvmMemoryMetrics.value?.nonheap) return 0
  const max = jvmMemoryMetrics.value.nonheap.max?.measurements?.[0]?.value || 0
  const used = jvmMemoryMetrics.value.nonheap.used?.measurements?.[0]?.value || 0
  if (max === 0) return 0
  return Math.round((used / max) * 100)
}

const formatNanoTime = (nanos) => {
  if (!nanos) return '-'
  const ms = nanos / 1000000
  return `${ms.toFixed(2)}ms`
}

const formatBytes = (bytes) => {
  if (!bytes) return '-'
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  if (bytes === 0) return '0 B'
  const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const formatDuration = (duration) => {
  if (!duration) return '-'
  // 处理 ISO-8601 持续时间格式，例如 PT0.007342597S
  const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:\.\d+)?)S)?/)
  if (!match) return duration

  const hours = parseFloat(match[1] || 0)
  const minutes = parseFloat(match[2] || 0)
  const seconds = parseFloat(match[3] || 0)

  if (hours > 0) return `${hours.toFixed(2)}h`
  if (minutes > 0) return `${minutes.toFixed(2)}m`
  if (seconds >= 1) return `${seconds.toFixed(3)}s`
  return `${(seconds * 1000).toFixed(2)}ms`
}

const loadEnv = async () => {
  loading.env = true
  try {
    const res = await env()
    const data = res

    // 根据实际返回的数据结构处理数据
    // 从 propertySources 中提取 systemProperties 和 systemEnvironment
    if (data?.propertySources && Array.isArray(data.propertySources)) {
      // 查找 systemProperties
      const systemPropertiesSource = data.propertySources.find(source => source.name === 'systemProperties')
      if (systemPropertiesSource?.properties) {
        systemProps.value = Object.entries(systemPropertiesSource.properties).map(([key, value]) => ({
          key,
          value: String(value.value)
        }))
      } else {
        systemProps.value = []
      }

      // 查找 systemEnvironment
      const systemEnvironmentSource = data.propertySources.find(source => source.name === 'systemEnvironment')
      if (systemEnvironmentSource?.properties) {
        envVars.value = Object.entries(systemEnvironmentSource.properties).map(([key, value]) => ({
          key,
          value: String(value.value)
        }))
      } else {
        envVars.value = []
      }

      // 处理配置属性 - 只显示前几个重要的属性源
      configProps.value = []
      const importantSources = data.propertySources.filter(source =>
        source.name !== 'systemProperties' && source.name !== 'systemEnvironment'
      )

      importantSources.forEach(source => {
        if (source.properties) {
          Object.entries(source.properties).forEach(([key, value]) => {
            configProps.value.push({
              key: `${source.name}.${key}`,
              value: String(value.value)
            })
          })
        }
      })
    } else {
      systemProps.value = []
      envVars.value = []
      configProps.value = []
    }

    // 初始化过滤数据
    filterEnv()
  } catch (err) {
    console.error(t('actuator.fetchEnvFailed', { message: '' }), err)
    ElMessage.error(t('actuator.fetchEnvFailed', { message: err.message || '' }))
    // 发生错误时也清空数据
    systemProps.value = []
    envVars.value = []
    configProps.value = []
    filterEnv()
  } finally {
    loading.env = false
  }
}

const loadLoggers = async () => {
  loading.loggers = true
  try {
    const res = await loggers() // 调用从api导入的loggers函数
    const loggersDataRes = res.loggers || {} // 使用不同的变量名避免冲突

    loggersData.value = Object.entries(loggersDataRes).map(([name, logger]) => ({
      name,
      configuredLevel: logger.configuredLevel,
      effectiveLevel: logger.effectiveLevel,
      newLevel: logger.configuredLevel
    }))

    // 初始化过滤数据
    filterLoggers()
  } catch (err) {
    ElMessage.error(t('actuator.fetchLoggersFailed', { message: err.message }))
  } finally {
    loading.loggers = false
  }
}

const loadThreads = async () => {
  loading.threads = true
  try {
    const res = await threaddump()
    // 添加数据结构检查
    if (res && res.threads && Array.isArray(res.threads)) {
      threadsData.value = res.threads.map(thread => ({
        id: thread.threadId,
        name: thread.threadName,
        state: thread.threadState,
        cpuTime: formatNanoTime(thread.cpuTime),
        blockedTime: thread.blockedTime ? `${thread.blockedTime}ms` : '-',
        daemon: thread.daemon,
        priority: thread.priority,
        threadGroupName: thread.threadGroupName,
        lockInfo: thread.lockInfo,
        lockOwnerName: thread.lockOwnerName,
        lockOwnerId: thread.lockOwnerId,
        lockedSynchronizers: thread.lockedSynchronizers,
        stackTrace: thread.stackTrace
      }))
    } else if (res && Array.isArray(res)) {
      // 处理直接返回线程数组的情况
      threadsData.value = res.map(thread => ({
        id: thread.threadId,
        name: thread.threadName,
        state: thread.threadState,
        cpuTime: formatNanoTime(thread.cpuTime),
        blockedTime: thread.blockedTime ? `${thread.blockedTime}ms` : '-',
        daemon: thread.daemon,
        priority: thread.priority,
        threadGroupName: thread.threadGroupName,
        lockInfo: thread.lockInfo,
        lockOwnerName: thread.lockOwnerName,
        lockOwnerId: thread.lockOwnerId,
        lockedSynchronizers: thread.lockedSynchronizers,
        stackTrace: thread.stackTrace
      }))
    } else {
      // 如果数据结构不符合预期，清空数据
      threadsData.value = []
      console.warn('线程数据格式不符合预期:', res)
    }
  } catch (err) {
    ElMessage.error(t('actuator.fetchThreadsFailed', { message: err.message }))
    // 发生错误时清空数据
    threadsData.value = []
  } finally {
    loading.threads = false
  }
}

const changeLogLevel = async (loggerName, level) => {
  try {
    await updateLoggerLevel(loggerName, level)
    ElMessage.success(t('actuator.logLevelChangeSuccess'))
    // 更新本地数据
    const logger = loggersData.value.find(item => item.name === loggerName)
    if (logger) {
      logger.configuredLevel = level
    }
  } catch (err) {
    ElMessage.error(t('actuator.logLevelChangeFailed', { message: err.message }))
  }
}

const showThreadDetail = (row) => {
  // 确保传递的数据是有效的
  if (row) {
    selectedThread.value = row
    threadDetailVisible.value = true
  }
}

const getLevelType = (level) => {
  const map = {
    'TRACE': 'info',
    'DEBUG': 'primary',
    'INFO': 'success',
    'WARN': 'warning',
    'ERROR': 'danger',
    'OFF': 'info'
  }
  return map[level] || 'info'
}

const getThreadStateType = (state) => {
  const map = {
    'RUNNABLE': 'success',
    'BLOCKED': 'danger',
    'WAITING': 'warning',
    'TIMED_WAITING': 'warning',
    'TERMINATED': 'info'
  }
  return map[state] || 'info'
}

const filterEnv = () => {
  const search = envSearch.value.toLowerCase()

  filteredSystemProps.value = systemProps.value.filter(
    item => item.key.toLowerCase().includes(search) || item.value.toLowerCase().includes(search)
  )

  filteredEnvVars.value = envVars.value.filter(
    item => item.key.toLowerCase().includes(search) || item.value.toLowerCase().includes(search)
  )

  filteredConfigProps.value = configProps.value.filter(
    item => item.key.toLowerCase().includes(search) || item.value.toLowerCase().includes(search)
  )
}

// 过滤日志记录器
const filterLoggers = () => {
  const search = loggerSearch.value.toLowerCase()
  if (!search) {
    filteredLoggersData.value = [...loggersData.value]
  } else {
    filteredLoggersData.value = loggersData.value.filter(logger =>
      logger.name.toLowerCase().includes(search)
    )
  }
  // 重置到第一页
  loggerPagination.currentPage = 1
}

// 处理分页大小变化
const handleLoggerPageSizeChange = (val) => {
  loggerPagination.pageSize = val
  loggerPagination.currentPage = 1
}

// 处理当前页变化
const handleLoggerCurrentPageChange = (val) => {
  loggerPagination.currentPage = val
}

const onTabChange = (tabName) => {
  switch (tabName) {
    case 'env':
      // 确保环境信息数据已加载
      if (systemProps.value.length === 0 && envVars.value.length === 0 && configProps.value.length === 0) {
        loadEnv()
      }
      break
    case 'loggers':
      if (loggersData.value.length === 0) loadLoggers()
      break
    case 'threads':
      if (threadsData.value.length === 0) loadThreads()
      break
    case 'info':
      // 当切换到应用信息标签时，加载JVM内存指标
      loadJvmMemoryMetrics()
      break
    case 'metrics':
      // 当切换到Metrics标签时，加载指标列表
      if (metricsData.value.length === 0) loadMetricsList()
      break
    case 'mappings':
      if (mappingsData.value.length === 0) loadMappings()
      break
    case 'configprops':
      if (configPropsData.value.length === 0) loadConfigProps()
      break
    case 'beans':
      if (beansData.value.length === 0) loadBeans()
      break
    case 'caches':
      if (cachesData.value.length === 0) loadCaches()
      break
    case 'scheduledtasks':
      loadScheduledTasks()
      break
    case 'conditions':
      if (conditionsData.value.positiveMatches.length === 0) loadConditions()
      break
    case 'startup':
      if (!startupData.value) loadStartup()
      break
    case 'sbom':
      if (sbomIds.value.length === 0) loadSbom()
      break
  }
}

const filterMappings = () => {
  if (!mappingsSearch.value) {
    filteredMappingsData.value = mappingsData.value
  } else {
    const search = mappingsSearch.value.toLowerCase()
    filteredMappingsData.value = mappingsData.value.filter(item =>
      (item.handler && item.handler.toLowerCase().includes(search)) ||
      (item.predicate && item.predicate.toLowerCase().includes(search))
    )
  }
  mappingsPagination.currentPage = 1
}

const filterConfigProps = () => {
  if (!configPropsSearch.value) {
    filteredConfigPropsData.value = configPropsData.value
  } else {
    const search = configPropsSearch.value.toLowerCase()
    filteredConfigPropsData.value = configPropsData.value.filter(item =>
      (item.name && item.name.toLowerCase().includes(search)) ||
      (item.prefix && item.prefix.toLowerCase().includes(search))
    )
  }
  configPropsPagination.currentPage = 1
}

const filterBeans = () => {
  if (!beansSearch.value) {
    filteredBeansData.value = beansData.value
  } else {
    const search = beansSearch.value.toLowerCase()
    filteredBeansData.value = beansData.value.filter(item =>
      (item.name && item.name.toLowerCase().includes(search)) ||
      (item.type && item.type.toLowerCase().includes(search))
    )
  }
  beansPagination.currentPage = 1
}

const filterCaches = () => {
  if (!cachesSearch.value) {
    filteredCachesData.value = cachesData.value
  } else {
    const search = cachesSearch.value.toLowerCase()
    filteredCachesData.value = cachesData.value.filter(item =>
      (item.name && item.name.toLowerCase().includes(search)) ||
      (item.manager && item.manager.toLowerCase().includes(search))
    )
  }
  cachesPagination.currentPage = 1
}

const filterConditions = () => {
  if (!conditionsSearch.value) {
    filteredConditionsData.value = { ...conditionsData.value }
  } else {
    const search = conditionsSearch.value.toLowerCase()
    filteredConditionsData.value = {
      positiveMatches: conditionsData.value.positiveMatches.filter(item =>
        item.name.toLowerCase().includes(search)
      ),
      negativeMatches: conditionsData.value.negativeMatches.filter(item =>
        item.name.toLowerCase().includes(search)
      )
    }
  }
  conditionsPagination.positiveCurrentPage = 1
  conditionsPagination.negativeCurrentPage = 1
}

const handleMappingsPageSizeChange = (val) => { mappingsPagination.pageSize = val }
const handleMappingsCurrentPageChange = (val) => { mappingsPagination.currentPage = val }
const handleConfigPropsPageSizeChange = (val) => { configPropsPagination.pageSize = val }
const handleConfigPropsCurrentPageChange = (val) => { configPropsPagination.currentPage = val }
const handleBeansPageSizeChange = (val) => { beansPagination.pageSize = val }
const handleBeansCurrentPageChange = (val) => { beansPagination.currentPage = val }
const handleCachesPageSizeChange = (val) => { cachesPagination.pageSize = val }
const handleCachesCurrentPageChange = (val) => { cachesPagination.currentPage = val }

const handlePositiveConditionsPageChange = (val) => { conditionsPagination.positiveCurrentPage = val }
const handleNegativeConditionsPageChange = (val) => { conditionsPagination.negativeCurrentPage = val }
const handleConditionsPageSizeChange = (val) => {
  conditionsPagination.pageSize = val
  conditionsPagination.positiveCurrentPage = 1
  conditionsPagination.negativeCurrentPage = 1
}

const handleStartupPageSizeChange = (val) => {
  startupPagination.pageSize = val
  startupPagination.currentPage = 1
}
const handleStartupCurrentPageChange = (val) => {
  startupPagination.currentPage = val
}

// 下载日志文件
const downloadLogFile = async () => {
  loading.logfile = true
  try {
    const response = await logfile()
    // 正确处理 Blob 响应
    const blob = new Blob([response.data || response], { type: 'text/plain' })

    // 尝试从响应头获取文件名
    let filename = `application-${new Date().toISOString().slice(0, 10)}.log`
    if (response.headers) {
      const disposition = response.headers['content-disposition']
      if (disposition && disposition.indexOf('attachment') !== -1) {
        const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
        const matches = filenameRegex.exec(disposition)
        if (matches != null && matches[1]) {
          filename = matches[1].replace(/['"]/g, '')
        }
      }
    }

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success(t('actuator.logFileDownloadSuccess'))
  } catch (err) {
    console.error(t('actuator.logFileDownloadFailed', { message: '' }), err)
    ElMessage.error(t('actuator.logFileDownloadFailed', { message: err.message || '' }))
  } finally {
    loading.logfile = false
  }
}

// 导出线程转储
const downloadThreadDump = async () => {
  loading.threads = true
  try {
    const res = await threaddump()

    // 生成线程转储文本内容
    let threadDumpContent = ''
    threadDumpContent += `Thread Dump Report - ${new Date().toLocaleString()}\n`
    threadDumpContent += '='.repeat(80) + '\n\n'

    if (res && res.threads && Array.isArray(res.threads)) {
      res.threads.forEach(thread => {
        threadDumpContent += `"${thread.threadName}" #${thread.threadId} ${thread.threadState}\n`
        if (thread.lockInfo) {
          threadDumpContent += `   Locked on: ${thread.lockInfo.className}@${thread.lockInfo.identityHashCode}\n`
        }
        if (thread.lockOwnerName) {
          threadDumpContent += `   Owned by: ${thread.lockOwnerName}#${thread.lockOwnerId}\n`
        }
        if (thread.stackTrace && Array.isArray(thread.stackTrace)) {
          thread.stackTrace.forEach(trace => {
            threadDumpContent += `        at ${trace.className}.${trace.methodName}(${trace.fileName}:${trace.lineNumber})\n`
          })
        }
        threadDumpContent += '\n'
      })
    } else if (res && Array.isArray(res)) {
      // 处理直接返回线程数组的情况
      res.forEach(thread => {
        threadDumpContent += `"${thread.threadName}" #${thread.threadId} ${thread.threadState}\n`
        if (thread.lockInfo) {
          threadDumpContent += `   Locked on: ${thread.lockInfo.className}@${thread.lockInfo.identityHashCode}\n`
        }
        if (thread.lockOwnerName) {
          threadDumpContent += `   Owned by: ${thread.lockOwnerName}#${thread.lockOwnerId}\n`
        }
        if (thread.stackTrace && Array.isArray(thread.stackTrace)) {
          thread.stackTrace.forEach(trace => {
            threadDumpContent += `        at ${trace.className}.${trace.methodName}(${trace.fileName}:${trace.lineNumber})\n`
          })
        }
        threadDumpContent += '\n'
      })
    }

    // 创建并下载文件
    const blob = new Blob([threadDumpContent], { type: 'text/plain;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `thread-dump-${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success(t('actuator.threadDumpSuccess'))
  } catch (err) {
    ElMessage.error(t('actuator.threadDumpFailed', { message: err.message }))
  } finally {
    loading.threads = false
  }
}

// 下载堆转储文件
const downloadHeapDump = async () => {
  try {
    ElMessageBox.confirm(
      t('actuator.heapDumpConfirm'),
      t('actuator.downloadConfirm'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      }
    ).then(async () => {
      loading.heapdump = true
      try {
        const response = await heapdump()
        // 正确处理 Blob 响应
        const blob = new Blob([response.data || response], { type: 'application/octet-stream' })

        // 尝试从响应头获取文件名
        let filename = `heapdump-${new Date().toISOString().slice(0, 10)}.hprof`
        if (response.headers) {
          const disposition = response.headers['content-disposition']
          if (disposition && disposition.indexOf('attachment') !== -1) {
            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
            const matches = filenameRegex.exec(disposition)
            if (matches != null && matches[1]) {
              filename = matches[1].replace(/['"]/g, '')
            }
          }
        }

        // 创建下载链接
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        ElMessage.success(t('actuator.heapDumpSuccess'))
      } catch (err) {
        console.error(t('actuator.heapDumpFailed', { message: '' }), err)
        ElMessage.error(t('actuator.heapDumpFailed', { message: err.message || '' }))
      } finally {
        loading.heapdump = false
      }
    }).catch(() => {
      // 用户取消下载
    })
  } catch (err) {
    ElMessage.error(t('actuator.operationFailed', { message: err.message || '' }))
  }
}

// 增强版 restart 函数，添加确认对话框和错误处理
const enhancedRestart = async () => {
  try {
    await ElMessageBox.confirm(t('actuator.restartConfirm'), t('actuator.restartConfirmTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })

    const response = await restart()
    ElMessage.success(t('actuator.restartSuccess'))
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('actuator.restartFailed', { message: error.response?.data || error.message }))
    }
  }
}

// 增强版 shutdown 函数，添加确认对话框和错误处理
const enhancedShutdown = async () => {
  try {
    await ElMessageBox.confirm(t('actuator.shutdownConfirm'), t('actuator.shutdownConfirmTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'error'
    })

    const response = await shutdown()
    ElMessage.success(t('actuator.shutdownSuccess'))
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('actuator.shutdownFailed', { message: error.response?.data || error.message }))
    }
  }
}

// 添加加载Metrics列表的方法
const loadMetricsList = async () => {
  loading.metrics = true
  try {
    // 获取所有指标名称列表
    const res = await metrics()
    if (res && res.names && Array.isArray(res.names)) {
      // 为了性能考虑，我们分批获取指标详情
      // 每次获取50个指标的详细信息
      const batchSize = 50
      const allMetrics = []

      for (let i = 0; i < Math.min(res.names.length, 200); i += batchSize) {
        const batch = res.names.slice(i, i + batchSize)
        const metricsDetails = await Promise.all(
          batch.map(name => metrics(name))
        )
        allMetrics.push(...metricsDetails)

        // 更新进度
        metricsData.value = [...allMetrics]
        filterMetrics()
      }

      metricsData.value = allMetrics
      filterMetrics()
    }
  } catch (err) {
    ElMessage.error(t('actuator.fetchMetricsFailed', { message: err.message }))
  } finally {
    loading.metrics = false
  }
}

// 显示指标详情
const showMetricDetail = (row) => {
  selectedMetric.value = row
  metricDetailDialogVisible.value = true
}

// 关闭指标详情对话框
const handleCloseMetricDetail = () => {
  metricDetailDialogVisible.value = false
  selectedMetric.value = null
}

// 指标分类变更处理
const onMetricsCategoryChange = (category) => {
  filterMetrics()
}

// 格式化指标值
const formatMetricValue = (value, unit) => {
  if (value === undefined || value === null) return '-'

  // 根据单位格式化值
  if (unit === 'bytes') {
    return formatBytes(value)
  } else if (unit === 'milliseconds') {
    return `${value.toFixed(2)} ms`
  } else if (unit === 'seconds') {
    return `${value.toFixed(2)} s`
  } else if (Number.isInteger(value)) {
    return value.toLocaleString()
  } else {
    return value.toFixed(2)
  }
}

// 过滤Metrics数据
const filterMetrics = () => {
  let filtered = [...metricsData.value]

  // 根据分类过滤
  const category = activeMetricsCategory.value
  if (category !== 'all') {
    filtered = filtered.filter(metric => {
      const name = metric.name.toLowerCase()
      switch (category) {
        case 'jvm':
          return name.startsWith('jvm.')
        case 'http':
          return name.startsWith('http.')
        case 'database':
          return name.includes('hikari') || name.includes('jdbc')
        case 'threads':
          return name.startsWith('executor.') || name.startsWith('jvm.threads')
        case 'system':
          return name.startsWith('system.') || name.startsWith('process.') || name.startsWith('disk.')
        case 'security':
          return name.startsWith('spring.security')
        case 'other':
          return !name.startsWith('jvm.') &&
                 !name.startsWith('http.') &&
                 !name.includes('hikari') &&
                 !name.includes('jdbc') &&
                 !name.startsWith('executor.') &&
                 !name.startsWith('jvm.threads') &&
                 !name.startsWith('system.') &&
                 !name.startsWith('process.') &&
                 !name.startsWith('disk.') &&
                 !name.startsWith('spring.security')
        default:
          return true
      }
    })
  }

  // 根据搜索关键词过滤
  const search = metricsSearch.value.toLowerCase()
  if (search) {
    filtered = filtered.filter(metric =>
      metric.name.toLowerCase().includes(search)
    )
  }

  filteredMetricsData.value = filtered
  // 重置到第一页
  metricsPagination.currentPage = 1
}

// 处理Metrics分页大小变化
const handleMetricsPageSizeChange = (val) => {
  metricsPagination.pageSize = val
  metricsPagination.currentPage = 1
}

// 处理Metrics当前页变化
const handleMetricsCurrentPageChange = (val) => {
  metricsPagination.currentPage = val
}

</script>

<style scoped>
.actuator-dashboard {
  padding: 16px;
  box-sizing: border-box;
  background-color: var(--bg-secondary);
}

.dashboard-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background: var(--bg-card);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
}

.version-tag {
  font-size: 14px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  border: none;
  box-shadow: var(--shadow-md);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.stat-card-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon.health {
  background-color: rgba(103, 194, 58, 0.12);
  color: #67c23a;
}

.stat-icon.uptime {
  background-color: rgba(64, 158, 255, 0.12);
  color: #409eff;
}

.stat-icon.runtime {
  background-color: rgba(230, 162, 60, 0.12);
  color: #e6a23c;
}

.stat-icon.memory {
  background-color: rgba(245, 108, 108, 0.12);
  color: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-placeholder);
  margin-bottom: 5px;
}
.stat-value {
  font-size: 12px;
  font-weight: bold;
  color: var(--text-primary);
}

.stat-value.status-up {
  color: #67C23A;
}

.stat-value.status-down {
  color: #F56C6C;
}
.stat-value.status-warn {
  color: #e5a142;
}

.stat-value.status-unknown {
  color: var(--text-placeholder);
}

.main-tabs {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.main-tabs :deep(.el-tabs__header) {
  margin: 0;
  background-color: var(--bg-card);
}

.main-tabs :deep(.el-tabs__nav) {
  border-radius: 0;
  border: none;
}

.main-tabs :deep(.el-tabs__item) {
  height: 50px;
  line-height: 50px;
  font-weight: 500;
  color: var(--text-secondary);
}

.main-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.section-title {
  margin: 10px 0;
  font-size: 16px;
  color: var(--text-primary);
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.search-input {
  width: 200px;
}

.tab-card {
  border: none;
  border-radius: 0 0 8px 8px;
  box-shadow: none;
}

.tab-card :deep(.el-card__header) {
  padding: 0;
  border-bottom: 1px solid var(--border-darker);
}

.health-content {
  padding: 20px;
}

.health-summary {
  display: flex;
  align-items: center;
  gap: 15px;
}

.summary-text {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.health-details {
  margin-top: 20px;
}

.component-item {
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 15px;
  background-color: var(--bg-input);
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.component-item:hover {
  box-shadow: var(--shadow-md);
}

.component-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.component-name {
  font-weight: 500;
  color: var(--text-primary);
}

.component-details {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.component-details p {
  margin: 5px 0;
}

.env-tabs {
  border: none;
}

.env-tabs :deep(.el-tabs__header) {
  background-color: var(--bg-secondary);
  margin: 0;
  padding: 0 10px;
}

.env-tabs :deep(.el-tabs__item) {
  height: 45px;
  line-height: 45px;
  font-weight: 500;
}

.env-tabs :deep(.el-tabs__content) {
  padding: 20px 0;
}

.env-table,
.loggers-table,
.threads-table {
  width: 100%;
}

.env-table :deep(.el-table__cell),
.loggers-table :deep(.el-table__cell),
.threads-table :deep(.el-table__cell) {
  background-color: transparent;
}

.info-tabs :deep(.el-tabs__header) {
  background-color: var(--bg-secondary);
  margin: 0;
  padding: 0 10px;
}

.info-tabs :deep(.el-tabs__item) {
  height: 45px;
  line-height: 45px;
  font-weight: 500;
}

.info-tabs :deep(.el-tabs__content) {
  padding: 20px 0;
}

:deep(.el-descriptions__body) {
  background-color: var(--bg-card);
}

.logger-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.pagination-info {
  margin-bottom: 15px;
  color: var(--text-secondary);
  font-size: 14px;
}

.files-content {
  padding: 20px;
}

.file-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 1px solid var(--border-darker);
  border-radius: 8px;
  background-color: var(--bg-input);
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.file-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-base);
}

.file-icon {
  margin-right: 20px;
}

.file-info h3 {
  margin: 0 0 10px 0;
  color: var(--text-primary);
}

.file-info p {
  margin: 0 0 20px 0;
  color: var(--text-secondary);
}

.metrics-container {
  margin-top: 20px;
  padding: 20px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
}

.metric-card {
  padding: 15px;
  background-color: var(--bg-card);
  border-radius: 6px;
  box-shadow: var(--shadow-md);
  margin-bottom: 15px;
}

.metric-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.metric-value {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
}

.metrics-overview {
  margin-bottom: 20px;
}

.metric-overview-card {
  padding: 15px;
  background-color: var(--bg-card);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
  text-align: center;
}

.metric-overview-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.metric-overview-value {
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.metrics-categories {
  margin: 20px 0;
}

.metrics-categories :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.metrics-categories :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.metrics-list-container {
  margin-top: 20px;
}

.metrics-table :deep(.el-table__cell) {
  background-color: transparent;
}

.metrics-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.metric-detail-content {
  padding: 20px 0;
}

.metric-detail-content :deep(.el-descriptions__label) {
  width: 120px !important;
}

.thread-detail-content :deep(.el-descriptions__label) {
  width: 120px !important;
}

.stack-trace-container {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid var(--border-darker);
  border-radius: 4px;
  padding: 10px;
  background-color: var(--bg-input);
}

.stack-trace-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.stack-trace-item:last-child {
  border-bottom: none;
}

.stack-line-number {
  width: 50px;
  text-align: right;
  padding-right: 10px;
  color: var(--text-muted);
  font-family: monospace;
}

.stack-content {
  flex: 1;
}

.stack-class {
  font-weight: bold;
  font-family: monospace;
}

.stack-file {
  color: var(--text-muted);
  font-size: 12px;
  font-family: monospace;
}

.no-stack-trace {
  text-align: center;
  color: var(--text-muted);
  padding: 20px;
}

.memory-summary {
  margin: 20px 0;
  padding: 15px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
}

.thread-stats {
  margin-bottom: 20px;
}

.stat-card-simple {
  padding: 15px;
  background-color: var(--bg-card);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
  text-align: center;
}

.stat-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}



.memory-summary-card {
  padding: 15px;
  background-color: var(--bg-card);
  border-radius: 6px;
  box-shadow: var(--shadow-md);
}

.memory-summary-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.memory-summary-value {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 8px;
  text-align: center;
}

.memory-usage-text {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .stats-row {
    margin-bottom: 10px;
  }

  .stat-card {
    margin-bottom: 10px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }
}

.property-preview {
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.json-content {
  background-color: #2d2d2d;
  color: #ccc;
  padding: 15px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  overflow: auto;
  max-height: 400px;
}

.sbom-list {
  padding: 10px 0;
}

.sbom-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 20px;
  border: 1px solid var(--border-darker);
  border-radius: 8px;
  background-color: var(--bg-input);
  transition: all 0.3s ease;
  margin-bottom: 20px;
  text-align: center;
}

.sbom-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-3px);
}

.sbom-icon {
  margin-bottom: 15px;
}

.sbom-info h3 {
  margin: 0 0 10px 0;
  color: var(--text-primary);
}

.sbom-info p {
  margin: 0 0 20px 0;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
