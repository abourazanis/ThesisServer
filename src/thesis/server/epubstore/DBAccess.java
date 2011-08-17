package thesis.server.epubstore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import thesis.server.epublib.domain.Author;
import thesis.server.epublib.domain.Identifier;
import thesis.server.epublib.domain.Metadata;

public class DBAccess {

	private String url = "jdbc:mysql://localhost:3306/";
	private String dbName = "epubstore";
	private String driver = "com.mysql.jdbc.Driver";
	private String userName = "epubstoreUser";
	private String password = "123abc123!!";

	private Connection connection = null;

	private void connect() throws SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		// This will load the MySQL driver, each DB has its own driver
		Class.forName(driver);

		// Setup the connection with the DB
		connection = DriverManager.getConnection(url + dbName, userName,
				password);
	}

	private void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		closeConnection();
		super.finalize();
	}

	private Connection getConnection() {
		if (connection == null) {
			try {
				connect();
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
		return connection;
	}

	public ResultSet executeQuery(String query) throws SQLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		connect();
		ResultSet res;
		Statement stat = getConnection().createStatement();
		res = stat.executeQuery(query);

		return res;
	}

	public int executeInsertUpdateQuery(String query) throws SQLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		int res;
		Statement stat = getConnection().createStatement();
		res = stat.executeUpdate(query);

		return res;
	}

	/*
	 * db specific queries
	 */

	public boolean insertNewRequest(String uniqID, int methodID, int epubID)
			throws SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		int res = 0;
		try {
			PreparedStatement prest = getConnection()
					.prepareStatement(
							"INSERT INTO epubstore.requests (uniqueIdentifier, securityMethods_Id, epubItems_epubItemID) VALUES (?, ?, ?)");
			prest.setString(1, uniqID);
			prest.setInt(2, methodID);
			prest.setInt(3, epubID);

			res = prest.executeUpdate();
		} catch (SQLException e) {
			res = 0;
		}
		return res > 0;
	}

	public boolean updateRequest(boolean isSuccessfull, int requestID)
			throws SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		int res = 0;
		try {
			Date date = new Date();
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			PreparedStatement prest = getConnection()
					.prepareStatement(
							"UPDATE epubstore.requests set isSuccessfull = ? and completionDate = ? where Id = ?");
			prest.setBoolean(1, isSuccessfull);
			prest.setDate(2, sqlDate);
			prest.setInt(3, requestID);

			res = prest.executeUpdate();
		} catch (SQLException e) {
			res = 0;
		}
		return res > 0;
	}

	public String getEncryptionName(int securityMethodID) throws SQLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		String name = null;

		try {
			PreparedStatement prest = getConnection().prepareStatement(
					"SELECT en.name as encName "
							+ "FROM epubstore.encryptions AS en "
							+ "INNER JOIN epubstore.securityMethods AS sec "
							+ "ON sec.encryptions_Id = en.Id "
							+ "WHERE sec.Id = ? ");
			prest.setInt(1, securityMethodID);

			ResultSet set = prest.executeQuery();
			name = set.getString("encName");
		} catch (SQLException e) {
			name = null;
		}

		return name;
	}

	public String getDecryptionSourceCode(int securityMethodID)
			throws SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		String src = null;

		try {
			PreparedStatement prest = getConnection().prepareStatement(
					"SELECT d.sourceCode as src "
							+ "FROM epubstore.decryptions AS d "
							+ "INNER JOIN epubstore.securityMethods AS sec "
							+ "ON sec.encryptions_Id = d.Id "
							+ " WHERE sec.Id = ?");
			prest.setInt(1, securityMethodID);

			ResultSet set = prest.executeQuery();
			if (set.next())
				src = set.getString("src");
		} catch (SQLException e) {
			src = null;
		}
		return src;
	}

	public String getPluginName(int epubId) throws SQLException {
		String src = null;

		try {
			PreparedStatement prest = getConnection()
					.prepareStatement(
							"select pluginName from securityPlugins "
									+ "inner join epubItems on epubItems.securityPlugins_Id = securityPlugins.Id "
									+ "where epubItems.epubItemID = ?");
			prest.setInt(1, epubId);

			ResultSet set = prest.executeQuery();
			if (set.next())
				src = set.getString("pluginName");
		} catch (SQLException e) {
			src = null;
		}
		return src;
	}

	public Metadata getMetadata(int epubID) throws SQLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		Metadata meta = new Metadata();

		try {
			PreparedStatement epubMetaStat = getConnection()
					.prepareStatement(
							"SELECT epub.titles AS titles, epub.dates AS dates, epub.subjects AS subj, epub.types AS types, "
									+ "epub.descriptions AS descr, epub.language AS lang, epub.rights AS rights "
									+ "FROM epubstore.epubItems as epub "
									+ "WHERE epub.epubItemId = ?");
			epubMetaStat.setObject(1, epubID);

			ResultSet set = epubMetaStat.executeQuery();
			if (set.next()) {
				String titles = set.getString("titles");
				String dates = set.getString("dates");
				String subjects = set.getString("subj");
				String types = set.getString("types");
				String descr = set.getString("descr");
				String lang = set.getString("lang");
				String rights = set.getString("rights");

				// set titles
				if (titles.indexOf(",") != -1) {
					List<String> titleList = Arrays.asList(titles.split(","));
					meta.setTitles(titleList);
				} else {
					meta.addTitle(titles);
				}

				// TODO: has problem with single year value( ex. 1980)
				// set dates
				// if (dates.indexOf(",") != -1) {
				// String datesAr[] = dates.split(",");
				// for (String d : datesAr) {
				// meta.addDate(new thesis.server.epublib.domain.Date(
				// DateFormat.getInstance().parse(d)));
				// }
				// } else {
				// meta.addTitle(dates);
				// }

				// set subjects
				if (subjects.indexOf(",") != -1) {
					List<String> subList = Arrays.asList(subjects.split(","));
					meta.setSubjects(subList);

				} else {
					List<String> subList = new ArrayList<String>();
					subList.add(subjects);
					meta.setSubjects(subList);
				}

				// set types
				if (types.indexOf(",") != -1) {
					List<String> typeList = Arrays.asList(types.split(","));
					meta.setTitles(typeList);

				} else {
					meta.addType(types);
				}

				meta.addDescription(descr);

				// set rights
				if (rights.indexOf(",") != -1) {
					List<String> rList = Arrays.asList(rights.split(","));
					meta.setRights(rList);

				} else {
					List<String> subList = new ArrayList<String>();
					subList.add(rights);
					meta.setRights(subList);
				}

				meta.setLanguage(lang);
			}

			// get authors
			PreparedStatement epubAuthStat = getConnection()
					.prepareStatement(
							"SELECT authors.firstname AS firstname, authors.lastname AS lastname, rel.roleName AS rolename, rel.roleValue AS rolevalue"
									+ " FROM epubstore.Author as authors "
									+ "INNER JOIN epubstore.epubItems_Authors"
									+ " ON epubstore.epubItems_Authors.Author_authorID = authors.authorID "
									+ "INNER JOIN epubstore.Relator as rel "
									+ "ON authors.Relator_relatorID = rel.relatorID "
									+ "WHERE epubItems_Authors.epubItems_epubItemID = ?");
			epubAuthStat.setInt(1, epubID);

			ResultSet setAuthors = epubAuthStat.executeQuery();
			while (setAuthors.next()) {
				Author auth = new Author(setAuthors.getString("firstname"),
						setAuthors.getString("lastname"));
				auth.setRole(setAuthors.getString("rolename"));
				meta.addAuthor(auth);

			}

			// get contributors
			PreparedStatement epubConStat = getConnection()
					.prepareStatement(
							"SELECT authors.firstname AS firstname, authors.lastname  AS lastname "
									+ "FROM epubstore.Author as authors "
									+ "INNER JOIN epubstore.epubItems_Contributors "
									+ "ON epubstore.epubItems_Contributors.Author_authorID = authors.authorID "
									+ "WHERE epubItems_Contributors.epubItems_epubItemID = ?");
			epubConStat.setInt(1, epubID);

			ResultSet setContr = epubConStat.executeQuery();
			while (setContr.next()) {
				Author auth = new Author(setContr.getString("firstname"),
						setContr.getString("lastname"));
				meta.addContributor(auth);
			}

			// get identifiers
			PreparedStatement epubIdentStat = getConnection()
					.prepareStatement(
							"SELECT id.scheme AS scheme, idd.value AS value "
									+ "FROM epubstore.Identifier as id "
									+ "INNER JOIN epubstore.epubItems_Identifiers as idd "
									+ "ON idd.Identifier_identifierID = id.identifierID "
									+ "WHERE idd.epubItems_epubItemId = ?");
			epubIdentStat.setInt(1, epubID);

			ResultSet setIdent = epubIdentStat.executeQuery();
			while (setIdent.next()) {
				meta.addIdentifier(new Identifier(setIdent.getString("scheme"),
						setIdent.getString("value")));
			}

			// get publishers
			PreparedStatement epubPubStat = getConnection()
					.prepareStatement(
							"SELECT  pub.name AS name  "
									+ "FROM epubstore.Publisher as pub "
									+ "INNER JOIN epubstore.epubItems_Publishers as pubb "
									+ "ON pub.publisherID = pubb.Publisher_publisherID "
									+ "WHERE pubb.epubItems_epubItemId = ?");
			epubPubStat.setInt(1, epubID);

			ResultSet setPub = epubPubStat.executeQuery();
			while (setPub.next()) {
				meta.addPublisher(setPub.getString("name"));
			}

		} catch (SQLException e) {
			meta = null;
		}

		return meta;
	}

	public List<EpubInfo> getEpubList() throws SQLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		ArrayList<EpubInfo> list = new ArrayList<EpubInfo>();

		PreparedStatement epubPubStat;
		try {
			epubPubStat = getConnection().prepareStatement(
					"SELECT  epubs.epubItemID AS ID,coverurl  "
							+ "FROM epubstore.epubItems as epubs ");

			ResultSet setPub = epubPubStat.executeQuery();
			while (setPub.next()) {
				int id = setPub.getInt("ID");
				String idStr = String.valueOf(id);
				Metadata meta = getMetadata(id);
				EpubInfo epub = new EpubInfo();
				epub.setMeta(meta);
				epub.setId(idStr);
				epub.setCoverUrl(setPub.getString("coverurl"));
				list.add(epub);
			}
		} catch (SQLException e) {
			list = null;
		}

		return list;
	}

	public String getEpubLocation(int epubID) {
		String location = null;

		try {
			PreparedStatement prest = getConnection().prepareStatement(
					"SELECT epub.location AS location "
							+ "FROM epubstore.epubItems AS epub "
							+ "WHERE epub.epubItemId = ? ");
			prest.setInt(1, epubID);

			ResultSet set = prest.executeQuery();
			if (set.next())
				location = set.getString("location");
		} catch (SQLException e) {
			location = null;
		}

		return location;
	}

}
