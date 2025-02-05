package games.RubiksCube;

import java.util.ArrayList;
import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.TStats;
import games.TStats.TAggreg;

/**
 * Evaluator for RubiksCube. For p in {1, ..., {@link CubeConfig#pMax}}: Test with {@link CubeConfig#EvalNmax}{@code [p]} 
 * cube states randomly picked via  {@link GameBoardCube#chooseStartState(int) GameBoardCube#chooseStartState(p)}
 * <ul>
 * <li> If mode=0: how many percent of the states are solved within &le; p twists? 
 * <li> If mode=1: how many percent of the states are solved within {@code epiLength} twists? 
 * </ul>  
 * The value of mode is set in the constructor. <br>
 * The value of {@code epiLength} is set from the agent's {@code getParOther().getStopEval()}.<br>
 * The value of {@link CubeConfig#pMax} is set from {@link params.OtherParams} element {@code pMax}.
 */
public class EvaluatorCube extends Evaluator {
 	private static final int[] AVAILABLE_MODES = new int[]{-1,0,1};
//	private int m_mode;			// now in Evaluator
	private	int countStates=0;
	private int epiLength=10;

	/**
	 * threshold for each value of m_mode
	 */
	protected double[] m_thresh={0.0,0.85,0.9}; // 
	
	public EvaluatorCube(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, gb, 0, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorCube(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, gb, mode, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorCube(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, stopEval, verbose);
		initEvaluator(gb);
	}
	
	private void initEvaluator(GameBoard gb) {
		if (gb != null) {
			assert (gb instanceof GameBoardCube);	
			((GameBoardCube)gb).getPMax();			// actualize CubeConfig.pMax, if GUI present
		}
	}
	
	/**
	 * @return true if evaluateAgentX is above {@link #m_thresh}.
	 * The choice for {@link #m_thresh} is made with 4th parameter mode in 
	 * {@link #EvaluatorCube(PlayAgent, GameBoard, int, int)} [default: mode=0].
	 */
	@Override
	public boolean evalAgent(PlayAgent playAgent) {
		assert (m_thresh.length >= AVAILABLE_MODES.length);
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1: 
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return false;
		case 0:  return evaluateAgent0(m_PlayAgent)>m_thresh[0];
		case 1:  return evaluateAgent0(m_PlayAgent)>m_thresh[1];
		default: return false;
		}
	}
	
	/**	
	 * For each p up to {@link CubeConfig#pMax}: Generate {@link CubeConfig#EvalNmax}{@code [p]} scrambled cubes 
	 * via {@link GameBoardCube#chooseStartState(int) GameBoardCube#chooseStartState(p)}. Measure the success rate with
	 * which the agent can solve them: 
	 * <ul>
	 * <li> {@link Evaluator#m_mode m_mode}{@code =0}:  percent solved within &le; p twists
	 * <li> {@link Evaluator#m_mode m_mode}{@code =1}:  percent solved within {@code epiLength} twists
	 * </ul> 
	 * @param pa the agent to evaluate. Use {@code pa}'s {@link PlayAgent#getParOther()#getStopEval()} to infer {@code epiLength}
	 *           ('EpiLength Eval' in OtherParams)
 	 * @return the weighted average success on different sets of scrambled cubes. Currently, constant weights are
	 * 			 hard-wired in source code.
	 */
 	private double evaluateAgent0(PlayAgent pa) {
		ArrayList<TStats> tsList = new ArrayList<TStats>();
		ArrayList<TAggreg> taggList = new ArrayList<TAggreg>();
		TStats tstats;
		TAggreg tagg;
		StateObservation so;
		double[] constWght = new double[CubeConfig.pMax];  	// weights for each p-level, see weightedAvgResTAggregList
		for (int p=0; p<CubeConfig.pMax; p++) { constWght[p]=1.0; }
//		epiLength = CubeConfig.EVAL_EPILENGTH; //50, 2*p; //(2*p>10) ? 2*p : 10;
		epiLength = pa.getParOther().getStopEval();
		if (epiLength<=CubeConfig.pMax) {
			System.err.println("WARNING: epiLength="+epiLength+" has to be larger than pMax="+CubeConfig.pMax+"!");
			System.err.println("         Setting epiLength to "+(CubeConfig.pMax+1));
			epiLength=CubeConfig.pMax+1;
			// if epiLength were not larger than pMax, the calculation in TAggreg would go wrong
			// (such that percentages would sum to something >1)
		}

 		countStates=0;
		for (int p=1; p<=CubeConfig.pMax; p++) {
 			for (int n=0; n<CubeConfig.EvalNmax[p]; n++) {
 				so = ((GameBoardCube) m_gb).chooseStartState(p);	// uses selectByTwist1(p)
 				so.resetMoveCounter();
 				
                while (!so.isGameOver() && so.getMoveCounter()<epiLength) {
 	                 so.advance(pa.getNextAction2(so, false, true));
                }
                int moveNum = so.getMoveCounter();
                tstats = new TStats(n,p,moveNum,epiLength);	// both p and epiLength are later used in TAggreg(tsList,p) to form counters
    			tsList.add(tstats);

                if(verbose > 1) {
                    System.out.print("Finished game " + n + " with moveNum " + moveNum + " twists.\n");
                }
			} // for (n)
			countStates += CubeConfig.EvalNmax[p];
 			tagg = new TAggreg(tsList,p);
 			taggList.add(tagg);
 		} // for (p)

		//the distinction between mode==0 and mode==1 happens in TStats.weightedAvgResTAggregList (!):
		lastResult = TStats.weightedAvgResTAggregList(taggList, constWght, m_mode);
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>=0) {
			TStats.printTAggregList(taggList);
			//System.out.println((CubeConfig.boardVecType==BoardVecType.CUBESTATE) ? "CUBESTATE" : "CUBEPLUSACTION");
		}
		return lastResult;
	}

 	@Override
 	public int[] getAvailableModes() {
 		return AVAILABLE_MODES;
 	}
 	
 	@Override
 	public int getDefaultEvalMode() {
		return 0;		
	}
 	
	public int getQuickEvalMode() 
	{
		return 0;
	}
	public int getTrainEvalMode() 
	{
		return 0;
	}

	@Override
	public String getPrintString() {
		switch (m_mode) {
		case 0:  return countStates+" cubes: % solved with minimal twists (best is 1.0): ";
		case 1:  return countStates+" cubes: % solved within epiLength="+ epiLength +" (best is 1.0): ";
		default: return null;
		}
	}

	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: % solved with min. twists, best is 1.0<br>"
				+ "1: % solved within EpiLength Eval, best is 1.0"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		switch (m_mode) {
		case 0:  return "% solved with minimal twists";
		case 1:  return "% solved below epiLength";
		default: return null;
		}
	}


}
