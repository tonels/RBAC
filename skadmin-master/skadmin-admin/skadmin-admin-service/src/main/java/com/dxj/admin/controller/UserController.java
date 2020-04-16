package com.dxj.admin.controller;

import com.dxj.admin.config.DataScope;
import com.dxj.admin.domain.entity.User;
import com.dxj.admin.domain.dto.RoleSmallDTO;
import com.dxj.admin.domain.dto.UserDTO;
import com.dxj.admin.domain.query.UserQuery;
import com.dxj.admin.service.DeptService;
import com.dxj.admin.service.RoleService;
import com.dxj.admin.service.UserService;
import com.dxj.common.enums.CommEnum;
import com.dxj.common.exception.BadRequestException;
import com.dxj.common.response.CodeMsg;
import com.dxj.common.response.Result;
import com.dxj.common.util.*;
import com.dxj.log.annotation.Log;
import com.dxj.tool.domain.entity.Picture;
import com.dxj.tool.domain.entity.VerificationCode;
import com.dxj.tool.domain.vo.EmailVo;
import com.dxj.tool.service.EmailService;
import com.dxj.tool.service.PictureService;
import com.dxj.tool.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dxj
 * @date 2019-04-23
 */
@RestController
@RequestMapping("api")
public class UserController {

    private final UserService userService;

    private final PictureService pictureService;

    private final DataScope dataScope;

    private final DeptService deptService;

    private final RoleService roleService;

    private final VerificationCodeService verificationCodeService;

    private final EmailService emailService;

    @Autowired
    public UserController(UserService userService, PictureService pictureService, DataScope dataScope, DeptService deptService, VerificationCodeService verificationCodeService, RoleService roleService, EmailService emailService) {
        this.userService = userService;
        this.pictureService = pictureService;
        this.dataScope = dataScope;
        this.deptService = deptService;
        this.verificationCodeService = verificationCodeService;
        this.roleService = roleService;
        this.emailService = emailService;
    }

    @Log("查询用户")
    @GetMapping(value = "/user")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_SELECT')")
    public ResponseEntity<Map<String, Object>> getUsers(UserQuery query, Pageable pageable) {
        Set<Long> deptSet = new HashSet<>();
        Set<Long> result = new HashSet<>();

        if (!ObjectUtils.isEmpty(query.getDeptId())) {
            deptSet.add(query.getDeptId());
            deptSet.addAll(dataScope.getDeptChildren(deptService.findByPid(query.getDeptId())));
        }

        // 数据权限
        Set<Long> deptIds = dataScope.getDeptIds();

        // 查询条件不为空并且数据权限不为空则取交集
        if (!CollectionUtils.isEmpty(deptIds) && !CollectionUtils.isEmpty(deptSet)) {

            // 取交集
            result.addAll(deptSet);
            result.retainAll(deptIds);

            // 若无交集，则代表无数据权限
            query.setDeptIds(result);
            if (result.size() == 0) {
                return new ResponseEntity<>(PageUtil.toPage(null, 0), HttpStatus.OK);
            } else return new ResponseEntity<>(userService.queryAll(query, pageable), HttpStatus.OK);
            // 否则取并集
        } else {
            result.addAll(deptSet);
            result.addAll(deptIds);
            query.setDeptIds(result);
            return new ResponseEntity<>(userService.queryAll(query, pageable), HttpStatus.OK);
        }
    }

    @Log("新增用户")
    @PostMapping(value = "/user")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_CREATE')")
    public ResponseEntity<UserDTO> create(@Validated @RequestBody User resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + CommEnum.USER_ENTITY + " cannot already have an ID");
        }
        checkLevel(resources);
        return new ResponseEntity<>(userService.create(resources), HttpStatus.CREATED);
    }

    @Log("修改用户")
    @PutMapping(value = "/user")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_EDIT')")
    public ResponseEntity<Void> update(@Validated(User.Update.class) @RequestBody User resources) {
        checkLevel(resources);
        userService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除用户")
    @DeleteMapping(value = "/user/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        Integer currentLevel = Collections.min(roleService.findByUsers_Id(SecurityContextHolder.getUserId()).stream().map(RoleSmallDTO::getLevel).collect(Collectors.toList()));
        Integer optLevel = Collections.min(roleService.findByUsers_Id(id).stream().map(RoleSmallDTO::getLevel).collect(Collectors.toList()));

        if (currentLevel > optLevel) {
            throw new BadRequestException("角色权限不足");
        }
        userService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 验证密码
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/user/validPass")
    public ResponseEntity<Object> validPass(@RequestBody User user) {

        UserDetails userDetails = SecurityContextHolder.getUserDetails();
        if (!userDetails.getPassword().equals(AesEncryptUtil.encryptPassword(userDetails.getUsername()+ user.getPassword()))) {
            return new ResponseEntity<>(new Result(CodeMsg.VALIDATE_ERROR), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Result(CodeMsg.SUCCESS), HttpStatus.OK);
    }

    /**
     * 修改密码
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/user/updatePass")
    public ResponseEntity<Void> updatePass(@RequestBody User user) {
        UserDetails userDetails = SecurityContextHolder.getUserDetails();
        if (userDetails.getPassword().equals(AesEncryptUtil.encryptPassword(userDetails.getUsername() + user.getPassword()))) {
            throw new BadRequestException("新密码不能与旧密码相同");
        }
        userService.updatePass(userDetails.getUsername(), AesEncryptUtil.encryptPassword(userDetails.getUsername() + user.getPassword()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 修改头像
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/user/updateAvatar")
    public ResponseEntity<Void> updateAvatar(@RequestParam MultipartFile file) {
        Picture picture = pictureService.upload(file, SecurityContextHolder.getUsername());
        userService.updateAvatar(SecurityContextHolder.getUsername(), picture.getUrl());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 修改邮箱
     *
     * @param user
     * @param user
     * @return
     */
    @Log("修改邮箱")
    @PostMapping(value = "/user/updateEmail/{code}")
    public ResponseEntity<Void> updateEmail(@PathVariable String code, @RequestBody User user) {
        UserDetails userDetails = SecurityContextHolder.getUserDetails();
        if (!userDetails.getPassword().equals(AesEncryptUtil.encryptPassword(userDetails.getUsername() + user.getPassword()))) {
            throw new BadRequestException("密码错误");
        }
        VerificationCode verificationCode = new VerificationCode(code, CommEnum.RESET_MAIL.getEntityName(), "email", user.getEmail());
        verificationCodeService.validated(verificationCode);
        userService.updateEmail(userDetails.getUsername(), user.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 发送邮箱验证码
     * @param code
     * @return
     */
    @PostMapping(value = "/code/resetEmail")
    public ResponseEntity<Void> resetEmail(@RequestBody VerificationCode code) {
        sendEmail(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void sendEmail(VerificationCode code) {
        code.setScenes(CommEnum.RESET_MAIL.getEntityName());
        EmailVo emailVo = verificationCodeService.sendEmail(code);
        emailService.send(emailVo, emailService.find());
    }

    /**
     * 如果当前用户的角色级别低于创建用户的角色级别，则抛出权限不足的错误
     *
     * @param resources
     */
    private void checkLevel(User resources) {
        Integer currentLevel = Collections.min(roleService.findByUsers_Id(SecurityContextHolder.getUserId()).stream().map(RoleSmallDTO::getLevel).collect(Collectors.toList()));
        Integer optLevel = roleService.findByRoles(resources.getRoles());
        if (currentLevel > optLevel) {
            throw new BadRequestException("角色权限不足");
        }
    }
}
