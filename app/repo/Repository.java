package repo;

import java.util.*;

//Crypto shit
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

//For date search, to check if they are on the same day
import org.apache.commons.lang.time.DateUtils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import models.*;

/**
 * Class that contains many general functions for the system.
 */
public class Repository {
	
	/**
	 * Checks if the given username/password is valid
	 * @param username the username
	 * @param password the password
	 * @return true if the username/password are valid
	 */
	public static boolean login(String username, String password){
		User tmp = User.find("byUsernameAndPassword", username, encodePassword(password)).first();
		if(tmp != null)
			return true;
		else
			return false;
	}
	
	
	/**
	 * Searches for exams within the given dates
	 * @param first the beginning of the date range
	 * @param last the end of the test range
	 * @return a list of all the results
	 */
	public static List<Exam> searchByDate(Date first, Date last){
		//Check for null
		if( first == null || last == null){
			return new ArrayList<Exam>(0);
		}
		
		List<Exam> exams = Exam.findAll();
		List<Exam> toRet = new ArrayList<Exam>(exams.size());
		
		//Manually compare them all
		for(Exam cur: exams){
			if( cur.getDate().after(first)
					&& (cur.getDate().before(last) || DateUtils.isSameDay(cur.getDate(), last) )  )
				toRet.add(cur);
		}
		
		return toRet;
	}
	
	/**
	 * Gets all the exams for a given patient
	 * @param patientId the patient's ID
	 * @return a list of all their exams
	 */
	//Maybe search by name? Im thinking a dropdown for this
	public static List<Exam> searchByPatient(Long patientId){
		Patient patient = User.findById(patientId);
		if(patient != null)
			return patient.getExams();
		else
			return new ArrayList<Exam>(0);
	}
	
	/**
	 * Gets all the exams by a given physician
	 * @param physicianId the physician's ID
	 * @return a list of all their exams
	 */
	public static List<Exam> searchByPhysician(Long physicianId){
		Physician physician = Physician.findById(physicianId);
		if(physician != null)
			return physician.getExams();
		else
			return new ArrayList<Exam>(0);
	}
	
	
	
	/**
	 * Encodes the given string
	 * @param plaintext the string to encode
	 * @return the encoded string
	 */
	public static String encodePassword(String plaintext){
		try {
			// only the first 8 Bytes of the constructor argument are used 
			// as material for generating the keySpec
			DESKeySpec keySpec = new DESKeySpec("YourSecr".getBytes("UTF8")); 
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyFactory.generateSecret(keySpec);
			BASE64Encoder base64encoder = new BASE64Encoder();
			
			// ENCODE plainTextPassword String
			byte[] cleartext = plaintext.getBytes("UTF8");      
			Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
			cipher.init(Cipher.ENCRYPT_MODE, key);
			plaintext = base64encoder.encode(cipher.doFinal(cleartext));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return plaintext;
	}
	
	/**
	 * Decodes the given string
	 * @param encoded the encoded string
	 * @return the decoded string
	 */
	public static String decodePassword(String encoded){
		try {
			// only the first 8 Bytes of the constructor argument are used 
			// as material for generating the keySpec
			DESKeySpec keySpec = new DESKeySpec("YourSecr".getBytes("UTF8")); 
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyFactory.generateSecret(keySpec);
			BASE64Decoder base64decoder = new BASE64Decoder();
			
			// DECODE encryptedPwd String
			byte[] encrypedPwdBytes = base64decoder.decodeBuffer(encoded);
			Cipher cipher = Cipher.getInstance("DES");// cipher is not thread safe
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
			encoded = plainTextPwdBytes.toString();
		} catch (Exception e) {
			System.out.println("Error decrypting");
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		} 
		
		return encoded;
	}
	
}
