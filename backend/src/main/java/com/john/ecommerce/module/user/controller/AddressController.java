package com.john.ecommerce.module.user.controller;

import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.user.dto.AddressCreateDTO;
import com.john.ecommerce.module.user.dto.AddressVO;
import com.john.ecommerce.module.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public R<AddressVO> create(@Valid @RequestBody AddressCreateDTO dto) {
        return R.ok(addressService.create(dto));
    }

    @PutMapping("/{id}")
    public R<AddressVO> update(@PathVariable Long id, @Valid @RequestBody AddressCreateDTO dto) {
        return R.ok(addressService.update(id, dto));
    }

    @GetMapping("/{id}")
    public R<AddressVO> getById(@PathVariable Long id) {
        return R.ok(addressService.getById(id));
    }

    @GetMapping
    public R<List<AddressVO>> list() {
        return R.ok(addressService.listMine());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id);
        return R.ok();
    }
}
