package com.john.ecommerce.module.payment.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.ledger.entity.LedgerAccount;
import com.john.ecommerce.module.payment.ledger.entity.LedgerFlow;
import com.john.ecommerce.module.payment.ledger.entity.LedgerTxn;
import com.john.ecommerce.module.payment.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.LEDGER)
public class LedgerAccountController {

    private final LedgerService ledgerService;

    @PostMapping("/ledger/account/open")
    public R<LedgerAccount> open(@RequestBody Map<String, Object> body) {
        String ownerType = (String) body.get("ownerType");
        Long ownerId = Long.valueOf(body.get("ownerId").toString());
        String accountType = (String) body.get("accountType");
        String currency = (String) body.getOrDefault("currency", "CNY");
        return R.ok(ledgerService.openAccount(ownerType, ownerId, accountType, currency));
    }

    @GetMapping("/ledger/accounts")
    public R<Page<LedgerAccount>> listAccounts(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(required = false) String ownerType,
                                               @RequestParam(required = false) Long ownerId) {
        return R.ok(ledgerService.listAccounts(page, size, ownerType, ownerId));
    }

    @GetMapping("/ledger/account/{id}")
    public R<LedgerAccount> getAccount(@PathVariable Long id) {
        return R.ok(ledgerService.getById(id));
    }

    @PostMapping("/ledger/account/{id}/credit")
    public R<Void> credit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        long amount = Long.parseLong(body.get("amount").toString());
        String remark = (String) body.getOrDefault("remark", "手动充值");
        ledgerService.credit(id, amount, "RECHARGE", "MANUAL", null, remark);
        return R.ok();
    }

    @GetMapping("/ledger/flow")
    public R<Page<LedgerFlow>> listFlows(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam Long accountId) {
        return R.ok(ledgerService.listFlows(page, size, accountId));
    }

    @GetMapping("/ledger/txn")
    public R<Page<LedgerTxn>> listTxns(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) String refType,
                                       @RequestParam(required = false) Long refId) {
        return R.ok(ledgerService.listTxns(page, size, refType, refId));
    }
}
