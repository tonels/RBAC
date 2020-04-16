package ${package}.controller;

import com.dxj.log.annotation.Log;
import ${package}.domain.${className};
import ${package}.service.${className}Service;
import ${package}.dto.${className}Query;
import ${package}.dto.${className}DTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
* @author ${author}
* @date ${date}
*/
@RestController
@RequestMapping("api")
public class ${className}Controller {

    @Autowired
    private ${className}Service ${changeClassName}Service;

    @Log("查询${className}")
    @GetMapping(value = "/${changeClassName}")
    @PreAuthorize("hasAnyRole('ADMIN', '${upperCaseClassName}_ALL', '${upperCaseClassName}_SELECT')")
    public ResponseEntity<Object> get${className}s(${className}Query criteria, Pageable pageable) {
        return new ResponseEntity<>(${changeClassName}Service.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @Log("新增${className}")
    @PostMapping(value = "/${changeClassName}")
    @PreAuthorize("hasAnyRole('ADMIN', '${upperCaseClassName}_ALL', '${upperCaseClassName}_CREATE')")
    public ResponseEntity<${className}DTO> create(@Validated @RequestBody ${className} resources) {
        return new ResponseEntity<>(${changeClassName}Service.create(resources), HttpStatus.CREATED);
    }

    @Log("修改${className}")
    @PutMapping(value = "/${changeClassName}")
    @PreAuthorize("hasAnyRole('ADMIN', '${upperCaseClassName}_ALL', '${upperCaseClassName}_EDIT')")
    public ResponseEntity<Void> update(@Validated @RequestBody ${className} resources) {
        ${changeClassName}Service.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除${className}")
    @DeleteMapping(value = "/${changeClassName}/{${pkChangeColName}}")
    @PreAuthorize("hasAnyRole('ADMIN', '${upperCaseClassName}_ALL', '${upperCaseClassName}_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable ${pkColumnType} ${pkChangeColName}) {
        ${changeClassName}Service.delete(${pkChangeColName});
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
