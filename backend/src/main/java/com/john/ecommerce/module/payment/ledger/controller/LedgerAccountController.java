package com.john.ecommerce.module.payment.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.LedgerAccountOpenDTO;
import com.john.ecommerce.module.payment.dto.LedgerAccountVO;
import com.john.ecommerce.module.payment.dto.LedgerCreditDTO;
import com.john.ecommerce.module.payment.dto.LedgerFlowVO;
import com.john.ecommerce.module.payment.dto.LedgerTxnVO;
import com.john.ecommerce.module.payment.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.LEDGER)
@PreAuthorize("hasRole('OPS')")
public class LedgerAccountController {

    private final LedgerService ledgerService;

    @PostMapping("/ledger/account/open")
    public R<LedgerAccountVO> open(@RequestBody LedgerAccountOpenDTO dto) {
        if (dto.getCurrency() == null || dto.getCurrency().isBlank()) {
            dto.setCurrency("CNY");
        }
        return R.ok(ledgerService.openAccount(dto));
    }

    @GetMapping("/ledger/accounts")
    public R<Page<LedgerAccountVO>> listAccounts(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String ownerType,
                                                 @RequestParam(required = false) Long ownerId) {
        return R.ok(ledgerService.listAccountVOs(page, size, ownerType, ownerId));
    }

    @GetMapping("/ledger/account/{id}")
    public R<LedgerAccountVO> getAccount(@PathVariable Long id) {
        return R.ok(ledgerService.getAccountVO(id));
    }

    @PostMapping("/ledger/account/{id}/credit")
    public R<Void> credit(@PathVariable Long id, @RequestBody LedgerCreditDTO dto) {
        ledgerService.credit(id, dto);
        return R.ok();
    }

    @GetMapping("/ledger/flow")
    public R<Page<LedgerFlowVO>> listFlows(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @RequestParam Long accountId) {
        return R.ok(ledgerService.listFlowVOs(page, size, accountId));
    }

    @GetMapping("/ledger/txn")
    public R<Page<LedgerTxnVO>> listTxns(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) String refType,
                                         @RequestParam(required = false) Long refId) {
        return R.ok(ledgerService.listTxnVOs(page, size, refType, refId));
    }
}
