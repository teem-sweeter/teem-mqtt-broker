import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router/index.js'
import i18n from '@/i18n'

const t = i18n.global.t

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  //baseURL: 'http://127.0.0.1:8888',
  // 超时
  timeout: 60000
});
function getErrorCode(code) {
  const map = {
    '401': 'request.authFailed',
    '403': 'request.noPermission',
    '404': 'request.notFound',
  }
  return t(map[code] || 'request.systemBusy')
}
function getToken() {
  return sessionStorage.token;
}
// request拦截器
service.interceptors.request.use(config => {
  // 是否需要设置 token
  const isToken = (config.headers || {}).isToken === false
  if (getToken() && !isToken) {
    config.headers['Authorization'] = 'Bearer ' + getToken();
  }
  if (config.method === 'get' && config.params) {
    let url = config.url + '?';
    for (const propName of Object.keys(config.params)) {
      const value = config.params[propName];
      let part = encodeURIComponent(propName) + "=";
      if (value !== null && typeof (value) !== "undefined") {
        if (typeof value === 'object') {
          for (const key of Object.keys(value)) {
            let params = propName + '[' + key + ']';
            let subPart = encodeURIComponent(params) + "=";
            url += subPart + encodeURIComponent(value[key]) + "&";
          }
        } else {
          url += part + encodeURIComponent(value) + "&";
        }
      }
    }
    url = url.slice(0, -1);
    config.params = {};
    config.url = url;
  }
  return config
}, error => {
  return Promise.reject(error);
});

function logout() {
  console.log(t('request.sessionExpired'))
  sessionStorage.token=""
  setTimeout(() => {
    router.push({
      //name: 'login',
      path: '/login'
    })
  }, 1500);
}
// 响应拦截器
service.interceptors.response.use(res => {
    const httpStatus = res.status;
    if (httpStatus === 200) {
      // 如果是文件下载（Blob）
      if (res.config.responseType === 'blob' || Object.prototype.toString.call(res.data) === '[object Blob]') {
        return res;
      }

      // 其他 JSON 数据处理
      const code = res.data.code || 200;
      const msg = res.data.msg || getErrorCode(code);

      if (code === 401) {
        logout();
      } else if (code === 500) {
        ElMessage.error(msg);
        return Promise.reject(new Error(msg));
      }else if (code === 503){
        console.warn(msg);
        return res.data;
      } else if (code !== 200) {
        ElMessage.error(msg);
        return Promise.reject('error');
      } else {
        return res.data;
      }
    }
  },
  async function (error) {
   // console.dir(error);
    let httpStatus = error.response.status;
    if (httpStatus === 401) {
      logout();
    }
    if (httpStatus === 503) {
      console.warn(error.message);
      return error.response.data;
    }
    let {message} = error;
    if (message == "Network Error") {
      message = t('request.networkError');
    } else if (message.includes("timeout")) {
      message = t('request.timeout');
      ElMessage({
              message: message,
              type: 'error',
              duration: 5 * 1000
            })
    } else if (message.includes("Request failed with status code")) {
      message = t('request.apiError', { code: message.substr(message.length - 3) });
    }
    return Promise.reject(error)
  }
);
export default service
