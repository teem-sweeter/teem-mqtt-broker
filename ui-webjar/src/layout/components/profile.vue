<template>
  <div class="profile">
    <el-dropdown
      placement="bottom-end"
      class="h-p-100 flex-center"
      popper-class="user-el-dropdown"
      @visible-change="handleDropdownChange"
    >
      <div class="user-info-wrapper flex-center">
        <div class="user-avatar">
          <span class="avatar-text">{{ avatarText }}</span>
        </div>
        <div class="user-meta">
          <div class="user-nickname">{{ userInfo.nickname || userInfo.username || t('profile.notLoggedIn') }}</div>
          <div class="user-role" v-if="userInfo.role">{{ userInfo.role }}</div>
        </div>
        <el-icon size="14" class="dropdown-arrow" :class="{ 'is-active': dropdownActive }">
          <ArrowDown />
        </el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu class="profile-dropdown-menu">
          <div class="dropdown-header">
            <div class="header-avatar">
              <span class="avatar-text-lg">{{ avatarText }}</span>
            </div>
            <div class="header-info">
              <div class="header-nickname">{{ userInfo.nickname || userInfo.username || t('profile.notLoggedIn') }}</div>
              <div class="header-username">{{ userInfo.username || '' }}</div>
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <el-dropdown-item @click="passWordDialogVisible = true" class="dropdown-item" v-if="false">
            <div class="item-icon">
              <el-icon><Edit /></el-icon>
            </div>
            <div class="item-content">
              <span class="item-label">{{ t('profile.changePassword') }}</span>
              <span class="item-desc">{{ t('profile.changePasswordDesc') }}</span>
            </div>
          </el-dropdown-item>
          <el-dropdown-item @click="loginOut" class="dropdown-item logout-item">
            <div class="item-icon logout-icon">
              <el-icon><SwitchButton /></el-icon>
            </div>
            <div class="item-content">
              <span class="item-label">{{ t('profile.logout') }}</span>
              <span class="item-desc">{{ t('profile.logoutDesc') }}</span>
            </div>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <el-dialog
      v-model="passWordDialogVisible"
      :title="t('profile.changePassword')"
      width="480"
      align-center
      center
      class="password-dialog"
      :close-on-click-modal="false"
    >
      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        status-icon
        :rules="rules"
        label-width="auto"
        class="password-form"
        size="large"
      >
        <el-form-item :label="t('profile.oldPassword')" prop="password">
          <el-input
            v-model="ruleForm.password"
            type="password"
            autocomplete="off"
            show-password
            :placeholder="t('profile.inputOldPassword')"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item :label="t('profile.newPassword')" prop="newPassword">
          <el-input
            v-model="ruleForm.newPassword"
            type="password"
            autocomplete="off"
            show-password
            :placeholder="t('profile.inputNewPassword')"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item :label="t('profile.confirmPassword')" prop="checkPassword">
          <el-input
            v-model="ruleForm.checkPassword"
            type="password"
            autocomplete="off"
            show-password
            :placeholder="t('profile.inputConfirmPassword')"
            :prefix-icon="Lock"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passWordDialogVisible = false" size="large">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitForm()" size="large" :loading="submitting">
            {{ t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import router from "@/router"
import {onMounted, ref, computed} from 'vue'
import {changePassword} from '@/api/user'
import {ElMessage} from 'element-plus'
import { User, ArrowDown, Edit, SwitchButton, Lock } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const passWordDialogVisible = ref(false)
const ruleFormRef = ref()
const ruleForm = ref({
  password: '',
  newPassword: '',
  checkPassword:''
})

const userInfo = ref({
  username: '',
  nickname: '',
  token: '',
})

const dropdownActive = ref(false)
const submitting = ref(false)
const avatarUrl = ref('')

const avatarText = computed(() => {
  const name = userInfo.value.nickname || userInfo.value.username || ''
  return name ? name.slice(0, 1).toUpperCase() : '?'
})


const rules = computed(() => ({
  newPassword: [
    { required: true, message: t('profile.inputNewPassword'), trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$/,
      message: t('profile.passwordRule'),
      trigger: "blur"
    }
  ],
  checkPassword: [
    { required: true, message: t('profile.inputConfirmPassword'), trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== ruleForm.value.newPassword) {
          callback(new Error(t('profile.passwordMismatch')));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ],
  password: [
    { required: true, message: t('profile.inputOldPassword'), trigger: 'blur' }
  ],
}))


const submitForm = async () => {
  try {
    const valid = await ruleFormRef.value.validate();
    if (valid) {
      submitting.value = true;
      await changePassword(ruleForm.value);
      ElMessage.success(t('profile.passwordChangeSuccess'));
      passWordDialogVisible.value = false;
      ruleForm.value = {
        password: '',
        newPassword: '',
        checkPassword: ''
      };
    }
  } catch (error) {
    console.error(t('profile.passwordChangeFailed') + ':', error);
  } finally {
    submitting.value = false;
  }
}

const handleDropdownChange = (visible) => {
  dropdownActive.value = visible;
}

// 退出登录
const loginOut = () => {
  ElMessage.success(t('profile.logoutSuccess'));
  setTimeout(() => {
    sessionStorage.clear();
    localStorage.removeItem("user");
    router.replace('/login');
  }, 500);
}

onMounted(() => {
  let u = localStorage.getItem("user");
  if (u) {
    userInfo.value = JSON.parse(u);
  }
})
</script>

<style scoped>
.user-info-wrapper {
  padding: 5px 12px;
  border-radius: 10px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  cursor: pointer;
  gap: 8px;
  display: flex;
  align-items: center;
}

.user-info-wrapper:hover {
  background: var(--bg-secondary);
  border-color: var(--border-light);
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-text {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1;
}

.avatar-text-lg {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: cover;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.user-nickname {
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.user-role {
  color: var(--text-muted);
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.2;
}

.dropdown-arrow {
  color: var(--text-muted);
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.dropdown-arrow.is-active {
  transform: rotate(180deg);
  color: var(--color-primary);
}

/* 下拉菜单样式 */
.profile-dropdown-menu {
  box-shadow: 0 10px 40px rgba(15, 23, 42, 0.12);
  background: var(--dropdown-bg) !important;
  padding: 8px !important;
  min-width: 220px;
  border-radius: 16px !important;
  border: 1px solid var(--border-base) !important;
  --el-dropdown-menuItem-hover-color: var(--color-primary);
  --el-dropdown-menuItem-hover-fill: var(--color-primary-bg);
}

.dropdown-header {
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.header-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--bg-active);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.header-info {
  flex: 1;
  overflow: hidden;
}

.header-nickname {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.header-username {
  font-size: 12px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-light);
  margin: 8px 0;
}

.dropdown-item {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 10px !important;
  margin: 2px 0;
  height: auto !important;
  line-height: normal !important;
}

.dropdown-item:hover {
  background: var(--color-primary-bg) !important;
}

.item-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--color-primary-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.25s ease;
}

.item-icon .el-icon {
  font-size: 16px;
  color: var(--color-primary);
  transition: transform 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.dropdown-item:hover .item-icon {
  background: var(--color-primary);
}

.dropdown-item:hover .item-icon .el-icon {
  color: #fff;
  transform: scale(1.1);
}

.item-content {
  margin-left: 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.item-desc {
  font-size: 12px;
  color: var(--text-placeholder);
}

.dropdown-item:hover .item-label {
  color: var(--color-primary-dark);
}

.logout-icon {
  background: var(--danger-bg);
}

.logout-icon .el-icon {
  color: var(--danger-color);
}

.logout-item:hover .logout-icon {
  background: var(--danger-color);
}

.logout-item:hover .logout-icon .el-icon {
  color: #fff;
}

.logout-item:hover .item-label {
  color: var(--danger-hover);
}

.logout-item .item-desc {
  color: var(--text-placeholder);
}

/* 密码对话框样式 */
.password-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.password-dialog :deep(.el-dialog__header) {
  background: var(--color-primary-gradient);
  margin: 0;
  padding: 20px;
  color: #fff;
}

.password-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 600;
}

.password-dialog :deep(.el-dialog__body) {
  padding: 24px;
  background: var(--dialog-header-bg);
}

.password-form {
  margin-top: 8px;
}

.password-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--text-regular);
  margin-bottom: 8px;
}

.password-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid var(--border-darker);
  transition: all 0.3s ease;
  box-shadow: none;
}

.password-form :deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-hover);
}

.password-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-shadow);
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--border-base);
}

.dialog-footer .el-button {
  border-radius: 6px;
  font-weight: 500;
  padding: 10px 20px;
}
</style>

<style>
.user-el-dropdown{
  border: none !important;
  background: transparent!important;
  >.el-popper__arrow:before{
    background: var(--color-primary)!important;
  }
  border-radius: 20px !important;
  .el-scrollbar {
    .el-scrollbar__wrap {
      border-radius: 8px !important;
    }
  }
}
</style>
