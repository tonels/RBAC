package com.dxj.tool.repository;

import com.dxj.tool.domain.entity.QuartzJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author dxj
 * @date 2019-01-07
 */
public interface QuartzJobRepository extends JpaRepository<QuartzJob, Long>, JpaSpecificationExecutor<QuartzJob> {

    /**
     * 查询不是启用的任务
     *
     * @return
     */
    List<QuartzJob> findByIsPauseIsFalse();
}
