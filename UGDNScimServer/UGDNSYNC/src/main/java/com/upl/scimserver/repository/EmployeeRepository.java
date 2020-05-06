package com.upl.scimserver.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.upl.scimserver.model.EmployeeModel;

public interface EmployeeRepository extends JpaRepository<EmployeeModel, String> {

 @Query(value =  "SELECT * from employee where source=:source and backwardsync=:backwardsync and (failedcount<=:failedcount or failedcount is null)", 
		 nativeQuery = true)
 public List<EmployeeModel> findBySourceAndBackwardSyncAndFailedCountLessThanEqual(@Param("source") String source, 
		 @Param("backwardsync") int backwardSync,
		 @Param("failedcount") int failedcount);
 
 @Modifying
 @Query("update EmployeeModel e set e.backwardSync = :backwardsyncParam where e.uidNo = :id")
 @Transactional
 void setBackwardSyncStatus(@Param("backwardsyncParam") Integer status, @Param("id") String id);

 @Modifying
 @Query("update EmployeeModel e set e.failedCount = :failedCount, e.failReason= :failReason where e.uidNo = :id")
 @Transactional
 void setFailedStatusWithReson(@Param("failedCount") Integer status,@Param("failReason") String failReason, @Param("id") String id);

}

