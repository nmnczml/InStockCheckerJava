
public class StartMe {

	public static void main(String[] args) {

		ScrapeEngine test = new ScrapeEngine();
		
		try {

			test.logging("Started...");
			test.setUp();

			try {

				test.MainLoop();

			} catch (Exception ex) {

			}

			test.driver.quit();
			
			System.out.print("Press any key to close...");
			System.in.read();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
