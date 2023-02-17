package timeWizard.controllers;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLDataException;
import java.util.*;

import javax.crypto.BadPaddingException;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import timeWizard.TableTask;
import timeWizard.User;
import timeWizard.CalendarTask;
import timeWizard.DAOLayer.MainDAO;
import timeWizard.tokens.AuthToken;
import timeWizard.tokens.EncryptedAuthToken;



@RestController
@RequestMapping(produces = "application/json")
@CrossOrigin(origins="http://localhost:8080")
public class WelcomeController {

	public <T> T mapObject(String jsonString, Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonString, clazz);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@PostMapping( path="/register", consumes ={"application/json"})
	public ResponseEntity<String> register(@RequestBody String userRegisterData) {

		User user = mapObject(userRegisterData,User.class);

		if(user == null) {
			return new ResponseEntity<String>("User may not have been initialized", HttpStatus.BAD_REQUEST);
		}
		user.encryptPassword();

		try {
			MainDAO.create(user);
		} catch (SQLDataException e) {
			return new ResponseEntity<String>("User already registered", HttpStatus.BAD_REQUEST);
		}
		
		AuthToken token = new AuthToken(user);
		EncryptedAuthToken encryptedToken = token.encrypt();

		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", encryptedToken.toString());
	    headers.add("Access-Control-Expose-Headers", "Authorization");
		return new ResponseEntity<String>("Login is accept", headers, HttpStatus.CREATED);
	}
	
	@PostMapping(path="/login", consumes ={"application/json"})
	public ResponseEntity<String> login(@RequestBody String userLoginData){
		HttpHeaders headers = new HttpHeaders();
		User loginingUser = null;
		
		try {
			ObjectMapper mapper = new ObjectMapper(); //Deserialization requested JSON
			loginingUser = mapper.readValue((new StringReader(userLoginData)), User.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(loginingUser == null) return new ResponseEntity<String>("User may not have been initialized", HttpStatus.BAD_REQUEST);
		loginingUser.encryptPassword();
		
		User verifyUser = (User)MainDAO.read(User.class, loginingUser.getEmail());
		if(!loginingUser.equals(verifyUser)) {
			return new ResponseEntity<String>("Login failed", headers, HttpStatus.BAD_REQUEST);
		}
		
		AuthToken token = new AuthToken(loginingUser);
		EncryptedAuthToken encryptedToken = token.encrypt();
		
	    headers.add("Authorization", encryptedToken.getEncryptedStringToken());
	    headers.add("Access-Control-Expose-Headers", "Authorization");
		return new ResponseEntity<String>("Login is accept", headers, HttpStatus.ACCEPTED);
	}
	
	@GetMapping(path="/checkToken")
	public ResponseEntity<String> checkToken(@RequestHeader(name = "Authorization") String token) {
		EncryptedAuthToken encryptedToken = new EncryptedAuthToken(token);
		try {
			if(encryptedToken.isTrue()) {
				return new ResponseEntity<String>("Token is true", HttpStatus.ACCEPTED);
			}
		} catch (BadPaddingException e) {
			return new ResponseEntity<String>("Token is expired", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<String>("Token is false", HttpStatus.BAD_REQUEST);
	}
	
	@PostMapping(path="/saveCalendarTask", consumes ={"application/json"})
	public ResponseEntity<String> saveCalendarTask(@RequestBody String taskData, @RequestHeader(name = "Authorization") String token) {
		CalendarTask task = new CalendarTask();
		String userEmail = "";
		
		try { 
			AuthToken decryptedToken = (new EncryptedAuthToken(token)).decrypt();
			userEmail = decryptedToken.getUserEmail();
		} catch (BadPaddingException e) {
			return new ResponseEntity<String>("Token is expired", HttpStatus.BAD_REQUEST);
		}
		System.out.println(1);
		
		try {
			ObjectMapper mapper = new ObjectMapper(); //Deserialization requested JSON
			task = mapper.readValue((new StringReader(taskData)), CalendarTask.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		task.setEmail(userEmail);
		try {
			MainDAO.create(task);
		} catch (SQLDataException e) {
			return new ResponseEntity<String>("Task didn't created", HttpStatus.BAD_REQUEST);
		}
		
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<String>("Task created", headers, HttpStatus.CREATED);
	}
	
	@PostMapping(path="/updateTask", consumes ={"application/json"})
	public ResponseEntity<String> updateTask(@RequestBody String taskData,@RequestHeader(name = "Authorization") String token) {
		CalendarTask task = new CalendarTask();
		String userEmail = "";
		
		try { 
			AuthToken decryptedToken = (new EncryptedAuthToken(token)).decrypt();
			userEmail = decryptedToken.getUserEmail();
		} catch (BadPaddingException e) {
			return new ResponseEntity<String>("Token is expired", HttpStatus.BAD_REQUEST);
		}
		System.out.println(1);
		
		try {
			ObjectMapper mapper = new ObjectMapper(); //Deserialization requested JSON
			task = mapper.readValue((new StringReader(taskData)), CalendarTask.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		task.setEmail(userEmail);
		try {
			MainDAO.update(task);
		} catch (SQLDataException e) {
			return new ResponseEntity<String>("Task didn't update", HttpStatus.BAD_REQUEST);
		}
		
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<String>("Task updated", headers, HttpStatus.CREATED);
	}

	@PostMapping(path="/deleteTask", consumes ={"application/json"})
	public ResponseEntity<String> deleteTask(@RequestBody String taskData,@RequestHeader(name = "Authorization") String token){
		CalendarTask task = new CalendarTask();
		String userEmail = "";

		try {
			AuthToken decryptedToken = (new EncryptedAuthToken(token)).decrypt();
			userEmail = decryptedToken.getUserEmail();
		} catch (BadPaddingException e) {
			return new ResponseEntity<String>("Token is expired", HttpStatus.BAD_REQUEST);
		}
		System.out.println(1);

		try {
			ObjectMapper mapper = new ObjectMapper(); //Deserialization requested JSON
			task = mapper.readValue((new StringReader(taskData)), CalendarTask.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		task.setEmail(userEmail);
		try {
			MainDAO.delete(task);
		} catch (SQLDataException e) {
			return new ResponseEntity<String>("Task didn't delete", HttpStatus.BAD_REQUEST);
		}

		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<String>("Task deleted", headers, HttpStatus.CREATED);
	}
	
	@GetMapping(path="/getAllCallendarTasks")
	public ResponseEntity<String> getAllCalendarTasks(@RequestHeader(name = "Authorization") String token) {
		EncryptedAuthToken encryptedToken = new EncryptedAuthToken(token);
		String email;
		String JSONList = "";
		
		try {
			if(!encryptedToken.isTrue()) {
				return new ResponseEntity<String>("Token is false", HttpStatus.BAD_REQUEST);
			}
			email = encryptedToken.decrypt().getUserEmail();
		} catch (BadPaddingException e) {
			return new ResponseEntity<String>("Token is expired", HttpStatus.BAD_REQUEST);
		}
		
		@SuppressWarnings (value="unchecked")
		ArrayList<CalendarTask> list = (ArrayList<CalendarTask>) MainDAO.readAll(CalendarTask.class,email);
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			JSONList = mapper.writeValueAsString(list);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<String>(JSONList,HttpStatus.ACCEPTED);
	}

	@PostMapping(path="/saveTableTask", consumes ={"application/json"})
	public ResponseEntity<String> saveTableTask(@RequestBody String taskData, @RequestHeader(name = "Authorization") String token) {
		TableTask task = new TableTask();
		String userEmail = "";

		try {
			AuthToken decryptedToken = (new EncryptedAuthToken(token)).decrypt();
			userEmail = decryptedToken.getUserEmail();
		} catch (BadPaddingException e) {
			return new ResponseEntity<String>("Token is expired", HttpStatus.BAD_REQUEST);
		}


		try {
			ObjectMapper mapper = new ObjectMapper(); //Deserialization requested JSON
			task = mapper.readValue((new StringReader(taskData)), TableTask.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		task.setEmail(userEmail);
		try {
			MainDAO.create(task);
		} catch (SQLDataException e) {
			return new ResponseEntity<String>("Task didn't created", HttpStatus.BAD_REQUEST);
		}

		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<String>("Task created", headers, HttpStatus.CREATED);
	}

	@GetMapping(path="/getAllTableTasks")
	public ResponseEntity<String> getAllTableTasks(@RequestHeader(name = "Authorization") String token) {
		EncryptedAuthToken encryptedToken = new EncryptedAuthToken(token);
		String email;
		String JSONList = "";

		try {
			if(!encryptedToken.isTrue()) {
				return new ResponseEntity<String>("Token is false", HttpStatus.BAD_REQUEST);
			}
			email = encryptedToken.decrypt().getUserEmail();
		} catch (BadPaddingException e) {
			return new ResponseEntity<String>("Token is expired", HttpStatus.BAD_REQUEST);
		}

		@SuppressWarnings (value="unchecked")
		ArrayList<CalendarTask> list = (ArrayList<CalendarTask>) MainDAO.readAll(TableTask.class,email);

		try {
			ObjectMapper mapper = new ObjectMapper();
			JSONList = mapper.writeValueAsString(list);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<String>(JSONList,HttpStatus.ACCEPTED);
	}
}
