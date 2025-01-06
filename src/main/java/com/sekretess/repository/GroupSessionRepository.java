package com.sekretess.repository;

import com.sekretess.model.GroupSessionModel;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@DynamicUpdate
public interface GroupSessionRepository extends JpaRepository<GroupSessionModel,String> {
}
