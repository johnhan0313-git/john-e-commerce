package com.john.ecommerce.module.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.fulfillment.dto.LogisticsCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.LogisticsVO;
import com.john.ecommerce.module.fulfillment.service.LogisticsService;
import com.john.ecommerce.module.merchant.entity.Shop;
import com.john.ecommerce.module.product.dto.SkuCreateDTO;
import com.john.ecommerce.module.product.dto.SkuVO;
import com.john.ecommerce.module.product.dto.SpuCreateDTO;
import com.john.ecommerce.module.product.dto.SpuVO;
import com.john.ecommerce.module.product.entity.Sku;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.SkuMapper;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import com.john.ecommerce.module.product.service.ProductService;
import com.john.ecommerce.module.product.service.SkuService;
import com.john.ecommerce.module.trade.dto.OrderVO;
import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.john.ecommerce.module.trade.service.OrderService;
import com.john.ecommerce.module.payment.entity.SettlementBill;
import com.john.ecommerce.module.payment.entity.SettlementOrder;
import com.john.ecommerce.module.payment.service.SettlementBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopPortalService {

    private final MerchantService merchantService;
    private final ProductService productService;
    private final SkuService skuService;
    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final LogisticsService logisticsService;
    private final SettlementBillService settlementBillService;

    public Page<SpuVO> listProducts(int page, int size, Integer status) {
        Shop shop = merchantService.requireCurrentShop();
        return productService.list(page, size, status, shop.getId(), null);
    }

    public SpuVO createProduct(SpuCreateDTO dto) {
        Shop shop = merchantService.requireCurrentShop();
        dto.setShopId(shop.getId());
        dto.setMerchantId(shop.getMerchantId());
        return productService.create(dto);
    }

    public SpuVO updateProduct(Long spuId, SpuCreateDTO dto) {
        Spu spu = requireOwnedSpu(spuId);
        // 门户不允许改归属
        dto.setShopId(spu.getShopId());
        dto.setMerchantId(spu.getMerchantId());
        return productService.update(spuId, dto);
    }

    public void updateProductStatus(Long spuId, Integer status) {
        Spu spu = requireOwnedSpu(spuId);
        productService.updateStatus(spu.getId(), status);
    }

    public List<SkuVO> listSkus(Long spuId) {
        requireOwnedSpu(spuId);
        return skuService.listBySpu(spuId);
    }

    public SkuVO createSku(Long spuId, SkuCreateDTO dto) {
        requireOwnedSpu(spuId);
        dto.setSpuId(spuId);
        return skuService.create(dto);
    }

    public SkuVO updateSku(Long skuId, SkuCreateDTO dto) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) throw new BizException("SKU不存在");
        requireOwnedSpu(sku.getSpuId());
        dto.setSpuId(sku.getSpuId());
        return skuService.update(skuId, dto);
    }

    public void deleteSku(Long skuId) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) throw new BizException("SKU不存在");
        requireOwnedSpu(sku.getSpuId());
        skuService.delete(skuId);
    }

    public Page<OrderVO> listOrders(int page, int size, Integer status) {
        Shop shop = merchantService.requireCurrentShop();
        return orderService.listForShop(page, size, status, shop.getId());
    }

    public OrderVO getOrder(Long orderId) {
        Shop shop = merchantService.requireCurrentShop();
        return orderService.getByIdForShop(orderId, shop.getId());
    }

    public LogisticsVO ship(Long orderId, LogisticsCreateDTO dto) {
        Shop shop = merchantService.requireCurrentShop();
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BizException("订单不存在");
        if (order.getShopId() == null || !shop.getId().equals(order.getShopId())) {
            throw new BizException("订单不属于当前店铺");
        }
        dto.setOrderId(orderId);
        return logisticsService.createShipment(dto);
    }

    public Page<SettlementBill> listSettlementBills(int page, int size) {
        Shop shop = merchantService.requireCurrentShop();
        return settlementBillService.listBillsForShop(page, size, shop.getId());
    }

    public Page<SettlementOrder> listSettlementOrders(int page, int size) {
        Shop shop = merchantService.requireCurrentShop();
        return settlementBillService.listOrdersForShop(page, size, shop.getId());
    }

    private Spu requireOwnedSpu(Long spuId) {
        Shop shop = merchantService.requireCurrentShop();
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) throw new BizException("商品不存在");
        if (spu.getShopId() == null || !shop.getId().equals(spu.getShopId())) {
            throw new BizException("商品不属于当前店铺");
        }
        return spu;
    }
}
