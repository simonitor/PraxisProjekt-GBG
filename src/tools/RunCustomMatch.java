package tools;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import games.Othello.StateObserverOthello;
import ludiiInterface.gateway.GbgAsLudiiAgent;
import ludiiInterface.othello.useCases.state.AsStateObserverOthello;
import ludiiInterface.othello.useCases.state.GbgStateFromLudiiContext;
import random.RandomAI;
import search.mcts.MCTS;
import util.AI;
import util.Context;
import util.GameLoader;
import util.Trial;
import util.model.Model;
import utils.LudiiAI;

/**
 * An example of a custom implementation of a match between different AIs,
 * i.e. not using the built-in EvalGamesSet functionality of Ludii.
 * 
 * By creating custom implementations of experiments, we can more
 * easily add our own custom stats to track, and request moves from AIs
 * that do not implement Ludii's abstract AI class. The downside is that
 * more boilerplate code must be written.
 * 
 * See RunLudiiMatch for an example that uses Ludii's built-in EvalGamesSet 
 * implementation.
 * 
 * Note that this example does not provide all of the functionality included
 * in the built-in EvalGamesSet implementation. For instance, this example will always
 * use the same agents for the same player number (e.g. Random AI always player
 * 1, UCT always player 2), whereas Ludii's built-in EvalGamesSet implementation can
 * rotate through assignments of agents to player numbers.
 * 
 * @author Dennis Soemers
 */
public class RunCustomMatch
{
	
	//-------------------------------------------------------------------------
	
	/** Name of game we wish to play */
//	static final String GAME_NAME = "Amazons.lud";
	static final String GAME_NAME = "Reversi.lud";
	
	/** Number of games to play */
	static final int NUM_GAMES = 10; //2;

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	private RunCustomMatch()
	{
		// do not instantiate
	}

	//-------------------------------------------------------------------------

	public static void main(final String[] args)
	{
		// load and create game
		final Game game = GameLoader.loadGameFromName(GAME_NAME);
//		game.create();		// /WK/ at least for Ludii-1.0.2 it is wrong to call it 

		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final List<AI> ais = new ArrayList<AI>();
		ais.add(null);
//		ais.add(new RandomAI());
//		ais.add(MCTS.createUCT());	// Note: built-in Ludii UCT! Not Example UCT.
		ais.add(new LudiiAI());
//		ais.add(new RandomAI());
		ais.add(new GbgAsLudiiAgent());
		
		// in this example, we're still using agents that extend Ludii's
		// abstract AI class, and therefore we call initAI() and 
		// selectAction() on them
		//
		// note that it is also possible to use different kinds of
		// agents which do not extend this class, and call whatever methods
		// you like on them
		for (int p = 1; p < ais.size(); ++p)
		{
			ais.get(p).initAI(game, p);
		}
		
		for (int gameCounter = 0; gameCounter < NUM_GAMES; ++gameCounter)
		{
			// play a game
			game.start(context);
			System.out.print(ais.get(1).friendlyName + " vs. " + ais.get(2).friendlyName + " ... ");
			
			final Model model = context.model();
			
			while (!context.trial().over())
			{
				model.startNewStep(context, ais, 1.0);
			}
			String pieces2 = "";
			if (ais.get(2) instanceof GbgAsLudiiAgent) {
				StateObserverOthello so = new AsStateObserverOthello(new GbgStateFromLudiiContext(context));
	            so.setPieceCounters();
				pieces2 = " (2-stones = "+so.getCountWhite()+"/64)";
			}
			System.out.println("Outcome = " + context.trial().status() + pieces2);
		}
		System.out.println("RunCustomMatch finished.");
		System.exit(0);
	}

	//-------------------------------------------------------------------------

}
