# Gist-service
Fork自https://github.com/att/rcloud-gist-services  
- 一个使用springboot以及jgit的项目，实现了大部分gist.github的API接口，[接口参考](https://docs.github.com/cn/free-pro-team@latest/rest/reference/gists)  
- 绝大多数基础代码和架构均来自rcloud-gist-services，之后会更改架构
- 删除原有的权限验证、锁（用空白锁代替），后续会添加权限相关
- 缓存使用redis
- 增加原rcloud-gist-services没有的获取commits列表和比较commit差异（diff）的API
- 前端将采用https://github.com/GitHub-Laziji/VBlog 的Fork仓库 https://github.com/wetor/VBlog
