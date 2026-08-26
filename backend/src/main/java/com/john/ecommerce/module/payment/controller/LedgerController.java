package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.LedgerAccountVO;
import com.john.ecommerce.module.payment.dto.LedgerFlowVO;
import com.john.ecommerce.module.payment.ledger.entity.LedgerAccount;
import com.john.ecommerce.module.payment.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.LEDGER)
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/my-account")
    public R<LedgerAccountVO> myAccount(@RequestParam(defaultValue = "USER_BALANCE") String accountType) {
        Long userId = UserContext.getCurrentUserId();
        LedgerAccount account = ledgerService.openAccount("USER", userId, accountType, "CNY");
        return R.ok(ledgerService.toAccountVO(account));
    }

    @PostMapping("/recharge")
    @PreAuthorize("hasRole('OPS')")
    public R<Void> recharge(@RequestParam long amount) {
        Long userId = UserContext.getCurrentUserId();
        LedgerAccount account = ledgerService.openAccount("USER", userId, "USER_BALANCE", "CNY");
        ledgerService.credit(account.getId(), amount, "RECHARGE", "MANUAL", null, "用户充值");
        return R.ok();
    }

    @GetMapping("/my-flow")
    public R<Page<LedgerFlowVO>> myFlows(@RequestParam(defaultValue = "USER_BALANCE") String accountType,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Long userId = UserContext.getCurrentUserId();
        LedgerAccount account = ledgerService.openAccount("USER", userId, accountType, "CNY");
        return R.ok(ledgerService.listFlowVOs(page, size, account.getId()));
    }
}
