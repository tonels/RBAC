package com.dxj.tool.repository;

import com.dxj.tool.domain.entity.QuartzLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author dxj
 * @date 2019-01-07
 */
public interface QuartzLogRepository extends JpaRepository<QuartzLog, Long>, JpaSpecificationExecutor<QuartzLog> {

}
