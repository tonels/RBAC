package com.dxj.tool.service;

import com.dxj.common.util.BaseQuery;
import com.dxj.common.util.PageUtil;
import com.dxj.common.util.ValidationUtil;
import com.dxj.tool.domain.entity.QiNiuContent;
import com.dxj.tool.domain.entity.QiNiuConfig;
import com.dxj.tool.util.QiNiuUtils;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.dxj.common.exception.BadRequestException;
import com.dxj.tool.repository.QiNiuConfigRepository;
import com.dxj.tool.repository.QiNiuContentRepository;
import com.dxj.common.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * @author dxj
 * @date 2018-05-31
 */
@Service
@CacheConfig(cacheNames = "qiNiu")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class QiNiuService {

    private final QiNiuConfigRepository qiNiuConfigRepository;

    private final QiNiuContentRepository qiNiuContentRepository;

    @Value("${qiniu.max-size}")
    private Long maxSize;

    @Autowired
    public QiNiuService(QiNiuConfigRepository qiNiuConfigRepository, QiNiuContentRepository qiNiuContentRepository) {
        this.qiNiuConfigRepository = qiNiuConfigRepository;
        this.qiNiuContentRepository = qiNiuContentRepository;
    }

    /**
     * 查配置
     *
     * @return
     */
    @Cacheable(cacheNames = "qiNiuConfig", key = "'1'")
    public QiNiuConfig find() {
        Optional<QiNiuConfig> qiNiuConfig = qiNiuConfigRepository.findById(1L);
        return qiNiuConfig.orElseGet(QiNiuConfig::new);
    }

    /**
     * 修改配置
     *
     * @param qiNiuConfig
     * @return
     */
    @CachePut(cacheNames = "qiNiuConfig", key = "'1'")
    @Transactional(rollbackFor = Exception.class)
    public QiNiuConfig update(QiNiuConfig qiNiuConfig) {
        if (!(qiNiuConfig.getHost().toLowerCase().startsWith("http://") || qiNiuConfig.getHost().toLowerCase().startsWith("https://"))) {
            throw new BadRequestException("外链域名必须以http://或者https://开头");
        }
        qiNiuConfig.setId(1L);
        return qiNiuConfigRepository.save(qiNiuConfig);
    }

    /**
     * 上传文件
     *
     * @param file
     * @param qiNiuConfig
     */
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public QiNiuContent upload(MultipartFile file, QiNiuConfig qiNiuConfig) {

        long size = maxSize * 1024 * 1024;
        if (file.getSize() > size) {
            throw new BadRequestException("文件超出规定大小");
        }
        if (qiNiuConfig.getId() == null) {
            throw new BadRequestException("请先添加相应配置，再操作");
        }
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(QiNiuUtils.getRegion(qiNiuConfig.getZone()));
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(qiNiuConfig.getAccessKey(), qiNiuConfig.getSecretKey());
        String upToken = auth.uploadToken(qiNiuConfig.getBucket());
        try {
            String key = file.getOriginalFilename();
            if (qiNiuContentRepository.findByKey(key) != null) {
                key = QiNiuUtils.getKey(key);
            }
            Response response = uploadManager.put(file.getBytes(), key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            //存入数据库
            QiNiuContent qiniuContent = new QiNiuContent();
            qiniuContent.setBucket(qiNiuConfig.getBucket());
            qiniuContent.setType(qiNiuConfig.getType());
            qiniuContent.setKey(putRet.key);
            qiniuContent.setUrl(qiNiuConfig.getHost() + "/" + putRet.key);
            qiniuContent.setSize(FileUtil.getSize(Integer.parseInt(file.getSize() + "")));
            return qiNiuContentRepository.save(qiniuContent);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * 查询文件
     *
     * @param id
     * @return
     */
    @Cacheable(key = "'content:'+#p0")
    public QiNiuContent findByContentId(Long id) {
        Optional<QiNiuContent> qiNiuContent = qiNiuContentRepository.findById(id);
        ValidationUtil.isNull(qiNiuContent, "QiNiuContent", "id", id);
        return qiNiuContent.orElse(null);
    }

    /**
     * 下载文件
     *
     * @param content
     * @param config
     * @return
     */
    public String download(QiNiuContent content, QiNiuConfig config) {
        String finalUrl;
        String TYPE = "公开";
        if (TYPE.equals(content.getType())) {
            finalUrl = content.getUrl();
        } else {
            Auth auth = Auth.create(config.getAccessKey(), config.getSecretKey());
            //1小时，可以自定义链接过期时间
            long expireInSeconds = 3600;
            finalUrl = auth.privateDownloadUrl(content.getUrl(), expireInSeconds);
        }
        return finalUrl;
    }

    /**
     * 删除文件
     *
     * @param content
     * @param config
     * @return
     */
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void delete(QiNiuContent content, QiNiuConfig config) {
        //构造一个带指定Zone对象的配置类
        BucketManager bucketManager = getBucketManager(config);
        try {
            bucketManager.delete(content.getBucket(), content.getKey());
            qiNiuContentRepository.delete(content);
        } catch (QiniuException ex) {
            qiNiuContentRepository.delete(content);
        }
    }

    private BucketManager getBucketManager(QiNiuConfig config) {
        Configuration cfg = new Configuration(QiNiuUtils.getRegion(config.getZone()));
        Auth auth = Auth.create(config.getAccessKey(), config.getSecretKey());
        return new BucketManager(auth, cfg);
    }

    /**
     * 同步数据
     *
     * @param config
     */
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void synchronize(QiNiuConfig config) {
        if (config.getId() == null) {
            throw new BadRequestException("请先添加相应配置，再操作");
        }
        //构造一个带指定Zone对象的配置类
        BucketManager bucketManager = getBucketManager(config);
        //文件名前缀
        String prefix = "";
        //每次迭代的长度限制，最大1000，推荐值 1000
        int limit = 1000;
        //指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
        String delimiter = "";
        //列举空间文件列表
        BucketManager.FileListIterator fileListIterator = bucketManager.createFileListIterator(config.getBucket(), prefix, limit, delimiter);
        while (fileListIterator.hasNext()) {
            //处理获取的file list结果
            QiNiuContent qiniuContent;
            FileInfo[] items = fileListIterator.next();
            for (FileInfo item : items) {
                if (qiNiuContentRepository.findByKey(item.key) == null) {
                    qiniuContent = new QiNiuContent();
                    qiniuContent.setSize(FileUtil.getSize(Integer.parseInt(item.fsize + "")));
                    qiniuContent.setKey(item.key);
                    qiniuContent.setType(config.getType());
                    qiniuContent.setBucket(config.getBucket());
                    qiniuContent.setUrl(config.getHost() + "/" + item.key);
                    qiNiuContentRepository.save(qiniuContent);
                }
            }
        }

    }

    /**
     * 批量删除
     *
     * @param ids
     * @param config
     */
    @CacheEvict(allEntries = true)
    public void deleteAll(Long[] ids, QiNiuConfig config) {
        for (Long id : ids) {
            delete(findByContentId(id), config);
        }
    }

    /**
     * 分页
     */
    @Cacheable(keyGenerator = "keyGenerator")
    public Object queryAll(QiNiuContent query, Pageable pageable) {
        return PageUtil.toPage(qiNiuContentRepository.findAll((root, criteriaQuery, criteriaBuilder) -> BaseQuery.getPredicate(root, query, criteriaBuilder), pageable));
    }
}
