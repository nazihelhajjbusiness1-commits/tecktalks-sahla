package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.TestcontainersConfiguration;
import com.farmmanagement.backend.auth.Role;
import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.deliveries.grading.DeliveryGrade;
import com.farmmanagement.backend.deliveries.grading.DeliveryGradeRepository;
import com.farmmanagement.backend.deliveries.settlement.dto.DeductionLineRequest;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementCalculationRequest;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementCalculationResponse;
import com.farmmanagement.backend.farmers.Farmer;
import com.farmmanagement.backend.farmers.FarmerRepository;
import com.farmmanagement.backend.grading.GradeDefinition;
import com.farmmanagement.backend.grading.GradeDefinitionRepository;
import com.farmmanagement.backend.pricing.PriceRule;
import com.farmmanagement.backend.pricing.PriceRuleRepository;
import com.farmmanagement.backend.products.Product;
import com.farmmanagement.backend.products.ProductRepository;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SettlementCalculationServiceTest {

    @Autowired
    private SettlementCalculationService settlementCalculationService;

    @Autowired
    private FarmerSettlementRepository farmerSettlementRepository;

    @Autowired
    private SettlementDeductionRepository settlementDeductionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GradeDefinitionRepository gradeDefinitionRepository;

    @Autowired
    private PriceRuleRepository priceRuleRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryGradeRepository deliveryGradeRepository;

    private static int counter = 0;

    private Product createProduct() {
        counter++;
        Product product = new Product();
        product.setName("Product" + counter);
        product.setVariety("Variety" + counter);
        product.setUnit("KG");
        product.setActive(true);
        return productRepository.save(product);
    }

    private GradeDefinition createGrade(Long productId, String code) {
        return gradeDefinitionRepository.save(
                GradeDefinition.builder()
                        .productId(productId)
                        .gradeCode(code)
                        .name(code)
                        .description("seed")
                        .displayOrder(0)
                        .active(true)
                        .build()
        );
    }

    private void createPriceRule(
            Long productId,
            Long gradeId,
            BigDecimal amount,
            PriceRule.Currency currency,
            OffsetDateTime effectiveFrom
    ) {
        priceRuleRepository.save(
                PriceRule.builder()
                        .productId(productId)
                        .gradeId(gradeId)
                        .amount(amount)
                        .currency(currency)
                        .effectiveFrom(effectiveFrom)
                        .effectiveTo(null)
                        .active(true)
                        .build()
        );
    }

    private Farmer createFarmer() {
        counter++;
        Farmer farmer = new Farmer();
        farmer.setFarmerCode("F" + counter);
        farmer.setName("Farmer " + counter);
        return farmerRepository.save(farmer);
    }

    private User createUser(Role role) {
        counter++;
        User user = new User();
        user.setFirstname("First" + counter);
        user.setLastname("Last" + counter);
        user.setUsername("user" + counter);
        user.setEmail("user" + counter + "@example.com");
        user.setPassword("password");
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Delivery createConfirmedDelivery(
            Farmer farmer,
            Product product,
            BigDecimal quantity,
            LocalDate deliveryDate,
            User creator,
            PriceRule.Currency totalPriceCurrency
    ) {
        Delivery delivery = new Delivery();
        delivery.setFarmer(farmer);
        delivery.setProduct(product);
        delivery.setQuantity(quantity);
        delivery.setUnit(product.getUnit());
        delivery.setDeliveryDate(deliveryDate);
        delivery.setStatus(DeliveryStatus.CONFIRMED);
        delivery.setCreatedBy(creator);
        delivery.setTotalPriceCurrency(totalPriceCurrency);
        delivery.setTotalPrice(quantity);
        return deliveryRepository.save(delivery);
    }

    private void createDeliveryGrade(Delivery delivery, GradeDefinition gradeDefinition, User gradedBy) {
        DeliveryGrade deliveryGrade = new DeliveryGrade();
        deliveryGrade.setDelivery(delivery);
        deliveryGrade.setGradeDefinition(gradeDefinition);
        deliveryGrade.setGradedBy(gradedBy);
        deliveryGrade.setGradedAt(LocalDateTime.now());
        deliveryGradeRepository.save(deliveryGrade);
    }

    private Delivery setUpConfirmedDeliveryWithPrice(
            BigDecimal quantity,
            BigDecimal unitPrice,
            PriceRule.Currency currency
    ) {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);
        User grader = createUser(Role.INSPECTOR);
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), grade.getId(), unitPrice, currency, from);

        Delivery delivery = createConfirmedDelivery(
                farmer, product, quantity, LocalDate.of(2026, 5, 10), creator, currency
        );
        createDeliveryGrade(delivery, grade, grader);
        return delivery;
    }

    private User createCalculatingUser() {
        return createUser(Role.ACCOUNTANT);
    }

    @Test
    void calculatesGrossCommissionAndNet_matchingTicketExample() {
        Delivery delivery = setUpConfirmedDeliveryWithPrice(
                new BigDecimal("500.000"), new BigDecimal("1.20"), PriceRule.Currency.USD
        );
        User calculator = createCalculatingUser();

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(new BigDecimal("5.00"));

        DeductionLineRequest deduction = new DeductionLineRequest();
        deduction.setType(DeductionType.TRANSPORT);
        deduction.setDescription("Transport fee");
        deduction.setAmount(new BigDecimal("20.00"));
        request.setDeductions(List.of(deduction));

        SettlementCalculationResponse response = settlementCalculationService.calculate(
                delivery.getId(), request, calculator.getId()
        );

        assertEquals(0, response.getGrossAmount().compareTo(new BigDecimal("600.0000")));
        assertEquals(0, response.getCommissionAmount().compareTo(new BigDecimal("30.0000")));
        assertEquals(0, response.getDeductionsTotal().compareTo(new BigDecimal("20.0000")));
        assertEquals(0, response.getNetAmount().compareTo(new BigDecimal("550.0000")));
        assertEquals(SettlementStatus.CALCULATED, response.getStatus());
        assertTrue(response.getSettlementId() != null);

        FarmerSettlement saved = farmerSettlementRepository.findByDeliveryId(delivery.getId()).orElseThrow();
        assertEquals(0, saved.getNetAmount().compareTo(new BigDecimal("550.0000")));
        assertEquals(SettlementStatus.CALCULATED, saved.getStatus());
    }

    @Test
    void zeroCommission_netEqualsGrossMinusDeductions() {
        Delivery delivery = setUpConfirmedDeliveryWithPrice(
                new BigDecimal("100.000"), new BigDecimal("2.00"), PriceRule.Currency.USD
        );
        User calculator = createCalculatingUser();

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(BigDecimal.ZERO);
        request.setDeductions(List.of());

        SettlementCalculationResponse response = settlementCalculationService.calculate(
                delivery.getId(), request, calculator.getId()
        );

        assertEquals(0, response.getGrossAmount().compareTo(new BigDecimal("200.0000")));
        assertEquals(0, response.getCommissionAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.getNetAmount().compareTo(new BigDecimal("200.0000")));
    }

    @Test
    void multipleDeductionLines_areSummedCorrectly() {
        Delivery delivery = setUpConfirmedDeliveryWithPrice(
                new BigDecimal("50.000"), new BigDecimal("10.00"), PriceRule.Currency.USD
        );
        User calculator = createCalculatingUser();

        DeductionLineRequest transport = new DeductionLineRequest();
        transport.setType(DeductionType.TRANSPORT);
        transport.setDescription("Transport");
        transport.setAmount(new BigDecimal("10.00"));

        DeductionLineRequest packaging = new DeductionLineRequest();
        packaging.setType(DeductionType.PACKAGING);
        packaging.setDescription("Packaging");
        packaging.setAmount(new BigDecimal("5.50"));

        DeductionLineRequest serviceFee = new DeductionLineRequest();
        serviceFee.setType(DeductionType.SERVICE_FEE);
        serviceFee.setDescription("Service fee");
        serviceFee.setAmount(new BigDecimal("2.25"));

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(BigDecimal.ZERO);
        request.setDeductions(List.of(transport, packaging, serviceFee));

        SettlementCalculationResponse response = settlementCalculationService.calculate(
                delivery.getId(), request, calculator.getId()
        );

        assertEquals(0, response.getDeductionsTotal().compareTo(new BigDecimal("17.75")));
        assertEquals(3, response.getDeductions().size());

        List<SettlementDeduction> persisted = settlementDeductionRepository.findBySettlementId(response.getSettlementId());
        assertEquals(3, persisted.size());
    }

    @Test
    void roundingScenario_commissionIsRoundedHalfUpToFourDecimals() {
        Delivery delivery = setUpConfirmedDeliveryWithPrice(
                new BigDecimal("3.000"), new BigDecimal("1.00"), PriceRule.Currency.USD
        );
        User calculator = createCalculatingUser();

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(new BigDecimal("33.335"));
        request.setDeductions(List.of());

        SettlementCalculationResponse response = settlementCalculationService.calculate(
                delivery.getId(), request, calculator.getId()
        );

        assertEquals(0, response.getGrossAmount().compareTo(new BigDecimal("3.0000")));
        assertEquals(0, response.getCommissionAmount().compareTo(new BigDecimal("1.0001")));
        assertEquals(0, response.getNetAmount().compareTo(new BigDecimal("1.9999")));
    }

    @Test
    void negativeDeductionAmount_isRejected() {
        Delivery delivery = setUpConfirmedDeliveryWithPrice(
                new BigDecimal("100.000"), new BigDecimal("1.00"), PriceRule.Currency.USD
        );
        User calculator = createCalculatingUser();

        DeductionLineRequest negative = new DeductionLineRequest();
        negative.setType(DeductionType.OTHER);
        negative.setDescription("Invalid");
        negative.setAmount(new BigDecimal("-5.00"));

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(BigDecimal.ZERO);
        request.setDeductions(List.of(negative));

        assertThrows(ValidationException.class, () ->
                settlementCalculationService.calculate(delivery.getId(), request, calculator.getId())
        );
    }

    @Test
    void netAmountGoingNegative_isRejected() {
        Delivery delivery = setUpConfirmedDeliveryWithPrice(
                new BigDecimal("10.000"), new BigDecimal("1.00"), PriceRule.Currency.USD
        );
        User calculator = createCalculatingUser();

        DeductionLineRequest tooLarge = new DeductionLineRequest();
        tooLarge.setType(DeductionType.OTHER);
        tooLarge.setDescription("Too large");
        tooLarge.setAmount(new BigDecimal("50.00"));

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(BigDecimal.ZERO);
        request.setDeductions(List.of(tooLarge));

        assertThrows(ValidationException.class, () ->
                settlementCalculationService.calculate(delivery.getId(), request, calculator.getId())
        );
    }

    @Test
    void currencyMismatchBetweenRequestAndResolvedPrice_isRejected() {
        Delivery delivery = setUpConfirmedDeliveryWithPrice(
                new BigDecimal("100.000"), new BigDecimal("1.00"), PriceRule.Currency.USD
        );
        User calculator = createCalculatingUser();

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.LBP);
        request.setCommissionRate(BigDecimal.ZERO);
        request.setDeductions(List.of());

        assertThrows(ValidationException.class, () ->
                settlementCalculationService.calculate(delivery.getId(), request, calculator.getId())
        );
    }

    @Test
    void recalculatingAnAlreadyCalculatedSettlement_isRejected() {
        Delivery delivery = setUpConfirmedDeliveryWithPrice(
                new BigDecimal("100.000"), new BigDecimal("1.00"), PriceRule.Currency.USD
        );
        User calculator = createCalculatingUser();

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(BigDecimal.ZERO);
        request.setDeductions(List.of());

        settlementCalculationService.calculate(delivery.getId(), request, calculator.getId());

        assertThrows(ConflictException.class, () ->
                settlementCalculationService.calculate(delivery.getId(), request, calculator.getId())
        );
    }

    @Test
    void nonConfirmedDelivery_isRejected() {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);
        User calculator = createCalculatingUser();

        Delivery delivery = new Delivery();
        delivery.setFarmer(farmer);
        delivery.setProduct(product);
        delivery.setQuantity(new BigDecimal("100.000"));
        delivery.setUnit(product.getUnit());
        delivery.setDeliveryDate(LocalDate.of(2026, 5, 10));
        delivery.setStatus(DeliveryStatus.WEIGHED);
        delivery.setCreatedBy(creator);
        delivery = deliveryRepository.save(delivery);

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(BigDecimal.ZERO);
        request.setDeductions(List.of());

        Long deliveryId = delivery.getId();
        assertThrows(ConflictException.class, () ->
                settlementCalculationService.calculate(deliveryId, request, calculator.getId())
        );
    }
}
