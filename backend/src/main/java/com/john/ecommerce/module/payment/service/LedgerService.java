package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.entity.LedgerAccount;
import com.john.ecommerce.module.payment.entity.LedgerFlow;
import com.john.ecommerce.module.payment.entity.LedgerTxn;
import com.john.ecommerce.module.payment.mapper.LedgerAccountMapper;
import com.john.ecommerce.module.payment.mapper.LedgerFlowMapper;
import com.john.ecommerce.module.payment.mapper.LedgerTxnMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerAccountMapper accountMapper;
    private final LedgerFlowMapper flowMapper;
    private final LedgerTxnMapper txnMapper;

    public LedgerAccount openAccount(Long tenantId, String ownerType, Long ownerId,
                                     String accountType, String currency) {
        LedgerAccount existing = accountMapper.selectOne(new LambdaQueryWrapper<LedgerAccount>()
                .eq(LedgerAccount::getOwnerType, ownerType)
                .eq(LedgerAccount::getOwnerId, ownerId)
                .eq(LedgerAccount::getAccountType, accountType)
                .last("LIMIT 1"));
        if (existing != null) return existing;

        LedgerAccount account = new LedgerAccount();
        account.setTenantId(tenantId);
        account.setOwnerType(ownerType);
        account.setOwnerId(ownerId);
        account.setAccountType(accountType);
        account.setCurrency(currency != null ? currency : "CNY");
        account.setBalance(0L);
        account.setFrozen(0L);
        account.setAvailable(0L);
        account.setVersion(0L);
        account.setStatus(1);
        accountMapper.insert(account);
        return account;
    }

    @Transactional
    public void credit(Long tenantId, String accountType, Long ownerId, long amount,
                       String refType, Long refId) {
        LedgerAccount account = requireAccount(accountType, ownerId);
        long before = account.getBalance();
        account.setBalance(before + amount);
        account.setAvailable(account.getAvailable() + amount);
        updateWithVersion(account);

        saveTxn(tenantId, account.getId(), "CREDIT", amount, refType, refId);
        saveFlow(tenantId, account.getId(), "IN", "CREDIT", amount, before, account.getBalance(), refType, refId);
    }

    @Transactional
    public void debit(Long tenantId, String accountType, Long ownerId, long amount,
                      String refType, Long refId) {
        LedgerAccount account = requireAccount(accountType, ownerId);
        if (account.getAvailable() < amount) throw new BizException("余额不足");
        long before = account.getBalance();
        account.setBalance(before - amount);
        account.setAvailable(account.getAvailable() - amount);
        updateWithVersion(account);

        saveTxn(tenantId, account.getId(), "DEBIT", amount, refType, refId);
        saveFlow(tenantId, account.getId(), "OUT", "DEBIT", amount, before, account.getBalance(), refType, refId);
    }

    @Transactional
    public void freeze(Long tenantId, String accountType, Long ownerId, long amount,
                       String refType, Long refId) {
        LedgerAccount account = requireAccount(accountType, ownerId);
        if (account.getAvailable() < amount) throw new BizException("可用余额不足");
        account.setFrozen(account.getFrozen() + amount);
        account.setAvailable(account.getAvailable() - amount);
        updateWithVersion(account);

        saveTxn(tenantId, account.getId(), "FREEZE", amount, refType, refId);
        saveFlow(tenantId, account.getId(), "OUT", "FREEZE", amount, account.getBalance(), account.getBalance(), refType, refId);
    }

    @Transactional
    public void unfreeze(Long tenantId, String accountType, Long ownerId, long amount,
                         String refType, Long refId) {
        LedgerAccount account = requireAccount(accountType, ownerId);
        if (account.getFrozen() < amount) throw new BizException("冻结金额不足");
        account.setFrozen(account.getFrozen() - amount);
        account.setAvailable(account.getAvailable() + amount);
        updateWithVersion(account);

        saveTxn(tenantId, account.getId(), "UNFREEZE", amount, refType, refId);
        saveFlow(tenantId, account.getId(), "IN", "UNFREEZE", amount, account.getBalance(), account.getBalance(), refType, refId);
    }

    @Transactional
    public void freezeDebit(Long tenantId, String accountType, Long ownerId, long amount,
                            String refType, Long refId) {
        LedgerAccount account = requireAccount(accountType, ownerId);
        if (account.getAvailable() < amount) throw new BizException("可用余额不足");
        long before = account.getBalance();
        account.setBalance(before - amount);
        account.setAvailable(account.getAvailable() - amount);
        updateWithVersion(account);

        saveTxn(tenantId, account.getId(), "FREEZE_DEBIT", amount, refType, refId);
        saveFlow(tenantId, account.getId(), "OUT", "FREEZE_DEBIT", amount, before, account.getBalance(), refType, refId);
    }

    private LedgerAccount requireAccount(String accountType, Long ownerId) {
        LedgerAccount account = accountMapper.selectOne(new LambdaQueryWrapper<LedgerAccount>()
                .eq(LedgerAccount::getAccountType, accountType)
                .eq(LedgerAccount::getOwnerId, ownerId)
                .last("LIMIT 1"));
        if (account == null) throw new BizException("账户不存在");
        return account;
    }

    private void updateWithVersion(LedgerAccount account) {
        int rows = accountMapper.updateById(account);
        if (rows == 0) throw new BizException("账户更新冲突，请重试");
    }

    private void saveTxn(Long tenantId, Long accountId, String txnType, long amount,
                         String refType, Long refId) {
        LedgerTxn txn = new LedgerTxn();
        txn.setTenantId(tenantId);
        txn.setTxnNo(UUID.randomUUID().toString().replace("-", ""));
        txn.setLedgerAccountId(accountId);
        txn.setTxnType(txnType);
        txn.setAmount(amount);
        txn.setStatus(1);
        txnMapper.insert(txn);
    }

    private void saveFlow(Long tenantId, Long accountId, String direction, String txnType,
                          long amount, long before, long after, String refType, Long refId) {
        LedgerFlow flow = new LedgerFlow();
        flow.setTenantId(tenantId);
        flow.setLedgerAccountId(accountId);
        flow.setFlowNo(UUID.randomUUID().toString().replace("-", ""));
        flow.setDirection(direction);
        flow.setTxnType(txnType);
        flow.setAmount(amount);
        flow.setBalanceBefore(before);
        flow.setBalanceAfter(after);
        flow.setRefType(refType);
        flow.setRefId(refId);
        flowMapper.insert(flow);
    }
}
