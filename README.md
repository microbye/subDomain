# subDomain
一个可以获取顶级域名下的相关子域名的java工具

说明：
此项目基于java的webmagic爬虫框架。
原理：以一个顶级域名参数如qq.com作为种子，逐级爬取网页中的所有链接并解析出相关子域名。

使用，下载jar包，在命令行中输入：
java -jar 包名 域名 时间(s)
