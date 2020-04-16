package com.dxj.admin.controller;

import com.dxj.admin.domain.entity.Dict;
import com.dxj.admin.domain.dto.DictDTO;
import com.dxj.admin.domain.query.DictQuery;
import com.dxj.admin.service.DictService;
import com.dxj.common.enums.CommEnum;
import com.dxj.common.exception.BadRequestException;
import com.dxj.log.annotation.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author dxj
 * @date 2019-04-10
 */
@RestController
@RequestMapping("api")
public class DictController {

    private final DictService dictService;


    @Autowired
    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @Log("查询字典")
    @GetMapping(value = "/dict")
    @PreAuthorize("hasAnyRole('ADMIN','DICT_ALL','DICT_SELECT')")
    public ResponseEntity<Map<String, Object>> getDict(DictQuery query, Pageable pageable) {
        return new ResponseEntity<>(dictService.queryAll(query, pageable), HttpStatus.OK);
    }

    @Log("新增字典")
    @PostMapping(value = "/dict")
    @PreAuthorize("hasAnyRole('ADMIN','DICT_ALL','DICT_CREATE')")
    public ResponseEntity<DictDTO> create(@Validated @RequestBody Dict resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + CommEnum.DICT_ENTITY + " cannot to create already have an ID");
        }
        return new ResponseEntity<>(dictService.create(resources), HttpStatus.CREATED);
    }

    @Log("修改字典")
    @PutMapping(value = "/dict")
    @PreAuthorize("hasAnyRole('ADMIN','DICT_ALL','DICT_EDIT')")
    public ResponseEntity<Void> update(@Validated(Dict.Update.class) @RequestBody Dict resources) {
        dictService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除字典")
    @DeleteMapping(value = "/dict/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DICT_ALL','DICT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dictService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
