# xiaohai-server-monitor
# 应用名将会体现在邮件通知的标题前缀中
application.name=monitor监控

# 配置邮件服务器
mail.host=smtp.qq.com
mail.port=465
mail.sslEnable=true
mail.from=1196396546@qq.com
mail.auth=true
mail.user=1196396546@qq.com
mail.pass=fwwxydwqzrmdhagj

# 配置1个用户，mail代表此用户支持邮件通知
users.sunzsh.mail=1196396546@qq.com

# 再配置1个用户
users.zhangsan.mail=410971103@qq.com

# 配置分组（用于按分组通知）
groups.admin=zhangsan

[redis服务]
ip=127.0.0.1
port=6379
affects=系统登陆
users=sunzsh
groups=admin

[MySQL]
ip=127.0.0.1
port=13306
affects=整个系统
users=sunzsh
groups=admin
