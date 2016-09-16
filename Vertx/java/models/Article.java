package models;


/**
 * @author sinthu
 *
 */
public class Article extends Text {
	public String nomArticle;

	public Article(StringBuffer text, String nomArticle) {
		super(text);
		this.nomArticle = nomArticle;
	}

	

}
