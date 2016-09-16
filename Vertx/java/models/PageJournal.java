package models;

import java.util.List;

/**
 * @author sinthu
 *
 */
public class PageJournal extends Page {
	public List<Text> contenu;

	public PageJournal(int numPage, List<Text> contenu) {
		super(numPage);
		this.contenu = contenu;
	}

	

	
	
}
