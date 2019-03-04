package TPPasswordGenerator;

import java.util.UUID;

import org.apache.synapse.MessageContext; 
import org.apache.synapse.mediators.AbstractMediator;

public class PasswordGenerator extends AbstractMediator { 

	public boolean mediate(MessageContext context) { 
		String password = getRandomPassword();
		context.setProperty("generated_password", password);
		
		return true;
	}
	
	private String getRandomPassword(){
		return UUID.randomUUID().toString();
	}
}
