import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 引入路由
import ElementPlus from 'element-plus' // 引入 Element Plus
import 'element-plus/dist/index.css' // 引入 Element 样式

const app = createApp(App)

app.use(router) // 💡 必须有这一句，<router-view> 才能生效！
app.use(ElementPlus)

app.mount('#app')