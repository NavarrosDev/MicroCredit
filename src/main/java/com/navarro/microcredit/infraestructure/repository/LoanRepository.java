package com.navarro.microcredit.infraestructure.repository;

import com.navarro.microcredit.domain.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    Optional<Loan> findByClientId(UUID clientId);
}
