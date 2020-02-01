import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class User {
	
	public String username;
	public String password;

	public static boolean Login(User user) {
		JSONArray users = getUsers();
		if(users == null) {
			User.createUser(user, users);
			return true;
		}
		
		for (Object obj : users) {
			
			JSONObject item = (JSONObject)obj;
			
			String _username = item.get("username").toString();
				
			if(item.get("username").toString().equals(user.username)) {
				if (item.get("password").toString().equals(user.password)) {
					return true;					
				}
				else {
					return false;
				}
			}	
		}
		
		
		User.createUser(user, users);
		return true;
	}
	
	
	public static JSONArray getUsers() {
		//JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader("user.json"))
        {
        	
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            JSONArray users = (JSONArray) obj;
            //reader.close();
            return users;
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
	}
	
	public static void createUser(User user, JSONArray users) {
    	if(users == null) {
    		users = new JSONArray();
    	}
    	
		//Write JSON file
        try (FileWriter file = new FileWriter("user.json")) {
        	JSONObject userObj = new JSONObject();
        	userObj.put("username", user.username);
        	userObj.put("password", user.password);
        	users.add(userObj);
            file.write(users.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
