package com.ecommerce.payment.job.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.payment.job.model.PaymentJob;
import com.ecommerce.payment.job.model.PaymentJobStatus;

public interface PaymentJobRepository extends JpaRepository<PaymentJob, Long> {
    @Query("""
    select pj.id
    from PaymentJob pj
    where pj.status = :jobStatus
    order by pj.id asc
    """)
    List<Long> findBatchTargets(
            @Param("jobStatus") PaymentJobStatus jobStatus,
            Pageable pageable
    );

    @Query("""
        update PaymentJob pj
           set pj.status = :toStatus
         where pj.id = :jobId
        """)
    @Modifying
    @Transactional
    int updateStatus(
            @Param("jobId") Long jobId,
            @Param("toStatus") PaymentJobStatus toStatus
    );
}
