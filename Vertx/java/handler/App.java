package handler;


import java.util.ArrayList;
import java.util.List;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import models.*;

/**
 * @author sinthu
 *
 */
public class App extends AbstractVerticle {

	private MongoClient dataBase;
	JsonArray tab = new JsonArray();

	@Override
	public void start() throws Exception {

		JsonObject config = new JsonObject();

		//Sandbox sur mLab
		config.put("connection_string", "mongodb://Vertx:Vertx@ds044679.mlab.com:44679/vertx");

		dataBase = MongoClient.createNonShared(vertx, config);

		//peuplement 
		//data();

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.route().handler(CookieHandler.create());

		//message de bienvenu
		router.get("/").handler(this::handleBienvenu);

		//voir toute la bibliothèque
		router.get("/bibiliotheque/list").handler(this::handleGetList);

		//voir un livre donné
		router.get("/bibiliotheque/livres/:IDlivre").handler(this::handleGetLivre);

		//voir un journal donné
		router.get("/bibiliotheque/journaux/:IDJournal").handler(this::handleGetJournal);

		//voir directement une page d'un livre
		router.get("/bibiliotheque/livres/:IDlivre/:numPage").handler(this::handleGetPageLivre);

		//voir directement une page d'un journal
		router.get("/bibiliotheque/journaux/:IDJournal/:numPage").handler(this::handleGetPageJournal);

		//voir tous les articles d'une page de journal
		router.get("/bibiliotheque/journaux/:IDJournal/:numPage/articles").handler(this::handleGetJournalPageArticles);
		
		//engistrement marque page
		router.get("/bibiliotheque/*/*/*/marquepage").handler(this::handleMarquePage);
		
		//recuperation marque page
		router.get("/marquepage").handler(this::handleSupMarquePage);

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}

	//----------------message-d'aceuil----------------------------------//
	private void handleBienvenu(RoutingContext routingContext){
		routingContext.response()
		.putHeader("content-type", "Vertx")
		.end("Bonjour \n Test Dev Vertx");

	}

	/*----------------voir toute la bibliothéque----------------------------------*/
	/*Récupération des livres et journaux et envoie au client*/
	private void handleGetList(RoutingContext routingContext){
		this.dataBase.find("Livres", new JsonObject(), res -> {
			if (res.succeeded()) {
				for (JsonObject json : res.result()) {
					tab.add(json);
				}
			} else {
				routingContext.response()
				.setStatusCode(400).end("Error");
			}
		});
		this.dataBase.find("Journaux", new JsonObject(), res -> {
			if (res.succeeded()) {
				for (JsonObject json : res.result()) {
					tab.add(json);
				}
			} else {
				routingContext.response()
				.setStatusCode(400).end("Error");
			}
		});
		routingContext.response()
		.putHeader("content-type", "Vertx")
		.end(tab.encodePrettily());
		tab.clear();

	}

	/*----------------voir un livre ou un journal----------------------------------*/
	

	private void handleGetLivre(RoutingContext routingContext){
		String livreId = routingContext.request().getParam("IDlivre");
		JsonObject query = new JsonObject().put("idLivre", livreId);
		this.getElement("Livres", query, routingContext);
	}

	private void handleGetJournal(RoutingContext routingContext){
		String journalId = routingContext.request().getParam("IDJournal");
		JsonObject query = new JsonObject().put("idJournal", journalId);
		this.getElement("Journaux", query, routingContext);
	}

	private void getElement(String collection, JsonObject query, RoutingContext routingContext){
		this.dataBase.find(collection, query, res -> {
			if (res.succeeded()) {
				//la boucle for pour tester si un livre est présent
				for (JsonObject json : res.result()) {
					tab.add(json);
				}
				routingContext.response()
				.putHeader("content-type", "Vertx")
				.end(tab.encodePrettily());
				tab.clear();
			} else {
				routingContext.response()
				.setStatusCode(404).end("Non trouvé");
			}
		});
	}

	/*----------------recupération d'une page d'un livre----------------------------------*/
	private void handleGetPageLivre(RoutingContext routingContext){
		String livreId = routingContext.request().getParam("IDlivre");
		String pageNum = routingContext.request().getParam("numPage");
		JsonObject query = new JsonObject().put("idLivre", livreId);
		int num;
		try {
			num = Integer.parseInt(pageNum);
		} catch (Exception e) {
			routingContext.response()
			.setStatusCode(404).end("Page Non trouvé");
			return;
		}
		this.dataBase.find("Livres", query, res -> {
			if (res.succeeded()) {
				JsonArray tmp = new JsonArray(), tmpp = new JsonArray();
				//récupération des chapitres du livre
				for(JsonObject json : res.result()) {
					tmp.addAll(json.getJsonArray("chapitre"));
				}
				JsonObject obj;
				//récupération des pages
				for (int i=0; i< tmp.size(); i++) {
					obj = tmp.getJsonObject(i);
					tmpp.addAll(obj.getJsonArray("pages"));
				}
				//récupération de la page demandée
				if(num <= tmpp.size()){
					obj = tmpp.getJsonObject(num-1);
					routingContext.response()
					.putHeader("content-type", "Vertx")
					.end(obj.encodePrettily());
					tab.clear();
				}else{
					routingContext.response()
					.setStatusCode(404).end("Page Non trouvé");
				}

			} else {
				routingContext.response()
				.setStatusCode(404).end("Non trouvé");
			}
		});

	}

	/*----------------récupération d'une page d'un journal----------------------------------*/
	private void handleGetPageJournal(RoutingContext routingContext){
		String journalId = routingContext.request().getParam("IDJournal");
		String pageNum = routingContext.request().getParam("numPage");
		JsonObject query = new JsonObject().put("idJournal", journalId);
		int num;
		try {
			num = Integer.parseInt(pageNum);
		} catch (Exception e) {
			routingContext.response()
			.setStatusCode(404).end("Page Non trouvé");
			return;
		}


		this.dataBase.find("Journaux", query, res -> {
			if (res.succeeded()) {
				JsonArray tmp = new JsonArray(), tmpp = new JsonArray();
				//récupération des pages
				for(JsonObject json : res.result()) {
					tmp.addAll(json.getJsonArray("pages"));
				}
				JsonObject obj;
				//récupération de la page demandée
				if(num <= tmp.size()){
					obj = tmp.getJsonObject(num-1);
					routingContext.response()
					.putHeader("content-type", "Vertx")
					.end(obj.encodePrettily());
					tab.clear();
				}else{
					routingContext.response()
					.setStatusCode(404).end("Page Non trouvé");
				}

			} else {
				routingContext.response()
				.setStatusCode(404).end("Non trouvé");
			}
		});


	}

	/*----------------récupération des article d'une page d'un journal----------------------------------*/
	private void handleGetJournalPageArticles(RoutingContext routingContext){
		String journalId = routingContext.request().getParam("IDJournal");
		String pageNum = routingContext.request().getParam("numPage");
		JsonObject query = new JsonObject().put("idJournal", journalId);
		int num;

		try {
			num = Integer.parseInt(pageNum);
		} catch (Exception e) {
			routingContext.response()
			.setStatusCode(404).end("Page Non trouvé");
			return;
		}


		this.dataBase.find("Journaux", query, res -> {
			if (res.succeeded()) {
				JsonArray tmp = new JsonArray(), tmpp = new JsonArray();
				JsonObject obj;
				//récupération des pages
				for(JsonObject json : res.result()) {
					tmp.addAll(json.getJsonArray("pages"));
				}
				//récupération des contenus
				if(num <= tmp.size()){
					obj = tmp.getJsonObject(num-1);
					System.out.println(obj);
					tmpp.addAll(obj.getJsonArray("contenu"));
				}else{
					routingContext.response()
					.setStatusCode(404).end("Page Non trouvé");
				}
				//récupération des articles
				tmp.clear();
				for(int i=0; i<tmpp.size(); i++){
					if(tmpp.getJsonObject(i).getString("nomArticle") != null){
						tmp.add(tmpp.getJsonObject(i));
					}
				}
				routingContext.response()
				.putHeader("content-type", "Vertx")
				.end(tmp.encodePrettily());
				tab.clear();


			} else {
				routingContext.response()
				.setStatusCode(404).end("Non trouvé");
			}
		});



	}
	
	/*---------------------------------------Gestion cookie----------------------*/
	private void handleMarquePage(RoutingContext routingContext){
		String str = routingContext.request().uri();
		int index=str.lastIndexOf('/');
		Cookie ckie =  Cookie.cookie("Marquepage", str);
		routingContext.addCookie(ckie);
	}
	
	private void handleSupMarquePage(RoutingContext routingContext){
		Cookie ckie = routingContext.getCookie("Marquepage");
		if(ckie == null){
			routingContext.response().setStatusCode(400).end("Pas de marque page");
		}else{
			routingContext.reroute(ckie.getValue());
		}
	}

	/*Peuplement de la base mongoDB*/
	public void data(){

		List list = new ArrayList();
		Text t = new Text(new StringBuffer("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. "));
		Page p = new PageLivre(1, t);
		Page p2 = new PageLivre(2, t);
		Page p3 = new PageLivre(3, t);
		Page p4 = new PageLivre(4, t);
		list.add(p);
		list.add(p2);
		list.add(p3);
		list.add(p4);
		ChapitreAbstrait c = new Chapitre(list, "chapitre1");
		List ls = new ArrayList();
		ls.add(c);

		Livre l = new Livre("L001", "TEST", "toto", ls);

		dataBase.save("Livres", ToJson.getJson(l), res -> {System.out.println("ok");});

		ls.clear();
		list.clear();

		t = new Text(new StringBuffer("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. "));
		p = new PageLivre(1, t);
		p2 = new PageLivre(2, t);
		p3 = new PageLivre(3, t);
		p4 = new PageLivre(4, t);
		list.add(p);
		list.add(p2);
		list.add(p3);
		list.add(p4);
		c = new ChapitreAbstrait(list);
		ls.add(c);

		l = new Livre("L002", "TEST2", "tata", ls);

		dataBase.save("Livres", ToJson.getJson(l), res -> {System.out.println("ok");});

		ls.clear();
		list.clear();

		t = new Text(new StringBuffer("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. "));
		p = new PageLivre(1, t);
		p2 = new PageLivre(2, t);
		p3 = new PageLivre(3, t);
		p4 = new PageLivre(4, t);
		list.add(p);
		list.add(p2);
		list.add(p3);
		list.add(p4);
		c = new ChapitreAbstrait(list);
		ls.add(c);

		l = new Livre("L003", "TEST3", "titi", ls);

		dataBase.save("Livres", ToJson.getJson(l), res -> {System.out.println("ok");});

		ls.clear();
		list.clear();

		t = new Text(new StringBuffer("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. "));
		StringBuffer sb = new StringBuffer("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. ");
		Article a = new Article(sb, "article1");
		Article a2 = new Article(sb, "article2");
		list.add(t);
		list.add(a);
		list.add(a2);
		p = new PageJournal(1, list);
		p2 = new PageJournal(2, list);
		p3 = new PageJournal(3, list);
		p4 = new PageJournal(4, list);

		ls.add(p);
		ls.add(p2);
		ls.add(p3);
		ls.add(p4);

		Journal j = new Journal("J001", ls);

		dataBase.save("Journaux", ToJson.getJson(j), res -> {System.out.println("ok");});

		ls.clear();
		list.clear();

		t = new Text(new StringBuffer("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. "));

		list.add(t);
		list.add(t);
		list.add(t);
		p = new PageJournal(1, list);
		p2 = new PageJournal(2, list);
		p3 = new PageJournal(3, list);
		p4 = new PageJournal(4, list);

		ls.add(p);
		ls.add(p2);
		ls.add(p3);
		ls.add(p4);

		j = new Journal("J002", ls);

		dataBase.save("Journaux", ToJson.getJson(j), res -> {System.out.println("ok");});

	}



}
