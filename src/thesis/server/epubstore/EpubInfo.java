package thesis.server.epubstore;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import thesis.server.epublib.domain.Metadata;

@XmlRootElement(name = "epubInfo")
@XmlType(propOrder = { "id", "meta", "coverUrl" })
public class EpubInfo {
	private Metadata meta;
	private String id;
	private String coverUrl;

	public EpubInfo() {
		this(null, null);
	}
	
	public EpubInfo(Metadata meta){
		this(meta,null);
	}

	public EpubInfo(Metadata meta,String id) {
		this.meta = meta;
		this.id = id;
	}

	public Metadata getMeta() {
		return meta;
	}

	public void setMeta(Metadata meta) {
		this.meta = meta;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}
}
