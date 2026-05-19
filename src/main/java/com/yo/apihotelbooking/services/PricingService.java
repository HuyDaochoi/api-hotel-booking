package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.PricingRuleRequest;
import com.yo.apihotelbooking.dto.response.PricingEstimateResponse;
import com.yo.apihotelbooking.dto.response.PricingRuleResponse;
import com.yo.apihotelbooking.schemas.domain.PricingRule;
import com.yo.apihotelbooking.schemas.domain.Room;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import com.yo.apihotelbooking.repository.PricingRuleRepository;
import com.yo.apihotelbooking.repository.RoomRepository;
import com.yo.apihotelbooking.repository.RoomTypeRepository;
import com.yo.apihotelbooking.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {
    private final PricingRuleRepository pricingRuleRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
//xem gia truioc
    public PricingEstimateResponse estimatePrice(Long roomId, LocalDate checkIn, LocalDate checkOut) throws NotFoundException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phòng với ID: " + roomId));

        BigDecimal basePrice = room.getRoomType().getBasePrice();
        List<PricingEstimateResponse.NightlyPriceBreakdown> breakdown = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            List<PricingRule> activeRules = pricingRuleRepository.findActiveRulesForDate(date, date.getDayOfWeek().getValue(), room.getRoomType().getId());
            
            PricingRule appliedRule = activeRules.isEmpty() ? null : activeRules.get(0);
            BigDecimal finalPrice = calculatePrice(basePrice, appliedRule);

            PricingEstimateResponse.NightlyPriceBreakdown item = new PricingEstimateResponse.NightlyPriceBreakdown();
            item.setDate(date);
            item.setOriginalPrice(basePrice);
            item.setFinalPrice(finalPrice);
            item.setAppliedRuleName(appliedRule != null ? appliedRule.getRuleName() : "Base Price");
            
            breakdown.add(item);
            total = total.add(finalPrice);
        }

        PricingEstimateResponse response = new PricingEstimateResponse();
        response.setRoomId(roomId);
        response.setTotalAmount(total);
        response.setBreakdown(breakdown);
        
        return response;
    }

    private BigDecimal calculatePrice(BigDecimal base, PricingRule rule) {

        if (rule == null) return base;
        
        if (rule.getPriceModifier() != null) {
            return base.add(rule.getPriceModifier());
        } else if (rule.getPricePercent() != null) {
            BigDecimal modifier = base.multiply(rule.getPricePercent())
                    .divide(new BigDecimal("100"), RoundingMode.HALF_UP);
            return base.add(modifier);
        }
        return base;
    }
   public List<PricingRuleResponse> getAllRules() {
        return pricingRuleRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }
    public PricingRuleResponse createRule(PricingRuleRequest request) throws NotFoundException, BadRequestException  {
        PricingRule rule = new PricingRule();
        mapRequestToEntity(request, rule);
        return mapToResponse(pricingRuleRepository.save(rule));
    }

    public PricingRuleResponse updateRule(Long id, PricingRuleRequest request) throws NotFoundException, BadRequestException {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy quy tắc giá ID: " + id));
        mapRequestToEntity(request, rule);
        return mapToResponse(pricingRuleRepository.save(rule));
    }

    public void deleteRule(Long id) {
        pricingRuleRepository.deleteById(id);
    }


   private void mapRequestToEntity(PricingRuleRequest request, PricingRule entity)
            throws NotFoundException, BadRequestException {
 
        if (request.getPriceModifier() != null && request.getPricePercent() != null) {
            throw new BadRequestException(
                    "Chỉ được dùng một trong hai: priceModifier hoặc pricePercent");
        }
        if (request.getPriceModifier() == null && request.getPricePercent() == null) {
            throw new BadRequestException(
                    "Phải có ít nhất một trong hai: priceModifier hoặc pricePercent");
        }
 
        // SEASONAL / SPECIAL_EVENT / DISCOUNT bắt buộc startDate ≤ endDate
        if (request.getRuleType() != null) {
            boolean needsDateRange = switch (request.getRuleType()) {
                case SEASONAL, SPECIAL_EVENT, DISCOUNT -> true;
                default -> false;
            };
            if (needsDateRange) {
                if (request.getStartDate() == null || request.getEndDate() == null) {
                    throw new BadRequestException(
                            "Loại rule " + request.getRuleType() + " bắt buộc phải có startDate và endDate");
                }
                if (request.getStartDate().isAfter(request.getEndDate())) {
                    throw new BadRequestException("startDate phải nhỏ hơn hoặc bằng endDate");
                }
            }
        }
 
        entity.setRuleName(request.getRuleName());
        entity.setRuleType(request.getRuleType());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setPriceModifier(request.getPriceModifier());
        entity.setPricePercent(request.getPricePercent());
        entity.setMinNights(request.getMinNights() != null ? request.getMinNights() : 1);
        entity.setPriority(request.getPriority()  != null ? request.getPriority()   : 0);
        entity.setIsActive(request.getIsActive()  != null ? request.getIsActive()   : true);
 
        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new NotFoundException(
                            "Không tìm thấy loại phòng ID: " + request.getRoomTypeId()));
            entity.setRoomType(roomType);
        } else {
            entity.setRoomType(null);
        }
    }
    
    private PricingRuleResponse mapToResponse(PricingRule entity) {
        return new PricingRuleResponse(
            entity.getRuleName(),
            entity.getRuleType(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getPriceModifier(),
            entity.getPricePercent(),
            entity.getPriority()
        );
    }

}