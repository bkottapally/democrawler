/**
 * 
 */
package com.democrawler;

import java.util.Set;

/**
 * @author Bhasker
 *
 */
public interface IWebCrawler {

	public abstract Set<String> crawl(String url);

}