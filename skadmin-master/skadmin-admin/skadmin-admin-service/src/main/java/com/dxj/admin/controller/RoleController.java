package com.dxj.admin.controller;

import cn.hutool.core.lang.Dict;
import com.dxj.admin.domain.entity.Role;
import com.dxj.admin.domain.dto.RoleDTO;
import com.dxj.admin.domain.dto.RoleSmallDTO;
import com.dxj.admin.domain.query.CommonQuery;
import com.dxj.admin.service.RoleService;
import com.dxj.common.enums.CommEnum;
import com.dxj.common.exception.BadRequestException;
import com.dxj.common.util.SecurityContextHolder;
import com.dxj.log.annotation.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dxj
 * @date 2019-04-03
 */
@RestController
@RequestMapping("api")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping(value = "/role/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ROLES_ALL','ROLES_SELECT')")
    public ResponseEntity<RoleDTO> getRoles(@PathVariable Long id) {
        return new ResponseEntity<>(roleService.findById(id), HttpStatus.OK);
    }

    /**
     * 返回全部的角色，新增用户时下拉选择
     *
     * @return
     */
    @GetMapping(value = "/role/all")
    @PreAuthorize("hasAnyRole('ADMIN','ROLES_ALL','USER_ALL','USER_CREATE','USER_EDIT')")
    public ResponseEntity<List<RoleDTO>> getAll(@PageableDefault(value = 2000, sort = {"level"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return new ResponseEntity<>(roleService.queryAll(pageable), HttpStatus.OK);
    }

    @Log("查询角色")
    @GetMapping(value = "/role")
    @PreAuthorize("hasAnyRole('ADMIN','ROLES_ALL','ROLES_SELECT')")
    public ResponseEntity<Map<String, Object>> getRoles(CommonQuery query, Pageable pageable) {
        return new ResponseEntity<>(roleService.queryAll(query, pageable), HttpStatus.OK);
    }
    @GetMapping(value = "/role/level")
    public ResponseEntity<Object> getLevel(){
        List<Integer> levels = roleService.findByUsers_Id(SecurityContextHolder.getUserId()).stream().map(RoleSmallDTO::getLevel).collect(Collectors.toList());
        return new ResponseEntity<>(Dict.create().set("level", Collections.min(levels)), HttpStatus.OK);
    }
    @Log("新增角色")
    @PostMapping(value = "/role")
    @PreAuthorize("hasAnyRole('ADMIN','ROLES_ALL','ROLES_CREATE')")
    public ResponseEntity<RoleDTO> create(@Validated @RequestBody Role resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + CommEnum.ROLE_ENTITY + " cannot already have an ID");
        }
        return new ResponseEntity<>(roleService.create(resources), HttpStatus.CREATED);
    }

    @Log("修改角色")
    @PutMapping(value = "/role")
    @PreAuthorize("hasAnyRole('ADMIN','ROLES_ALL','ROLES_EDIT')")
    public ResponseEntity<Void> update(@Validated(Role.Update.class) @RequestBody Role resources) {
        roleService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("修改角色权限")
    @PutMapping(value = "/role/permission")
    @PreAuthorize("hasAnyRole('ADMIN','ROLES_ALL','ROLES_EDIT')")
    public ResponseEntity<Void> updatePermission(@RequestBody Role resources) {
        roleService.updatePermission(resources, roleService.findById(resources.getId()));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("修改角色菜单")
    @PutMapping(value = "/role/menu")
    @PreAuthorize("hasAnyRole('ADMIN','ROLES_ALL','ROLES_EDIT')")
    public ResponseEntity<Void> updateMenu(@RequestBody Role resources) {
        roleService.updateMenu(resources, roleService.findById(resources.getId()));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除角色")
    @DeleteMapping(value = "/role/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ROLES_ALL','ROLES_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
