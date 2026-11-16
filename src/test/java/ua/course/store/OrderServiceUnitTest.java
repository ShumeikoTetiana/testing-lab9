package ua.course.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Unit-тести без БД і мережі: репозиторії замінені Mockito, перевіряється лише логіка OrderService. */
class OrderServiceUnitTest {
    private final ProductRepository products = mock(ProductRepository.class);
    private final OrderRepository orders = mock(OrderRepository.class);

    private OrderService service(OrderPolicy policy) {
        return new OrderService(products, orders, policy);
    }

    @Test
    @DisplayName("Знижка 20% від 2000 грн із підтвердженим купоном")
    void discountAppliedAtThreshold() {
        assertEquals(160000, service((p, q) -> true).calculateTotal(100000, 2, false, true, false));
    }

    @Test
    @DisplayName("Без купона знижки немає")
    void noDiscountWithoutCoupon() {
        assertEquals(200000, service((p, q) -> true).calculateTotal(100000, 2, false, false, false));
    }

    @Test
    @DisplayName("Нижче порогу 2000 грн знижки немає")
    void noDiscountBelowThreshold() {
        assertEquals(199999, service((p, q) -> true).calculateTotal(199999, 1, false, true, false));
    }

    @Test
    @DisplayName("Знижка округлюється донизу до копійки")
    void discountRoundedDown() {
        assertEquals(160001, service((p, q) -> true).calculateTotal(200001, 1, false, true, false));
    }

    @Test
    @DisplayName("Кількість поза межами 1–20 відхиляється")
    void invalidQuantity() {
        var s = service((p, q) -> true);
        assertThrows(IllegalArgumentException.class, () -> s.calculateTotal(10000, 0));
        assertThrows(IllegalArgumentException.class, () -> s.calculateTotal(10000, 21));
    }

    @Test
    @DisplayName("Відмова зовнішнього дозволу: замовлення не зберігається")
    void policyRejectionDoesNotSave() throws Exception {
        when(products.findById(1L)).thenReturn(new Product(1, "SKU-1", 10000, 5, false));
        assertThrows(OrderRejectedException.class,
                () -> service((p, q) -> false).placeOrder(new OrderRequest("REF-1", 1, 1, false, false)));
        verifyNoInteractions(orders);
    }
}
