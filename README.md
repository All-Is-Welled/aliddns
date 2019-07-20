# aliddns

# 阿里域名解析

`java -jar aliddns-0.0.1.jar Key Secret Domain RRs Timeout`

# 参数说明
Key: accessKeyId 阿里云获取

Secret: accessKeySecret 阿里云获取

Domain: 域名 如 example.com

RRs: 子域名前缀列 demo,demo1,demo2,@ 逗号隔开   注意：@ 放在最后

Timeout: 刷新间隔时间

# 例

若要同时解析 www.example.com、example.com、demo.example.com、demo1.example.com，则需要运行如下命令

`java -jar aliddns-0.0.1.jar accessKeyId accessKeySecret  example.com www,demo,demo1,@  5`
