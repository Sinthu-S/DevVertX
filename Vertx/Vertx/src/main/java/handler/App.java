package handler;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class App extends AbstractVerticle {

	private MongoClient dataBase;

	@Override
	public void start() throws Exception {

		JsonObject config = new JsonObject();
		config.put("connection_string", "mongodb://<dbuser>:<dbpassword>@ds044679.mlab.com:44679/vertx");

		dataBase = MongoClient.createNonShared(vertx, config);

		System.out.println("ferfeg");

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		
		
		//voir toute la bibliothèque
		router.get("/bibliotheque/list").handler(this::handleGetList);

		//voir un livre donné
		router.get("/bibiliotheque/livres/:IDlivre ").handler(this::handleGetLivre);

		//voir un journal donné
		router.get("/bibiliotheque/journaux/:IDJournal").handler(this::handleGetJournal);

		//voir directement une page d'un livre
		router.get("/bibiliotheque/livres/:IDlivre/:numPage").handler(this::handleGetPageLivre);

		//voir directement une page d'un journal
		router.get("/bibiliotheque/journaux/:IDJournal/:numPage").handler(this::handleGetPageJournal);

		//voir tous les articles d'une page de journal
		router.get("/bibiliotheque/journaux/:IDJournal/:numPage/articles").handler(this::handleGetJournalPageArticles);

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}

	private void handleGetList(RoutingContext routingContext){
		this.dataBase.find("", new JsonObject(), res -> {

			if (res.succeeded()) {
				routingContext.response()
				.putHeader("content-type", "application/json")
				.end(res.toString());

			} else {

				routingContext.response()
				.setStatusCode(400).end("ereur");
			}

		});
	}

	private void handleGetLivre(RoutingContext routingContext){

	}

	private void handleGetJournal(RoutingContext routingContext){

	}

	private void handleGetPageLivre(RoutingContext routingContext){

	}

	private void handleGetPageJournal(RoutingContext routingContext){

	}

	private void handleGetJournalPageArticles(RoutingContext routingContext){

	}
	
	
}
