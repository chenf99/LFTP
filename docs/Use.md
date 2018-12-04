# 使用说明

## 环境

- 代码运行环境：`Java jdk11`
- 在不同主机/服务器上运行时，需要提前开启要用到的防火墙的端口

## 服务器

- 启动服务器命令：

  ```bash
  java -jar LFTP.jar server [-sp]
  ```

  在当前主机/服务器上开启LFTP服务器，通过`-sp`可以设置服务器的控制端口，默认7545

- LFTP在服务器端存放文件的路径是`server/`，接收到的文件都会放在这个路径下，发给客户端的文件也是从这个文件夹下发出

- 在客户端对服务器发起请求时，服务器会有相应的输出

- 服务器的数据端口是随机分配的，`lget`时分配的端口范围在`20000~21000`，`lsend`时分配的端口范围在`30000~31000`

## 客户端

- 查看命令提示：

  ```java
  java -jar LFTP.jar help
  ```

  可以查看到关于LFTP的所有命令和参数的信息

- 上传文件：

  ```bash
  java -jar LFTP.jar lsend -f filename [-s serverAddress] [-sp serverPort] [-cp ClientPort]
  ```

  `-f`：文件名，可以是路径，如`book/book.pdf`，不能为空

  `-s`：服务器地址，默认`127.0.0.1`

  `-sp`：服务器控制端口，默认7545

  `-cp`：客户端控制端口，默认6000

- 下载文件：

  ```bash
  java -jar LFTP.jar lget -f filename [-s serverAddress] [-sp serverPort] [-cp ClientPort]
  ```

  下载文件的命令除`lget`外，其他都和上传文件相同

  下载文件的存放路径是`download/`文件夹下

- 查看服务端文件列表：

  ```bash
  java -jar LFTP.jar listall [-s serverAddress] [-sp serverPort] [-cp ClientPort]
  ```

  `-s`：服务器地址，默认`127.0.0.1`

  `-sp`：服务器控制端口，默认7545

  `-cp`：客户端控制端口，默认6000

