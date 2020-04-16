package com.dxj.tool.repository;

import com.dxj.tool.domain.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author dxj
 * @date 2019-05-26
 */
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * 获取有效的验证码
     * @param scenes 业务场景，如重置密码，重置邮箱等等
     * @param type
     * @param value
     * @return
     */
    VerificationCode findByScenesAndTypeAndValueAndStatusIsTrue(String scenes, String type, String value);
}
