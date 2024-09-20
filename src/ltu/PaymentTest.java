package ltu;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

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
    public void testGetMonthlyAmountWithNegativeIncome() {
        String personId = "2000010100000"; 
        int income = -1;
        int studyRate = 100;
        int completionRatio = 100;
        payment.getMonthlyAmount(personId, income, studyRate, completionRatio);
    }

    @Test
    public void testGetMonthlyAmountUnderage() {
        String personId = "2006010100000"; // Age 18, assuming current year is 2024
        int income = 0;
        int studyRate = 100;
        int completionRatio = 100;
        int amount = payment.getMonthlyAmount(personId, income, studyRate, completionRatio);
        assertEquals(0, amount); // Expecting zero amount for underage
    }

    @Test
    public void testGetMonthlyAmountOverAgeLimitForSubsidy() {
        String personId = "1967010100000"; // Age 57, assuming current year is 2024
        int income = 0;
        int studyRate = 100;
        int completionRatio = 100;
        int amount = payment.getMonthlyAmount(personId, income, studyRate, completionRatio);
        assertEquals(0, amount); // Expecting zero amount for over age limit for subsidy
    }

    @Test
    public void testGetMonthlyAmountOverAgeLimitForLoan() {
        String personId = "1977010100000"; // Age 47, assuming current year is 2024
        int income = 0;
        int studyRate = 100;
        int completionRatio = 100;
        int amount = payment.getMonthlyAmount(personId, income, studyRate, completionRatio);
        assertEquals(2816, amount); // Expecting zero loan + subsidy
    }

    @Test
    public void testGetMonthlyAmountBelowHalfTimeStudy() {
        String personId = "2000010100000"; 
        int income = 0;
        int studyRate = 49; // Less than half time
        int completionRatio = 100;
        int amount = payment.getMonthlyAmount(personId, income, studyRate, completionRatio);
        assertEquals(0, amount); // Expecting zero amount for below half-time study
    }

    @Test
    public void testGetSubsidyHalfTimeStudy() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getSubsidy", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24; 
        int income = 0;
        int studyRate = 50; // Half time
        int completionRatio = 100;
        int subsidy = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field halfSubsidyField = PaymentImpl.class.getDeclaredField("HALF_SUBSIDY");
        halfSubsidyField.setAccessible(true);
        int halfSubsidy = (int) halfSubsidyField.get(payment);

        assertEquals(halfSubsidy, subsidy); // 50% subsidiary
    }

    @Test
    public void testGetSubsidyFullTimeStudy() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getSubsidy", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 0;
        int studyRate = 100; // Full time
        int completionRatio = 100;
        int subsidy = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field fullSubsidyField = PaymentImpl.class.getDeclaredField("FULL_SUBSIDY");
        fullSubsidyField.setAccessible(true);
        int fullSubsidy = (int) fullSubsidyField.get(payment);

        assertEquals(fullSubsidy, subsidy); // 100% subsidiary
    }

    @Test
    public void testGetSubsidyHighIncomeFullTime() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getSubsidy", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 90000; // Above limit for full-time study
        int studyRate = 100; // Full time
        int completionRatio = 100;
        int subsidy = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field zeroSubsidyField = PaymentImpl.class.getDeclaredField("ZERO_SUBSIDY");
        zeroSubsidyField.setAccessible(true);
        int zeroSubsidy = (int) zeroSubsidyField.get(payment);
        assertEquals(zeroSubsidy, subsidy); // Expecting zero subsidy for high income full-time
    }

    @Test
    public void testGetSubsidyHighIncomeLessThanFullTime() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getSubsidy", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 130000; // Above limit for less than full-time study
        int studyRate = 50;
        int completionRatio = 100;
        int subsidy = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field zeroSubsidyField = PaymentImpl.class.getDeclaredField("ZERO_SUBSIDY");
        zeroSubsidyField.setAccessible(true);
        int zeroSubsidy = (int) zeroSubsidyField.get(payment);

        assertEquals(zeroSubsidy, subsidy); // Expecting zero subsidy for high income less than full-time
    }

    @Test
    public void testGetSubsidyLowCompletionRatio() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getSubsidy", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 0;
        int studyRate = 100;
        int completionRatio = 49; // Below 50%
        int subsidy = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field zeroSubsidyField = PaymentImpl.class.getDeclaredField("ZERO_SUBSIDY");
        zeroSubsidyField.setAccessible(true);
        int zeroSubsidy = (int) zeroSubsidyField.get(payment);

        assertEquals(zeroSubsidy, subsidy); // Expecting zero subsidy for low completion ratio
    }

    @Test
    public void testGetLoanFullTime() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getLoan", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 0;
        int studyRate = 100;
        int completionRatio = 100;
        int loan = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field fullLoanField = PaymentImpl.class.getDeclaredField("FULL_LOAN");
        fullLoanField.setAccessible(true);
        int fullLoan = (int) fullLoanField.get(payment);

        assertEquals(fullLoan, loan); // Full loan amount
    }

    @Test
    public void testGetLoanHalfTime() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getLoan", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 0;
        int studyRate = 50;
        int completionRatio = 100;
        int loan = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field halfLoanField = PaymentImpl.class.getDeclaredField("HALF_LOAN");
        halfLoanField.setAccessible(true);
        int halfLoan = (int) halfLoanField.get(payment);

        assertEquals(halfLoan, loan); // Half loan amount
    }

    @Test
    public void testGetLoanHighIncomeFullTime() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getLoan", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 90000; // Above limit for full-time study
        int studyRate = 100;
        int completionRatio = 100;
        int loan = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field zeroLoanField = PaymentImpl.class.getDeclaredField("ZERO_LOAN");
        zeroLoanField.setAccessible(true);
        int zeroLoan = (int) zeroLoanField.get(payment);

        assertEquals(zeroLoan, loan); // Expecting zero loan for high income full-time
    }

    @Test
    public void testGetLoanHighIncomeLessThanFullTime() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getLoan", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24; 
        int income = 130000; // Above limit for less than full-time study
        int studyRate = 50;
        int completionRatio = 100;
        int loan = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field zeroLoanField = PaymentImpl.class.getDeclaredField("ZERO_LOAN");
        zeroLoanField.setAccessible(true);
        int zeroLoan = (int) zeroLoanField.get(payment);

        assertEquals(zeroLoan, loan); // Expecting zero loan for high income less than full-time
    }

    @Test
    public void testGetLoanLowCompletionRatio() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getLoan", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24; 
        int income = 0;
        int studyRate = 100;
        int completionRatio = 49; // Below 50%
        int loan = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field zeroLoanField = PaymentImpl.class.getDeclaredField("ZERO_LOAN");
        zeroLoanField.setAccessible(true);
        int zeroLoan = (int) zeroLoanField.get(payment);

        assertEquals(zeroLoan, loan); // Expecting zero loan for low completion ratio
    }

    @Test
    public void testGetAge() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getAge", String.class);
        method.setAccessible(true);
        String personId = "2000010100000"; // Updated to 13 characters
        int age = (Integer) method.invoke(payment, personId);
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
    public void testGetLoanZeroHighIncome() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getLoan", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 1000000;
        int studyRate = 100;
        int completionRatio = 100;
        int loan = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field zeroLoanField = PaymentImpl.class.getDeclaredField("ZERO_LOAN");
        zeroLoanField.setAccessible(true);
        int zeroLoan = (int) zeroLoanField.get(payment);

        assertEquals(zeroLoan, loan);
    }

    @Test
    public void testGetLoanHalfStudyRate() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getLoan", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 50000;
        int studyRate = 50;
        int completionRatio = 100;
        int loan = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field halfLoanField = PaymentImpl.class.getDeclaredField("HALF_LOAN");
        halfLoanField.setAccessible(true);
        int halfLoan = (int) halfLoanField.get(payment);

        assertEquals(halfLoan, loan);
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

    @Test
    public void testGetSubsidyZeroHighIncome() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getSubsidy", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 1000000;
        int studyRate = 100;
        int completionRatio = 100;
        int subsidy = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field zeroSubsidyField = PaymentImpl.class.getDeclaredField("ZERO_SUBSIDY");
        zeroSubsidyField.setAccessible(true);
        int zeroSubsidy = (int) zeroSubsidyField.get(payment);

        assertEquals(zeroSubsidy, subsidy);
    }

    @Test
    public void testGetSubsidyHalfStudyRate() throws Exception {
        Method method = PaymentImpl.class.getDeclaredMethod("getSubsidy", int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        int age = 24;
        int income = 50000;
        int studyRate = 50;
        int completionRatio = 100;
        int subsidy = (int) method.invoke(payment, age, income, studyRate, completionRatio);

        Field halfSubsidyField = PaymentImpl.class.getDeclaredField("HALF_SUBSIDY");
        halfSubsidyField.setAccessible(true);
        int halfSubsidy = (int) halfSubsidyField.get(payment);

        assertEquals(halfSubsidy, subsidy);
    }
    
    @Test
    public void testPaymentDate() {
        LocalDate now = LocalDate.now();
        LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        if (lastDayOfMonth.getDayOfWeek() == DayOfWeek.SATURDAY) {
            lastDayOfMonth = lastDayOfMonth.minusDays(1);
        } else if (lastDayOfMonth.getDayOfWeek() == DayOfWeek.SUNDAY) {
            lastDayOfMonth = lastDayOfMonth.minusDays(2);
        }
        String expectedPaymentDate = lastDayOfMonth.toString().replace("-", "");
        assertEquals(expectedPaymentDate, payment.getNextPaymentDay());
    }
}