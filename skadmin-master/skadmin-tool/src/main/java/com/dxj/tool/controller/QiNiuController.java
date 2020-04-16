package com.dxj.tool.controller;

import com.dxj.common.response.Result;
import com.dxj.tool.domain.entity.QiNiuContent;
import com.dxj.tool.service.QiNiuService;
import lombok.extern.slf4j.Slf4j;
import com.dxj.log.annotation.Log;
import com.dxj.tool.domain.entity.QiNiuConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 七牛云上传
 *
 * @author dxj
 * @date 2019/04/28 6:55:53
 */
@Slf4j
@RestController
@RequestMapping("api")
public class QiNiuController {

    private final QiNiuService qiNiuService;


    @Autowired
    public QiNiuController(QiNiuService qiNiuService) {
        this.qiNiuService = qiNiuService;
    }

    @GetMapping(value = "/qiNiuConfig")
    public ResponseEntity<QiNiuConfig> get() {
        return new ResponseEntity<>(qiNiuService.find(), HttpStatus.OK);
    }

    @Log("配置七牛云存储")
    @PutMapping(value = "/qiNiuConfig")
    public ResponseEntity<Void> emailConfig(@Validated @RequestBody QiNiuConfig qiniuConfig) {
        qiNiuService.update(qiniuConfig);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("查询文件")
    @GetMapping(value = "/qiNiuContent")
    public ResponseEntity<Object> getRoles(QiNiuContent query, Pageable pageable) {
        return new ResponseEntity<>(qiNiuService.queryAll(query, pageable), HttpStatus.OK);
    }

    /**
     * 上传文件到七牛云
     *
     * @param file
     * @return
     */
    @Log("上传文件")
    @PostMapping(value = "/qiNiuContent")
    public ResponseEntity<Result> upload(@RequestParam MultipartFile file) {
        QiNiuContent qiniuContent = qiNiuService.upload(file, qiNiuService.find());
        return new ResponseEntity<>(Result.success(qiniuContent), HttpStatus.OK);
    }

    /**
     * 同步七牛云数据到数据库
     *
     * @return
     */
    @Log("同步七牛云数据")
    @PostMapping(value = "/qiNiuContent/synchronize")
    public ResponseEntity<Void> synchronize() {
        log.warn("REST request to synchronize qiNiu : {}");
        qiNiuService.synchronize(qiNiuService.find());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 下载七牛云文件
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Log("下载文件")
    @GetMapping(value = "/qiNiuContent/download/{id}")
    public ResponseEntity<Map<String, Object>> download(@PathVariable Long id) {

        Map<String, Object> map = new HashMap<>();
        map.put("url", qiNiuService.download(qiNiuService.findByContentId(id), qiNiuService.find()));
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    /**
     * 删除七牛云文件
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Log("删除文件")
    @DeleteMapping(value = "/qiNiuContent/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        qiNiuService.delete(qiNiuService.findByContentId(id), qiNiuService.find());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 批量删除文件
     *
     * @param ids
     * @return
     */
    @Log("批量删除文件")
    @DeleteMapping(value = "/qiNiuContent")
    public ResponseEntity<Void> deleteAll(@RequestBody Long[] ids) {
        qiNiuService.deleteAll(ids, qiNiuService.find());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
