<template>
  <div class="acl-rules-page">
    <div class="page-header">
      <div class="page-title">{{ $t('security.aclRules') }}</div>
      <el-button type="primary" @click="openCreateDialog">{{ $t('security.addAclRule') }}</el-button>
    </div>

    <el-card shadow="never">
      <div class="filter-container">
        <el-input
          v-model="searchQuery"
          :placeholder="$t('security.searchPlaceholderAcl')"
          style="width: 300px;"
          clearable
          @clear="handleFilter"
          @keyup.enter="handleFilter"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">{{ $t('common.search') }}</el-button>
      </div>

      <el-table v-if="loading || filteredRules.length > 0" :data="filteredRules" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="priority" :label="$t('security.priority')" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clientId" :label="$t('security.clientId')" min-width="120">
          <template #default="{ row }">
            <span>{{ row.clientId || '*' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" :label="$t('security.username')" min-width="100">
          <template #default="{ row }">
            <span>{{ row.username || '*' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="topic" :label="$t('security.topic')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="permission" :label="$t('security.permission')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getPermissionTagType(row.permission)">
              {{ formatPermission(row.permission) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" :label="$t('security.action')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.action === 'ALLOW' ? 'success' : 'danger'">
              {{ row.action === 'ALLOW' ? $t('security.allow') : $t('security.deny') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" :label="$t('security.remark')" min-width="150" show-overflow-tooltip />
        <el-table-column :label="$t('common.operation')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">{{ $t('common.edit') }}</el-button>
            <el-popconfirm :title="$t('security.msgConfirmDelete')" @confirm="handleDelete(row)">
              <template #reference>
                <el-button size="small" type="danger">{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && filteredRules.length === 0" :description="$t('common.noData')" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('security.editAclRule') : $t('security.addAclRule')" width="720px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('security.clientId')" prop="clientId">
              <el-input v-model="form.clientId" placeholder="* 或指定 ClientID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('security.username')" prop="username">
              <el-input v-model="form.username" placeholder="* 或指定用户名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="$t('security.topic')" prop="topic">
          <el-input v-model="form.topic" :placeholder="$t('security.placeholderTopic')" />
          <span class="form-hint" style="display: block; font-size: 12px; color: var(--el-text-color-secondary); margin-top: 4px;">
            {{ $t('security.tipAclTopic') }}
          </span>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('security.permission')" prop="permission">
              <el-select v-model="form.permission" style="width: 100%;">
                <el-option :label="$t('security.pubSub')" value="PUB_SUB" />
                <el-option :label="$t('security.pub')" value="PUB" />
                <el-option :label="$t('security.sub')" value="SUB" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('security.action')" prop="action">
              <el-radio-group v-model="form.action">
                <el-radio label="ALLOW">{{ $t('security.allow') }}</el-radio>
                <el-radio label="DENY">{{ $t('security.deny') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="$t('security.priority')" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="1000" style="width: 150px;" />
          <span class="form-hint" style="margin-left: 10px; font-size: 12px; color: var(--el-text-color-secondary);">
            {{ $t('security.placeholderPriority') }}
          </span>
        </el-form-item>
        <el-form-item :label="$t('security.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="" maxlength="500" show-word-limit />
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
import { getAclRules, createAclRule, updateAclRule, deleteAclRule } from '@/api/security'

export default {
  name: 'AclRules',
  data() {
    return {
      rulesList: [],
      filteredRules: [],
      searchQuery: '',
      loading: false,
      dialogVisible: false,
      isEdit: false,
      saving: false,
      editId: null,
      form: this.getDefaultForm(),
      rules: {
        topic: [{ required: true, message: this.$t('security.placeholderTopic'), trigger: 'blur' }],
        permission: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
        action: [{ required: true, message: '请选择动作类型', trigger: 'change' }]
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
        const data = await getAclRules()
        this.rulesList = data || []
        this.handleFilter()
      } catch (e) {
        console.error('Failed to fetch ACL rules', e)
        this.$message.error(this.$t('common.loading') + ' ' + this.$t('common.failed'))
      } finally {
        this.loading = false
      }
    },
    handleFilter() {
      const q = this.searchQuery.trim().toLowerCase()
      if (!q) {
        this.filteredRules = this.rulesList
        return
      }
      this.filteredRules = this.rulesList.filter(r =>
        (r.clientId && r.clientId.toLowerCase().includes(q)) ||
        (r.username && r.username.toLowerCase().includes(q)) ||
        (r.topic && r.topic.toLowerCase().includes(q))
      )
    },
    getDefaultForm() {
      return {
        clientId: '',
        username: '',
        topic: '',
        permission: 'PUB_SUB',
        action: 'ALLOW',
        priority: 0,
        remark: ''
      }
    },
    getPermissionTagType(perm) {
      if (perm === 'PUB') return 'warning'
      if (perm === 'SUB') return 'success'
      return 'primary'
    },
    formatPermission(permission) {
      if (permission === 'PUB_SUB') return this.$t('security.pubSub')
      if (permission === 'PUB') return this.$t('security.pub')
      if (permission === 'SUB') return this.$t('security.sub')
      return permission
    },
    openCreateDialog() {
      this.isEdit = false
      this.editId = null
      this.form = this.getDefaultForm()
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate()
        }
      })
    },
    openEditDialog(row) {
      this.isEdit = true
      this.editId = row.id
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate()
        }
      })
    },
    async handleSave() {
      const valid = await this.$refs.formRef.validate().catch(() => false)
      if (!valid) return
      this.saving = true
      try {
        if (this.isEdit) {
          await updateAclRule(this.editId, this.form)
        } else {
          await createAclRule(this.form)
        }
        this.$message.success(this.$t('security.msgSaveSuccess'))
        this.dialogVisible = false
        await this.fetchData()
      } catch (e) {
        console.error('Save failed', e)
      } finally {
        this.saving = false
      }
    },
    async handleDelete(row) {
      try {
        await deleteAclRule(row.id)
        this.$message.success(this.$t('security.msgDeleteSuccess'))
        await this.fetchData()
      } catch (e) {
        console.error('Delete failed', e)
      }
    }
  }
}
</script>

<style scoped>
.acl-rules-page {
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
.filter-container {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}
</style>
