<template>
  <div class="webhook-page">
    <div class="page-header">
      <div class="page-title">{{ $t('webhook.title') }}</div>
      <el-button type="primary" @click="openCreateDialog">{{ $t('webhook.create') }}</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="webhooks" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="name" :label="$t('webhook.name')" min-width="140" />
        <el-table-column prop="url" :label="$t('webhook.url')" min-width="240" show-overflow-tooltip />
        <el-table-column prop="topicFilter" :label="$t('webhook.topicFilter')" width="160" />
        <el-table-column prop="qos" label="QoS" width="70" />
        <el-table-column :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled" @change="(val) => handleToggle(row, val)" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">{{ $t('common.edit') }}</el-button>
            <el-popconfirm :title="$t('webhook.deleteConfirm')" @confirm="handleDelete(row)">
              <template #reference>
                <el-button size="small" type="danger">{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && webhooks.length === 0" :description="$t('webhook.noWebhooks')" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('webhook.edit') : $t('webhook.create')" width="640px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('webhook.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('webhook.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('webhook.url')" prop="url">
          <el-input v-model="form.url" placeholder="https://example.com/api/webhook" />
        </el-form-item>
        <el-form-item :label="$t('webhook.topicFilter')" prop="topicFilter">
          <el-input v-model="form.topicFilter" placeholder="sensor/#" />
          <span class="form-hint">{{ $t('webhook.topicFilterHint') }}</span>
        </el-form-item>
        <el-form-item :label="$t('webhook.minQoS')" prop="qos">
          <el-select v-model="form.qos" style="width:120px">
            <el-option :label="`QoS 0`" :value="0" />
            <el-option :label="`QoS 1`" :value="1" />
            <el-option :label="`QoS 2`" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('webhook.contentType')" prop="contentType">
          <el-input v-model="form.contentType" placeholder="application/json" />
        </el-form-item>
        <el-form-item :label="$t('webhook.headers')" prop="headers">
          <el-input v-model="form.headers" type="textarea" :rows="3" :placeholder="$t('webhook.headersPlaceholder')" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('webhook.retryCount')" prop="retryCount">
              <el-input-number v-model="form.retryCount" :min="0" :max="10" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('webhook.retryInterval')" prop="retryInterval">
              <el-input-number v-model="form.retryInterval" :min="1" :max="60" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('webhook.remark')" prop="remark">
          <el-input v-model="form.remark" :placeholder="$t('webhook.remarkPlaceholder')" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getWebhooks, createWebhook, updateWebhook, deleteWebhook, toggleWebhook } from '@/api/webhook'

export default {
  name: 'WebHook',
  data() {
    return {
      webhooks: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      saving: false,
      editId: null,
      form: this.getDefaultForm(),
      rules: {
        name: [{ required: true, message: this.$t('webhook.nameRequired'), trigger: 'blur' }],
        url: [
          { required: true, message: this.$t('webhook.urlRequired'), trigger: 'blur' },
          { type: 'url', message: this.$t('webhook.urlInvalid'), trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        this.webhooks = await getWebhooks()
      } catch (e) {
        console.error('Failed to fetch webhooks', e)
      } finally {
        this.loading = false
      }
    },
    getDefaultForm() {
      return {
        name: '',
        url: '',
        topicFilter: '#',
        qos: 0,
        contentType: 'application/json',
        headers: '',
        retryCount: 0,
        retryInterval: 5,
        connectTimeout: 10,
        readTimeout: 30,
        enabled: true,
        remark: ''
      }
    },
    openCreateDialog() {
      this.isEdit = false
      this.editId = null
      this.form = this.getDefaultForm()
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.isEdit = true
      this.editId = row.id
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSave() {
      const valid = await this.$refs.formRef.validate().catch(() => false)
      if (!valid) return
      this.saving = true
      try {
        if (this.isEdit) {
          await updateWebhook(this.editId, this.form)
        } else {
          await createWebhook(this.form)
        }
        this.dialogVisible = false
        await this.fetchData()
      } catch (e) {
        console.error('Save failed', e)
      } finally {
        this.saving = false
      }
    },
    async handleToggle(row, enable) {
      try {
        await toggleWebhook(row.id, enable)
      } catch (e) {
        row.enabled = !enable
        console.error('Toggle failed', e)
      }
    },
    async handleDelete(row) {
      try {
        await deleteWebhook(row.id)
        await this.fetchData()
      } catch (e) {
        console.error('Delete failed', e)
      }
    }
  }
}
</script>

<style scoped>
.webhook-page {
  padding: 16px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
}
.form-hint {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
