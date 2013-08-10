package me.jacklin213.lingift.utils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import me.jacklin213.lingift.LinGift;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateChecker {

	public LinGift plugin;

	private String version;
	private String link;
	private URL filesFeed;
	public Float pluginVersion;
	public Float latestVersion;

	public UpdateChecker(LinGift instance, String url) {
		plugin = instance;
		try {
			this.filesFeed = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public boolean updateNeeded() {
		try {
			InputStream input = this.filesFeed.openConnection()
					.getInputStream();
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(input);

			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();

			this.version = children.item(1).getTextContent()
					.replaceAll("[a-zA-Z ]", "");
			this.link = children.item(3).getTextContent();

			this.pluginVersion = Float.parseFloat(plugin.getDescription()
					.getVersion());
			this.latestVersion = Float.parseFloat(this.version);
			if ((plugin.getDescription().getVersion().equals(this.version))
					|| (this.pluginVersion > this.latestVersion)) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getVersion() {
		return this.version;
	}

	public String getLink() {
		return this.link;
	}

}
