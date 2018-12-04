# LFTP
计算机网络期中项目

![gif](docs/img/mp4.gif)

## 功能
客户端从服务器接收/上传大文件

### 要求
- 使用CS模型
- 命令行启动应用，类似于:
```bash
LFTP  lsend myserver  mylargefile
LFTP  lget  myserver  mylargefile
```
- UDP作为传输层协议
- 实现类似TCP的100%可靠性
- 实现类似TCP的流量控制
- 实现类似TCP的拥塞控制
- 服务端必须能同时支持多个客户端请求
- 应用必须提供有意义的输出信息

### 设计文档
xxx

### 使用说明

[Use-doc](docs/User.md)

### 测试文档
[Test-doc](docs/Test.md)