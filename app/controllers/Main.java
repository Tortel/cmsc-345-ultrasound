package controllers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import models.*;
import play.mvc.Controller;
import play.data.validation.Required;
import repo.Repository;

/**
 * Non-secured controller class.<br>
 * This handles the index, create account, and forgot password pages.
 */
public class Main extends Controller {
	
	/**
	 * Displays the main welcome page.
	 */
	public static void index(){
		if(!Security.isConnected()){
			render();
		} else {
			PageController.welcome();
		}
	}
	
	/**
	 * Displays the form to create a new account
	 */
	public static void createAccount(){
		render();
	}
	
	/**
	 * Creates a new user
	 * @param email
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @param address
	 * @param phoneNumber
	 * @param sex
	 * @param code code to create the account as a physician
	 */
	public static void newAccount(@Required(message = "Please enter a valid email address") String email,
								  @Required(message = "Please enter a password") String password,
								  @Required(message = "Please enter your first name") String firstName,
								  @Required(message = "Please enter your last name") String lastName,
								  @Required(message = "Please enter your address")  String address,
								  @Required(message = "Please enter your phone number") String phoneNumber,
								  @Required String sex, String code){
		
    	//Check that the email isn't registered already
    	if(User.find("byUsername", email).fetch().size() > 0){
    		//Email already registered.
    		validation.addError("email", "Email already registered", "email");
    	}
    	
    	//Validate email address
	    String  expression="^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	    CharSequence inputStr = email;
	    Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(inputStr);
	    if(!matcher.matches()){
		    validation.addError("email", "Enter a valid email address", email);
	    }
	    
	    //Validate the phone number
	    expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";  
	    inputStr = phoneNumber;  
	    pattern = Pattern.compile(expression);  
	    matcher = pattern.matcher(inputStr);  
	    if( !matcher.matches() ){
	    	validation.addError("phoneNumber", "Enter a valid phone number", phoneNumber);
	    }
	    
	    //Check if there is an entered code, and if its valid
	    if(!code.equals("") && !code.equals("physician")){
	    	validation.addError("code", "Please enter a valid physician code, or leave it blank", code);
	    }
	    
    	if (validation.hasErrors()){
            render("Main/createAccount.html", email, firstName, lastName, address, phoneNumber, code);
        }
    	
    	//If the code is correct, create user as physician
    	if(code.equals("physician")){
    		new Physician(email, password, firstName, lastName).save();
    	} else {
    		//Else, create them as a patient
    		new Patient(email, password, firstName, lastName, address, phoneNumber, sex.charAt(0)).save();
    	}
    	
    	//Authenticate them automatically
    	session.put("username", email);
    	PageController.welcome();
	}
	
	/**
	 * Readers the forgot password page
	 */
	public static void forgotPassword(){
		render();
	}
	
	/**
	 * Emails a user their password
	 * @param email
	 */
	public static void sendPassword(@Required(message = "A valid email address is required") String email){
		User user = User.find("byUsername", email).first();
		
		if(validation.hasErrors() || user == null){
			if(user == null){
				validation.addError(email, "Email address is not registered", email);
			}
			render("Main/forgotPassword.html");
		}
		
		//This sends the email
		user.recoverPassword();
		
		render(email);
	}
}
