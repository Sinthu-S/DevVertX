package models;

import java.util.List;

/**
 * @author sinthu
 *
 */
public class Journal {
	public String idJournal;
	public List<Text> pages;
	
	public Journal(String idJournal, List<Text> pages) {
		super();
		this.idJournal = idJournal;
		this.pages = pages;
	}
	
	
}
