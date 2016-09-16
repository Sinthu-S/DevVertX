package models;

/**
 * @author sinthu
 *
 */
public class PageLivre extends Page{
	
	public Text textLivre;

	public PageLivre(int numPage, Text textLivre) {
		super(numPage);
		this.textLivre = textLivre;
	}

	

}
