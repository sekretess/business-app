package com.sekretess.repository;

import com.sekretess.model.IdentityKeyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdentityKeyRepository extends JpaRepository<IdentityKeyModel, String> {
}
