package com.john.ecommerce.module.payment.ledger.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.dto.LedgerAccountOpenDTO;
import com.john.ecommerce.module.payment.dto.LedgerAccountVO;
import com.john.ecommerce.module.payment.dto.LedgerCreditDTO;
import com.john.ecommerce.module.payment.dto.LedgerFlowVO;
import com.john.ecommerce.module.payment.dto.LedgerTxnVO;
import com.john.ecommerce.module.payment.ledger.entity.LedgerAccount;
import com.john.ecommerce.module.payment.ledger.entity.LedgerFlow;
import com.john.ecommerce.module.payment.ledger.entity.LedgerTxn;
import com.john.ecommerce.module.payment.ledger.enums.LedgerDirection;
import com.john.ecommerce.module.payment.ledger.enums.LedgerTxnType;
import com.john.ecommerce.module.payment.ledger.mapper.LedgerAccountMapper;
import com.john.ecommerce.module.payment.ledger.mapper.LedgerFlowMapper;
import com.john.ecommerce.module.payment.ledger.mapper.LedgerTxnMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerAccountMapper accountMapper;
    private final LedgerTxnMapper txnMapper;
    private final LedgerFlowMapper flowMapper;

    @Transactional
    public LedgerAccount openAccount(String ownerType, Long ownerId, String accountType, String currency) {
        LedgerAccount existing = accountMapper.selectOne(
                new LambdaQueryWrapper<LedgerAccount>()
                        .eq(LedgerAccount::getOwnerType, ownerType)
                        .eq(LedgerAccount::getOwnerId, ownerId)
                        .eq(LedgerAccount::getAccountType, accountType)
                        .eq(LedgerAccount::getCurrency, currency != null ? currency : "CNY"));
        if (existing != null) return existing;

        LedgerAccount account = new LedgerAccount();
        account.setOwnerType(ownerType);
        account.setOwnerId(ownerId);
        account.setAccountType(accountType);
        account.setCurrency(currency != null ? currency : "CNY");
        account.setBalance(0L);
        account.setFrozen(0L);
        account.setAvailable(0L);
        account.setVersion(0);
        account.setStatus(1);
        accountMapper.insert(account);
        return account;
    }

    @Transactional
    public void credit(Long accountId, long amount, String bizType, String refType, Long refId, String remark) {
        if (amount <= 0) throw new BizException("入账金额必须大于0");
        LedgerAccount account = getAndLock(accountId);
        long before = account.getBalance();
        account.setBalance(before + amount);
        account.setAvailable(account.getAvailable() + amount);
        accountMapper.updateById(account);
        recordTxnAndFlow(account, LedgerTxnType.CREDIT, LedgerDirection.IN, amount, before, account.getBalance(), bizType, refType, refId, remark);
    }

    @Transactional
    public void debit(Long accountId, long amount, String bizType, String refType, Long refId, String remark) {
        if (amount <= 0) throw new BizException("扣款金额必须大于0");
        LedgerAccount account = getAndLock(accountId);
        if (account.getAvailable() < amount) throw new BizException("可用余额不足");
        long before = account.getBalance();
        account.setBalance(before - amount);
        account.setAvailable(account.getAvailable() - amount);
        accountMapper.updateById(account);
        recordTxnAndFlow(account, LedgerTxnType.DEBIT, LedgerDirection.OUT, amount, before, account.getBalance(), bizType, refType, refId, remark);
    }

    @Transactional
    public void freeze(Long accountId, long amount, String bizType, String refType, Long refId, String remark) {
        if (amount <= 0) throw new BizException("冻结金额必须大于0");
        LedgerAccount account = getAndLock(accountId);
        if (account.getAvailable() < amount) throw new BizException("可用余额不足，无法冻结");
        account.setFrozen(account.getFrozen() + amount);
        account.setAvailable(account.getAvailable() - amount);
        accountMapper.updateById(account);
        recordTxnAndFlow(account, LedgerTxnType.FREEZE, LedgerDirection.OUT, amount, account.getBalance(), account.getBalance(), bizType, refType, refId, remark);
    }

    @Transactional
    public void unfreeze(Long accountId, long amount, String bizType, String refType, Long refId, String remark) {
        if (amount <= 0) throw new BizException("解冻金额必须大于0");
        LedgerAccount account = getAndLock(accountId);
        if (account.getFrozen() < amount) throw new BizException("冻结金额不足");
        account.setFrozen(account.getFrozen() - amount);
        account.setAvailable(account.getAvailable() + amount);
        accountMapper.updateById(account);
        recordTxnAndFlow(account, LedgerTxnType.UNFREEZE, LedgerDirection.IN, amount, account.getBalance(), account.getBalance(), bizType, refType, refId, remark);
    }

    @Transactional
    public void freezeDebit(Long accountId, long amount, String bizType, String refType, Long refId, String remark) {
        if (amount <= 0) throw new BizException("冻结扣款金额必须大于0");
        LedgerAccount account = getAndLock(accountId);
        if (account.getFrozen() < amount) throw new BizException("冻结金额不足");
        long before = account.getBalance();
        account.setBalance(before - amount);
        account.setFrozen(account.getFrozen() - amount);
        accountMapper.updateById(account);
        recordTxnAndFlow(account, LedgerTxnType.FREEZE_DEBIT, LedgerDirection.OUT, amount, before, account.getBalance(), bizType, refType, refId, remark);
    }

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, long amount, String bizType, String refType, Long refId, String remark) {
        debit(fromAccountId, amount, bizType, refType, refId, "转出: " + remark);
        credit(toAccountId, amount, bizType, refType, refId, "转入: " + remark);
    }

    public LedgerAccount getById(Long id) {
        return accountMapper.selectById(id);
    }

    public Page<LedgerAccount> listAccounts(int page, int size, String ownerType, Long ownerId) {
        LambdaQueryWrapper<LedgerAccount> w = new LambdaQueryWrapper<LedgerAccount>()
                .eq(ownerType != null, LedgerAccount::getOwnerType, ownerType)
                .eq(ownerId != null, LedgerAccount::getOwnerId, ownerId)
                .orderByDesc(LedgerAccount::getCreatedAt);
        return accountMapper.selectPage(new Page<>(page, size), w);
    }

    public Page<LedgerFlow> listFlows(int page, int size, Long accountId) {
        return flowMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<LedgerFlow>()
                        .eq(LedgerFlow::getLedgerAccountId, accountId)
                        .orderByDesc(LedgerFlow::getCreatedAt));
    }

    public Page<LedgerTxn> listTxns(int page, int size, String refType, Long refId) {
        LambdaQueryWrapper<LedgerTxn> w = new LambdaQueryWrapper<LedgerTxn>()
                .eq(refType != null, LedgerTxn::getRefType, refType)
                .eq(refId != null, LedgerTxn::getRefId, refId)
                .orderByDesc(LedgerTxn::getCreatedAt);
        return txnMapper.selectPage(new Page<>(page, size), w);
    }

    @Transactional
    public LedgerAccountVO openAccount(LedgerAccountOpenDTO dto) {
        return toAccountVO(openAccount(dto.getOwnerType(), dto.getOwnerId(), dto.getAccountType(), dto.getCurrency()));
    }

    public LedgerAccountVO getAccountVO(Long id) {
        LedgerAccount account = getById(id);
        if (account == null) throw new BizException("账本账户不存在");
        return toAccountVO(account);
    }

    public Page<LedgerAccountVO> listAccountVOs(int page, int size, String ownerType, Long ownerId) {
        Page<LedgerAccount> p = listAccounts(page, size, ownerType, ownerId);
        Page<LedgerAccountVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toAccountVO).toList());
        return result;
    }

    @Transactional
    public void credit(Long accountId, LedgerCreditDTO dto) {
        long amount = dto.getAmount() != null ? dto.getAmount() : 0L;
        String remark = dto.getRemark() != null ? dto.getRemark() : "手动充值";
        credit(accountId, amount, "RECHARGE", "MANUAL", null, remark);
    }

    public Page<LedgerFlowVO> listFlowVOs(int page, int size, Long accountId) {
        Page<LedgerFlow> p = listFlows(page, size, accountId);
        Page<LedgerFlowVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toFlowVO).toList());
        return result;
    }

    public Page<LedgerTxnVO> listTxnVOs(int page, int size, String refType, Long refId) {
        Page<LedgerTxn> p = listTxns(page, size, refType, refId);
        Page<LedgerTxnVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toTxnVO).toList());
        return result;
    }

    public LedgerAccountVO toAccountVO(LedgerAccount a) {
        LedgerAccountVO vo = new LedgerAccountVO();
        vo.setId(a.getId());
        vo.setOwnerType(a.getOwnerType());
        vo.setOwnerId(a.getOwnerId());
        vo.setAccountType(a.getAccountType());
        vo.setCurrency(a.getCurrency());
        vo.setBalance(a.getBalance());
        vo.setFrozen(a.getFrozen());
        vo.setAvailable(a.getAvailable());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }

    private LedgerFlowVO toFlowVO(LedgerFlow f) {
        LedgerFlowVO vo = new LedgerFlowVO();
        vo.setId(f.getId());
        vo.setLedgerAccountId(f.getLedgerAccountId());
        vo.setTxnId(f.getTxnId());
        vo.setDirection(f.getDirection());
        vo.setAmount(f.getAmount());
        vo.setBalanceBefore(f.getBalanceBefore());
        vo.setBalanceAfter(f.getBalanceAfter());
        vo.setBizType(f.getBizType());
        vo.setRefType(f.getRefType());
        vo.setRefId(f.getRefId());
        vo.setRemark(f.getRemark());
        vo.setCreatedAt(f.getCreatedAt());
        return vo;
    }

    private LedgerTxnVO toTxnVO(LedgerTxn t) {
        LedgerTxnVO vo = new LedgerTxnVO();
        vo.setId(t.getId());
        vo.setTxnNo(t.getTxnNo());
        vo.setTxnType(t.getTxnType());
        vo.setAmount(t.getAmount());
        vo.setCurrency(t.getCurrency());
        vo.setStatus(t.getStatus());
        vo.setBizType(t.getBizType());
        vo.setRefType(t.getRefType());
        vo.setRefId(t.getRefId());
        vo.setRemark(t.getRemark());
        vo.setCreatedAt(t.getCreatedAt());
        return vo;
    }

    private LedgerAccount getAndLock(Long accountId) {
        LedgerAccount account = accountMapper.selectById(accountId);
        if (account == null) throw new BizException("账本账户不存在: " + accountId);
        return account;
    }

    private void recordTxnAndFlow(LedgerAccount account, LedgerTxnType txnType, LedgerDirection direction,
                                  long amount, long balanceBefore, long balanceAfter,
                                  String bizType, String refType, Long refId, String remark) {
        LedgerTxn txn = new LedgerTxn();
        txn.setTxnNo("LT" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        txn.setTxnType(txnType.getCode());
        txn.setAmount(amount);
        txn.setCurrency(account.getCurrency());
        txn.setStatus(1);
        txn.setBizType(bizType);
        txn.setRefType(refType);
        txn.setRefId(refId);
        txn.setRemark(remark);
        txnMapper.insert(txn);

        LedgerFlow flow = new LedgerFlow();
        flow.setLedgerAccountId(account.getId());
        flow.setTxnId(txn.getId());
        flow.setDirection(direction.getCode());
        flow.setAmount(amount);
        flow.setBalanceBefore(balanceBefore);
        flow.setBalanceAfter(balanceAfter);
        flow.setBizType(bizType);
        flow.setRefType(refType);
        flow.setRefId(refId);
        flow.setRemark(remark);
        flowMapper.insert(flow);
    }
}
