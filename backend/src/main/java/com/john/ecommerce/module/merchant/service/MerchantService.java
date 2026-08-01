package com.john.ecommerce.module.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.merchant.dto.MerchantApplyDTO;
import com.john.ecommerce.module.merchant.dto.MerchantAuditDTO;
import com.john.ecommerce.module.merchant.dto.MerchantMeVO;
import com.john.ecommerce.module.merchant.dto.MerchantUpdateDTO;
import com.john.ecommerce.module.merchant.dto.MerchantVO;
import com.john.ecommerce.module.merchant.dto.ShopVO;
import com.john.ecommerce.module.merchant.entity.Merchant;
import com.john.ecommerce.module.merchant.entity.Shop;
import com.john.ecommerce.module.merchant.mapper.MerchantMapper;
import com.john.ecommerce.module.user.service.UserIdentityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantService {

    public static final String HEADER_SHOP_ID = "X-Shop-Id";

    private final MerchantMapper merchantMapper;
    private final ShopService shopService;
    private final UserIdentityService userIdentityService;

    public MerchantVO apply(MerchantApplyDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BizException("用户未登录");
        Merchant existing = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, userId));
        if (existing != null) throw new BizException("已提交入驻申请");

        Merchant merchant = new Merchant();
        merchant.setUserId(userId);
        merchant.setName(dto.getName());
        merchant.setLogo(dto.getLogo());
        merchant.setLicenseNo(dto.getLicenseNo());
        merchant.setLicenseImages(dto.getLicenseImages());
        merchant.setContactName(dto.getContactName());
        merchant.setContactPhone(dto.getContactPhone());
        merchant.setCommissionRate(dto.getCommissionRate() != null ? dto.getCommissionRate() : BigDecimal.ZERO);
        merchant.setStatus(0);
        merchantMapper.insert(merchant);
        userIdentityService.ensureSeller(userId);
        return toVO(merchant);
    }

    /**
     * Update seller entity profile. Does not touch shop status/audit.
     * Rejected merchants are moved back to pending for a fresh主体审核.
     */
    @Transactional
    public MerchantVO updateProfile(MerchantUpdateDTO dto) {
        Merchant merchant = findByCurrentUser();
        if (merchant == null) throw new BizException("尚未入驻");

        merchant.setName(dto.getName().trim());
        merchant.setLogo(dto.getLogo());
        merchant.setLicenseNo(dto.getLicenseNo());
        merchant.setLicenseImages(dto.getLicenseImages());
        merchant.setContactName(dto.getContactName().trim());
        merchant.setContactPhone(dto.getContactPhone().trim());

        if (merchant.getStatus() != null && merchant.getStatus() == 2) {
            merchant.setStatus(0);
        }
        merchantMapper.updateById(merchant);
        return toVO(merchant);
    }

    @Transactional
    public MerchantVO audit(Long id, MerchantAuditDTO dto) {
        Merchant merchant = require(id);
        if (merchant.getStatus() != 0) throw new BizException("当前状态不可审核");
        if (Boolean.TRUE.equals(dto.getApproved())) {
            merchant.setStatus(1);
            merchant.setSettledAt(System.currentTimeMillis());
            merchantMapper.updateById(merchant);
            // First shop bootstrap only; subsequent shops use shop apply/audit.
            shopService.createDefaultShop(merchant.getId(), merchant.getName(), merchant.getLogo());
            userIdentityService.ensureSeller(merchant.getUserId());
        } else {
            merchant.setStatus(2);
            merchantMapper.updateById(merchant);
        }
        return toVO(merchant);
    }

    public MerchantMeVO me() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BizException("用户未登录");
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, userId));
        if (merchant == null) {
            return null;
        }
        MerchantMeVO vo = new MerchantMeVO();
        vo.setMerchant(toVO(merchant));
        List<ShopVO> shops = shopService.listByMerchant(merchant.getId());
        vo.setShops(shops);
        Shop current = resolveCurrentShop(merchant, false);
        if (current != null) {
            vo.setCurrentShop(shopService.toVO(current));
        }
        return vo;
    }

    public Merchant findByCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BizException("用户未登录");
        return merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, userId));
    }

    public Merchant requireApproved() {
        Merchant merchant = findByCurrentUser();
        if (merchant == null) throw new BizException("尚未入驻");
        if (merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new BizException("卖家未通过审核");
        }
        return merchant;
    }

    public Shop requireCurrentShop() {
        Merchant merchant = requireApproved();
        Shop shop = resolveCurrentShop(merchant, true);
        if (shop == null) throw new BizException("店铺不存在");
        return shop;
    }

    private Shop resolveCurrentShop(Merchant merchant, boolean requireOpen) {
        Long headerShopId = readShopIdHeader();
        if (headerShopId != null) {
            if (requireOpen) {
                return shopService.requireOwnedOpen(headerShopId, merchant.getId());
            }
            return shopService.requireOwned(headerShopId, merchant.getId());
        }
        Shop fallback = shopService.findDefaultByMerchantId(merchant.getId());
        if (fallback == null && requireOpen) {
            throw new BizException("暂无营业中的店铺");
        }
        return fallback;
    }

    private Long readShopIdHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        String raw = request.getHeader(HEADER_SHOP_ID);
        if (raw == null || raw.isBlank()) return null;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new BizException("无效的店铺 ID");
        }
    }

    public MerchantVO getById(Long id) {
        return toVO(require(id));
    }

    public Page<MerchantVO> list(int page, int size, Integer status) {
        Page<Merchant> p = merchantMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Merchant>()
                        .eq(status != null, Merchant::getStatus, status)
                        .orderByDesc(Merchant::getCreatedAt));
        Page<MerchantVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    private Merchant require(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) throw new BizException("商家不存在");
        return merchant;
    }

    private MerchantVO toVO(Merchant m) {
        MerchantVO vo = new MerchantVO();
        vo.setId(m.getId());
        vo.setUserId(m.getUserId());
        vo.setName(m.getName());
        vo.setLogo(m.getLogo());
        vo.setLicenseNo(m.getLicenseNo());
        vo.setLicenseImages(m.getLicenseImages());
        vo.setContactName(m.getContactName());
        vo.setContactPhone(m.getContactPhone());
        vo.setStatus(m.getStatus());
        vo.setStatusLabel(statusLabel(m.getStatus()));
        vo.setCommissionRate(m.getCommissionRate());
        vo.setSettledAt(m.getSettledAt());
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }

    private String statusLabel(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }
}
