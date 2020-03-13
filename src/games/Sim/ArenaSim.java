package games.Sim;

import java.io.IOException;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.Hex.HexConfig;
import games.TicTacToe.ArenaTTT;
import games.TicTacToe.EvaluatorTTT;
import games.TicTacToe.FeatureTTT;
import games.TicTacToe.XNTupleFuncsTTT;


public class ArenaSim extends Arena{

	public ArenaSim(String title, boolean withUI) {
		super(title,withUI);		
	}
		
	@Override
	public String getGameName() {
		return "Sim";
	}

    /**
     * set the number of players for Sim
     */
    public static void setNumPlayers(int val) {
    	ConfigSim.NUM_PLAYERS = val;
    }

    /**
     * set the number of nodes for Sim
     */
    public static void setNumNodes(int val) {
    	ConfigSim.NUM_NODES = val;
    }

    /**
     * set the number of nodes for Sim
     */
    public static void setCoalition(String coalition) {
    	ConfigSim.COALITION = coalition;
    }

	@Override
	public GameBoard makeGameBoard() {
		gb = new GameBoardSim(this);	
		return gb;
	}

	@Override
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
		return new EvaluatorSim(pa,gb,stopEval,mode,verbose);
	}

	public Feature makeFeatureClass(int featmode) {
		return new FeatureSim(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
//		return new XNTupleFuncsTTT(); // /WK/ BUG!
//		return new XNTupleFuncsSim(15,3,2);		// /WK/ Bug: this is special to K_6 + 2 players. Generalize!!
		return new XNTupleFuncsSim(ConfigSim.NUM_NODES*(ConfigSim.NUM_NODES-1)/2,
				   ConfigSim.NUM_PLAYERS+1,
				   ConfigSim.NUM_PLAYERS);		
	}

	
	@Override
	public void performArenaDerivedTasks() {
	
	}

	public static void main(String[] args) throws IOException 
	{
		ArenaSim t_Frame = new ArenaSim("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[Arena.main] args="+args+" not allowed. Use TicTacToeBatch.");
		}
	}
}
