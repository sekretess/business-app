package com.sekretess.repository;

import com.sekretess.model.SessionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SessionModel,String> {
}
