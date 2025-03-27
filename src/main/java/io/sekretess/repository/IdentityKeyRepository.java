package io.sekretess.repository;

import io.sekretess.model.IdentityKeyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdentityKeyRepository extends JpaRepository<IdentityKeyModel, String> {
}
