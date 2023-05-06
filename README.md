
# Server-Monitor

基于telnet的轻量化定时检测公司各服务是否正常运行的java程序，异常/恢复时 系统利用邮件通知或webhook

## 判定规则十分简单

* **服务正常：** telnet能连通
* **服务异常：** telnet无法连通

## 使用方法

1. 使用maven package打包后，重命名成简单一点的文件名
2. 在jar包相同目录下，添加配置文件：`config.conf`，配置说明见下方
3. 命令：`java -jar xxxx.jar`


## 配置文件 `config.conf` 说明

1. 配置应用名`application.name`，此名称将体现在邮件提醒的标题中
2. 配置发件账户 `mail.xxx`
3. 配置多个服务，每个服务名必须写在 `[ ]` 中
4. 每个服务必须包含`ip`和`port`
5. 每个服务中的affects配置表示当前服务如果停止，将影响的产品或其他服务    

### 样例  

```
# 应用名将会体现在邮件通知的标题前缀中
application.name=汇智监控

# 配置邮件服务器
mail.host=smtp.qq.com
mail.port=465
mail.sslEnable=true
mail.from=
mail.auth=true
mail.user=
mail.pass=
# 配置1个用户，mail代表此用户支持邮件通知
users.qiusang.mail=123@gmail.com
# 再配置1个用户
users.zhangsan.mail=123@qq.com
# 配置分组（用于按分组通知）
groups.admin=zhangsan

[springboot订单服务]
ip=127.0.0.1
port=8080
affects=移动端商场
users=qiusang
groups=admin



```
