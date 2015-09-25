/**
 * 
 */
package com.democrawler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * @author Bhasker
 * 
 * This class is a single threaded, single domain web crawler.
 * 
 */
public class SingleDomainWebCrawler implements IWebCrawler {

	private String baseURL;
	private List<String> links;
	private Set<String> processedLinks;

	public SingleDomainWebCrawler() {
		links = new ArrayList<String>(); // Links to be processed
		processedLinks = new LinkedHashSet<String>(); // Links already processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crawler.IWebCrawler#crawl(java.lang.String)
	 */
	@Override
	public Set<String> crawl(String url) {
		baseURL = url;
		links.add(baseURL);
		for (int i = 0; i < links.size(); i++) {
			String link = links.get(i);
			if (!processedLinks.contains(link)) {
				parseSinglePage(link);
				processedLinks.add(link);
			}
		}

		return processedLinks;
	}

	protected void parseSinglePage(final String rootLink) {
		try {
			String html = getPageContent(rootLink);	
			if(html == null || html.isEmpty()){ //The page is either empty or not an HTML page.
				processedLinks.add(rootLink);
				return;
			}
//			System.out.println("Processing : "+rootLink);
			Reader reader = new StringReader(html);
			HTMLEditorKit.Parser parser = new ParserDelegator();
			parser.parse(reader, new HTMLEditorKit.ParserCallback() {
				
				@Override
				public void handleStartTag(HTML.Tag t, MutableAttributeSet a,
						int pos) {
					if (t == HTML.Tag.A) { // Process Anchor tags only
						Object link = a.getAttribute(HTML.Attribute.HREF);
						if (link != null && !link.toString().trim().equals("#")) {
							String linkStr = String.valueOf(link);
							linkStr = getAbsoluteURL(linkStr); 
							if(linkStr.contains(baseURL)) { //is from the same domain
								if(processedLinks.contains(linkStr)){ // ignore if already processed
									return;
								}
								links.add(linkStr);
							} else {
								 // Just to keep track of the external links even if not processed
								processedLinks.add(String.valueOf(a.getAttribute(HTML.Attribute.HREF)));
							}
							
						}
					} 
				}

				@Override
				public void handleSimpleTag(Tag t, MutableAttributeSet a,
						int pos) {
					//Only to keep track of links. Not processed.
					String link = null;
					if (t == HTML.Tag.IMG) {
						link = String.valueOf(a.getAttribute(HTML.Attribute.SRC));
					} else if (t == HTML.Tag.LINK) {
						link = String.valueOf(a.getAttribute(HTML.Attribute.HREF));
					}
					if(link != null){
						link = getAbsoluteURL(link);
						processedLinks.add(link);
					}
				}

				/**
				 * @param link
				 * @return
				 */
				private String getAbsoluteURL(String link) {
					if (!link.startsWith("http") && !link.startsWith("https")) { // Is relative URL
						link = baseURL + "/" + link;
					}
					return link;
				}
			}, false);
			processedLinks.add(rootLink);
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}

	/**
	 * 
	 * @param rootLink
	 * @return
	 * @throws Exception
	 */
	protected String getPageContent(String rootLink) throws Exception {
		HttpURLConnection urlConnection = (HttpURLConnection) new URL(rootLink)
				.openConnection();
		if (isNonHTMLURL(urlConnection)) {
			return null;
		}
		StringBuilder webpage = new StringBuilder();
		String inputLine = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(
				(InputStream) urlConnection.getContent()));
		while ((inputLine = in.readLine()) != null)
			webpage.append(inputLine);
		in.close();
		return webpage.toString();
	}

	/**
	 * @param urlConnection
	 */
	protected boolean isNonHTMLURL(HttpURLConnection urlConnection) {
		String contentType = urlConnection.getHeaderField("Content-Type");
		if (contentType.startsWith("image/")) {
			return true;
		} else if (contentType.equals("application/x-shockwave-flash")) {
			return true;
		} // Can consider more types as required.
		return false;
	}

}
