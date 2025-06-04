<template>
  <div class="app-layout">
    <div class="sidebar">
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="洗衣助手" width="160" height="160" />
        <span class="logo-text">洗衣助手</span>
      </div>
    </div>
    <div class="main-content">
      <div class="chat-container">
        <div class="message-list" ref="messaggListRef">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="
              message.isUser ? 'message user-message' : 'message bot-message'
            "
          >
            <i
              :class="
                message.isUser
                  ? 'fa-solid fa-user message-icon'
                  : 'fa-solid fa-robot message-icon'
              "
            ></i>
            <span>
              <span v-if="message.type === 'text'" v-html="message.content"></span>
              <div v-else-if="message.type === 'image'" class="image-message">
                <img :src="message.url" alt="Uploaded Image" />
              </div>
              <span
                class="loading-dots"
                v-if="message.isThinking || message.isTyping"
              >
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
            </span>
          </div>
        </div>
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            placeholder="请输入消息"
            @keyup.enter="sendMessage"
          ></el-input>
          <el-button @click="sendMessage" :disabled="isSending" type="primary"
            >发送</el-button
          >
          <input
            type="file"
            ref="fileInput"
            @change="handleFileUpload"
            multiple
            accept="image/*"
            style="display: none"
          />
          <el-button @click="triggerFileInput" type="primary"
            >上传图片</el-button
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import axios from 'axios';
import OSS from 'ali-oss';

const messaggListRef = ref();
const isSending = ref(false);
const inputMessage = ref('');
const messages = ref([]);
const fileInput = ref(null);
const imageUrls = ref([]);

const scrollToBottom = () => {
  if (messaggListRef.value) {
    messaggListRef.value.scrollTop = messaggListRef.value.scrollHeight;
  }
};

const sendMessage = () => {
  if (inputMessage.value.trim() || imageUrls.value.length > 0) {
    sendRequest(inputMessage.value.trim());
    inputMessage.value = '';
  }
};

const sendRequest = async (message) => {
  isSending.value = true;
  
  // 创建用户消息（合并文本和所有图片）
  const userMsg = {
    isUser: true,
    type: 'combined', // 自定义类型，同时包含文本和图片
    content: message,
    urls: imageUrls.value, // 所有已上传的图片 URL
    isTyping: false,
    isThinking: false,
  };

  messages.value.push(userMsg);
  
  // 添加机器人思考状态
  const botMsg = {
    isUser: false,
    type: 'text',
    content: '',
    isTyping: true,
    isThinking: true,
  };
  messages.value.push(botMsg);
  scrollToBottom();

  try {
    const requestData = {
      imageUrls: imageUrls.value, // 传递所有图片 URL
      text: message
    };

    const response = await axios.post(
      'http://localhost:8003/youxi/photos',
      requestData,
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );
    
    console.log('后端响应:', response.data.data);
    const lastMsg = messages.value[messages.value.length - 1];
    lastMsg.content = response.data.data;
    lastMsg.isTyping = false;
    lastMsg.isThinking = false;
  } catch (error) {
    console.error('请求失败:', error);
    const lastMsg = messages.value[messages.value.length - 1];
    lastMsg.content = '请求失败，请重试';
    lastMsg.isTyping = false;
    lastMsg.isThinking = false;
  } finally {
    isSending.value = false;
    // 发送成功后清空图片 URL（根据需求决定是否保留）
    imageUrls.value = [];
  }
};

const triggerFileInput = () => {
  fileInput.value.click();
};

const handleFileUpload = async (event) => {
  const files = event.target.files;
  const ossClient = initOSS();
  const urlPromises = [];

  if (files.length === 0) {
    console.error('未选择文件');
    return;
  }

  for (const file of files) {
    try {
      const result = await ossClient.put(file.name, file);
      urlPromises.push(result.url); // 直接存储 URL
    } catch (error) {
      console.error(`文件 ${file.name} 上传失败:`, error);
    }
  }

  try {
    // 使用 Promise.all 确保所有文件上传成功
    const uploadedUrls = await Promise.all(urlPromises);
    
    // 将新上传的图片 URL 添加到现有列表中
    imageUrls.value = [...imageUrls.value, ...uploadedUrls];
    
    console.log('所有上传成功的图片 URL:', imageUrls.value);
    
    // 回显所有成功上传的图片
    uploadedUrls.forEach((url) => {
      messages.value.push({
        isUser: true,
        type: 'image',
        url: url,
        isTyping: false,
        isThinking: false,
      });
    });
    
    scrollToBottom();
  } catch (error) {
    console.error('至少一个文件上传失败:', error);
  }
};

const initOSS = () => {
  return new OSS({
    region: 'oss-cn-hangzhou',//替换为你的
    accessKeyId: '',//替换为你的
    accessKeySecret: '',//替换为你的
    bucket: '',//替换为你的
  });
};
</script>

<style scoped>
/* 原有响应式样式保持不变... */
.app-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 200px;
  background-color: #f4f4f9;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  margin-top: 10px;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: #fff;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
}

.message {
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 4px;
  display: flex;
}

.user-message {
  max-width: 70%;
  background-color: #e1f5fe;
  align-self: flex-end;
  flex-direction: row-reverse;
}

.bot-message {
  max-width: 100%;
  background-color: #f1f8e9;
  align-self: flex-start;
}

.message-icon {
  margin: 0 10px;
  font-size: 1.2em;
}

.loading-dots {
  padding-left: 5px;
}

.dot {
  display: inline-block;
  margin-left: 5px;
  width: 8px;
  height: 8px;
  background-color: #000000;
  border-radius: 50%;
  animation: pulse 1.2s infinite ease-in-out both;
}

.dot:nth-child(2) {
  animation-delay: -0.6s;
}


@keyframes pulse {
  0%,
  100% {
    transform: scale(0.6);
    opacity: 0.4;
  }

  50% {
    transform: scale(1);
    opacity: 1;
  }
}

.image-message img {
  max-width: 100%;
  border-radius: 4px;
}

.input-container {
  display: flex;
}

.input-container .el-input {
  flex: 1;
  margin-right: 10px;
}
:deep(table) {
  width: 100%;
  border-collapse: collapse; /* 合并边框 */
  margin: 10px 0;
  box-sizing: border-box; /* 新增 */
}

:deep(th, td) {
  border: 1px solid black; /* 为单元格设置边框 */
  padding: 8px;
  text-align: left; /* 左对齐文本 */
}

:deep(th) {
  background-color: #f5f5f5; /* 表头背景色 */
  font-weight: 500; /* 字体加粗 */
}

:deep(tr:nth-child(even)) { /* 隔行变色 */
  background-color: #f9f9f9;
}

:deep(tr:hover) {
background-color: #f1f1f1; /* 鼠标悬停时的背景色 */
}
</style>