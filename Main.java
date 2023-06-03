import java.util.*;
import java.io.File;

public class Main {
	
	public static void main(String[] args) {
        
        try {
            RPGTool r = new RPGTool("d41d8cd98f00b204e9800998ecf8427e", true);
            RPGTool.FileList rf = r.getFilelist();
            long time1 = System.currentTimeMillis();
            rf.addFile(new File("/sdcard/RPG/pictures/Hukubiki_1.rpgmvp"));
            System.out.println(rf.getDecrypt().deFile());
            rf.setFile(0, new File("/sdcard/RPG/pictures/Hukubiki_1.png"));
            System.out.println(rf.getEncrypt().enFile());
            long time2 = System.currentTimeMillis();
            System.out.println((time2 - time1) / 1000.0 + "s");
        } catch(Exception e) {
            e.printStackTrace();
        }
		
    }
    
}
