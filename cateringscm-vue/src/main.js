import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 引入路由
import ElementPlus from 'element-plus' // 引入 Element Plus
import 'element-plus/dist/index.css' // 引入 Element 样式
import * as ElementPlusIconsVue from '@element-plus/icons-vue' // 引入所有图标

const app = createApp(App)

// 全局注册所有 Element Plus 图标，让 icon="Plus" 等字符串写法生效
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router) // 💡 必须有这一句，<router-view> 才能生效！
app.use(ElementPlus)

app.mount('#app')
