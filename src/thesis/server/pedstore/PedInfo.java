package thesis.server.pedstore;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "pedInfo")
@XmlType(propOrder = { "id", "name", "author", "subject", "coverPath" })
public class PedInfo {

	private String id;
	private String name;
	private String subject;
	private String author;
	private String coverPath;

	public PedInfo(){
		this(null,null,null);
	}
	
	public PedInfo(String id, String name, String imgPath) {
		this(id, name, imgPath, null, null);
	}

	public PedInfo(String id, String name, String subject, String author,
			String imgPath) {
		super();
		this.id = id;
		this.name = name;
		this.subject = subject;
		this.author = author;
		this.coverPath = imgPath;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCoverPath() {
		return coverPath;
	}

	public void setCoverPath(String imgPath) {
		this.coverPath = imgPath;
	}
}
