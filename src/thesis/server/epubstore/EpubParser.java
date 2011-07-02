package thesis.server.epubstore;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import thesis.server.epublib.domain.Author;
import thesis.server.epublib.domain.Metadata;

public class EpubParser {

	private final static String pedContainerFilePath = "META-INF/container.xml";

	private final static String ROOTFILES = "rootfiles";
	private final static String ROOTFILE = "rootfile";
	private final static String FULL_PATH = "full-path";
	private final static String METADATA = "metadata";
	private final static String AUTHOR = "author";
	private final static String TITLE = "title";
	private final static String SUBJECT = "subject";
	private final static String COVER = "cover";

	public EpubParser() {
	}

	public EpubInfo parse(String pedPath) {
		String author = "";
		String title = "";
		String subject = "";
		String cover = "";

		String descriptorFilePath = getPedDescriptorPath(pedPath);

		try {

			File file = new File(pedPath + descriptorFilePath);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName(METADATA);

			if (nodeLst.getLength() > 0) {

				Node fstNode = nodeLst.item(0);

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element fstElmnt = (Element) fstNode;

					NodeList authorElemLst = fstElmnt
							.getElementsByTagName(AUTHOR);
					Element authorElem = (Element) authorElemLst.item(0);
					NodeList authorNode = authorElem.getChildNodes();
					author = authorNode.item(0).getNodeValue();

					NodeList titleElemLst = fstElmnt
							.getElementsByTagName(TITLE);
					Element titleElem = (Element) titleElemLst.item(0);
					NodeList titleNode = titleElem.getChildNodes();
					title = titleNode.item(0).getNodeValue();

					NodeList subjectElemLst = fstElmnt
							.getElementsByTagName(SUBJECT);
					Element subjectElem = (Element) subjectElemLst.item(0);
					NodeList subjectNode = subjectElem.getChildNodes();
					subject = subjectNode.item(0).getNodeValue();

					NodeList coverElemlst = fstElmnt
							.getElementsByTagName(COVER);
					Element coverElem = (Element) coverElemlst.item(0);
					NodeList coverNode = coverElem.getChildNodes();
					cover = pedPath + coverNode.item(0).getNodeValue();//TODO: use System.getProperty("user.dir") and the specific image folder

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Metadata meta = new Metadata();
		meta.addTitle(title);
		meta.addAuthor(new Author(author));
		ArrayList<String> list = new ArrayList<String>();
		list.add(subject);
		meta.setSubjects(list);
		EpubInfo ped = new EpubInfo(meta);
		ped.setCoverUrl(cover);
		return ped;
	}

	private String getPedDescriptorPath(String pedPath) {
		String defaultResult ="DOC/document.xml";
		String result = defaultResult;

		try {

			File file = new File(pedPath + pedContainerFilePath);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName(ROOTFILES);

			if (nodeLst.getLength() > 0) {

				Node fstNode = nodeLst.item(0);

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element fstElmnt = (Element) fstNode;
					NodeList fstNmElmntLst = fstElmnt
							.getElementsByTagName(ROOTFILE);
					Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
					NodeList fstNm = fstNmElmnt.getChildNodes();
					NamedNodeMap attributes = fstNm.item(0).getAttributes();
					Node fullPath = attributes.getNamedItem(FULL_PATH);
					result = fullPath.getNodeValue();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

}
