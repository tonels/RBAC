package com.dxj.admin.controller;

import com.dxj.admin.domain.entity.Permission;
import com.dxj.admin.domain.dto.PermissionDTO;
import com.dxj.admin.domain.query.CommonQuery;
import com.dxj.admin.service.PermissionService;
import com.dxj.common.enums.CommEnum;
import com.dxj.common.exception.BadRequestException;
import com.dxj.log.annotation.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author dxj
 * @date 2019-04-03
 */
@RestController
@RequestMapping("api")
public class PermissionController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 返回全部的权限，新增角色时下拉选择
     *
     * @return
     */
    @GetMapping(value = "/permission/tree")
    @PreAuthorize("hasAnyRole('ADMIN','PERMISSION_ALL','PERMISSION_CREATE','PERMISSION_EDIT','ROLES_SELECT','ROLES_ALL')")
    public ResponseEntity<Object> getTree() {
        return new ResponseEntity<>(permissionService.getPermissionTree(permissionService.findByPid(0L)), HttpStatus.OK);
    }

    @Log("查询权限")
    @GetMapping(value = "/permission")
    @PreAuthorize("hasAnyRole('ADMIN','PERMISSION_ALL','PERMISSION_SELECT')")
    public ResponseEntity<Map<String, Object>> getPermissions(CommonQuery query) {
        List<PermissionDTO> permissionDTOS = permissionService.queryAll(query);
        return new ResponseEntity<>(permissionService.buildTree(permissionDTOS), HttpStatus.OK);
    }

    @Log("新增权限")
    @PostMapping(value = "/permission")
    @PreAuthorize("hasAnyRole('ADMIN','PERMISSION_ALL','PERMISSION_CREATE')")
    public ResponseEntity<PermissionDTO> create(@Validated @RequestBody Permission resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + CommEnum.PERMISSION_ENTITY + " cannot already have an ID");
        }
        return new ResponseEntity<>(permissionService.create(resources), HttpStatus.CREATED);
    }

    @Log("修改权限")
    @PutMapping(value = "/permission")
    @PreAuthorize("hasAnyRole('ADMIN','PERMISSION_ALL','PERMISSION_EDIT')")
    public ResponseEntity<Void> update(@Validated(Permission.Update.class) @RequestBody Permission resources) {
        permissionService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除权限")
    @DeleteMapping(value = "/permission/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PERMISSION_ALL','PERMISSION_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
