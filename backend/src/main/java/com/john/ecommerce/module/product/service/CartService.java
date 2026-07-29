package com.john.ecommerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.product.dto.CartAddDTO;
import com.john.ecommerce.module.product.dto.CartVO;
import com.john.ecommerce.module.product.entity.Cart;
import com.john.ecommerce.module.product.entity.Sku;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.CartMapper;
import com.john.ecommerce.module.product.mapper.SkuMapper;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;

    public CartVO add(CartAddDTO dto) {
        Long userId = requireUserId();
        Sku sku = skuMapper.selectById(dto.getSkuId());
        if (sku == null) throw new BizException("SKU不存在");

        Cart existing = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getSkuId, dto.getSkuId()));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            cartMapper.updateById(existing);
            return toVO(existing, sku);
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setSkuId(dto.getSkuId());
        cart.setQuantity(dto.getQuantity());
        cart.setSelected(1);
        cartMapper.insert(cart);
        return toVO(cart, sku);
    }

    public CartVO updateQuantity(Long id, Integer quantity) {
        Cart cart = requireOwned(id);
        if (quantity == null || quantity < 1) throw new BizException("数量无效");
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        Sku sku = skuMapper.selectById(cart.getSkuId());
        return toVO(cart, sku);
    }

    public void updateSelected(Long id, Integer selected) {
        Cart cart = requireOwned(id);
        cart.setSelected(selected != null && selected == 1 ? 1 : 0);
        cartMapper.updateById(cart);
    }

    public List<CartVO> listMine() {
        Long userId = requireUserId();
        return cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .orderByDesc(Cart::getUpdatedAt))
                .stream().map(c -> {
                    Sku sku = skuMapper.selectById(c.getSkuId());
                    return toVO(c, sku);
                }).toList();
    }

    public void delete(Long id) {
        requireOwned(id);
        cartMapper.deleteById(id);
    }

    public void clear() {
        Long userId = requireUserId();
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }

    private Cart requireOwned(Long id) {
        Long userId = requireUserId();
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !userId.equals(cart.getUserId())) {
            throw new BizException("购物车项不存在");
        }
        return cart;
    }

    private Long requireUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BizException("用户未登录");
        return userId;
    }

    private CartVO toVO(Cart c, Sku sku) {
        CartVO vo = new CartVO();
        vo.setId(c.getId());
        vo.setSkuId(c.getSkuId());
        vo.setQuantity(c.getQuantity());
        vo.setSelected(c.getSelected());
        vo.setCreatedAt(c.getCreatedAt());
        if (sku != null) {
            vo.setSkuName(sku.getSkuName());
            vo.setSpuId(sku.getSpuId());
            Spu spu = spuMapper.selectById(sku.getSpuId());
            if (spu != null) vo.setSpuName(spu.getName());
        }
        return vo;
    }
}
