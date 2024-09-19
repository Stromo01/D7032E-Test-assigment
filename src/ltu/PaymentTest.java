package ltu;

import static org.junit.Assert.*;

import java.io.IOException;

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

    @Test
    public void testGetMonthlyAmountValid() {
        // Assuming personId format is YYYYMMDDXXXX
        String personId = "200001010000";
        int income = 50000;
        int studyRate = 100;
        int completionRatio = 100;
        int expectedAmount = payment.FULL_LOAN + payment.FULL_SUBSIDY;
        assertEquals(expectedAmount, payment.getMonthlyAmount(personId, income, studyRate, completionRatio));
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
    public void testGetMonthlyAmountEdgeCaseAge() {
        String personId = "197701010000"; // Age 47
        int income = 50000;
        int studyRate = 100;
        int completionRatio = 100;
        int expectedAmount = payment.ZERO_LOAN + payment.FULL_SUBSIDY;
        assertEquals(expectedAmount, payment.getMonthlyAmount(personId, income, studyRate, completionRatio));
    }

    @Test
    public void testGetMonthlyAmountEdgeCaseStudyRate() {
        String personId = "200001010000";
        int income = 50000;
        int studyRate = 50;
        int completionRatio = 100;
        int expectedAmount = payment.HALF_LOAN + payment.HALF_SUBSIDY;
        assertEquals(expectedAmount, payment.getMonthlyAmount(personId, income, studyRate, completionRatio));
    }

    @Test
    public void testGetMonthlyAmountEdgeCaseCompletionRatio() {
        String personId = "200001010000";
        int income = 50000;
        int studyRate = 100;
        int completionRatio = 49; // Below threshold
        int expectedAmount = payment.ZERO_LOAN + payment.ZERO_SUBSIDY;
        assertEquals(expectedAmount, payment.getMonthlyAmount(personId, income, studyRate, completionRatio));
    }

}
