package com.netdatel.adminserviceapi.repository;

import com.netdatel.adminserviceapi.entity.ClientHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientHistoryRepository extends JpaRepository<ClientHistory, Integer> {

    List<ClientHistory> findByClientIdOrderByChangeDateDesc(Integer clientId);

    Page<ClientHistory> findByClientIdOrderByChangeDateDesc(Integer clientId, Pageable pageable);
}