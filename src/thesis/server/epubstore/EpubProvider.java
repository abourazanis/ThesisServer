package thesis.server.epubstore;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public enum EpubProvider {
	instance;
//	private final static String epubFolder = "/home/tas0s/epubs";
//	private final String fileSeparator = System.getProperty("file.separator");
	private Map<String,EpubInfo> epubs;
	private DBAccess dao;
	

	private EpubProvider() {
		epubs = new HashMap<String,EpubInfo>();
		dao = new DBAccess();
		populateEpubs();
	}

	private void populateEpubs() {
		
		List<EpubInfo> epubList;
		try {
			epubList = dao.getEpubList();
			for (EpubInfo epub : epubList) {
				epubs.put(epub.getId(), epub);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public Map<String,EpubInfo> getEpubList(){
		return epubs;
	}

}
