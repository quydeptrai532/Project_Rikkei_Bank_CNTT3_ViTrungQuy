package com.example.rikkeibank.aspect;

import com.example.rikkeibank.model.dto.request.TransferRequest;
import com.example.rikkeibank.model.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TransactionAuditAspect {

    // Kích hoạt khi hàm performTransfer thực thi thành công trả về thực thể Transaction
    @AfterReturning(
            pointcut = "execution(* com.example.rikkeibank.service.TransactionService.performTransfer(..))",
            returning = "result"
    )
    public void logTransferSuccess(JoinPoint joinPoint, Object result) {
        if (result instanceof Transaction) {
            Transaction tx = (Transaction) result;
            log.info("[AUDIT] Account {} transferred {} to Account {}. Transaction Code: {}",
                    tx.getFromAccount().getAccountNumber(),
                    tx.getAmount(),
                    tx.getToAccount().getAccountNumber(),
                    tx.getTransactionCode());
        }
    }

    // Kích hoạt khi hàm ném ra bất kỳ Exception nào (ví dụ: Không đủ số dư)
    @AfterThrowing(
            pointcut = "execution(* com.example.rikkeibank.service.TransactionService.performTransfer(..))",
            throwing = "error"
    )
    public void logTransferFailure(JoinPoint joinPoint, Throwable error) {
        Object[] args = joinPoint.getArgs();
        String targetAccount = "N/A";
        String amount = "0";

        if (args.length > 1 && args[1] instanceof TransferRequest) {
            TransferRequest request = (TransferRequest) args[1];
            targetAccount = request.getTargetAccountNumber();
            amount = request.getAmount() != null ? request.getAmount().toString() : "0";
        }

        log.error("[AUDIT] GIAO DỊCH CHUYỂN TIỀN THẤT BẠI tới tài khoản {}. Số tiền: {}. Nguyên nhân: {}",
                targetAccount, amount, error.getMessage());
    }
}