package models;

import java.util.List;

/**
 * @author sinthu
 *
 */
public class Livre {
	public String idLivre;
	public String titre;
	public String auteur;
	public List<Chapitre> chapitre;
	
	public Livre(String idLivre, String titre, String auteur, List<Chapitre> chapitre) {
		super();
		this.idLivre = idLivre;
		this.titre = titre;
		this.auteur = auteur;
		this.chapitre = chapitre;
	}
	
	

}
