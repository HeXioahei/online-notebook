<template>
  <div class="register-container">
    <div class="register-card card">
      <header class="register-header">
        <h1>创建账号</h1>
        <p>注册一个新账号开始使用</p>
      </header>
      
      <form @submit.prevent="handleRegister" class="register-form">
        <section class="form-group">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            class="input"
            placeholder="请输入用户名（3-20个字符）"
            required
            minlength="3"
            maxlength="20"
          />
        </section>
        
        <section class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="input"
            placeholder="请输入密码（至少6个字符）"
            required
            minlength="6"
          />
        </section>
        
        <section class="form-group">
          <label for="email">邮箱（可选）</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            class="input"
            placeholder="请输入邮箱地址"
          />
        </section>
        
        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="success" class="success-message">{{ success }}</div>
        
        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      
      <footer class="register-footer">
        <p>已有账号？<router-link to="/login">立即登录</router-link></p>
      </footer>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../utils/api'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const form = ref({
      username: '',
      password: '',
      email: ''
    })
    const error = ref('')
    const success = ref('')
    const loading = ref(false)
    
    const handleRegister = async () => {
      error.value = ''
      success.value = ''
      loading.value = true
      
      try {
        const response = await api.post('/users/register', form.value)
        
        if (response.data.success) {
          success.value = '注册成功！即将跳转到登录页面...'
          setTimeout(() => {
            router.push('/login')
          }, 1500)
        } else {
          error.value = response.data.message
        }
      } catch (err) {
        error.value = err.response?.data?.message || '注册失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }
    
    return {
      form,
      error,
      success,
      loading,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 400px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  color: #667eea;
  font-size: 28px;
  margin-bottom: 10px;
}

.register-header p {
  color: #718096;
  font-size: 14px;
}

.register-form {
  margin-bottom: 20px;
}

.btn-block {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  font-weight: 600;
}

.register-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.register-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>