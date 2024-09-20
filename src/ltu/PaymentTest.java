package ltu;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.Before;

import static ltu.CalendarFactory.getCalendar;

public class PaymentTest
{
    PaymentImpl payment;

    @Before
    public void newPaymentObject() throws IOException {
        payment = new PaymentImpl(getCalendar());
    }

    @Test
    public void testNextPaymentDay() {
        assertEquals("20240930", payment.getNextPaymentDay());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMonthlyAmountInvalidPersonId() {
        String personId = "invalid";
        int income = 50000;
        int studyRate = 100;
        int completionRatio = 100;
        payment.getMonthlyAmount(personId, income, studyRate, completionRatio);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMonthlyAmountNegativeIncome() {
        String personId = "200001010000";
        int income = -1;
        int studyRate = 100;
        int completionRatio = 100;
        payment.getMonthlyAmount(personId, income, studyRate, completionRatio);
    }

    @Test
    public void testGetAge() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getAge", String.class);
        method.setAccessible(true);
        String personId = "200001010000";
        int age = (int) method.invoke(payment, personId);
        assertEquals(24, age); // Assuming the current year is 2024
    }

    @Test
    public void testGetLoan() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getLoan", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 50000;
        int studyRate = 100;
        int completionRatio = 100;
        int loan = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field fullLoanField = PaymentImpl.class.getDeclaredField("FULL_LOAN");
        fullLoanField.setAccessible(true);
        int fullLoan = (int) fullLoanField.get(payment);

        assertEquals(fullLoan, loan);
    }

    @Test
    public void testGetSubsidy() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getSubsidy", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 50000;
        int studyRate = 100;
        int completionRatio = 100;
        int subsidy = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field fullSubsidyField = PaymentImpl.class.getDeclaredField("FULL_SUBSIDY");
        fullSubsidyField.setAccessible(true);
        int fullSubsidy = (int) fullSubsidyField.get(payment);

        assertEquals(fullSubsidy, subsidy);
    }
}