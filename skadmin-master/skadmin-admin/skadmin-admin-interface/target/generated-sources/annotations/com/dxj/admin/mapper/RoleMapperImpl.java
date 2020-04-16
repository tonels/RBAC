package com.dxj.admin.mapper;

import com.dxj.admin.domain.dto.DeptDTO;
import com.dxj.admin.domain.dto.MenuDTO;
import com.dxj.admin.domain.dto.PermissionDTO;
import com.dxj.admin.domain.dto.RoleDTO;
import com.dxj.admin.domain.entity.Dept;
import com.dxj.admin.domain.entity.Menu;
import com.dxj.admin.domain.entity.Permission;
import com.dxj.admin.domain.entity.Role;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2020-03-23T18:18:02+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_144 (Oracle Corporation)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private DeptMapper deptMapper;

    @Override
    public Role toEntity(RoleDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Role role = new Role();

        role.setId( dto.getId() );
        role.setName( dto.getName() );
        role.setDataScope( dto.getDataScope() );
        role.setLevel( dto.getLevel() );
        role.setRemark( dto.getRemark() );
        role.setPermissions( permissionDTOSetToPermissionSet( dto.getPermissions() ) );
        role.setMenus( menuDTOSetToMenuSet( dto.getMenus() ) );
        role.setDepts( deptDTOSetToDeptSet( dto.getDepts() ) );
        role.setCreateTime( dto.getCreateTime() );

        return role;
    }

    @Override
    public RoleDTO toDto(Role entity) {
        if ( entity == null ) {
            return null;
        }

        RoleDTO roleDTO = new RoleDTO();

        roleDTO.setId( entity.getId() );
        roleDTO.setName( entity.getName() );
        roleDTO.setDataScope( entity.getDataScope() );
        roleDTO.setLevel( entity.getLevel() );
        roleDTO.setRemark( entity.getRemark() );
        roleDTO.setPermissions( permissionSetToPermissionDTOSet( entity.getPermissions() ) );
        roleDTO.setMenus( menuSetToMenuDTOSet( entity.getMenus() ) );
        roleDTO.setDepts( deptSetToDeptDTOSet( entity.getDepts() ) );
        roleDTO.setCreateTime( entity.getCreateTime() );

        return roleDTO;
    }

    @Override
    public List<Role> toEntity(List<RoleDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Role> list = new ArrayList<Role>( dtoList.size() );
        for ( RoleDTO roleDTO : dtoList ) {
            list.add( toEntity( roleDTO ) );
        }

        return list;
    }

    @Override
    public List<RoleDTO> toDto(List<Role> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<RoleDTO> list = new ArrayList<RoleDTO>( entityList.size() );
        for ( Role role : entityList ) {
            list.add( toDto( role ) );
        }

        return list;
    }

    protected Set<Permission> permissionDTOSetToPermissionSet(Set<PermissionDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Permission> set1 = new HashSet<Permission>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( PermissionDTO permissionDTO : set ) {
            set1.add( permissionMapper.toEntity( permissionDTO ) );
        }

        return set1;
    }

    protected Set<Menu> menuDTOSetToMenuSet(Set<MenuDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Menu> set1 = new HashSet<Menu>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( MenuDTO menuDTO : set ) {
            set1.add( menuMapper.toEntity( menuDTO ) );
        }

        return set1;
    }

    protected Set<Dept> deptDTOSetToDeptSet(Set<DeptDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Dept> set1 = new HashSet<Dept>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( DeptDTO deptDTO : set ) {
            set1.add( deptMapper.toEntity( deptDTO ) );
        }

        return set1;
    }

    protected Set<PermissionDTO> permissionSetToPermissionDTOSet(Set<Permission> set) {
        if ( set == null ) {
            return null;
        }

        Set<PermissionDTO> set1 = new HashSet<PermissionDTO>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Permission permission : set ) {
            set1.add( permissionMapper.toDto( permission ) );
        }

        return set1;
    }

    protected Set<MenuDTO> menuSetToMenuDTOSet(Set<Menu> set) {
        if ( set == null ) {
            return null;
        }

        Set<MenuDTO> set1 = new HashSet<MenuDTO>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Menu menu : set ) {
            set1.add( menuMapper.toDto( menu ) );
        }

        return set1;
    }

    protected Set<DeptDTO> deptSetToDeptDTOSet(Set<Dept> set) {
        if ( set == null ) {
            return null;
        }

        Set<DeptDTO> set1 = new HashSet<DeptDTO>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Dept dept : set ) {
            set1.add( deptMapper.toDto( dept ) );
        }

        return set1;
    }
}
