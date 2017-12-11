import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.management.OperatingSystemMXBean;

public class Main {
	
	public static String SSE_URL;
	public final static String GAME = "TESTGAME";
	public final static String GAME_DISPLAY = "Test Game";
	public final static String GAME_COLOR_ID = "6";

	public static void main(String[] args) throws Exception {
		SSE_URL = SSE.getSSEURL();
		SSE.init(GAME, GAME_DISPLAY, GAME_COLOR_ID, SSE_URL);
    	try {
    		SSE.registerMeta();
    		
    		JSONObject cpuGradient = new JSONObject()
    				.put("gradient", new JSONObject()
    				.put("zero", new JSONObject()
    						.put("red", 0)
    						.put("green", 255)
    						.put("blue", 0))
    				.put("hundred", new JSONObject()
    						.put("red", 255)
    						.put("green", 0)
    						.put("blue", 0)));
    		
    		JSONObject memoryGradient = new JSONObject()
    				.put("gradient", new JSONObject()
    				.put("zero", new JSONObject()
    						.put("red", 0)
    						.put("green", 255)
    						.put("blue", 0))
    				.put("hundred", new JSONObject()
    						.put("red", 255)
    						.put("green", 0)
    						.put("blue", 0)));
    		
    		JSONObject batteryGradient = new JSONObject()
    				.put("gradient", new JSONObject()
    				.put("zero", new JSONObject()
    						.put("red", 255)
    						.put("green", 0)
    						.put("blue", 0))
    				.put("hundred", new JSONObject()
    						.put("red", 0)
    						.put("green", 255)
    						.put("blue", 0)));

    		
    		JSONObject colorEvent1 = new JSONObject();
    		colorEvent1.put("device-type", "keyboard");
    		colorEvent1.put("zone", "one");
    		colorEvent1.put("mode", "color");
    		colorEvent1.put("color", cpuGradient);
    		SSE.registerEvent("CPUGRADIENT", colorEvent1);
    		
    		JSONObject colorEvent2 = new JSONObject();
    		colorEvent2.put("device-type", "keyboard");
    		colorEvent2.put("zone", "two");
    		colorEvent2.put("mode", "color");
    		colorEvent2.put("color", memoryGradient);
    		SSE.registerEvent("MEMORYGRADIENT", colorEvent2);
    		
    		JSONObject colorEvent3 = new JSONObject();
    		colorEvent3.put("device-type", "keyboard");
    		colorEvent3.put("zone", "three");
    		colorEvent3.put("mode", "color");
    		colorEvent3.put("color", batteryGradient);
    		SSE.registerEvent("BATTERYGRADIENT", colorEvent3);
    		
    		com.sun.management.OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    		Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
    		
    		while (true) {
    			int cpuPercent = (int) (osBean.getSystemCpuLoad() * 100);
    			SSE.sendEvent("CPUGRADIENT", cpuPercent);
    			
    			int memoryPercent = (int) (100 - ((float)osBean.getFreePhysicalMemorySize() / osBean.getTotalPhysicalMemorySize() * 100));
    			SSE.sendEvent("MEMORYGRADIENT", memoryPercent);
    			
    			Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
    			SSE.sendEvent("BATTERYGRADIENT", (int) batteryStatus.BatteryLifePercent);
    			
    			try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    		
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
public interface Kernel32 extends StdCallLibrary {
		
		public Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class);

	    /**
	     * @see http://msdn2.microsoft.com/en-us/library/aa373232.aspx
	     */
	    public class SYSTEM_POWER_STATUS extends Structure {
	        public byte ACLineStatus;
	        public byte BatteryFlag;
	        public byte BatteryLifePercent;
	        public byte Reserved1;
	        public int BatteryLifeTime;
	        public int BatteryFullLifeTime;

	        @Override
	        protected List<String> getFieldOrder() {
	            ArrayList<String> fields = new ArrayList<String>();
	            fields.add("ACLineStatus");
	            fields.add("BatteryFlag");
	            fields.add("BatteryLifePercent");
	            fields.add("Reserved1");
	            fields.add("BatteryLifeTime");
	            fields.add("BatteryFullLifeTime");
	            return fields;
	        }

	        /**
	         * The AC power status
	         */
	        public String getACLineStatusString() {
	            switch (ACLineStatus) {
	                case (0): return "Offline";
	                case (1): return "Online";
	                default: return "Unknown";
	            }
	        }

	        /**
	         * The battery charge status
	         */
	        public String getBatteryFlagString() {
	            switch (BatteryFlag) {
	                case (1): return "High, more than 66 percent";
	                case (2): return "Low, less than 33 percent";
	                case (4): return "Critical, less than five percent";
	                case (8): return "Charging";
	                case ((byte) 128): return "No system battery";
	                default: return "Unknown";
	            }
	        }

	        /**
	         * The percentage of full battery charge remaining
	         */
	        public String getBatteryLifePercent() {
	            return (BatteryLifePercent == (byte) 255) ? "Unknown" : BatteryLifePercent + "%";
	        }

	        /**
	         * The number of seconds of battery life remaining
	         */
	        public String getBatteryLifeTime() {
	            return (BatteryLifeTime == -1) ? "Unknown" : BatteryLifeTime + " seconds";
	        }

	        /**
	         * The number of seconds of battery life when at full charge
	         */
	        public String getBatteryFullLifeTime() {
	            return (BatteryFullLifeTime == -1) ? "Unknown" : BatteryFullLifeTime + " seconds";
	        }

	        @Override
	        public String toString() {
	            StringBuilder sb = new StringBuilder();
	            sb.append("ACLineStatus: " + getACLineStatusString() + "\n");
	            sb.append("Battery Flag: " + getBatteryFlagString() + "\n");
	            sb.append("Battery Life: " + getBatteryLifePercent() + "\n");
	            sb.append("Battery Left: " + getBatteryLifeTime() + "\n");
	            sb.append("Battery Full: " + getBatteryFullLifeTime() + "\n");
	            return sb.toString();
	        }
	    }

	    /**
	     * Fill the structure.
	     */
	    public int GetSystemPowerStatus(SYSTEM_POWER_STATUS result);
	}
}
