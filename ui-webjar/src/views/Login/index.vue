<template>
  <div class="login-container">
    <!-- 背景动画层 - 模拟摄像头扫描效果 -->
    <div class="bg-animation">
      <div class="scan-line"></div>
      <div class="grid-overlay"></div>
      <div class="particles"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="login-wrapper">
      <div class="login-card">
        <!-- Logo区域 -->
        <div class="logo-section">
          <div class="tech-logo">
            <div class="logo-ring"></div>
            <svg class="mqtt-icon" viewBox="0 0 48 48" fill="none">
              <circle cx="24" cy="24" r="5" fill="var(--color-primary)"/>
              <circle cx="24" cy="24" r="10" stroke="var(--color-primary)" stroke-width="1.5" opacity="0.6"/>
              <circle cx="24" cy="24" r="16" stroke="var(--color-primary)" stroke-width="1" opacity="0.3"/>
              <line x1="9" y1="24" x2="17" y2="24" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" opacity="0.7"/>
              <line x1="24" y1="9" x2="24" y2="17" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" opacity="0.7"/>
              <line x1="39" y1="24" x2="31" y2="24" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" opacity="0.7"/>
              <line x1="24" y1="39" x2="24" y2="31" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" opacity="0.7"/>
              <circle cx="9" cy="24" r="2.5" fill="var(--color-primary)" opacity="0.8"/>
              <circle cx="24" cy="9" r="2.5" fill="var(--color-primary)" opacity="0.8"/>
              <circle cx="39" cy="24" r="2.5" fill="var(--color-primary)" opacity="0.8"/>
              <circle cx="24" cy="39" r="2.5" fill="var(--color-primary)" opacity="0.8"/>
            </svg>
          </div>
          <h1 class="app-title">MQTT-BROKER</h1>
          <p class="subtitle">{{ t('login.subtitle') }}</p>
        </div>

        <!-- 登录表单 -->
        <div class="form-section">
          <el-form :model="form" :rules="rules" ref="ruleForm" class="tech-form">
            <el-form-item prop="username" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </div>
                <el-input
                  v-model="form.username"
                  :placeholder="t('login.username')"
                  class="tech-input"
                  :input-style="{ background: 'transparent' }"
                />
                <div class="input-border"></div>
              </div>
            </el-form-item>

            <el-form-item prop="password" class="form-item">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <circle cx="12" cy="16" r="1"></circle>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                  </svg>
                </div>
                <el-input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  :placeholder="t('login.password')"
                  class="tech-input"
                  :input-style="{ background: 'transparent' }"
                />
                <div class="input-border"></div>
                <div class="password-toggle" @click="togglePasswordVisibility">
                  <svg v-if="showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                  <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                </div>
              </div>
            </el-form-item>

            <el-form-item class="remember-section">
              <label class="remember-label">
                <el-checkbox v-model="remember" class="tech-checkbox">
                </el-checkbox>
                <span class="checkbox-text">{{ t('login.rememberPassword') }}</span>
              </label>
            </el-form-item>

            <el-form-item>
              <el-button
                class="login-btn"
                type="primary"
                @click="onSubmit"
                :loading="loginLoading"
              >
                <span class="btn-text">{{ loginLoading ? t('login.verifying') : t('login.login') }}</span>
                <div class="btn-glare"></div>
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 科技感装饰元素 -->
        <div class="tech-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="scan-beam"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import router from "@/router";
import { ref, reactive, onMounted, nextTick } from "vue";
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
import { getDynamicRoutes } from "@/router/menus.js";
import { getAuthRouters } from "@/router/authRouter.js";
import { useAuthRouterStore } from "@/stores/authRouter.js";
import { login } from "@/api/login.js";
import { ElNotification } from "element-plus";

const authRouterStore = useAuthRouterStore();
const ruleForm = ref(null);
const form = reactive({
  username: "",
  password: "",
});

const remember = ref(false);
const showPassword = ref(false);
const loginLoading = ref(false);

const rules = {
  username: [
    { required: true, message: t('login.enterUsername'), trigger: "blur" },
  ],
  password: [
    { required: true, message: t('login.enterPassword'), trigger: "blur" },
  ],
};

// 密码显示切换
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

// 记住密码逻辑（仅记住用户名，不存储明文密码）
onMounted(() => {
  const savedUsername = localStorage.getItem("rememberedUsername");
  if (savedUsername) {
    form.username = savedUsername;
    remember.value = true;
  }
});

const onSubmit = () => {
  ruleForm.value.validate(async (valid) => {
    if (valid) {
      try {
        loginLoading.value = true;
        const response = await login(form);
        if (response && response.token) {
          sessionStorage.token = response.token;
          //缓存用户信息
          localStorage.setItem("user", JSON.stringify({ ...response, username: form.username }));

          setTimeout(async () => {
            const dynamicRoutes = await getDynamicRoutes();
            const newRoutes = getAuthRouters(dynamicRoutes);
            authRouterStore.addRouterList(newRoutes);
            newRoutes.forEach((val) => router.addRoute(val));
            await nextTick();
            router.push("/dashboard");
          }, 100);

          // 处理记住密码的存储（仅记住用户名）
          if (remember.value) {
            localStorage.setItem("rememberedUsername", form.username);
          } else {
            localStorage.removeItem("rememberedUsername");
          }
        } else {
          ElNotification({
            title: t('login.error'),
            message: t('login.invalidCredentials'),
            type: "error",
          });
        }
      } catch (error) {
        ElNotification({
          title: t('login.error'),
          message: t('login.loginFailed'),
          type: "error",
        });
      } finally {
        loginLoading.value = false;
      }
    }
  });
};
</script>

<style scoped lang="scss">
/* ============ 1. 容器与背景（与注册页完全统一） ============ */
.login-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: var(--bg-secondary);
  background-image:
    radial-gradient(ellipse at 20% 20%, rgba(0, 113, 227, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(189, 90, 242, 0.08) 0%, transparent 50%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro SC', 'SF Pro Text',
  'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.bg-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  background:
    radial-gradient(ellipse at 20% 30%, rgba(0, 113, 227, 0.12) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 70%, rgba(189, 90, 242, 0.1) 0%, transparent 60%),
    radial-gradient(ellipse at 50% 50%, rgba(90, 200, 250, 0.08) 0%, transparent 70%);
  animation: bgFloat 15s ease-in-out infinite;
  pointer-events: none;
}

@keyframes bgFloat {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 1; }
  25% { transform: translate(20px, -20px) scale(1.05); opacity: 0.95; }
  50% { transform: translate(-15px, 25px) scale(0.95); opacity: 0.9; }
  75% { transform: translate(15px, 15px) scale(1.02); opacity: 0.98; }
}

/* ============ 2. 背景动效层 ============ */
.scan-line {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border: 1px solid rgba(0, 113, 227, 0.15);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: ripple 6s ease-out infinite;
  pointer-events: none;
}

@keyframes ripple {
  0% { width: 0; height: 0; opacity: 0.6; border-width: 2px; }
  50% { width: 120%; height: 120%; opacity: 0.2; border-width: 1px; }
  100% { width: 200%; height: 200%; opacity: 0; border-width: 0.5px; }
}

.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(0, 113, 227, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 113, 227, 0.04) 1px, transparent 1px);
  background-size: 60px 60px;
  animation: gridPulse 8s ease-in-out infinite;
  pointer-events: none;
}

@keyframes gridPulse {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 0.7; }
}

.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.particles::before,
.particles::after {
  content: '';
  position: absolute;
  width: 3px;
  height: 3px;
  background: rgba(0, 113, 227, 0.5);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(0, 113, 227, 0.3);
  animation: float 10s infinite ease-in-out;
}

.particles::before { left: 15%; animation-delay: 0s; }
.particles::after { left: 85%; animation-delay: 5s; }

@keyframes float {
  0%, 100% { transform: translateY(100vh) translateX(0); opacity: 0; }
  15% { opacity: 0.7; }
  85% { opacity: 0.7; }
  100% { transform: translateY(-100vh) translateX(60px); opacity: 0; }
}

/* ============ 3. 包装器与毛玻璃卡片 ============ */
.login-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 16px;
}

.login-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(24px);
  -webkit-backdrop-filter: saturate(180%) blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 28px 28px;
  width: 100%;
  max-width: 400px;
  position: relative;
  z-index: 1;
  overflow: visible;
  box-shadow:
    0 24px 64px rgba(0, 0, 0, 0.1),
    0 8px 24px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  animation: cardAppear 0.7s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes cardAppear {
  from { opacity: 0; transform: translateY(30px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* ============ 4. Logo与标题 ============ */
.logo-section {
  text-align: center;
  margin-bottom: 28px;
  position: relative;
}

.tech-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: var(--bg-card);
  border-radius: 50%;
  margin-bottom: 16px;
  position: relative;
  box-shadow:
    0 8px 32px var(--shadow-md),
    0 0 0 6px var(--bg-secondary),
    0 0 0 8px var(--border-light);
  animation: logoFloat 4s ease-in-out infinite;
}

.logo-ring {
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  border: 1.5px dashed var(--color-primary);
  opacity: 0.35;
  animation: ringRotate 12s linear infinite;
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

@keyframes ringRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.mqtt-icon {
  width: 36px;
  height: 36px;
}

.app-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

.subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
  font-weight: 500;
}

/* ============ 5. 表单与输入框 ============ */
.form-section { position: relative; }
.tech-form { width: 100%; }
.form-item { margin-bottom: 20px; }

.input-wrapper {
  position: relative;
  height: 48px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  display: flex;
  align-items: center;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid var(--border-light);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
  width: 100%;
}

.input-wrapper:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: var(--color-primary);
  box-shadow: 0 4px 12px var(--color-primary-hover);
  transform: translateY(-1px);
}

.input-wrapper:focus-within {
  background: var(--bg-card);
  border-color: var(--color-primary);
  box-shadow:
    0 0 0 4px var(--color-primary-hover),
    0 4px 16px var(--color-primary-hover);
  transform: translateY(-2px);
}

.input-icon {
  width: 48px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  transition: color 0.2s ease;
  flex-shrink: 0;
}

.input-wrapper:focus-within .input-icon { color: var(--color-primary); }

.tech-input {
  flex: 1;
  height: 100%;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 400;
  padding: 0 8px;
  min-width: 0;
}

.tech-input :deep(.el-input__inner) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  color: var(--text-primary) !important;
  height: 100% !important;
  padding: 0 !important;
  font-size: 15px !important;
  font-weight: 400 !important;
  width: 100% !important;
}

.tech-input :deep(.el-input__inner::placeholder) { color: var(--text-placeholder) !important; }
.tech-input :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; border: none !important; }
.tech-input :deep(.el-input__wrapper:hover) { box-shadow: none !important; }
.tech-input :deep(.el-input__wrapper.is-focus) { box-shadow: none !important; }

.input-border {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--color-primary-gradient);
  transition: width 0.3s ease, opacity 0.3s ease;
  transform: translateX(-50%);
  opacity: 0;
}

.input-wrapper:focus-within .input-border { width: 85%; opacity: 1; }

.password-toggle {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: var(--text-muted);
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  flex-shrink: 0;
}

.password-toggle:hover {
  color: var(--color-primary);
  background: var(--color-primary-hover);
}

/* ============ 6. 复选框 ============ */
.remember-section { margin-bottom: 28px; }

.remember-label {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
  user-select: none;
}

.checkbox-text {
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 400;
}

/* 隐藏 Element Plus 渲染的空 label，内容已用 checkbox-text 替代 */
.tech-checkbox :deep(.el-checkbox__label) {
  display: none !important;
}

/* 未选中状态方框 */
.tech-checkbox :deep(.el-checkbox__inner) {
  width: 20px !important;
  height: 20px !important;
  background: var(--bg-card) !important;
  border: 2px solid var(--border-darker) !important;
  border-radius: 6px !important;
  transition: all 0.2s ease !important;
}

/* 选中状态方框 */
.tech-checkbox :deep(.el-checkbox__inner::after) {
  border-color: var(--bg-card) !important;
  border-bottom-color: var(--bg-card) !important;
  border-right-color: var(--bg-card) !important;
}

.tech-checkbox.is-checked :deep(.el-checkbox__inner) {
  background: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}

/* hover 状态 */
.tech-checkbox :deep(.el-checkbox__inner:hover) {
  border-color: var(--color-primary) !important;
}

/* focus 状态 */
.tech-checkbox :deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 3px var(--color-primary-hover) !important;
}

/* ============ 7. 按钮 ============ */
.login-btn {
  width: 100%;
  height: 46px;
  background: var(--color-primary-gradient);
  border: none;
  border-radius: 12px;
  color: #ffffff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow:
    0 8px 24px var(--color-primary-shadow),
    0 4px 12px rgba(0, 0, 0, 0.08);
  letter-spacing: -0.01em;
}

.login-btn:hover {
  background: var(--color-primary-interactive);
  transform: translateY(-2px);
  box-shadow:
    0 12px 32px var(--color-primary-shadow),
    0 6px 16px rgba(0, 0, 0, 0.12);
}

.login-btn:active {
  transform: translateY(0);
  transition-duration: 0.1s;
  background: var(--color-primary-dark);
}

.login-btn :deep(.el-button) {
  border: none !important;
  background: transparent !important;
  color: white !important;
  font-weight: 500 !important;
  font-size: 16px !important;
  width: 100% !important;
  height: 100% !important;
}

.btn-text {
  position: relative;
  z-index: 1;
  letter-spacing: -0.01em;
}

.btn-glare {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.25) 45%,
    rgba(255, 255, 255, 0.4) 50%,
    rgba(255, 255, 255, 0.25) 55%,
    transparent 100%
  );
  transition: left 0.5s ease;
  pointer-events: none;
}

.login-btn:hover .btn-glare { left: 100%; }

/* ============ 8. 科技装饰元素 ============ */
.tech-decoration {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.decoration-circle {
  position: absolute;
  border: 1px solid rgba(0, 113, 227, 0.08);
  border-radius: 50%;
  animation: rotate 40s linear infinite;
}

.circle-1 { width: 120px; height: 120px; top: -60px; right: -60px; animation-duration: 35s; }
.circle-2 { width: 80px; height: 80px; bottom: -40px; left: -40px; animation-duration: 25s; animation-direction: reverse; }

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.scan-beam {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 3px;
  height: 3px;
  background: radial-gradient(circle, rgba(0, 113, 227, 0.6) 0%, transparent 75%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: rotateBeam 10s linear infinite;
  box-shadow: 0 0 16px rgba(0, 113, 227, 0.4);
  pointer-events: none;
}

@keyframes rotateBeam {
  0% { transform: translate(-50%, -50%) rotate(0deg) translateX(140px) rotate(0deg); opacity: 0; }
  25% { opacity: 0.8; }
  50% { transform: translate(-50%, -50%) rotate(180deg) translateX(140px) rotate(-180deg); opacity: 1; }
  75% { opacity: 0.8; }
  100% { transform: translate(-50%, -50%) rotate(360deg) translateX(140px) rotate(-360deg); opacity: 0; }
}

/* ============ 9. 响应式适配 ============ */
@media (max-width: 480px) {
  .login-wrapper { max-width: 94%; padding: 12px; }
  .login-card { padding: 28px 24px; border-radius: 20px; }
  .logo-section { margin-bottom: 28px; }
  .tech-logo { width: 68px; height: 68px; border-radius: 50%; }
  .mqtt-icon { width: 36px; height: 36px; }
  .app-title { font-size: 22px; }
  .subtitle { font-size: 13px; }
  .form-item { margin-bottom: 20px; }
  .input-wrapper { height: 48px; border-radius: 12px; }
  .tech-input { font-size: 15px; }
  .tech-input :deep(.el-input__inner) { font-size: 15px !important; }
  .input-icon { width: 44px; }
  .input-icon svg { width: 18px; height: 18px; }
  .remember-section { margin-bottom: 20px; }
  .login-btn { height: 46px; border-radius: 12px; font-size: 15px; }
}

@media (max-width: 360px) {
  .login-wrapper { max-width: 96%; padding: 8px; }
  .login-card { padding: 22px 16px; border-radius: 18px; }
  .tech-logo { width: 56px; height: 56px; border-radius: 50%; }
  .mqtt-icon { width: 30px; height: 30px; }
  .app-title { font-size: 20px; }
  .subtitle { font-size: 12px; }
  .input-wrapper { height: 44px; border-radius: 10px; }
  .login-btn { height: 42px; border-radius: 10px; font-size: 14px; }
}

/* ============ 10. 暗色模式（跟随 html.dark 而非 prefers-color-scheme） ============ */
html.dark .login-container {
  background: var(--bg-secondary);
  background-image:
    radial-gradient(ellipse at 20% 20%, rgba(43, 224, 140, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(43, 224, 140, 0.05) 0%, transparent 50%);
}

html.dark .bg-animation {
  background:
    radial-gradient(ellipse at 20% 30%, rgba(43, 224, 140, 0.12) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 70%, rgba(43, 224, 140, 0.08) 0%, transparent 60%),
    radial-gradient(ellipse at 50% 50%, rgba(43, 224, 140, 0.06) 0%, transparent 70%);
}

html.dark .login-card {
  background: rgba(20, 21, 28, 0.92);
  border-color: var(--border-base);
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

html.dark .app-title { color: var(--text-primary); }
html.dark .subtitle { color: var(--text-secondary); }

html.dark .tech-logo {
  box-shadow:
    0 8px 32px var(--shadow-md),
    0 0 0 6px var(--bg-secondary),
    0 0 0 8px var(--border-light);
}

html.dark .input-wrapper {
  background: var(--bg-input);
  border-color: var(--border-base);
}
html.dark .input-wrapper:hover { border-color: var(--color-primary); background: var(--bg-input); }
html.dark .input-wrapper:focus-within {
  background: var(--bg-card);
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px var(--color-primary-hover), 0 4px 16px var(--color-primary-hover);
}

html.dark .tech-input :deep(.el-input__inner) { color: var(--text-primary) !important; }
html.dark .tech-input :deep(.el-input__inner::placeholder) { color: var(--text-placeholder) !important; }
html.dark .input-icon { color: var(--text-secondary); }
html.dark .input-wrapper:focus-within .input-icon { color: var(--color-primary); }
html.dark .input-border { background: var(--color-primary-gradient); }

html.dark .password-toggle { color: var(--text-secondary); }
html.dark .password-toggle:hover { color: var(--color-primary); background: var(--color-primary-hover); }

html.dark .checkbox-text { color: var(--text-secondary); }

html.dark .tech-checkbox :deep(.el-checkbox__inner) { background: var(--bg-input) !important; border-color: var(--border-darker) !important; }
html.dark .tech-checkbox.is-checked :deep(.el-checkbox__inner) { background: var(--color-primary) !important; border-color: var(--color-primary) !important; }

html.dark .login-btn {
  background: var(--color-primary-gradient);
  box-shadow: 0 8px 24px var(--color-primary-shadow), 0 4px 12px rgba(0, 0, 0, 0.2);
}
html.dark .login-btn:hover {
  background: var(--color-primary-interactive);
  box-shadow: 0 12px 32px var(--color-primary-shadow), 0 6px 16px rgba(0, 0, 0, 0.25);
}

html.dark .decoration-circle { border-color: rgba(43, 224, 140, 0.1); }
html.dark .scan-beam {
  background: radial-gradient(circle, rgba(43, 224, 140, 0.6) 0%, transparent 75%);
  box-shadow: 0 0 16px rgba(43, 224, 140, 0.4);
}
html.dark .grid-overlay {
  background-image:
    linear-gradient(rgba(43, 224, 140, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(43, 224, 140, 0.05) 1px, transparent 1px);
}
html.dark .scan-line { border-color: rgba(43, 224, 140, 0.2); }
html.dark .particles::before, html.dark .particles::after {
  background: rgba(43, 224, 140, 0.5);
  box-shadow: 0 0 8px rgba(43, 224, 140, 0.3);
}
</style>
