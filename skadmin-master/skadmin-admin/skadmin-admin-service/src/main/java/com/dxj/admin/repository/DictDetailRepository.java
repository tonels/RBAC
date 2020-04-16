package com.dxj.admin.repository;

import com.dxj.admin.domain.entity.DictDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
* @author dxj
* @date 2019-04-10
*/
public interface DictDetailRepository extends JpaRepository<DictDetail, Long>, JpaSpecificationExecutor<DictDetail> {
}
