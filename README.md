# lanchain4j-cmy
## 项目简介
洗衣助手是专为 Java 程序员打造的大模型应用项目，作为一款人工智能客服系统，其接入通义千问的 qwen - vl - max 大模型。该系统能够实现用户输入照片，依据预设模板输出相应护理方案的功能。
输入图片会先由前端上传到阿里OSS，再返给后端。

## 项目展示
<img width="957" alt="show1" src="https://github.com/c-mengyi/langchain4j-cmy/blob/master/images/3ae775298864d8b916e42f852e94e254.png" />
<img width="957" alt="show2" src="https://github.com/c-mengyi/langchain4j-cmy/blob/master/images/528f82073fd6f8c36222019eae2c8df4.png" />
<img width="957" alt="show3" src="https://github.com/c-mengyi/langchain4j-cmy/blob/master/images/abc24372c07ec73ddda723b32137f2bf.png" />
<img width="957" alt="show4" src="https://github.com/c-mengyi/langchain4j-cmy/blob/master/images/2c70858828704f7e182cba56ff41b84b.png" />
<img width="957" alt="show5" src="https://github.com/c-mengyi/langchain4j-cmy/blob/master/images/cc08e936c71c7357cf5ad70e3839d1d5.png" />

## 部署步骤
- cmy-java是后端项目
- 用IDEA导入后端项目，等待依赖自动下载
- 修改DASH_API_KEY（这个需要注册[阿里云](https://www.aliyun.com/)账号，点击大模型导航栏，再点击通义千问max，然后获取API_KEY，将API_KEY配置到系统环境变量中，环境变量名就叫DASH_SCOPE_API_KEY）
- 运行Langchain4jApplication.java，浏览器输入[http://127.0.0.1:8003/doc.html](http://127.0.0.1:8003/doc.html)，可在线调试

- cmy-ui是前端项目，cmy-ui放在一个无中文的路径下
- 安装Node.js（node-v18.17.1-x64.msi）
- 配置npm镜像，打开cmd 执行`npm config set registry https://registry.npmmirror.com`
- 安装最新版vscode
- 使用vscode打开cmy-ui，将components\ChatWindow.vue中initOSS相关内容换成自己的，Ctrl+S保存
- 在vscode中新建终端，输入npm run dev，根据报错安装相应依赖即可
- 浏览器打开[http://localhost:5173/](http://localhost:5173/)，即可对话。

