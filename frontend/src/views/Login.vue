<template>
  <div class="login-container">
    <div class="login-card card">
      <header class="login-header">
        <h1>在线笔记本</h1>
        <p>欢迎回来，请登录您的账号</p>
      </header>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <section class="form-group">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            class="input"
            placeholder="请输入用户名"
            required
          />
        </section>
        
        <section class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="input"
            placeholder="请输入密码"
            required
          />
        </section>
        
        <div v-if="error" class="error-message">{{ error }}</div>
        
        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      
      <footer class="login-footer">
        <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
      </footer>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../utils/api'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const form = ref({
      username: '',
      password: ''
    })
    const error = ref('')
    const loading = ref(false)
    
    const handleLogin = async () => {
      error.value = ''
      loading.value = true
      
      try {
        const response = await api.post('/users/login', form.value)
        
        if (response.data.success) {
          localStorage.setItem('userId', response.data.data.id)
          localStorage.setItem('username', response.data.data.username)
          router.push('/notes')
        } else {
          error.value = response.data.message
        }
      } catch (err) {
        error.value = err.response?.data?.message || '登录失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }
    
    return {
      form,
      error,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #667eea;
  font-size: 28px;
  margin-bottom: 10px;
}

.login-header p {
  color: #718096;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.btn-block {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  font-weight: 600;
}

.login-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.login-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>