<template>
  <div class="credentials-page">
    <div class="page-header">
      <div class="page-title">{{ $t('security.clientCredentials') }}</div>
      <el-button type="primary" @click="openCreateDialog">{{ $t('security.addCredential') }}</el-button>
    </div>

    <el-card shadow="never">
      <div class="filter-container">
        <el-input
          v-model="searchQuery"
          :placeholder="$t('security.searchPlaceholderCredential')"
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

      <el-table v-if="loading || filteredCredentials.length > 0" :data="filteredCredentials" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="clientId" :label="$t('security.clientId')" min-width="150" />
        <el-table-column prop="username" :label="$t('security.username')" min-width="120" />
        <el-table-column :label="$t('security.enabled')" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="(val) => handleToggle(row, val)" />
          </template>
        </el-table-column>
        <el-table-column prop="remark" :label="$t('security.remark')" min-width="180" show-overflow-tooltip />
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
      <el-empty v-if="!loading && filteredCredentials.length === 0" :description="$t('common.noData')" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('security.editCredential') : $t('security.addCredential')" width="720px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('security.clientId')" prop="clientId">
              <el-input v-model="form.clientId" :placeholder="$t('security.placeholderClientId')" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('security.username')" prop="username">
              <el-input v-model="form.username" :placeholder="$t('security.placeholderUsername')" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('security.password')" :prop="isEdit ? '' : 'password'">
              <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空表示不修改密码' : $t('security.placeholderPassword')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('security.enabled')" prop="enabled">
              <el-switch v-model="form.enabled" />
            </el-form-item>
          </el-col>
        </el-row>

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
import { getCredentials, createCredential, updateCredential, deleteCredential, toggleCredential } from '@/api/security'

export default {
  name: 'ClientCredentials',
  data() {
    return {
      credentials: [],
      filteredCredentials: [],
      searchQuery: '',
      loading: false,
      dialogVisible: false,
      isEdit: false,
      saving: false,
      editId: null,
      form: this.getDefaultForm(),
      rules: {
        clientId: [{ required: true, message: this.$t('security.placeholderClientId'), trigger: 'blur' }],
        username: [{ required: true, message: this.$t('security.placeholderUsername'), trigger: 'blur' }],
        password: [{ required: true, message: this.$t('security.placeholderPassword'), trigger: 'blur' }]
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
        const data = await getCredentials()
        this.credentials = data || []
        this.handleFilter()
      } catch (e) {
        console.error('Failed to fetch credentials', e)
        this.$message.error(this.$t('common.loading') + ' ' + this.$t('common.failed'))
      } finally {
        this.loading = false
      }
    },
    handleFilter() {
      const q = this.searchQuery.trim().toLowerCase()
      if (!q) {
        this.filteredCredentials = this.credentials
        return
      }
      this.filteredCredentials = this.credentials.filter(c =>
        (c.clientId && c.clientId.toLowerCase().includes(q)) ||
        (c.username && c.username.toLowerCase().includes(q))
      )
    },
    getDefaultForm() {
      return {
        clientId: '',
        username: '',
        password: '',
        enabled: true,
        remark: ''
      }
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
      this.form = {
        ...row,
        password: '' // 密码留空
      }
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
          await updateCredential(this.editId, this.form)
        } else {
          await createCredential(this.form)
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
    async handleToggle(row, enable) {
      try {
        await toggleCredential(row.id, enable)
        this.$message.success(this.$t('security.msgSaveSuccess'))
      } catch (e) {
        row.enabled = !enable
        console.error('Toggle failed', e)
      }
    },
    async handleDelete(row) {
      try {
        await deleteCredential(row.id)
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
.credentials-page {
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
