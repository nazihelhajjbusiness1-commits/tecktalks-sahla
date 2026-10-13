package com.farmmanagement.backend.ledger;

import com.farmmanagement.backend.TestcontainersConfiguration;
import com.farmmanagement.backend.auth.Role;
import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.deliveries.grading.DeliveryGrade;
import com.farmmanagement.backend.deliveries.grading.DeliveryGradeRepository;
import com.farmmanagement.backend.deliveries.settlement.SettlementCalculationService;
import com.farmmanagement.backend.deliveries.settlement.SettlementConfirmationService;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementCalculationRequest;
import com.farmmanagement.backend.farmers.Farmer;
import com.farmmanagement.backend.farmers.FarmerRepository;
import com.farmmanagement.backend.grading.GradeDefinition;
import com.farmmanagement.backend.grading.GradeDefinitionRepository;
import com.farmmanagement.backend.ledger.dto.FarmerBalancesResponse;
import com.farmmanagement.backend.payments.PaymentMethod;
import com.farmmanagement.backend.payments.PaymentService;
import com.farmmanagement.backend.payments.dto.PaymentResponse;
import com.farmmanagement.backend.payments.dto.RecordPaymentRequest;
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
class LedgerAndPaymentIntegrationTest {

    @Autowired private SettlementCalculationService settlementCalculationService;
    @Autowired private SettlementConfirmationService settlementConfirmationService;
    @Autowired private PaymentService paymentService;
    @Autowired private LedgerService ledgerService;
    @Autowired private FarmerLedgerEntryRepository ledgerRepository;

    @Autowired private ProductRepository productRepository;
    @Autowired private FarmerRepository farmerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private GradeDefinitionRepository gradeDefinitionRepository;
    @Autowired private PriceRuleRepository priceRuleRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private DeliveryGradeRepository deliveryGradeRepository;

    private static int counter = 0;

    // ---- DT-71: ledger posting ----

    @Test
    void confirmingSettlement_createsExactlyOneCredit_andBalanceEqualsNet() {
        Delivery delivery = setUpCalculatedSettlement(
                new BigDecimal("500.000"), new BigDecimal("1.20"), new BigDecimal("5.00"));
        User confirmer = createUser(Role.MANAGER);

        settlementConfirmationService.confirm(delivery.getId(), confirmer.getId());

        Long farmerId = delivery.getFarmer().getId();
        List<FarmerLedgerEntry> credits = ledgerRepository
                .findByFarmerIdOrderByOccurredAtAscIdAsc(farmerId);
        assertEquals(1, credits.size());
        assertEquals(LedgerEntryType.SETTLEMENT_CREDIT, credits.get(0).getEntryType());

        // gross 600, 5% commission 30 => net 570
        assertEquals(0, getUsd(farmerId).compareTo(new BigDecimal("570.0000")));
    }

    @Test
    void repeatedConfirmation_doesNotDuplicateLedgerCredit() {
        Delivery delivery = setUpCalculatedSettlement(
                new BigDecimal("100.000"), new BigDecimal("2.00"), BigDecimal.ZERO);
        User confirmer = createUser(Role.MANAGER);

        settlementConfirmationService.confirm(delivery.getId(), confirmer.getId());
        settlementConfirmationService.confirm(delivery.getId(), confirmer.getId());

        Long farmerId = delivery.getFarmer().getId();
        assertEquals(1, ledgerRepository.findByFarmerIdOrderByOccurredAtAscIdAsc(farmerId).size());
        assertEquals(0, getUsd(farmerId).compareTo(new BigDecimal("200.0000")));
    }

    // ---- DT-72: payments ----

    @Test
    void partialThenFinalPayment_reducesBalanceToZero() {
        Long farmerId = confirmedFarmerWithUsdBalance(new BigDecimal("550.0000"));
        User accountant = createUser(Role.ACCOUNTANT);

        PaymentResponse first = paymentService.recordPayment(
                payment(farmerId, "300.00"), accountant.getId());
        assertEquals(0, first.getOutstandingAfter().compareTo(new BigDecimal("250.0000")));
        assertEquals(0, getUsd(farmerId).compareTo(new BigDecimal("250.0000")));

        paymentService.recordPayment(payment(farmerId, "250.00"), accountant.getId());
        assertEquals(0, getUsd(farmerId).compareTo(new BigDecimal("0.0000")));
    }

    @Test
    void overpayment_isRejected() {
        Long farmerId = confirmedFarmerWithUsdBalance(new BigDecimal("100.0000"));
        User accountant = createUser(Role.ACCOUNTANT);

        assertThrows(ValidationException.class,
                () -> paymentService.recordPayment(payment(farmerId, "100.01"), accountant.getId()));
    }

    @Test
    void zeroPayment_isRejected() {
        Long farmerId = confirmedFarmerWithUsdBalance(new BigDecimal("100.0000"));
        User accountant = createUser(Role.ACCOUNTANT);

        assertThrows(ValidationException.class,
                () -> paymentService.recordPayment(payment(farmerId, "0.00"), accountant.getId()));
    }

    @Test
    void paymentInCurrencyWithNoOutstandingBalance_isRejected() {
        Long farmerId = confirmedFarmerWithUsdBalance(new BigDecimal("100.0000"));
        User accountant = createUser(Role.ACCOUNTANT);

        RecordPaymentRequest lbp = payment(farmerId, "50.00");
        lbp.setCurrency(PriceRule.Currency.LBP);
        assertThrows(ValidationException.class,
                () -> paymentService.recordPayment(lbp, accountant.getId()));
    }

    @Test
    void usdAndLbpBalances_areTrackedSeparately() {
        Long farmerId = confirmedFarmerWithUsdBalance(new BigDecimal("570.0000"));
        // Seed an independent LBP credit via an adjustment entry.
        FarmerLedgerEntry lbp = new FarmerLedgerEntry();
        lbp.setFarmerId(farmerId);
        lbp.setEntryType(LedgerEntryType.ADJUSTMENT_CREDIT);
        lbp.setAmount(new BigDecimal("12000000.0000"));
        lbp.setCurrency(PriceRule.Currency.LBP);
        lbp.setOccurredAt(LocalDateTime.now());
        ledgerRepository.save(lbp);

        assertEquals(0, getUsd(farmerId).compareTo(new BigDecimal("570.0000")));
        assertEquals(0, ledgerService.getOutstanding(farmerId, PriceRule.Currency.LBP)
                .compareTo(new BigDecimal("12000000.0000")));
    }

    // ---- helpers ----

    private BigDecimal getUsd(Long farmerId) {
        return ledgerService.getOutstanding(farmerId, PriceRule.Currency.USD);
    }

    private RecordPaymentRequest payment(Long farmerId, String amount) {
        RecordPaymentRequest req = new RecordPaymentRequest();
        req.setFarmerId(farmerId);
        req.setAmount(new BigDecimal(amount));
        req.setCurrency(PriceRule.Currency.USD);
        req.setPaymentMethod(PaymentMethod.CASH);
        return req;
    }

    private Long confirmedFarmerWithUsdBalance(BigDecimal targetNet) {
        // net = quantity * unitPrice (commission 0) => choose unitPrice 1 so net == quantity.
        Delivery delivery = setUpCalculatedSettlement(
                targetNet.setScale(3), BigDecimal.ONE.setScale(2), BigDecimal.ZERO);
        User confirmer = createUser(Role.MANAGER);
        settlementConfirmationService.confirm(delivery.getId(), confirmer.getId());
        FarmerBalancesResponse balances = ledgerService.getBalances(delivery.getFarmer().getId());
        assertTrue(balances.balances().stream().anyMatch(b -> b.currency() == PriceRule.Currency.USD));
        return delivery.getFarmer().getId();
    }

    private Delivery setUpCalculatedSettlement(BigDecimal quantity, BigDecimal unitPrice, BigDecimal commissionRate) {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);
        User grader = createUser(Role.INSPECTOR);
        User calculator = createUser(Role.ACCOUNTANT);
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        createPriceRule(product.getId(), grade.getId(), unitPrice, PriceRule.Currency.USD);

        Delivery delivery = createDeliveryWithStatus(farmer, product, quantity, creator);
        createDeliveryGrade(delivery, grade, grader);

        SettlementCalculationRequest request = new SettlementCalculationRequest();
        request.setCurrency(PriceRule.Currency.USD);
        request.setCommissionRate(commissionRate);
        request.setDeductions(List.of());
        settlementCalculationService.calculate(delivery.getId(), request, calculator.getId());
        return delivery;
    }

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
                        .productId(productId).gradeCode(code).name(code)
                        .description("seed").displayOrder(0).active(true).build());
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
                        .build());
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

    private Delivery createDeliveryWithStatus(Farmer farmer, Product product, BigDecimal quantity, User creator) {
        Delivery delivery = new Delivery();
        delivery.setFarmer(farmer);
        delivery.setProduct(product);
        delivery.setQuantity(quantity);
        delivery.setUnit(product.getUnit());
        delivery.setDeliveryDate(LocalDate.of(2026, 5, 10));
        delivery.setStatus(DeliveryStatus.CONFIRMED);
        delivery.setCreatedBy(creator);
        delivery.setTotalPriceCurrency(PriceRule.Currency.USD);
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
}
