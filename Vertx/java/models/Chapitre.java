package models;

import java.util.List;

/**
 * @author sinthu
 *
 */
public class Chapitre extends ChapitreAbstrait{
	public String titre;

	public Chapitre(List<PageLivre> pages, String titre) {
		super(pages);
		this.titre = titre;
	}
	
	
}
