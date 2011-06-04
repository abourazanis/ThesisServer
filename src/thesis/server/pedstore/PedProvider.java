package thesis.server.pedstore;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public enum PedProvider {
	instance;
	private final static String pedsFolder = "/home/tas0s/peds";
	private final String fileSeparator = System.getProperty("file.separator");
	private PedParser parser;
	private Map<String,PedInfo> peds;
	

	private PedProvider() {
		peds = new HashMap<String,PedInfo>();
		parser = new PedParser();
		populatePeds();
	}

	private void populatePeds() {
		File pedDir = new File(pedsFolder);
		if (pedDir.exists() && pedDir.canRead()) {
			String[] fileList = pedDir.list();
			for (String filename : fileList) {
				PedInfo info= parser.parse(pedsFolder + fileSeparator +  filename + fileSeparator);
				info.setId(filename);
				peds.put(filename,info);
			}
		}
	}
	
	public Map<String,PedInfo> getPedList(){
		return peds;
	}

}
