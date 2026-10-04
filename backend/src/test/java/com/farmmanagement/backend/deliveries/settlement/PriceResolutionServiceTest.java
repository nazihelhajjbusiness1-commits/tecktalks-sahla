package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.TestcontainersConfiguration;
import com.farmmanagement.backend.auth.Role;
import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.deliveries.grading.DeliveryGrade;
import com.farmmanagement.backend.deliveries.grading.DeliveryGradeRepository;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementPreviewResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class PriceResolutionServiceTest {

    @Autowired
    private PriceResolutionService priceResolutionService;

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

    private PriceRule createPriceRule(
            Long productId,
            Long gradeId,
            BigDecimal amount,
            PriceRule.Currency currency,
            OffsetDateTime effectiveFrom,
            OffsetDateTime effectiveTo
    ) {
        return priceRuleRepository.save(
                PriceRule.builder()
                        .productId(productId)
                        .gradeId(gradeId)
                        .amount(amount)
                        .currency(currency)
                        .effectiveFrom(effectiveFrom)
                        .effectiveTo(effectiveTo)
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

    private Delivery createDelivery(
            Farmer farmer,
            Product product,
            BigDecimal quantity,
            LocalDate deliveryDate,
            DeliveryStatus status,
            User createdBy,
            PriceRule.Currency totalPriceCurrency
    ) {
        Delivery delivery = new Delivery();
        delivery.setFarmer(farmer);
        delivery.setProduct(product);
        delivery.setQuantity(quantity);
        delivery.setUnit(product.getUnit());
        delivery.setDeliveryDate(deliveryDate);
        delivery.setStatus(status);
        delivery.setCreatedBy(createdBy);
        delivery.setTotalPriceCurrency(totalPriceCurrency);
        if (totalPriceCurrency != null) {
            delivery.setTotalPrice(quantity);
        }
        return deliveryRepository.save(delivery);
    }

    private DeliveryGrade createDeliveryGrade(Delivery delivery, GradeDefinition gradeDefinition, User gradedBy) {
        DeliveryGrade deliveryGrade = new DeliveryGrade();
        deliveryGrade.setDelivery(delivery);
        deliveryGrade.setGradeDefinition(gradeDefinition);
        deliveryGrade.setGradedBy(gradedBy);
        deliveryGrade.setGradedAt(LocalDateTime.now());
        return deliveryGradeRepository.save(deliveryGrade);
    }

    @Test
    void resolvesPrice_whenExactlyOneActiveRuleExists() {
        Product product = createProduct();
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), grade.getId(), new BigDecimal("1.20"), PriceRule.Currency.USD, from, null);

        PriceRule resolved = priceResolutionService.resolvePrice(
                product.getId(), grade.getId(), PriceRule.Currency.USD, from.plusDays(10)
        );

        assertEquals(0, resolved.getAmount().compareTo(new BigDecimal("1.20")));
        assertEquals(PriceRule.Currency.USD, resolved.getCurrency());
    }

    @Test
    void boundary_matchesExactlyAtEffectiveFrom() {
        Product product = createProduct();
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 3, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), grade.getId(), new BigDecimal("2.00"), PriceRule.Currency.USD, from, null);

        PriceRule resolved = priceResolutionService.resolvePrice(
                product.getId(), grade.getId(), PriceRule.Currency.USD, from
        );

        assertEquals(0, resolved.getAmount().compareTo(new BigDecimal("2.00")));
    }

    @Test
    void boundary_doesNotMatchExactlyAtEffectiveTo() {
        Product product = createProduct();
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.of(2026, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), grade.getId(), new BigDecimal("3.00"), PriceRule.Currency.USD, from, to);

        assertThrows(ConflictException.class, () ->
                priceResolutionService.resolvePrice(product.getId(), grade.getId(), PriceRule.Currency.USD, to)
        );
    }

    @Test
    void excludesInactivePriceRule() {
        Product product = createProduct();
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        priceRuleRepository.save(
                PriceRule.builder()
                        .productId(product.getId())
                        .gradeId(grade.getId())
                        .amount(new BigDecimal("4.00"))
                        .currency(PriceRule.Currency.USD)
                        .effectiveFrom(from)
                        .effectiveTo(null)
                        .active(false)
                        .build()
        );

        assertThrows(ConflictException.class, () ->
                priceResolutionService.resolvePrice(product.getId(), grade.getId(), PriceRule.Currency.USD, from.plusDays(1))
        );
    }

    @Test
    void differentGradeOnSameProduct_resolvesOnlyMatchingGrade() {
        Product product = createProduct();
        GradeDefinition gradeA = createGrade(product.getId(), "GRADE_A");
        GradeDefinition gradeB = createGrade(product.getId(), "GRADE_B");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), gradeA.getId(), new BigDecimal("1.00"), PriceRule.Currency.USD, from, null);
        createPriceRule(product.getId(), gradeB.getId(), new BigDecimal("2.00"), PriceRule.Currency.USD, from, null);

        PriceRule resolved = priceResolutionService.resolvePrice(
                product.getId(), gradeB.getId(), PriceRule.Currency.USD, from.plusDays(1)
        );

        assertEquals(0, resolved.getAmount().compareTo(new BigDecimal("2.00")));
    }

    @Test
    void differentProduct_doesNotCrossMatch() {
        Product productA = createProduct();
        Product productB = createProduct();
        GradeDefinition gradeOnA = createGrade(productA.getId(), "GRADE_A");
        GradeDefinition gradeOnB = createGrade(productB.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(productA.getId(), gradeOnA.getId(), new BigDecimal("1.00"), PriceRule.Currency.USD, from, null);

        assertThrows(ConflictException.class, () ->
                priceResolutionService.resolvePrice(productB.getId(), gradeOnB.getId(), PriceRule.Currency.USD, from.plusDays(1))
        );
    }

    @Test
    void resolvesUsdPrice() {
        Product product = createProduct();
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), grade.getId(), new BigDecimal("1.50"), PriceRule.Currency.USD, from, null);

        PriceRule resolved = priceResolutionService.resolvePrice(
                product.getId(), grade.getId(), PriceRule.Currency.USD, from.plusDays(1)
        );

        assertEquals(PriceRule.Currency.USD, resolved.getCurrency());
    }

    @Test
    void resolvesLbpPrice() {
        Product product = createProduct();
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), grade.getId(), new BigDecimal("90000.00"), PriceRule.Currency.LBP, from, null);

        PriceRule resolved = priceResolutionService.resolvePrice(
                product.getId(), grade.getId(), PriceRule.Currency.LBP, from.plusDays(1)
        );

        assertEquals(PriceRule.Currency.LBP, resolved.getCurrency());
    }

    @Test
    void noMatchingCurrency_throwsConflict() {
        Product product = createProduct();
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), grade.getId(), new BigDecimal("90000.00"), PriceRule.Currency.LBP, from, null);

        assertThrows(ConflictException.class, () ->
                priceResolutionService.resolvePrice(product.getId(), grade.getId(), PriceRule.Currency.USD, from.plusDays(1))
        );
    }

    @Test
    void multipleOverlappingActiveRules_throwsConflict() {
        Product product = createProduct();
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), grade.getId(), new BigDecimal("1.00"), PriceRule.Currency.USD, from, null);
        createPriceRule(product.getId(), grade.getId(), new BigDecimal("1.10"), PriceRule.Currency.USD, from, null);

        assertThrows(ConflictException.class, () ->
                priceResolutionService.resolvePrice(product.getId(), grade.getId(), PriceRule.Currency.USD, from.plusDays(1))
        );
    }

    @Test
    void previewSettlement_confirmedDelivery_returnsExpectedFields() {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);
        User grader = createUser(Role.INSPECTOR);
        GradeDefinition grade = createGrade(product.getId(), "GRADE_A");
        LocalDate deliveryDate = LocalDate.of(2026, 5, 10);
        OffsetDateTime from = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        createPriceRule(product.getId(), grade.getId(), new BigDecimal("1.25"), PriceRule.Currency.USD, from, null);

        Delivery delivery = createDelivery(
                farmer, product, new BigDecimal("500.000"), deliveryDate,
                DeliveryStatus.CONFIRMED, creator, PriceRule.Currency.USD
        );
        createDeliveryGrade(delivery, grade, grader);

        SettlementPreviewResponse response = priceResolutionService.previewSettlement(delivery.getId());

        assertEquals(delivery.getId(), response.getDeliveryId());
        assertEquals("GRADE_A", response.getGrade());
        assertEquals(0, response.getAcceptedWeight().compareTo(new BigDecimal("500.000")));
        assertEquals(0, response.getUnitPrice().compareTo(new BigDecimal("1.25")));
        assertEquals(PriceRule.Currency.USD, response.getCurrency());
        assertNotNull(response.getPriceRuleId());
    }

    @Test
    void previewSettlement_nonConfirmedDelivery_throwsConflict() {
        Product product = createProduct();
        Farmer farmer = createFarmer();
        User creator = createUser(Role.RECEIVING_EMPLOYEE);
        LocalDate deliveryDate = LocalDate.of(2026, 5, 10);

        Delivery delivery = createDelivery(
                farmer, product, new BigDecimal("500.000"), deliveryDate,
                DeliveryStatus.WEIGHED, creator, null
        );

        assertThrows(ConflictException.class, () -> priceResolutionService.previewSettlement(delivery.getId()));
    }
}
