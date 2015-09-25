/**
 * 
 */
package com.democrawler;

import java.util.Set;

/**
 * @author Bhasker
 *
 */
public class Application {

	public static void main(String[] args) {
		IWebCrawler crawler = new SingleDomainWebCrawler();
		Set<String> links = crawler.crawl("http://www.testweb.com");
		System.out.println("Links Identified : ");
		for(String link : links){
			System.out.println(link);
		}
		
	}
}
