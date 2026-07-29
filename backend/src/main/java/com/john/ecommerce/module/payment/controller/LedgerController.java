package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.LedgerAccountVO;
import com.john.ecommerce.module.payment.entity.LedgerAccount;
import com.john.ecommerce.module.payment.entity.LedgerFlow;
import com.john.ecommerce.module.payment.mapper.LedgerAccountMapper;
import com.john.ecommerce.module.payment.mapper.LedgerFlowMapper;
import com.john.ecommerce.module.payment.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.LEDGER)
public class LedgerController {

    private final LedgerAccountMapper accountMapper;
    private final LedgerFlowMapper flowMapper;
    private final LedgerService ledgerService;

    @GetMapping("/account")
    public R<LedgerAccountVO> myAccount(@RequestParam(defaultValue = "USER_BALANCE") String accountType) {
        Long userId = UserContext.getCurrentUserId();
        LedgerAccount account = accountMapper.selectOne(new LambdaQueryWrapper<LedgerAccount>()
                .eq(LedgerAccount::getOwnerType, "USER")
                .eq(LedgerAccount::getOwnerId, userId)
                .eq(LedgerAccount::getAccountType, accountType)
                .last("LIMIT 1"));
        if (account == null) {
            account = ledgerService.openAccount(TenantContext.getTenantId(),
                    "USER", userId, accountType, "CNY");
        }
        return R.ok(toVO(account));
    }

    @PostMapping("/recharge")
    public R<Void> recharge(@RequestParam long amount) {
        Long userId = UserContext.getCurrentUserId();
        Long tenantId = TenantContext.getTenantId();
        ledgerService.openAccount(tenantId, "USER", userId, "USER_BALANCE", "CNY");
        ledgerService.credit(tenantId, "USER_BALANCE", userId, amount, "RECHARGE", null);
        return R.ok();
    }

    @GetMapping("/flow")
    public R<Page<LedgerFlow>> flows(@RequestParam Long accountId,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return R.ok(flowMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<LedgerFlow>()
                        .eq(LedgerFlow::getLedgerAccountId, accountId)
                        .orderByDesc(LedgerFlow::getCreatedAt)));
    }

    private LedgerAccountVO toVO(LedgerAccount a) {
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
}
