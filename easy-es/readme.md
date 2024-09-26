### 问题
#### Failed: 1: type is missing
看看是否elasticsearch版本比较旧，因为es新版本不能有type了，所以升级es到7.X版本即可。
spring-boot系列的es属性是elasticsearch.version

### 官方文档
https://www.yuque.com/books/share/52959a47-d9e5-404e-a750-7d139dfd3b24/ppc5vy

### 避坑指南
- 由于我们底层用了ES官方的RestHighLevelClient,所以对ES版本有要求,底层用的RestHighLevelClient版本为7.10,所以对7.10的es兼容性最好,目前实测下来ES版本为7.x 都可以完美兼容
- keyword类型和text类型
- and和or的使用需要区别于MySQL和MP,因为ES的查询参数是树形数据结构,和MySQL平铺的不一样
  https://www.yuque.com/laohan-14b9d/foyrfa/gpg1vp
