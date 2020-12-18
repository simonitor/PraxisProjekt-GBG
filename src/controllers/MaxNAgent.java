package controllers;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.StateObservation;
import games.XArenaMenu;
import games.RubiksCube.StateObserverCube;
import gui.MessageBox;
import params.MaxNParams;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

/**
 * The Max-N agent implements the Max-N algorithm [Korf91] via interface {@link PlayAgent}. 
 * Max-N is the generalization of the well-known Minimax agent to N players. It works on {@link ScoreTuple}, 
 * an N-tuple of game scores. It traverses the game tree up to a prescribed 
 * depth (default: 10, see {@link ParMaxN}). To speed up calculations, already 
 * visited states are stored in a HashMap.  
 * <p>
 * {@link MaxNAgent} is for <b>deterministic</b> games. For non-deterministic games see 
 * {@link ExpectimaxNAgent}.
 * <p>
 * See [Korf91] 
 * R. E. Korf: <em>Multi-Player Alpha-Beta Pruning</em>, Artificial Intelligence 48, 1991, 99-111.
 * 
 * @author Wolfgang Konen, TH Koeln, 2017-2020
 * 
 * @see ScoreTuple
 * @see ExpectimaxNAgent
 * @see ParMaxN
 * @see MaxNParams
 */
public class MaxNAgent extends AgentBase implements PlayAgent, Serializable
{
	private Random rand;
	protected int m_depth=10;
	protected boolean m_useHashMap=false; //true;
	private HashMap<String,ScoreTuple> hm;
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;
		
	public MaxNAgent(String name)
	{
		super(name);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
		hm = new HashMap<String, ScoreTuple>();
		super.setAgentState(AgentState.TRAINED);
	}
	
	public MaxNAgent(String name, int depth)
	{
		this(name);
		m_depth = depth;
	}
		
	public MaxNAgent(String name, int depth, boolean useHashMap)
	{
		this(name);
		m_depth = depth;
		m_useHashMap = useHashMap;
	}
		
	public MaxNAgent(String name, ParMaxN mPar, ParOther oPar)
	{
		super(name,oPar);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
		hm = new HashMap<String, ScoreTuple>();
		super.setAgentState(AgentState.TRAINED);
		m_depth = mPar.getMaxNDepth();
		m_useHashMap = mPar.getMaxNUseHashmap();
//		m_oPar = new ParOther(oPar);		// AgentBase::m_oPar
	}
		
	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see Arena#loadAgent
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		m_arena.m_xab.setMaxNDepthFrom(n, this.getDepth() );
//		m_arena.m_xab.setOParFrom(n, this.getParOther() );		// do or don't?
	}
	
	/**
	 * Get the best next action and return it
	 * @param so			current game state (not changed on return)
	 * @param random		allow epsilon-greedy random action selection	
	 * @param silent
	 * @return actBest		the best action 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
	 * 
	 */	
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
        List<ACTIONS> actions = so.getAvailableActions();
	
        // --- obsolete now, we do not use MaxNWrapper anymore, but MaxN2Wrapper
//        try {
//		    // this is just a runtime check in case MaxNWrapper whether the wrapped agent implements estimateGameValueTuple
//        	this.estimateGameValueTuple(so, null);
//        } catch (RuntimeException e) {
//        	if (e.getMessage()==AgentBase.EGV_EXCEPTION_TEXT) {
//            	String str = "MaxNWrapper: The wrapped agent does not implement estimateGameValueTuple "
//            			+ "\n --> set Other pars: Wrapper nPly to 0";
//    			MessageBox.show(null,str,
//    					"MaxNAgent", JOptionPane.ERROR_MESSAGE);      
//    			return null;
//        	} else {
//        		throw e;	// other exceptions: rethrow
//        	}
//        }

        // this starts the recursion:
		ACTIONS_VT act_vt = getBestAction(so, so,  random,  silent, 0, null);
		
		return act_vt;
	}

	/**
	 * Loop over all actions available for {@code so} to find the action with the best 
	 * score tuple (best score for {@code so}'s player).
	 * 
	 * @param so		current game state (not changed on return)
	 * @param refer		(not used currently) referring game state (=so on initial call)	
	 * @param random	(not used currently) allow epsilon-greedy random action selection	
	 * @param silent
	 * @param depth		tree depth
	 * @param prevTuple TODO
	 * @return		best action + score tuple
	 */
	private ACTIONS_VT getBestAction(StateObservation so, StateObservation refer, boolean random, 
			boolean silent, int depth, ScoreTuple prevTuple) 
	{
		int i,j;
		ScoreTuple currScoreTuple=null;
        ScoreTuple sc;
		StateObservation NewSO;
		ScoreTuple scBest = null;
        ACTIONS actBest = null;
        ACTIONS_VT act_vt = null;
        ArrayList<ACTIONS> bestActions = new ArrayList<>();
        String stringRep ="";

        assert so.isLegalState() : "Not a legal state"; 
        
//        if (this instanceof MaxNWrapper) 
//        	System.out.println("MaxN: depth="+depth+"  "+so.stringDescr());
        
        double value, maxValue = -Double.MAX_VALUE;
        ArrayList<ACTIONS> acts = so.getAvailableActions();
        double[] VTable =  new double[acts.size()];
        boolean lowest = true;
    	currScoreTuple = (prevTuple==null) ? new ScoreTuple(so,lowest) : prevTuple;
        int P = so.getPlayer();
        
        for(i = 0; i < acts.size(); ++i)
        {
        	NewSO = so.copy();
        	NewSO.advance(acts.get(i));
        	
//        	boolean found = false;
    		if (NewSO.isGameOver())
    		{
    			boolean rgs = m_oPar.getRewardIsGameScore();
    			currScoreTuple = NewSO.getRewardTuple(rgs);
    		} else {
    			if (depth<this.m_depth) {
    				stringRep = NewSO.stringDescr();
    	        	sc = retrieveFromHashMap(m_useHashMap,stringRep);
    				if (sc==null) {
    					// here is the recursion: getAllScores may call getBestAction back:
    					currScoreTuple = getAllScores(NewSO,refer,depth+1, currScoreTuple);	
    					
    					if (m_useHashMap) {
    						hm.put(stringRep, currScoreTuple);
    		    			//System.out.println(stringRep+":"+currScoreTuple);
    					}
    				} else {
    					currScoreTuple = sc;
//    					found=true;
    				}
    			} else {
    				// this terminates the recursion:
    				// (after finishing the for-loop for every element of acts)
    				currScoreTuple = estimateGameValueTuple(NewSO, currScoreTuple);
    				// For derived class MaxNWrapper, estimateGameValueTuple returns
    				// the score tuple of the wrapped agent. 
    			}    			
    		}
//			if (m_useHashMap && !found) {
//				stringRep = NewSO.stringDescr();
//				hm.put(stringRep, currScoreTuple);
//			}
			
			// only debug for RubiksCube:
//			System.out.println(depth+": "+((StateObserverCube)NewSO).getCubeState().getTwistSeq()+", "+currScoreTuple);
//			System.out.println(((StateObserverCube)NewSO).getCubeState().getTwistSeq()+", "+currScoreTuple);

    		if ( currScoreTuple.scTup.length!=so.getNumPlayers()) {
    			int dummy =1;
    			throw new RuntimeException( "Ooops, scTup too short");    			
    		}
    		
			VTable[i] = currScoreTuple.scTup[P];
			
			value = VTable[i] = currScoreTuple.scTup[P];
			// always *maximize* P's element in the tuple currScoreTuple, 
			// where P is the player to move in state so:
        	if (value==maxValue) bestActions.add(acts.get(i));
        	if (value>maxValue) {
        		maxValue = value;
        		scBest = new ScoreTuple(currScoreTuple);
        		bestActions.clear();
        		bestActions.add(acts.get(i));
        	}
        } // for
        
        // There might be one or more than one action with minValue. 
        // Break ties by selecting one of them randomly:
        actBest = bestActions.get(rand.nextInt(bestActions.size()));

        assert actBest != null : "Oops, no best action actBest";
        // optional: print the best action
        if (!silent) {
        	NewSO = so.copy();
        	NewSO.advance(actBest);
        	System.out.println("---Best Move: "+NewSO.stringDescr()+"   "+maxValue);
        }			

        act_vt = new ACTIONS_VT(actBest.toInt(), false, VTable, maxValue, scBest);
        return act_vt;         
	}

	private ScoreTuple retrieveFromHashMap(boolean m_useHashMap, String stringRep) {
		ScoreTuple sc = null;
    	if (m_useHashMap) {
			// speed up MaxNAgent for repeated calls by storing/retrieving the 
			// scores of visited states in HashMap hm:
			sc = hm.get(stringRep); 		// returns null if not in hm
			//System.out.println(stringRep+":"+sc);
    	} 
    	
    	return sc;
	}
	
	/**
	 * Return the agent's score for that after state.
	 * @param sob			the current game state;
	 * @return				the probability that the player to move wins from that 
	 * 						state. If game is over: the score for the player who 
	 * 						*would* move (if the game were not over).
	 * Each player wants to maximize its score	 
	 */
	@Override
	public double getScore(StateObservation sob) {
		return getAllScores(sob,sob,0, null).scTup[sob.getPlayer()];
	}
	@Override
	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		return getAllScores(sob,sob,0, null);
	}

	private ScoreTuple getAllScores(StateObservation sob, StateObservation refer, int depth, ScoreTuple prevTuple) {
        ACTIONS_VT act_vt = null;

        // --- obsolete now since MaxNWrapper has been removed in favor of MaxN2Wrapper
//		if (this instanceof MaxNWrapper) {
//			if (((MaxNWrapper)this).getWrappedPlayAgent() instanceof TDNTuple3Agt)
//				prevTuple = estimateGameValueTuple(sob, prevTuple);
//				// this is for wrappedAgent==TDNTuple3Agt and the case (N>=3): fill in the game value estimate
//				// for the player who created sob. Will be used by subsequent states as a surrogate for the 
//				// then unknown value for that player. 
//		}
		
		// here is the recursion: getBestAction calls getAllScores(...,depth+1):
		act_vt = getBestAction(sob, refer, false, true, depth, prevTuple);  
		
		return act_vt.getScoreTuple();		// return ScoreTuple for best action
	}

//	/**
//	 * When the recursion tree has reached its maximal depth m_depth, then return
//	 * an estimate of the game score. This function may be overridden in a game-
//	 * specific way by classes derived from {@link MaxNAgent}.
//	 * <p>
//	 * This  stub method just returns {@link StateObservation#getReward(StateObservation, boolean)},
//	 * which might be too simplistic for not-yet finished games, because the current reward does
//	 * not reflect future rewards.
//	 * @param sob	the state observation
//	 * @return		the estimated score
//	 */
//	@Override
//	public double estimateGameValue(StateObservation sob) {
//		return sob.getReward(sob,true);
//	}

	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players). This function may be overridden 
	 * in a game-specific way by classes derived from {@link MaxNAgent}. 
	 * <p>
	 * This  stub method just returns {@link StateObservation#getReward(int,boolean)} for every 
	 * player, which might be too simplistic for not-yet finished games, because the current 
	 * reward may not reflect future rewards.
	 * @param sob	the state observation
	 * @return		the estimated score tuple
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		ScoreTuple sc = new ScoreTuple(sob);
		for (int i=0; i<sob.getNumPlayers(); i++) 
			sc.scTup[i] = sob.getReward(i, rgs);
		return sc;
	}

	@Override
	public String stringDescr() {
		String cs = getClass().getName();
		cs = cs + ", depth:"+m_depth;
		return cs;
	}

	public int getDepth() {
		return m_depth;
	}
	

}