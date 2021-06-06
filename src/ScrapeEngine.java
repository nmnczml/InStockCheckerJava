
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.io.*;  
import java.util.Scanner;  

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
 
public class ScrapeEngine {
	public WebDriver driver;
 	ChromeOptions options = null;
	private static final String TASKLIST = "tasklist";
	private static final String KILL = "taskkill /F /IM ";
	public ArrayList<String> alAsins = new ArrayList<String>();

	public HashMap hmCSV = new HashMap<String, String>();
	public int innerGetCounter = 0;
 	public String stat = "";

	private void GetURL(String URL) {
		innerGetCounter++;
		if (driver == null) {
			driver = new ChromeDriver(options);

			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

		} 
		//to handle chrome memory leak killing after every 10 session (on windows)
		else if (innerGetCounter % 10 == 0)

		{
			driver.quit();

			logging("killing old Chrome.exe");
			try {
				if (isProcessRunning("chrome.exe"))
					killProcess("chrome.exe");
			} catch (Exception e) {

				e.printStackTrace();
			}

			driver = new ChromeDriver(options);

			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		}

		driver.get(URL);

	}

	public boolean isProcessRunning(String serviceName) throws Exception {

		Process p = Runtime.getRuntime().exec(TASKLIST);
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {

			// System.out.println(line);
			if (line.contains(serviceName)) {
				return true;
			}
		}

		return false;

	}

	public void killProcess(String serviceName) throws Exception {

		Runtime.getRuntime().exec(KILL + serviceName);

	}

	public void setUp() throws Exception {


		options = new ChromeOptions();

		// for Windows
		// options.addArguments("user-data-dir=C:\\Users\\Administrator\\AppData\\Local\\Google\\Chrome\\User
		// Data");
		options.addArguments("webdriver.chrome.driver=/Users/numancizmeli/Documents/nmnczmlPortfolio/InStockCheckerJava");
		options.addArguments("user-data-dir=/Users/numancizmeli/Library/Application Support/Google/Chrome/tmp1");
	 

		// options.addArguments("headless");

		DesiredCapabilities capabilities = DesiredCapabilities.chrome();

		capabilities.setCapability(ChromeOptions.CAPABILITY, options);

	 
		GetURL("https://www.amazon.com/");


	}

	 
 

	private void logging(Exception ex) {
		try {
			System.out.println(ex);

		} catch (Exception ex2) {
			ex2.printStackTrace();
		}

	}

	public void logging(String ex) {
		try {
			System.out.println(ex);

		} catch (Exception ex2) {
			ex2.printStackTrace();
		}

	}

	public void addProductToList(String asin, String sku) {
		try {
			String productURL = "https://www.amazon.com/dp/" + asin;

			GetURL(productURL);

			String availability = "";
			try {
				availability = driver.findElement(By.xpath("//*[@id=\"availability\"]")).getText();
			} catch (Exception ex) {
				availability = driver.findElement(By.xpath("//*[@id=\"availability\"]/span")).getText();
			}
			// In Stock.
			

			logging("Product Title: " + asin + " Sku: " + sku + " Availability: " + availability);

			addValToCSV(asin, sku, availability);

			logging("Product added successfully: " + asin);

		} catch (Exception ex) {
			addValToCSV(asin, sku, "UnKnown");
			logging(ex);
			logging("Product failed: " + asin);

		}

	}

 

	public void createCSVFileHeader() {
		StringBuilder sbCSV = new StringBuilder();

		sbCSV.append("Seller SKU");
		sbCSV.append(",");
		sbCSV.append("Product ID");
		sbCSV.append(",");
		sbCSV.append("Item name (aka Title)");
		sbCSV.append(",");
		sbCSV.append("Standard Price");
		sbCSV.append(",");
		sbCSV.append("Quantity");
		sbCSV.append(",");
		sbCSV.append("Condition Type");
		sbCSV.append(",");
		sbCSV.append("Offer Condition Note");

		sbCSV.append("\n");
		hmCSV.put("header", sbCSV.toString());

	}

	public void addValToCSV(String asin, String Sku, String Availablity) {
		StringBuilder sbCSV = new StringBuilder();

		if (!hmCSV.containsKey(Sku)) {

			sbCSV.append(Sku);
			sbCSV.append(",");
			sbCSV.append(asin);
			sbCSV.append(",");
			sbCSV.append("");
			sbCSV.append(",");
			sbCSV.append("");
			sbCSV.append(",");
			sbCSV.append("0");
			sbCSV.append(",");
			sbCSV.append("");
			sbCSV.append(",");
			sbCSV.append(Availablity);

			sbCSV.append("\n");

			hmCSV.put(Sku, sbCSV.toString());
		}

	}

	private HashMap<String, String> getAsinList() {
		
		HashMap<String, String> alAsins = new HashMap<String, String>();

		 
		try {
			FileInputStream fis=new FileInputStream("AsinList.txt");       
			Scanner sc=new Scanner(fis);     

			while(sc.hasNextLine())  
			{  
				System.out.println();     
				
				String asin = sc.nextLine();
				String sku ="SKU"+ asin;

				if (!alAsins.containsKey(sku))
					alAsins.put(sku, asin);
			}  
				sc.close();       
			 

			 

			return alAsins;

		} catch (Exception ex) {
			logging(ex);

		} 
		return alAsins;

	}

	public void MainLoop() {

		try {
			hmCSV = new HashMap<String, String>();
			HashMap<String, String> hmAsins = getAsinList();
			int currentSize = hmAsins.size();
			
			createCSVFileHeader();

			int innerCounter = 0;
			Iterator it = hmAsins.entrySet().iterator();
			Random r = new Random();
			while (it.hasNext()) {
				try {

					int threadSleep = r.nextInt(6000);
					logging("Sleeping " + threadSleep);
					Thread.sleep(threadSleep);

					HashMap.Entry pair = (HashMap.Entry) it.next();
					String sku = pair.getKey().toString();
					String asin = pair.getValue().toString();
					addProductToList(asin, sku);
					logging(pair.getKey() + " = " + pair.getValue());
					innerCounter++;
					this.stat = "Items " + innerCounter + "/" + currentSize;
					logging(stat);

				} catch (Exception ex) {
					ex.printStackTrace();
				
					Thread.sleep(60000 * 5);// there might be ddos protection, wait for 5 min
				}
			}

			writeResultsToCSV();

			System.out.println("Finished");

		} catch (Exception ex) {
 			ex.printStackTrace();
		}
	}

	private void writeResultsToCSV() {
		StringBuilder sbResult = new StringBuilder();

		ArrayList<Integer> keys = new ArrayList<Integer>(hmCSV.keySet());
		for (int i = keys.size() - 1; i >= 0; i--) {
			String v = (String) hmCSV.get(keys.get(i));
			System.out.println(v);
			sbResult.append(v);

		}

		try (PrintWriter writer = new PrintWriter(new File("Result.csv"))) {

			writer.write(sbResult.toString());

			System.out.println("Wrote the file");

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

}
