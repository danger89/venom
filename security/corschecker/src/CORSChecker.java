import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.UnknownHostException;



public class CORSChecker {
	
	  public static void main(String[] args) {
		  
		  try
		  {
			System.out.print(System.lineSeparator() + "#########################################################################" + System.lineSeparator());
			System.out.print("#    _____ ____  _____   _____    _____ _               _               #" + System.lineSeparator());
			System.out.print("#   / ____/ __ \\|  __ \\ / ____|  / ____| |             | |              #" + System.lineSeparator());
			System.out.print("#  | |   | |  | | |__) | (___   | |    | |__   ___  ___| | _____ _ __   #" + System.lineSeparator());
			System.out.print("#  | |   | |  | |  _  / \\___ \\  | |    | '_ \\ / _ \\/ __| |/ / _ \\ '__|  #" + System.lineSeparator());
			System.out.print("#  | |___| |__| | | \\ \\ ____) | | |____| | | |  __/ (__|   <  __/ |     #" + System.lineSeparator());
			System.out.print("#   \\_____\\____/|_|  \\_\\_____/   \\_____|_| |_|\\___|\\___|_|\\_\\___|_|     #" + System.lineSeparator());
			System.out.print("#                                                                       #" + System.lineSeparator());
			System.out.print("#  By: Milad Khoshdel                  Blog: https://blog.regux.com     #" + System.lineSeparator());
			System.out.print("#  Email: Miladkhoshdel@gmail.com                                       #" + System.lineSeparator());
			System.out.print("#                                                                       #" + System.lineSeparator());
			System.out.print("#########################################################################" + System.lineSeparator() + System.lineSeparator());

			                                                                 
			Pattern p = Pattern.compile("(http|https):\\/\\/[A-Za-z\\.-]*\\.[a-zA-Z]{2,10}.*");			  
			System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
			String domainname;
			String origin = "http://test-site-example.com";
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter a URL (Exapmle: https://foo.bar): "); 
			domainname = sc.nextLine();
			domainname = domainname.replaceAll("\\s+","");
			URL obj = null;
			if(domainname.contains("http"))
				obj = new URL(domainname);
			else 
				obj = new URL("http://" + domainname);
			Matcher m = p.matcher(obj.toString());
			while(!m.matches())
			{

				System.out.print(obj.toString() + " is invalid. Please Enter a Valid URL (Exapmle: https://foo.bar): ");
				domainname = sc.next();
				if(domainname.contains("http"))
					obj = new URL(domainname);
				else 
					obj = new URL("http://" + domainname);
				m = p.matcher(obj.toString());
			}

			
			
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
			conn.setRequestProperty("Origin", origin);
			
			
			System.out.print(System.lineSeparator() + "Complete Response Header:" + System.lineSeparator() + System.lineSeparator());
			System.out.print(System.lineSeparator() + "[+] Your URL: " + obj);
			System.out.print(System.lineSeparator() + "[+] Origin Method Set to " + conn.getRequestProperty("Origin"));
			System.out.print(System.lineSeparator() + "[+] Getting Response Code: " + conn.getResponseCode() + System.lineSeparator());
			
			Map<String, List<String>> map = conn.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				System.out.println("[+] " + entry.getKey() + ":" + entry.getValue());		
			}		
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(conn.getInputStream()));
					String inputLine;
					StringBuffer content = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
					    content.append(inputLine);
					}
					in.close();
					conn.disconnect();
			
			System.out.print("----------------------------------------");
			System.out.print(System.lineSeparator() + "[+] Status: " + System.lineSeparator());

			
			if (conn.getHeaderField("Access-Control-Allow-Origin") != null)
			{
				if (conn.getHeaderField("Access-Control-Allow-Origin").contains("*"))
				{
					System.out.println(" [-] Vurnerable!   --->    " + "Access-Control-Allow-Origin: " + (conn.getHeaderField("Access-Control-Allow-Origin")));
				}
				else if ((conn.getHeaderField("Access-Control-Allow-Origin").contains(origin)))
				{
					System.out.println(" [-] Vurnerable!   --->    " + "Access-Control-Allow-Origin: " + (conn.getHeaderField("Access-Control-Allow-Origin")));
				}
				else
				{
					System.out.println(" [-] Not Vulnerable");
				}
				
				if (conn.getHeaderField("Access-Control-Allow-Credentials") != null)
				{
					if (conn.getHeaderField("Access-Control-Allow-Credentials").contains("true"))
					{
						System.out.println(" [-] Warning!      --->    " + "Access-Control-Allow-Credentials: " + (conn.getHeaderField("Access-Control-Allow-Origin")));
					}
				}
				
				sc.close();
			}
			else
			{
				System.out.println(" [-] There is no Access-Control-Allow-Origin Header.");
			}
		  }
		  catch(UnknownHostException exception)
		  {
			  System.out.print(System.lineSeparator() + " [-] Error: Host is Invalid. Please Check your URL and make sure that you are connected to the internet.");
		  }
		  catch (Exception e)
		  {
			  System.out.print(System.lineSeparator() + " [-] Some Error Happened.");
	      }  
	  }
}
