package ltu;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import static ltu.CalendarFactory.getCalendar;


public class PaymentTest
{
    PaymentImpl payment;
    @Test
    public void newPaymentObject()throws IOException{
        payment = new PaymentImpl(getCalendar());
    }


    @Test
    public void testNextPaymentDay() 
    {
        assertEquals("20240930", payment.getNextPaymentDay());
        
    }

}
