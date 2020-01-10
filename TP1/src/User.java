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
	public String passord;
	
	public static boolean findUser(User user) {
		/*to do*/
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
            System.out.println(users);
            
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
	
	public static void createUser(User user) {
		//Write JSON file
        try (FileWriter file = new FileWriter("user.json")) {
 
        	JSONArray users = getUsers();
        	if(users == null) {
        		users = new JSONArray();
        	}
        	
        	JSONObject userObj = new JSONObject();
        	userObj.put(user.username, user.passord);
        	users.add(userObj);
            file.write(users.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
