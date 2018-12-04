# LFTP设计文档

## 简介

LFTP是一个java实现的网络应用，支持局域网、互联网上两台主机实现大文件传输。

***

## 实现内容

- 使用客户端-服务器的C-S架构
- 使用UDP协议进行传输
- 实现100%可靠传输
- 实现类似TCP的流量控制和拥塞控制
- 服务器支持多个客户端并行服务
- 应用提供有意义的输出信息

***

## 设计

### 项目结构

```
./code/LFTP/src
├── main # 程序控制
│   ├── LFTP.java  # 主程序入口，解析命令行
│   ├── Client.java # Client客户端
│   └── Server.java # Server服务端
│
├── service # 文件收发服务
│   ├── SendThread.java # 文件发送服务线程
│   └── ReceiveThread.java # 文件接收服务线程
│
└── service # 底层服务
    ├── FileIO.java # 文件读写IO管理
    ├── ByteConverter.java # 数据包序列化
    ├── Percentage.java # 进度条显示
    └── Packet.java # 数据包封装类
```

***

### 流程架构

TODO



***

## 实现机制



### GBN协议实现100%可靠传输

TODO



### 流量控制

TODO

### 拥塞控制

TODO



***

## 具体流程







 