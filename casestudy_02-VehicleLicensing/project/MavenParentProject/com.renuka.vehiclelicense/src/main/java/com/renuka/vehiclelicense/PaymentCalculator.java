package com.renuka.vehiclelicense;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext; 
import org.apache.synapse.mediators.AbstractMediator;

public class PaymentCalculator extends AbstractMediator {
	
	private Log log = LogFactory.getLog(PaymentCalculator.class);

	public boolean mediate(MessageContext context) { 
		Integer num_of_months = (Integer)context.getProperty("num_of_months");
		Double insurance_rate = (Double)context.getProperty("insurance_rate");
		
		double final_payment = calculatePayment(num_of_months, insurance_rate);
		log.info("FINAL PAYMENT = " + final_payment);
		context.setProperty("final_payment", final_payment);
		
		return true;
	}
	
	private double calculatePayment(int num_of_months, double insurance_rate){
		double final_payment = num_of_months * insurance_rate;
		return final_payment;
	}
}
