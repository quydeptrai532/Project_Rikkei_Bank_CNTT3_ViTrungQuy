package com.example.rikkeibank.repository;

import com.example.rikkeibank.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // UC-06: Lấy danh sách giao dịch mà tài khoản này tham gia (gửi hoặc nhận)
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId")
    Page<Transaction> findTransactionsByAccountId(@Param("accountId") Long accountId, Pageable pageable);
}