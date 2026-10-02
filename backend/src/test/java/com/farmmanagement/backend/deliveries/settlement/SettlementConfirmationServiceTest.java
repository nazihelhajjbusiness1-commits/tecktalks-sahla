package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.TestcontainersConfiguration;
import com.farmmanagement.backend.auth.Role;
import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.deliveries.grading.DeliveryGrade;
import com.farmmanagement.backend.deliveries.grading.DeliveryGradeRepository;
import com.farmmanagement.backend.deliveries.settlement.dto.FarmerSettlementResponse;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementCalculationRequest;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SettlementConfirmationServiceTest {

    @Autowired
    private SettlementConfirmationService settlementConfirmationService;

    @Autowired
    private SettlementCalculationService settlementCalculationService;

    @Autowired
    private FarmerSettlementService farmerSettlementService;

    @Autowired
    private FarmerSettlementRepository farmerSettlementRepository;

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

    private void createPriceRule(Long productId, Long gradeId, BigDecimal amount, PriceRule.Currency currency) {
        priceRuleRepository.save(
                PriceRule.builder()
                        .productId(productId)
                        .gradeId(gradeId)
                        .amount(amount)
                        .currency(currency)
                        .effectiveFrom(OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
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

    private Delivery createDeliveryWithStatus(
            Farmer farmer, Product product, BigDecimal quantity, User creator, DeliveryStatus status, PriceRule.Currency currency
    ) {
        Delivery delivery = new Delivery();
        delivery.setFarmer(farmer);
        delivery.setProduct(product);
        delivery.setQuantity(quantity);
        delivery.setUnit(product.getUnit());
        delivery.setDeliveryDate(LocalDate.of(2026, 5, 10));
        delivery.setStatus(status);
        delivery.setCreatedBy(creator);
        delivery.setTotalPriceCurrency(currency);
        if (currency != null) {
            delivery.setTotalPrice(quantity);
        }
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

    private Delivery setUpCalculatedSettlement(BigDecimal quantity, BigDecimal unitPrice, BigDecimal commissionRate) {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);
        User grader = createUser(Role.INSPECTOR);
        User calculator = createUser(Role.ACCOUNTANT);
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        createPriceRule(product.getId(), grade.getId(), unitPrice, PriceRule.Currency.USD);

        Delivery delivery = createDeliveryWithStatus(
                farmer, product, quantity, creator, DeliveryStatus.CONFIRMED, PriceRule.Currency.USD
        );
        createDeliveryGrade(delivery, grade, grader);

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(commissionRate);
        request.setDeductions(List.of());

        settlementCalculationService.calculate(delivery.getId(), request, calculator.getId());

        return delivery;
    }

    @Test
    void confirmsACalculatedSettlement_freezesFieldsAndRecordsActor() {
        Delivery delivery = setUpCalculatedSettlement(
                new BigDecimal("100.000"), new BigDecimal("2.00"), new BigDecimal("10.00")
        );
        User confirmer = createUser(Role.MANAGER);

        FarmerSettlementResponse response = settlementConfirmationService.confirm(delivery.getId(), confirmer.getId());

        assertEquals(SettlementStatus.CONFIRMED, response.getStatus());
        assertEquals(delivery.getFarmer().getId(), response.getFarmerId());
        assertNotNull(response.getConfirmedAt());
        assertEquals((confirmer.getFirstname() + " " + confirmer.getLastname()).trim(), response.getConfirmedBy());
        assertEquals(0, response.getGrossAmount().compareTo(new BigDecimal("200.0000")));
        assertEquals(0, response.getNetAmount().compareTo(new BigDecimal("180.0000")));
        assertNotNull(response.getSourcePriceRuleId());

        FarmerSettlement saved = farmerSettlementRepository.findByDeliveryId(delivery.getId()).orElseThrow();
        assertEquals(SettlementStatus.CONFIRMED, saved.getStatus());
        assertNotNull(saved.getConfirmedAt());
        assertNotNull(saved.getConfirmedBy());
    }

    @Test
    void repeatedConfirmation_returnsExistingResultWithoutCreatingDuplicates() {
        Delivery delivery = setUpCalculatedSettlement(
                new BigDecimal("50.000"), new BigDecimal("1.00"), BigDecimal.ZERO
        );
        User confirmer = createUser(Role.MANAGER);

        FarmerSettlementResponse first = settlementConfirmationService.confirm(delivery.getId(), confirmer.getId());
        FarmerSettlementResponse second = settlementConfirmationService.confirm(delivery.getId(), confirmer.getId());

        assertEquals(SettlementStatus.CONFIRMED, second.getStatus());
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getConfirmedAt(), second.getConfirmedAt());
        assertEquals(0, first.getNetAmount().compareTo(second.getNetAmount()));

        List<FarmerSettlement> all = farmerSettlementRepository.findAll().stream()
                .filter(s -> s.getDelivery().getId().equals(delivery.getId()))
                .toList();
        assertEquals(1, all.size());
        assertEquals(SettlementStatus.CONFIRMED, all.get(0).getStatus());
    }

    @Test
    void confirmingBeforeCalculation_isRejected() {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);
        User confirmer = createUser(Role.MANAGER);

        Delivery delivery = createDeliveryWithStatus(
                farmer, product, new BigDecimal("10.000"), creator, DeliveryStatus.CONFIRMED, PriceRule.Currency.USD
        );

        FarmerSettlement draft = new FarmerSettlement();
        draft.setDelivery(delivery);
        farmerSettlementRepository.save(draft);

        assertThrows(ConflictException.class, () ->
                settlementConfirmationService.confirm(delivery.getId(), confirmer.getId())
        );
    }

    @Test
    void confirmingAVoidedSettlement_isRejected() {
        Delivery delivery = setUpCalculatedSettlement(
                new BigDecimal("10.000"), new BigDecimal("1.00"), BigDecimal.ZERO
        );
        User confirmer = createUser(Role.MANAGER);

        FarmerSettlement settlement = farmerSettlementRepository.findByDeliveryId(delivery.getId()).orElseThrow();
        settlement.setStatus(SettlementStatus.VOIDED);
        farmerSettlementRepository.save(settlement);

        assertThrows(ConflictException.class, () ->
                settlementConfirmationService.confirm(delivery.getId(), confirmer.getId())
        );
    }

    @Test
    void confirmingWhenDeliveryIsNotConfirmedStatus_isRejected() {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);
        User confirmer = createUser(Role.MANAGER);

        Delivery delivery = createDeliveryWithStatus(
                farmer, product, new BigDecimal("10.000"), creator, DeliveryStatus.WEIGHED, null
        );

        Long deliveryId = delivery.getId();
        assertThrows(ConflictException.class, () ->
                settlementConfirmationService.confirm(deliveryId, confirmer.getId())
        );
    }

    @Test
    void confirmingWithNoSettlementAtAll_isRejected() {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);
        User confirmer = createUser(Role.MANAGER);

        Delivery delivery = createDeliveryWithStatus(
                farmer, product, new BigDecimal("10.000"), creator, DeliveryStatus.CONFIRMED, PriceRule.Currency.USD
        );

        Long deliveryId = delivery.getId();
        assertThrows(ResourceNotFoundException.class, () ->
                settlementConfirmationService.confirm(deliveryId, confirmer.getId())
        );
    }

    @Test
    void getSettlement_returnsConfirmedSettlementData() {
        Delivery delivery = setUpCalculatedSettlement(
                new BigDecimal("20.000"), new BigDecimal("3.00"), new BigDecimal("2.00")
        );
        User confirmer = createUser(Role.MANAGER);
        settlementConfirmationService.confirm(delivery.getId(), confirmer.getId());

        FarmerSettlementResponse response = farmerSettlementService.getByDeliveryId(delivery.getId());

        assertEquals(SettlementStatus.CONFIRMED, response.getStatus());
        assertEquals(delivery.getId(), response.getDeliveryId());
        assertNotNull(response.getConfirmedAt());
        assertNotNull(response.getConfirmedBy());
    }

    @Test
    void getSettlement_withNoSettlement_throwsNotFound() {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);

        Delivery delivery = createDeliveryWithStatus(
                farmer, product, new BigDecimal("10.000"), creator, DeliveryStatus.PENDING, null
        );

        Long deliveryId = delivery.getId();
        assertThrows(ResourceNotFoundException.class, () -> farmerSettlementService.getByDeliveryId(deliveryId));
    }
}
