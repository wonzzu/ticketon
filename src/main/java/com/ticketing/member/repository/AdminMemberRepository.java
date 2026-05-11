package com.ticketing.member.repository;

import com.ticketing.member.domain.AdminMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminMemberRepository extends JpaRepository<AdminMember, Long> {

    boolean existsByEmployeeNumber(String employeeNumber);
}
